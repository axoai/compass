package org.zalando.compass.domain.logic;

import org.zalando.compass.domain.model.Relation;

import java.util.List;

public interface RelationService {

    List<Relation> readAll();

    Relation read(String id);

}
