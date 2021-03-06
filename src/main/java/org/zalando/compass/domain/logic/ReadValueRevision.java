package org.zalando.compass.domain.logic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.compass.domain.model.PageRevision;
import org.zalando.compass.domain.model.Revision;
import org.zalando.compass.domain.model.Value;
import org.zalando.compass.domain.model.ValueRevision;
import org.zalando.compass.domain.persistence.NotFoundException;
import org.zalando.compass.domain.persistence.RevisionRepository;
import org.zalando.compass.domain.persistence.ValueRevisionRepository;
import org.zalando.compass.library.pagination.Cursor;
import org.zalando.compass.library.pagination.PageResult;
import org.zalando.compass.library.pagination.Pagination;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
class ReadValueRevision {

    private final ValueRevisionRepository repository;
    private final RevisionRepository revisionRepository;
    private final ValueSelector selector;

    @Autowired
    ReadValueRevision(final ValueRevisionRepository repository,
            final RevisionRepository revisionRepository, final ValueSelector selector) {
        this.repository = repository;
        this.revisionRepository = revisionRepository;
        this.selector = selector;
    }

    public PageResult<Revision> readPageRevisions(final String key, final Pagination<Long> query) {
        final List<Revision> revisions = repository.findPageRevisions(key, query.increment()).stream()
                .map(Revision::withTypeUpdate)
                .collect(toList());

        return query.paginate(revisions);
    }

    public PageRevision<Value> readPageAt(final String key, final Map<String, JsonNode> filter, final long revisionId) {
        final Revision revision = revisionRepository.read(revisionId)
                .orElseThrow(NotFoundException::new)
                .withTypeUpdate();

        final List<Value> values = repository.findPage(key, revisionId)
                .stream().map(ValueRevision::toValue).collect(toList());

        if (filter.isEmpty()) {
            // special case, just for reading many values
            return new PageRevision<>(revision, PageResult.create(values, false, false));
        }

        return new PageRevision<>(revision, PageResult.create(selector.select(values, filter), false, false));
    }

    // TODO this is rather inefficient
    public Revision readLatestRevision(final String key, final Map<String, JsonNode> dimensions) {
        final Revision revision = readPageRevisions(key, Cursor.<Long, Void>initial().with(null, 1).paginate()).getHead();
        return readAt(key, dimensions, revision.getId()).getRevision();
    }

    public PageResult<Revision> readRevisions(final String key, final Map<String, JsonNode> dimensions,
            final Pagination<Long> query) {
        final List<Revision> revisions = repository.findRevisions(key, dimensions, query.increment());
        return query.paginate(revisions);
    }

    public ValueRevision readAt(final String key, final Map<String, JsonNode> dimensions, final long revision) {
        final List<ValueRevision> values = repository.findValueRevisions(key, revision);
        final List<ValueRevision> matched = selector.select(values, dimensions);

        return matched.stream()
                .findFirst().orElseThrow(NotFoundException::new);
    }

}
