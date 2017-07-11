package org.zalando.compass.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@AllArgsConstructor
class DimensionCollectionRepresentation {

    URI next;
    URI prev;
    List<DimensionRepresentation> dimensions;

}
