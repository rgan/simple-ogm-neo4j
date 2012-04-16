package org.ogm;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyConverterTest {

    @Test
    public void shouldHandlePrimitiveTypes() {
        PropertyConverter propertyConverter = new PropertyConverter();
        assertEquals(5, propertyConverter.convertToNeo4jPropertyValue(5));
        assertEquals(5.0, propertyConverter.convertToNeo4jPropertyValue(5.0));
        assertTrue((Boolean) propertyConverter.convertToNeo4jPropertyValue(true));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForUnhandledType() {
        PropertyConverter propertyConverter = new PropertyConverter();
        propertyConverter.convertToNeo4jPropertyValue(new Date());
    }
}
