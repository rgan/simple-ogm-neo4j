package org.ogm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepositoryTest {

    private AbstractGraphDatabase graphDb;
    private Repository repository;

    @Before
    public void setup() {
        graphDb = new EmbeddedGraphDatabase("/tmp/test");
        repository = new Repository(graphDb);
        deleteAllNodes();
        clearAllIndexes();
    }

    @After
    public void teardown() {
        graphDb.shutdown();
    }

    @Test
    public void shouldSaveEntity() {
        Club club = new Club("clubname", "");
        repository.save(club);
        assertNotNull(club.getId());
        Node node = graphDb.getNodeById(club.getId());
        assertEquals("clubname", node.getProperty("name"));
    }

    @Test
    public void shouldUpdateEntity() {
        Club club = new Club("updated clubname", "");
        repository.save(club);
        club.setName("updatedName");
        Node node = graphDb.getNodeById(club.getId());
        assertEquals("updated clubname", node.getProperty("name"));
    }

    @Test
    public void shouldFindById() {
        Club club = new Club("foobar", "");
        repository.save(club);
        Club clubFromDb = (Club) repository.findById(club.getId());
        assertEquals("foobar", clubFromDb.getName());
    }

    @Test(expected = NotFoundException.class)
    public void shouldDeleteEntity() {
        Club club = new Club("updated clubname", "");
        repository.save(club);
        repository.delete(club);
        graphDb.getNodeById(club.getId());
    }

    private void deleteAllNodes() {
        Transaction transaction = graphDb.beginTx();
        for (Node node : graphDb.getAllNodes()) {
            for (Relationship rel : node.getRelationships(Direction.OUTGOING)) {
                rel.delete();
            }
        }
        for (Node node : graphDb.getAllNodes()) {
            node.delete();
        }
        transaction.success();
        transaction.finish();
    }

    private void clearAllIndexes() {
        IndexManager indexManager = graphDb.index();
        for (String ix : indexManager.nodeIndexNames()) {
            indexManager.forNodes(ix).delete();
        }
        for (String ix : indexManager.relationshipIndexNames()) {
            indexManager.forRelationships(ix).delete();
        }
    }
}
