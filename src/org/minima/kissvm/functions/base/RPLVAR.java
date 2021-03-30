package org.minima.kissvm.functions.base;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.tokens.Token;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;

/**
 * Search for a variable assignment in a Script and replace it's value.
 * @author spartacusrex
 *
 */
public class RPLVAR extends MinimaFunction {

	public RPLVAR() {
		super("RPLVAR");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(3);
		
		//Get the script..
		StringValue script = zContract.getStringParam(0, this);
		String ss = script.toString();
		
		//Get the variable name
		StringValue var    = zContract.getStringParam(1, this);;
		
		//Get the expression
		StringValue exp    = zContract.getStringParam(2, this);;
				
		//Now replace.. 
		String search = "LET "+var+" = ";
		int len       = search.length();
		int index     = ss.indexOf(search);
		
		//Not Found..
		if(index == -1) {
			return new StringValue(ss);
		}
		
		//Otherwise..
		int start = index+len;
		
		//Now find the end..
		int end = script.toString().length();
		for(String command : Token.TOKENS_COMMAND) {
			int comm = ss.indexOf(command,start);
			if(comm != -1) {
				if(comm < end) {
					end = comm;
				}
			}
		}
		
		//Now replace that section..
		String ret = ss.substring(0,start);
		ret += " "+exp.toString()+" ";
		ret += ss.substring(end);
		
		return new StringValue(ret);
	}
	
	@Override
	public MinimaFunction getNewFunction() {
		return new RPLVAR();
	}
}
