package org.ogm;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.ogm.annotations.Direction;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
        Club club = (Club) PersistentEntity.from(node);
        assertEquals("foo", club.getName());
    }

    @Test
    public void shouldGetRelationsWhenValueIsNull() {
        ClubWithSingleMessage club = new ClubWithSingleMessage();
        Set<Relation> relations = club.getRelations();
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertEquals("single_message", relation.getType());
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
        assertEquals("message", relation.getType());
        assertEquals(Direction.INCOMING, relation.getDirection());
        assertEquals(1, relation.getEntities().size());
    }
}
