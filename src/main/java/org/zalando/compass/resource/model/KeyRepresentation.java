package org.zalando.compass.resource.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.zalando.compass.domain.model.Key;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@AllArgsConstructor
public final class KeyRepresentation {

    String id;
    JsonNode schema;
    String description;

    public static KeyRepresentation valueOf(final Key key) {
        return new KeyRepresentation(
                key.getId(),
                key.getSchema(),
                key.getDescription());
    }

}
