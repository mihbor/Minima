package org.minima.kissvm.tokens.symbols;

public class Symbol {

	/**
	 * What kind of Symbol is this
	 */
	public static final int SYMBOL_TYPE_FUNCTION = 0;
	public static final int SYMBOL_TYPE_VARIABLE = 1;
	public static final int SYMBOL_TYPE_CONSTANT = 2;
	
	/**
	 * What Data type is the symbol
	 */
	public static final int SYMBOL_DATA_BOOL	 = 0;
	public static final int SYMBOL_DATA_HEX 	 = 1;
	public static final int SYMBOL_DATA_NUMBER   = 2;
	public static final int SYMBOL_DATA_STRING   = 3;
	
	
	String	mName;
	int 	mType;
	int 	mDataValue;
	
	public Symbol(String zName, int zType, int zDataValue ) {
		mName 		= zName;
		mType 		= zType;
		mDataValue 	= zDataValue;
	}
}
