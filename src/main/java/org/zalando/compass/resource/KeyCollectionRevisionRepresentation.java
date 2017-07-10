package org.zalando.compass.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@AllArgsConstructor
final class KeyCollectionRevisionRepresentation {

    // TODO without href
    RevisionRepresentation revision;
    List<KeyRepresentation> keys;

}
