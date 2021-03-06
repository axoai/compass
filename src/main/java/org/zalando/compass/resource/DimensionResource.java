package org.zalando.compass.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import lombok.AllArgsConstructor;
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
import org.zalando.compass.domain.model.Revisioned;
import org.zalando.compass.library.pagination.Cursor;
import org.zalando.compass.library.pagination.PageResult;
import org.zalando.compass.resource.model.DimensionCollectionRepresentation;
import org.zalando.compass.resource.model.DimensionRepresentation;
import org.zalando.fauxpas.ThrowingUnaryOperator;

import javax.annotation.Nullable;
import java.io.IOException;

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
import static org.zalando.compass.resource.Linking.link;
import static org.zalando.compass.resource.MediaTypes.JSON_MERGE_PATCH_VALUE;
import static org.zalando.compass.resource.MediaTypes.JSON_PATCH_VALUE;

@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
class DimensionResource {

    private final ObjectMapper mapper;
    private final DimensionService service;

    @RequestMapping(method = PUT, path = "/dimensions/{id}")
    public ResponseEntity<DimensionRepresentation> createOrReplace(
            @PathVariable final String id,
            @Nullable @RequestHeader(name = IF_NONE_MATCH, required = false) final String ifNoneMatch,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final Dimension body) {

        final Dimension dimension = body.withId(id);

        final boolean created = createOrReplace(dimension, comment, ifNoneMatch);
        final DimensionRepresentation representation = DimensionRepresentation.valueOf(dimension);

        return ResponseEntity
                .status(created ? CREATED : OK)
                .body(representation);
    }

    private boolean createOrReplace(final Dimension dimension, @Nullable final String comment,
            @Nullable final String ifNoneMatch) {

        if ("*".equals(ifNoneMatch)) {
            service.create(dimension, comment);
            return true;
        } else {
            return service.replace(dimension, comment);
        }
    }

    @RequestMapping(method = GET, path = "/dimensions")
    public ResponseEntity<DimensionCollectionRepresentation> getAll(
            @RequestParam(name = "q", required = false) @Nullable final String q,
            @RequestParam(required = false, defaultValue = "25") final Integer limit,
            @RequestParam(name = "cursor", required = false, defaultValue = "") final Cursor<String, String> original) {

        final Cursor<String, String> cursor = original.with(q, limit);
        // TODO get rid of q parameter
        final PageResult<Dimension> page = service.readPage(q, cursor.paginate());

        return ResponseEntity.ok(
                page.render(DimensionCollectionRepresentation::new, cursor, Dimension::getId,
                        c -> link(methodOn(DimensionResource.class).getAll(null, null, c)),
                        DimensionRepresentation::valueOf));
    }

    @RequestMapping(method = GET, path = "/dimensions/{id}")
    public ResponseEntity<DimensionRepresentation> get(@PathVariable final String id) {
        final Revisioned<Dimension> revisioned = service.read(id);
        return Conditional.build(revisioned, DimensionRepresentation::valueOf);
    }

    @RequestMapping(method = PATCH, path = "/dimensions/{id}", consumes = {APPLICATION_JSON_VALUE, JSON_MERGE_PATCH_VALUE})
    public ResponseEntity<DimensionRepresentation> update(
            @PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final JsonMergePatch patch) throws IOException, JsonPatchException {

        return patch(id, comment, patch::apply);
    }

    @RequestMapping(method = PATCH, path = "/dimensions/{id}", consumes = JSON_PATCH_VALUE)
    public ResponseEntity<DimensionRepresentation> update(
            @PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment,
            @RequestBody final JsonPatch patch) throws IOException, JsonPatchException {

        return patch(id, comment, patch::apply);
    }

    private ResponseEntity<DimensionRepresentation> patch(final String id, final @Nullable String comment,
            final ThrowingUnaryOperator<JsonNode, JsonPatchException> patch) throws IOException, JsonPatchException {
        final Dimension before = service.readOnly(id);
        final JsonNode node = mapper.valueToTree(before);

        final JsonNode patched = patch.tryApply(node);
        final Dimension after = mapper.treeToValue(patched, Dimension.class);

        return createOrReplace(id, null, comment, after);
    }

    @RequestMapping(method = DELETE, path = "/dimensions/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable final String id,
            @Nullable @RequestHeader(name = "Comment", required = false) final String comment) {
        service.delete(id, comment);
    }

}
