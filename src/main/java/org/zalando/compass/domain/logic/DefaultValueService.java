package org.zalando.compass.domain.logic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.compass.domain.model.PageRevision;
import org.zalando.compass.domain.model.Revision;
import org.zalando.compass.domain.model.Revisioned;
import org.zalando.compass.domain.model.Value;
import org.zalando.compass.domain.model.ValueRevision;
import org.zalando.compass.library.pagination.Cursor;
import org.zalando.compass.library.pagination.PageResult;
import org.zalando.compass.library.pagination.Pagination;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
class DefaultValueService implements ValueService {

    private final ReplaceValue replace;
    private final ReadValue read;
    private final ReadValueRevision readRevision;
    private final DeleteValue delete;

    @Autowired
    DefaultValueService(final ReplaceValue replace, final ReadValue read,
            final ReadValueRevision readRevision, final DeleteValue delete) {
        this.replace = replace;
        this.read = read;
        this.readRevision = readRevision;
        this.delete = delete;
    }

    @Transactional(isolation = SERIALIZABLE)
    @Override
    public boolean replace(final String key, final List<Value> values, @Nullable final String comment) {
        return replace.replace(key, values, comment);
    }

    @Transactional(isolation = SERIALIZABLE)
    @Override
    public void create(final String key, final List<Value> values, @Nullable final String comment) {
        replace.create(key, values, comment);
    }

    @Transactional(isolation = SERIALIZABLE)
    @Override
    public boolean replace(final String key, final Value value, @Nullable final String comment) {
        return replace.replace(key, value, comment);
    }

    @Transactional(isolation = SERIALIZABLE)
    @Override
    public void create(final String key, final Value value, @Nullable final String comment) throws EntityAlreadyExistsException {
        replace.create(key, value, comment);
    }

    @Transactional(readOnly = true)
    @Override
    public Revisioned<List<Value>> readPage(final String key, final Map<String, JsonNode> filter) {
        final List<Value> values = read.readAll(key, filter);

        final PageResult<Revision> revisions = readRevision.readPageRevisions(key,
                Cursor.<Long, Void>initial().with(null, 1).paginate());

        if (revisions.getElements().isEmpty()) {
            return Revisioned.create(values, null);
        }

        final PageRevision<Value> revision = readRevision.readPageAt(key, filter, revisions.getHead().getId());
        return Revisioned.create(values, revision.getRevision());
    }

    @Transactional(readOnly = true)
    @Override
    public Revisioned<Value> read(final String key, final Map<String, JsonNode> filter) {
        final Value value = readOnly(key, filter);
        final Revision revision = readRevision.readLatestRevision(key, filter);
        return Revisioned.create(value, revision);
    }

    @Transactional(readOnly = true)
    @Override
    public Value readOnly(final String key, final Map<String, JsonNode> filter) {
        return read.read(key, filter);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Revision> readPageRevisions(final String key, final Pagination<Long> query) {
        return readRevision.readPageRevisions(key, query);
    }

    @Transactional(readOnly = true)
    @Override
    public PageRevision<Value> readPageAt(final String key, final Map<String, JsonNode> filter, final long revision) {
        return readRevision.readPageAt(key, filter, revision);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Revision> readRevisions(final String key, final Map<String, JsonNode> dimensions,
            final Pagination<Long> query) {
        return readRevision.readRevisions(key, dimensions, query);
    }

    @Transactional(readOnly = true)
    @Override
    public ValueRevision readAt(final String key, final Map<String, JsonNode> dimensions, final long revision) {
        return readRevision.readAt(key, dimensions, revision);
    }

    @Transactional // TODO isolation?!
    @Override
    public void delete(final String key, final Map<String, JsonNode> filter, @Nullable final String comment) {
        delete.delete(key, filter, comment);
    }

}
