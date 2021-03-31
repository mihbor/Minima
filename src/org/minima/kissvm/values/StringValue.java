package org.minima.kissvm.values;

import org.minima.objects.base.MiniString;

public class StringValue extends Value {
	
	/**
	 * The String
	 */
	MiniString mString;
	
	public StringValue(String zString) {
		mString = new MiniString(zString);
	}
	
	@Override
	public String toString() {
		return mString.toString();
	}
	
	public MiniString getMiniString() {
		return mString;
	}
	
	@Override
	public int getValueType() {
		return VALUE_STRING;
	}
	
	/**
	 * Add this script and return the result
	 * 
	 * @param zStringValue
	 * @return
	 */
	public StringValue add(StringValue zStringValue) {
		return new StringValue(mString.toString()+zStringValue.toString());
	}

	@Override
	public boolean isFalse() {
		return mString.toString().toLowerCase().equals("false");
	}
}
