package org.minima.kissvm.tokens.symbols;

import java.util.ArrayList;
import java.util.Hashtable;

import org.minima.kissvm.exceptions.MinimaParseException;
import org.minima.kissvm.tokens.Token;
import org.minima.kissvm.tokens.Tokenizer;
import org.minima.system.input.functions.tokens;

public class SymbolTable {

	Hashtable<String, Symbol> mAllSymbols;
	
	public SymbolTable() {
		mAllSymbols = new Hashtable<>();
	}
	
	public void createTable(ArrayList<Token> zTokens) {
		for(Token tok : zTokens) {
			
			//Is it worthy of adding..
			if(tok.getTokenType() == Token.TOKEN_VARIABLE) {
//				insert(tok.getToken(), zType, zDataValue);
				
			}
			
		}
	}
	
	public void insert(String zName, int zType, int zDataValue) {
		mAllSymbols.put(zName, new Symbol(zName, zType, zDataValue));
	}
	
	public Symbol find(String zName) {
		return mAllSymbols.get(zName);
	}
	
	
	public static void main(String[] zArgs) {
	
		String ss = "LET d = ABS(-99) LET ff = 2.34 LET g = ff + d";
		
		Tokenizer tokz = new Tokenizer(ss);
		
		try {
			ArrayList<Token> tokens = tokz.tokenize();
		
			//Now create symbol table..
			
		
		} catch (MinimaParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
