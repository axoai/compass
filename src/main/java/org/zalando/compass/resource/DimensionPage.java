package org.zalando.compass.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zalando.compass.domain.model.Dimension;

import java.util.List;

@Getter
@AllArgsConstructor
class DimensionPage {

    private final List<Dimension> dimensions;

}
