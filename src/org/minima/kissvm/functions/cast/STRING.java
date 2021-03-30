package org.minima.kissvm.functions.cast;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.values.HEXValue;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;
import org.minima.objects.base.MiniString;

public class STRING extends MinimaFunction {

	public STRING() {
		super("STRING");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(1);
		
		Value val = getParameter(0).getValue(zContract);
		
		//HEX value gets converted..
		if(val.getValueType() == Value.VALUE_HEX) {
			HEXValue hex = (HEXValue)val;
			return new StringValue(new String( hex.getRawData(), MiniString.MINIMA_CHARSET ));
		}
		
		return new StringValue(val.toString());
	}

	@Override
	public MinimaFunction getNewFunction() {
		return new STRING();
	}
}
