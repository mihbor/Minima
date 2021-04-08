package org.minima.kissvm.functions.number;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.values.NumberValue;
import org.minima.kissvm.values.Value;

public class FLOOR extends MinimaFunction {

	public FLOOR() {
		super("FLOOR");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(1);
		
		NumberValue number = zContract.getNumberParam(0, this);

		return new NumberValue(number.getNumber().floor());
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public MinimaFunction getNewFunction() {
		return new FLOOR();
	}
}