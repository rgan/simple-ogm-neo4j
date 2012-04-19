package org.ogm;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Repository {

    private GraphDatabaseService graphDb;

    public Repository(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public PersistentEntity findById(Long id) {
        return PersistentEntity.from(graphDb.getNodeById(id));
    }

    public Node save(final PersistentEntity entity) {
        TransactionalOperation<Node> op = new TransactionalOperation<Node>(graphDb) {
            public Node execute() {
                Node node;
                if (entity.getId() != null) {
                    node = graphDb.getNodeById(entity.getId());
                } else {
                    node = graphDb.createNode();
                    entity.setId(node.getId());
                }

                Map<String, Object> map = entity.getProperties();
                for (String key : map.keySet()) {
                    node.setProperty(key, map.get(key));
                }
                for (Relation relation : entity.getRelations()) {
                    persistRelation(node, relation);
                }
                return node;
            }
        };
        return op.doInTransaction();
    }

    public void delete(final PersistentEntity entity) {
        TransactionalOperation<Node> op = new TransactionalOperation<Node>(graphDb) {
            public Node execute() {
                if (entity.getId() != null) {
                    Node node = graphDb.getNodeById(entity.getId());
                    node.delete();
                    return node;
                }
                return null;
            }
        };
        op.doInTransaction();
    }

    private void persistRelation(final Node node, Relation relation) {
        Set<PersistentEntity> entities = relation.getEntities();
        final Iterable<Relationship> relationships = node.getRelationships(relation.getType(), relation.getDirection());
        if (entities == null) {
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            return;
        }
        Set<Node> relatedNodes = getOrCreateNodes(entities);
        for (Relationship relationship : relationships) {
            if (!relatedNodes.remove(relationship.getOtherNode(node)))
                relationship.delete();
        }
        Set<Node> nodesToCreateRelationshipWith = Sets.filter(relatedNodes, new Predicate<Node>() {
            public boolean apply(Node relatedNode) {
                for (final Relationship existingRelationship : relationships) {
                    if (existingRelationship.getOtherNode(node).equals(relatedNode)) {
                        return false;
                    }
                }
                return true;
            }
        });
        for (Node relatedNode : nodesToCreateRelationshipWith) {
            node.createRelationshipTo(relatedNode, relation.getType());
        }
    }

    private Set<Node> getOrCreateNodes(Set<PersistentEntity> entities) {
        Set<Node> nodes = new HashSet<Node>();
        for (PersistentEntity entity : entities) {
            nodes.add(save(entity));
        }
        return nodes;
    }


}
