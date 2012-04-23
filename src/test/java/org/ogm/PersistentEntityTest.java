package org.ogm;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.neo4j.graphdb.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersistentEntityTest {

    @Test
    public void shouldReturnPersistentFields() {
        Club club = new Club("clubname", "notpersistent");
        Set<Field> persistentFields = club.getPersistentFields();
        assertEquals(1, persistentFields.size());
        assertEquals("name", persistentFields.iterator().next().getName());
    }

    @Test
    public void shouldReturnRelations() {
        Club club = new Club("clubname", "notpersistent");
        Set<Field> relationshipFields = club.getRelationshipFields();
        assertEquals(1, relationshipFields.size());
        Field field = relationshipFields.iterator().next();
        assertEquals("clubMessages", field.getName());
    }

    @Test
    public void shouldGetProperties() {
        Club club = new Club("clubname", "notpersistent");
        Map<String, Object> properties = club.getProperties();
        assertEquals(2, properties.size());
        assertEquals("org.ogm.Club", properties.get(PersistentEntity.TYPE));
        assertEquals("clubname", properties.get("name"));
    }

    @Test
    public void shouldCreateEntityFromNode() {
        Node node = mock(Node.class);
        when(node.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.Club");
        when(node.getProperty("name")).thenReturn("foo");
        when(node.getRelationships(isA(RelationshipType.class), isA(Direction.class))).thenReturn(Sets.<Relationship>newHashSet());
        Club club = (Club) PersistentEntity.from(node);
        assertEquals("foo", club.getName());
    }

    @Test
    public void shouldGetRelationsWhenValueIsNull() {
        ClubWithSingleMessage club = new ClubWithSingleMessage();
        Set<Relation> relations = club.getRelations();
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertEquals("single_message", relation.getType().name());
        assertEquals(Direction.INCOMING, relation.getDirection());
        assertNull(relation.getEntities());
    }

    @Test
    public void shouldGetRelationsWhenValueIsAPersistentEntity() {
        ClubMessage message = new ClubMessage();
        Club club = new Club("clubname", "notpersistent");
        club.addMessage(message);
        Set<Relation> relations = club.getRelations();
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertEquals("message", relation.getType().name());
        assertEquals(Direction.INCOMING, relation.getDirection());
        assertEquals(1, relation.getEntities().size());
    }

    @Test
    public void shouldCreateEntityWithSingleRelationFromNode() {
        Node node = mock(Node.class);
        when(node.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.ClubWithSingleMessage");
        when(node.getProperty("name")).thenReturn("foo");

        Node msgNode = mock(Node.class);
        when(msgNode.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.ClubMessage");
        when(msgNode.getProperty("message")).thenReturn("testMessage");

        Relationship relationship = mock(Relationship.class);
        DynamicRelationshipType messageRelType = DynamicRelationshipType.withName("single_message");
        when(relationship.getType()).thenReturn(messageRelType);
        when(relationship.getOtherNode(node)).thenReturn(msgNode);

        when(node.getRelationships(argThat(new IsSameRelationType(messageRelType)), eq(Direction.INCOMING))).thenReturn(Sets.<Relationship>newHashSet(relationship));

        ClubWithSingleMessage club = (ClubWithSingleMessage) PersistentEntity.from(node);
        ClubMessage clubMessage = club.getMessage();
        assertNotNull(clubMessage);
        assertEquals("testMessage", clubMessage.getMessage());
    }

    @Test
    public void shouldCreateEntityWithMultiValuedRelationFromNode() {
        Node node = mock(Node.class);
        when(node.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.Club");
        when(node.getProperty("name")).thenReturn("foo");

        Node msgNode = mock(Node.class);
        when(msgNode.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.ClubMessage");
        when(msgNode.getProperty("message")).thenReturn("testMessage");

        Relationship relationship = mock(Relationship.class);
        DynamicRelationshipType messageRelType = DynamicRelationshipType.withName("message");
        when(relationship.getType()).thenReturn(messageRelType);
        when(relationship.getOtherNode(node)).thenReturn(msgNode);

        when(node.getRelationships(argThat(new IsSameRelationType(messageRelType)), eq(Direction.INCOMING))).thenReturn(Sets.<Relationship>newHashSet(relationship));


        Club club = (Club) PersistentEntity.from(node);
        Set<ClubMessage> messages = club.getMessages();
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("testMessage", messages.iterator().next().getMessage());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfInvalidRelationType() {
        Node node = mock(Node.class);
        when(node.getProperty(PersistentEntity.TYPE)).thenReturn("org.ogm.Club");
        when(node.getProperty("name")).thenReturn("foo");
        Set<Relation> relations = new HashSet<Relation>();
        ClubMessage message = new ClubMessage();
        relations.add(new Relation(Direction.INCOMING, "invalid", Sets.newHashSet(message)));

        PersistentEntity.from(node);
    }

    class IsSameRelationType extends ArgumentMatcher<DynamicRelationshipType> {
      private DynamicRelationshipType me;

        IsSameRelationType(DynamicRelationshipType me) {
            this.me = me;
        }

        public boolean matches(Object other) {
          return ((DynamicRelationshipType) other).name().equals(me.name());
      }
   }
}
