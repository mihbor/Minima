package org.minima.tests.kissvm.functions.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.exceptions.MinimaParseException;
import org.minima.kissvm.expressions.ConstantExpression;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.functions.base.RPLVAR;
import org.minima.kissvm.values.BooleanValue;
import org.minima.kissvm.values.HEXValue;
import org.minima.kissvm.values.NumberValue;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;
import org.minima.objects.Transaction;
import org.minima.objects.Witness;

//ScriptValue RPLVAR (ScriptValue script ScriptValue var1 ScriptValue var2)
public class RPLVARTests {

    @Test
    public void testConstructors() {
        RPLVAR fn = new RPLVAR();
        MinimaFunction mf = fn.getNewFunction();

        assertEquals("RPLVAR", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = MinimaFunction.getFunction("RPLVAR");
            assertEquals("RPLVAR", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (MinimaParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        RPLVAR fn = new RPLVAR();

        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = b", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A123B = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A123B")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a123b = b", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = 5", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5 LET B = A + 1 LET A = B")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = c LET b = a + 1 LET a = b", ((StringValue) res).toString()); // Replaces only first occurence
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = CONCAT(A B C D)")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = c", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 1 + 2")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = c", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 1 + 2 as")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("LET a = c", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        RPLVAR fn = new RPLVAR();

        // Invalid param count
        {
            MinimaFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            mf.addParameter(new ConstantExpression(new StringValue("C")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("ABCDEFGHIJKLMNOPQRSTUVWXYZ")));
            mf.addParameter(new ConstantExpression(new StringValue("99")));
            mf.addParameter(new ConstantExpression(new StringValue("45")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("abcdefghijklmnopqrstuvwxyz", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("ABCDEFGHIJKLMNOPQRSTUVWXYZ 99")));
            mf.addParameter(new ConstantExpression(new StringValue("99")));
            mf.addParameter(new ConstantExpression(new StringValue("45")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_STRING, res.getValueType());
                assertEquals("abcdefghijklmnopqrstuvwxyz 99", ((StringValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

        // Invalid param types
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x1234")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(100)));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new HEXValue("0x1234")));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new NumberValue(100)));
            mf.addParameter(new ConstantExpression(new StringValue("B")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new NumberValue(100)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("LET A = 5")));
            mf.addParameter(new ConstantExpression(new StringValue("A")));
            mf.addParameter(new ConstantExpression(new NumberValue(100)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

    }
}
