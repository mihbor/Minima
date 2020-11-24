package org.minima.objects;

import org.minima.database.mmr.MMRSet;
import org.minima.database.txpowtree.BlockTreeNode;

public class TxBlock {

	TxPoW mHeaderPow;
	
	MMRSet mMMRData;
	
	public TxBlock(BlockTreeNode zNode) {
		mHeaderPow = zNode.getTxPow();
		mMMRData = zNode.getMMRSet();
	}
	
}
