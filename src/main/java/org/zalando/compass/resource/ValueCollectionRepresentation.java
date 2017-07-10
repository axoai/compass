package org.zalando.compass.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.zalando.compass.domain.model.Value;

import java.net.URI;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@AllArgsConstructor
final class ValueCollectionRepresentation {

    URI prev;
    URI next;
    List<ValueRepresentation> values;

}
