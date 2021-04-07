package org.minima.kissvm.functions.txn.input;

import java.util.ArrayList;

import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.values.HEXValue;
import org.minima.kissvm.values.Value;
import org.minima.objects.Coin;
import org.minima.objects.Transaction;

public class GETINTOK extends MinimaFunction {

	public GETINTOK() {
		super("GETINTOK");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(1);
		
		//Which Output
		int input = zContract.getNumberParam(0, this).getNumber().getAsInt();
				
		//Get the Transaction
		Transaction trans = zContract.getTransaction();
		
		//Check output exists..
		ArrayList<Coin> ins = trans.getAllInputs();
		if(input<0 || ins.size()<=input) {
			throw new ExecutionException("Input number out of range "+input+"/"+ins.size());
		}
		
		//Get it..
		Coin cc = ins.get(input);
		
		//Return the address	
		return new HEXValue(cc.getTokenID());
	}

	@Override
	public MinimaFunction getNewFunction() {
		return new GETINTOK();
	}
}
