package org.zalando.compass.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

@lombok.Value
public final class DimensionRevision {

    String id;
    Revision revision;
    JsonNode schema;
    String relation;
    String description;

}
