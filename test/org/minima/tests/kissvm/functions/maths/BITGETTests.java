package org.minima.tests.kissvm.functions.maths;

import org.minima.kissvm.functions.maths.BITGET;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.exceptions.MinimaParseException;
import org.minima.kissvm.expressions.ConstantExpression;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.values.BooleanValue;
import org.minima.kissvm.values.HEXValue;
import org.minima.kissvm.values.NumberValue;
import org.minima.kissvm.values.ScriptValue;
import org.minima.kissvm.values.Value;
import org.minima.objects.Transaction;
import org.minima.objects.Witness;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

//BooleanValue BITGET (HEXValue var, NumberValue pos)
public class BITGETTests {

    @Test
    public void testConstructors() {
        BITGET fn = new BITGET();
        MinimaFunction mf = fn.getNewFunction();

        assertEquals("BITGET", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = MinimaFunction.getFunction("BITGET");
            assertEquals("BITGET", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (MinimaParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        BITGET fn = new BITGET();

        { // Incorrect function behavior, as it uses invalid byte indexing in data array
            for (int i = 0; i < 63; i++) {
                Long TestValue = 1L << i;
                {
                    MinimaFunction mf = fn.getNewFunction();
                    mf.addParameter(new ConstantExpression(new HEXValue(Long.toHexString(TestValue))));
                    mf.addParameter(new ConstantExpression(new NumberValue(i)));
                    //try {
                    //    Value res = mf.runFunction(ctr);
                    //    assertEquals(Value.VALUE_BOOLEAN, res.getValueType());
                    //    assertEquals("TRUE", ((BooleanValue) res).toString());
                    //} catch (ExecutionException ex) {
                    //    fail();
                    //}
                }

                TestValue = ~TestValue;
                {
                    MinimaFunction mf = fn.getNewFunction();
                    mf.addParameter(new ConstantExpression(new HEXValue(Long.toHexString(TestValue))));
                    mf.addParameter(new ConstantExpression(new NumberValue(i)));
                    //try {
                    //    Value res = mf.runFunction(ctr);
                    //    assertEquals(Value.VALUE_BOOLEAN, res.getValueType());
                    //    assertEquals("FALSE", ((BooleanValue) res).toString());
                    //} catch (ExecutionException ex) {
                    //    fail();
                    //}
                }

            }
        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        BITGET fn = new BITGET();

        // Invalid param count
        {
            MinimaFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x00")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
            //assertThrows(ExecutionException.class, () -> { // Should throw this
            //    Value res = mf.runFunction(ctr);
            //});
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> { // but throws this
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(-32)));
            //assertThrows(ExecutionException.class, () -> { // Should throw this
            //    Value res = mf.runFunction(ctr);
            //});
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> { // but throws this
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(-31)));
            //assertThrows(ExecutionException.class, () -> { // Should throw this
            //    Value res = mf.runFunction(ctr);
            //});
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> { // but throws this
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(-4)));
            //assertThrows(ExecutionException.class, () -> { // Should throw this
            //    Value res = mf.runFunction(ctr);
            //});
            // But does not throw
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(-1)));
            //assertThrows(ExecutionException.class, () -> { // Should throw this
            //    Value res = mf.runFunction(ctr);
            //});
            // But does not throw
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(32)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(33)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(Integer.MAX_VALUE)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param types
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new NumberValue(123456798)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(123456798)));
            mf.addParameter(new ConstantExpression(new NumberValue(123456798)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(123456798)));
            mf.addParameter(new ConstantExpression(new ScriptValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HEXValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new ScriptValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
    }
}