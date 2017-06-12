package org.zalando.compass.resource;

import org.zalando.compass.domain.model.Relation;

import java.util.List;

@lombok.Value
public final class RelationPage {

    List<Relation> relations;

}
