package org.ogm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

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
        assertEquals("org.ogm.Club", node.getProperty(PersistentEntity.TYPE));
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
        Transaction transaction = graphDb.beginTx();
        Node node = graphDb.createNode();
        node.setProperty(PersistentEntity.TYPE, "org.ogm.Club");
        node.setProperty("name", "foobar");
        transaction.success();
        transaction.finish();
        Club clubFromDb = (Club) repository.findById(node.getId());
        assertEquals("foobar", clubFromDb.getName());
    }

    @Test
    public void shouldFindByIdWithRelationship() {
        Transaction transaction = graphDb.beginTx();
        Node clubNode = graphDb.createNode();
        clubNode.setProperty(PersistentEntity.TYPE, "org.ogm.ClubWithSingleMessage");
        Node msgNode = graphDb.createNode();
        msgNode.setProperty(PersistentEntity.TYPE, "org.ogm.ClubMessage");
        msgNode.setProperty("message", "this is a test");
        msgNode.createRelationshipTo(clubNode, DynamicRelationshipType.withName("single_message"));
        transaction.success();
        transaction.finish();

        ClubWithSingleMessage clubFromDb = (ClubWithSingleMessage) repository.findById(clubNode.getId());
        ClubMessage message = clubFromDb.getMessage();
        assertNotNull(message);
        assertEquals("this is a test", message.getMessage());
    }

    @Test
    public void shouldFindByIdWithMultiValuedRelationship() {
        Transaction transaction = graphDb.beginTx();
        Node clubNode = graphDb.createNode();
        clubNode.setProperty(PersistentEntity.TYPE, "org.ogm.Club");
        clubNode.setProperty("name", "name");
        Node firstMsgNode = graphDb.createNode();
        firstMsgNode.setProperty(PersistentEntity.TYPE, "org.ogm.ClubMessage");
        firstMsgNode.setProperty("message", "first message");

        Node secondMsgNode = graphDb.createNode();
        secondMsgNode.setProperty(PersistentEntity.TYPE, "org.ogm.ClubMessage");
        secondMsgNode.setProperty("message", "second message");

        firstMsgNode.createRelationshipTo(clubNode, DynamicRelationshipType.withName("message"));
        secondMsgNode.createRelationshipTo(clubNode, DynamicRelationshipType.withName("message"));
        transaction.success();
        transaction.finish();

        Club clubFromDb = (Club) repository.findById(clubNode.getId());
        Set<ClubMessage> messages = clubFromDb.getMessages();
        assertEquals(2, messages.size());
    }


    @Test(expected = NotFoundException.class)
    public void shouldDeleteEntity() {
        Club club = new Club("updated clubname", "");
        repository.save(club);
        repository.delete(club);
        graphDb.getNodeById(club.getId());
    }

    @Test
    public void shouldSaveEntityWithNullRelationValue() {
        ClubWithSingleMessage club = new ClubWithSingleMessage();
        repository.save(club);
        Node node = graphDb.getNodeById(club.getId());
        assertFalse(node.getRelationships().iterator().hasNext());
    }

    @Test
    public void shouldSaveEntityWithSingleRelation() {
        ClubWithSingleMessage club = new ClubWithSingleMessage();
        ClubMessage msg = new ClubMessage();
        club.setMessage(msg);
        repository.save(club);
        Node node = graphDb.getNodeById(club.getId());
        assertTrue(node.getRelationships().iterator().hasNext());
        assertNotNull(msg.getId());
    }

    @Test
     public void shouldSaveEntityWithCollectionValuedRelation() {
        Club club = new Club();
        ClubMessage firstMsg = new ClubMessage();
        club.addMessage(firstMsg);
        repository.save(club);
        Node node = graphDb.getNodeById(club.getId());

        Relationship relationship = node.getRelationships().iterator().next();
        Node relatedNode = graphDb.getNodeById(firstMsg.getId());
        assertEquals(relatedNode, relationship.getEndNode());
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
