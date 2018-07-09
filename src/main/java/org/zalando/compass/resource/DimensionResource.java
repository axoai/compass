package org.zalando.compass.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.compass.domain.logic.DimensionService;
import org.zalando.compass.domain.model.Dimension;
import org.zalando.compass.library.pagination.PageResult;
import org.zalando.compass.library.pagination.Pagination;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.zalando.compass.domain.logic.BadArgumentException.checkArgument;
import static org.zalando.compass.resource.Linking.link;
import static org.zalando.compass.resource.MediaTypes.JSON_MERGE_PATCH_VALUE;
import static org.zalando.compass.resource.MediaTypes.JSON_PATCH_VALUE;

@RestController
@RequestMapping(path = "/dimensions")
class DimensionResource implements Reserved {

    private final JsonReader reader;
    private final ObjectMapper mapper;
    private final DimensionService service;

    @Autowired
    public DimensionResource(final JsonReader reader, final ObjectMapper mapper, final DimensionService service) {
        this.reader = reader;
        this.mapper = mapper;
        this.service = service;
    }

    @RequestMapping(method = PUT, path = "/{id}")
    public ResponseEntity<DimensionRepresentation> createOrReplace(@PathVariable final String id,
            @Nullable @RequestHeader(name = IF_NONE_MATCH, required = false) final String ifNoneMatch,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final JsonNode node) throws IOException {

        ensureConsistentId(id, node);
        final Dimension dimension = reader.read(node, Dimension.class);

        final boolean created = createOrReplace(dimension, comment, ifNoneMatch);
        final DimensionRepresentation representation = DimensionRepresentation.valueOf(dimension);

        return ResponseEntity
                .status(created ? CREATED : OK)
                .body(representation);
    }

    public boolean createOrReplace(final Dimension dimension, @Nullable final String comment,
            @Nullable final String ifNoneMatch) {

        if ("*".equals(ifNoneMatch)) {
            service.create(dimension, comment);
            return true;
        } else {
            return service.replace(dimension, comment);
        }
    }

    private void ensureConsistentId(@PathVariable final String inUrl, final JsonNode node) {
        final JsonNode inBody = node.path("id");

        if (inBody.isMissingNode()) {
            ObjectNode.class.cast(node).put("id", inUrl);
        } else {
            checkArgument(inUrl.equals(inBody.asText()), "If present, ID in body must match with URL");
        }
    }

    @RequestMapping(method = GET)
    public ResponseEntity<DimensionCollectionRepresentation> getAll(
            @RequestParam(name = "q", required = false) @Nullable final String q,
            @RequestParam(value = "limit", required = false, defaultValue = "25") final String limit,
            @Nullable @RequestParam(value = "_after", required = false) final String after,
            @Nullable @RequestParam(value = "_before", required = false) final String before) throws IOException {

        final Pagination<String> query = Pagination.create(after, before,
                reader.read("Limit", limit, int.class));
        final PageResult<Dimension> page = service.readPage(q, query);

        final List<DimensionRepresentation> representations = page.getElements().stream()
                .map(DimensionRepresentation::valueOf)
                .collect(toList());

        return ResponseEntity.ok(new DimensionCollectionRepresentation(
                page.hasNext() ?
                        link(methodOn(DimensionResource.class).getAll(q, limit, page.getTail().getId(), null)) : null,
                page.hasPrevious() ?
                        link(methodOn(DimensionResource.class).getAll(q, limit, null, page.getHead().getId())) : null,
                representations));
    }

    @RequestMapping(method = GET, path = "/{id}")
    public ResponseEntity<DimensionRepresentation> get(@PathVariable final String id) {
        return ResponseEntity.ok(DimensionRepresentation.valueOf(service.read(id)));
    }

    @RequestMapping(method = PATCH, path = "/{id}", consumes = {APPLICATION_JSON_VALUE, JSON_MERGE_PATCH_VALUE})
    public ResponseEntity<DimensionRepresentation> update(@PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final ObjectNode content) throws IOException, JsonPatchException {

        final Dimension dimension = service.read(id);
        final JsonNode node = mapper.valueToTree(dimension);

        final JsonMergePatch patch = JsonMergePatch.fromJson(content);
        final JsonNode patched = patch.apply(node);
        return createOrReplace(id, null, comment, patched);
    }

    @RequestMapping(method = PATCH, path = "/{id}", consumes = JSON_PATCH_VALUE)
    public ResponseEntity<DimensionRepresentation> update(@PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final ArrayNode content) throws IOException, JsonPatchException {

        final JsonPatch patch = reader.read(content, JsonPatch.class);

        final Dimension dimension = service.read(id);
        final JsonNode node = mapper.valueToTree(dimension);

        final JsonNode patched = patch.apply(node);
        return createOrReplace(id, null, comment, patched);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment) {
        service.delete(id, comment);
    }

}
