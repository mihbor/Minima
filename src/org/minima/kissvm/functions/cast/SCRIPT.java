package org.minima.kissvm.functions.cast;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;

public class SCRIPT extends MinimaFunction {

	public SCRIPT() {
		super("SCRIPT");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(1);
		
		//Must be a STRING
		StringValue sv = zContract.getStringParam(0, this);
		
		//Clean it for script..!
		return new StringValue(Contract.cleanScript(sv.toString()));
	}

	@Override
	public MinimaFunction getNewFunction() {
		return new SCRIPT();
	}
}
