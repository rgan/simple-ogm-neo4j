package org.ogm;

import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
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
    public void shouldGetProperties() {
        Club club = new Club("clubname", "notpersistent");
        Map<String,Object> properties = club.getProperties();
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
}
