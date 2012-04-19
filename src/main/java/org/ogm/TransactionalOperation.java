package org.ogm;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public abstract class TransactionalOperation<T> {

    private GraphDatabaseService graphDb;

    protected TransactionalOperation(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public T doInTransaction() {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            T returnValue = execute();
            tx.success();
            return returnValue;
        } finally {
            if (tx != null) tx.finish();
        }
    }

    protected abstract T execute();
}
