package org.minima.system.commands.txn;

import java.util.ArrayList;

import org.minima.database.MinimaDB;
import org.minima.database.mmr.MMRProof;
import org.minima.database.txpowtree.TxPoWTreeNode;
import org.minima.database.wallet.KeyRow;
import org.minima.database.wallet.Wallet;
import org.minima.objects.Coin;
import org.minima.objects.CoinProof;
import org.minima.objects.ScriptProof;
import org.minima.objects.Transaction;
import org.minima.objects.Witness;
import org.minima.objects.base.MiniNumber;
import org.minima.system.brains.TxPoWSearcher;
import org.minima.system.commands.CommandException;
import org.minima.system.params.GlobalParams;
import org.minima.utils.MinimaLogger;

public class txnutils {

	public static void setMMRandScripts(Transaction zTransaction, Witness zWitness) throws Exception {
		//get the tip..
		TxPoWTreeNode tip = MinimaDB.getDB().getTxPoWTree().getTip();
		
		//Get all the input coins..
		ArrayList<Coin> baseinputs = zTransaction.getAllInputs();
		
		//Are any of the inputs floating
		ArrayList<Coin> inputs = new ArrayList<>();
		for(Coin cc : baseinputs) {
			if(cc.getCoinID().isEqual(Coin.COINID_ELTOO)) {
			
				//Get the MOST recent coin to attach to this transaction..
				Coin floater = TxPoWSearcher.getFloatingCoin(tip, cc.getAmount(), cc.getAddress(), cc.getTokenID());	
				
				if(floater == null) {
					throw new CommandException("Could not find valid unspent coin for "+cc.toJSON());
				}
				
				inputs.add(floater);
				
			}else {
				
				//Get the complete coin given the CoinID 
				//could be a pre-made coin.. so use correct MMREntry / Created 
				Coin current = TxPoWSearcher.searchCoin(cc.getCoinID());
				if(current == null) {
					throw new CommandException("Coin with CoinID not found : "+cc.getCoinID().to0xString());
				}
				
				inputs.add(current);
			}
		}
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
				
		//Add the inputs..
		for(Coin input : inputs) {
			//How deep
			if(input.getBlockCreated().isMore(minblock)) {
				minblock = input.getBlockCreated();
			}
		}
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.MINIMA_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.MINIMA_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		if(mmrnode == null) {
			//Not enough blocks..
			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Get the main Wallet
		Wallet walletdb = MinimaDB.getDB().getWallet();
		
		//Clear the MMR..
		zWitness.clearCoinProofs();
		
		//Add the MMR proofs for the coins..
		for(Coin input : inputs) {
			
			//Get the proof..
			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
			
			//Create the CoinProof..
			CoinProof cp = new CoinProof(input, proof);
			
			//Add it to the witness data
			zWitness.addCoinProof(cp);
			
			//Add the script proofs
			String scraddress 	= input.getAddress().to0xString();
			KeyRow keyrow 		= walletdb.getKeysRowFromAddress(scraddress); 
			if(keyrow == null) {
				throw new Exception("SERIOUS ERROR script missing for simple address : "+scraddress);
			}
			
			ScriptProof pscr = new ScriptProof(keyrow.getScript());
			zWitness.addScript(pscr);
		}
	}
	
	public static void setMMRandScripts(Coin zCoin, Witness zWitness) throws Exception {
		//get the tip..
		TxPoWTreeNode tip = MinimaDB.getDB().getTxPoWTree().getTip();
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
		
		//How deep
		if(zCoin.getBlockCreated().isMore(minblock)) {
			minblock = zCoin.getBlockCreated();
		}		
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.MINIMA_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.MINIMA_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		MinimaLogger.log("MMR PROOF "+tip.getBlockNumber()+" / "+mmrnode.getBlockNumber());
		if(mmrnode == null) {
			//Not enough blocks..
			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Get the main Wallet
		Wallet walletdb = MinimaDB.getDB().getWallet();
			
		//Get the proof..
		MMRProof proof = mmrnode.getMMR().getProofToPeak(zCoin.getMMREntryNumber());
		
		//Create the CoinProof..
		CoinProof cp = new CoinProof(zCoin, proof);
		
		//Add it to the witness data
		zWitness.addCoinProof(cp);
		
		//Add the script proofs
		String scraddress 	= zCoin.getAddress().to0xString();
		KeyRow keyrow 		= walletdb.getKeysRowFromAddress(scraddress); 
		if(keyrow == null) {
			throw new Exception("SERIOUS ERROR script missing for simple address : "+scraddress);
		}
		
		ScriptProof pscr = new ScriptProof(keyrow.getScript());
		zWitness.addScript(pscr);
	}
}
