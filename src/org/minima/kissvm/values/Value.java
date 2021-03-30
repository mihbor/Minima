/**
 * 
 */
package org.minima.kissvm.values;

import org.minima.kissvm.tokens.Token;

/**
 * @author Spartacus Rex
 *
 */
public abstract class Value {
	
	/**
	 * The Only Value Types
	 */
	public static final int VALUE_HEX     = 1;
	public static final int VALUE_NUMBER  = 2;
	public static final int VALUE_STRING  = 4;
	public static final int VALUE_BOOLEAN = 8;
	
	/**
	 * What type of Value is this..
	 * @return
	 */
	public abstract int getValueType();
	
	/**
	 * TRUE or FALSE
	 */
	public boolean isTrue() {
		return !isFalse();
	}
	
	public abstract boolean isFalse();
	
	/**
	 * Strict Check this type and throw an exception if not
	 */
	public void verifyType(int zType) throws IllegalArgumentException{
		if (getValueType() != zType) {
			throw new IllegalArgumentException("Incorrect value type, expected "+zType+" found "+getValueType());
		}
	}
	
	
	/**
	 * GLOBAL STATIC FUNCTION for creating a Value from any string
	 */
	public static Value getValue(String zValue){
		//Trim it..
		String str = zValue.trim();
		
		if(str.startsWith("[") && str.endsWith("]")) {
			//remove the square brackets..
			String sc = str.substring(1,str.length()-1);
			return new StringValue(sc);
			
		}else if(str.startsWith("0x")) {
			return new HEXValue(str);

		}else if(str.equalsIgnoreCase("TRUE")) {
			return BooleanValue.TRUE;

		}else if(str.equalsIgnoreCase("FALSE")) {
			return BooleanValue.FALSE;

		}else if(Token.isNumeric(str)){
			return new NumberValue(str);
		
		}else {
			throw new IllegalArgumentException("Invalid value : "+zValue);
		}
	}
	
	/**
	 * GLOBAL STATIC FUNCTION for telling the value type
	 */
	public static int getValueType(String zValue) throws IllegalArgumentException {
		if(zValue.startsWith("[") && zValue.endsWith("]")) {
			//Then initialise the value 
			return VALUE_STRING;
			
		}else if(zValue.startsWith("0x")) {
			return VALUE_HEX;

		}else if(zValue.equals("TRUE")) {
			return VALUE_BOOLEAN;

		}else if(zValue.equals("FALSE")) {
			return VALUE_BOOLEAN;

		}else if(Token.isNumeric(zValue)){
			return VALUE_NUMBER;
	
		}else {
			throw new IllegalArgumentException("Invalid value type : "+zValue);
		}
	}
	
	/**
	 * Get the type as a string
	 * 
	 * @param zType
	 * @return
	 */
	public static String getValueTypeString(int zType) {
		if(zType == VALUE_BOOLEAN) {
			return "BOOLEAN";
		}else if(zType == VALUE_HEX) {
			return "HEX";
		}else if(zType == VALUE_NUMBER) {
			return "NUMBER";
		}else if(zType == VALUE_STRING) {
			return "SCRIPT";
		}
		
		return "ERROR_UNKNOWN_TYPE";
	}
	
	/**
	 * Check that both values are of the same type and throw an exception if not
	 * @param zV1
	 * @param zV2
	 */
	public static void checkSameType(Value zV1,Value zV2) throws IllegalArgumentException {
		if(zV1.getValueType() != zV2.getValueType()) {
			throw new IllegalArgumentException("Operation requires that both value types MUST be the same");
		}
	}
	
	public static void checkSameType(Value zV1,Value zV2, int zType) throws IllegalArgumentException {
		if(zV1.getValueType() != zV2.getValueType()) {
			throw new IllegalArgumentException("Operation requires that both value types MUST be the same");
		}
		
		if(zV1.getValueType() != zType) {
			throw new IllegalArgumentException("Operation requires that both value types are of type "+zType);
		}
	}
}
