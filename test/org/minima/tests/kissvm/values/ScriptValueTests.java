package org.minima.tests.kissvm.values;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;

public class ScriptValueTests {

    @Test
    public void testConstructors() {

        StringValue sv1 = new StringValue("");
        StringValue sv2 = new StringValue("[]");
        StringValue sv3 = new StringValue("return true");
        StringValue sv4 = new StringValue("HELLO WORLD");

        assertEquals("should be equal ", "hello world", Value.getValue("[HELLO WORLD]").toString());
        assertEquals("should be equal ", "[ ]", sv2.toString());
        assertEquals("should be equal ", "RETURN TRUE", sv3.toString());
        assertEquals("should be equal ", "hello world", sv4.toString());

        assertEquals("should be equal ", "RETURN TRUE hello world", sv3.add(sv4).toString());
        assertEquals("should be equal ", "hello world RETURN TRUE", sv4.add(sv3).toString());

        assertEquals("should be equal ", StringValue.VALUE_STRING, sv1.getValueType());
        assertEquals("should be equal ", StringValue.VALUE_STRING, sv2.getValueType());
        assertEquals("should be equal ", StringValue.VALUE_STRING, sv3.getValueType());
        assertEquals("should be equal ", StringValue.VALUE_STRING, sv4.getValueType());

    }
}
