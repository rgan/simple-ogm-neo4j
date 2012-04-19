package org.ogm;


import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import java.util.Set;

public class Relation {
    private RelationshipType type;
    private Direction direction;
    private Set<PersistentEntity> entities;

    public Relation(Direction direction, String type, Set<PersistentEntity> entities) {
        this.direction = direction;
        this.type = DynamicRelationshipType.withName(type);
        this.entities = entities;
    }

    public RelationshipType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public Set<PersistentEntity> getEntities() {
        return entities;
    }
}
