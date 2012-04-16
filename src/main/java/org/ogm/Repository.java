package org.ogm;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

public class Repository {

    private GraphDatabaseService graphDb;

    public Repository(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public PersistentEntity findById(Long id) {
        return PersistentEntity.from(graphDb.getNodeById(id));
    }

    public void save(final PersistentEntity entity) {
        doInTransaction(new TransactionalMethod() {
            public void execute() {
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
            }
        });
    }

    public void delete(final PersistentEntity entity) {
        doInTransaction(new TransactionalMethod() {
            public void execute() {
                if (entity.getId() != null) {
                    Node node = graphDb.getNodeById(entity.getId());
                    node.delete();
                }
            }
        });
    }

    protected void doInTransaction(TransactionalMethod method) {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            method.execute();
            tx.success();
        } finally {
            if (tx != null) tx.finish();
        }
    }

    protected interface TransactionalMethod {
        void execute();
    }


}
