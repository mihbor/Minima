package org.minima.objects.greet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.minima.database.mmr.MMREntry;
import org.minima.database.mmr.MMRSet;
import org.minima.database.txpowtree.BlockTree;
import org.minima.database.txpowtree.BlockTreeNode;
import org.minima.database.txpowtree.CascadeTree;
import org.minima.objects.TxPoW;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.MiniFormat;
import org.minima.utils.MinimaLogger;
import org.minima.utils.Streamable;

public class SyncPackage implements Streamable{

	/**
	 * The Current SyncPackage Version..
	 */
	MiniNumber mCascadeNode = MiniNumber.ZERO;
	
	ArrayList<SyncPacket> mNodes = new ArrayList<>();
	
	public SyncPackage() {}
	
	public void setCascadeNode(MiniNumber zNumber) {
		mCascadeNode = zNumber;
	}
	
	public MiniNumber getCascadeNode() {
		return mCascadeNode;
	}
	
	public ArrayList<SyncPacket> getAllNodes(){
		return mNodes;
	}
	
	public void setMMRHashOnly() {
		for(SyncPacket spack : mNodes) {
			//TxPoW txpow = spack.getTxPOW();
			MMRSet mmr  = spack.getMMRSet();
			if(mmr!=null) {
				mmr.setHashOnly();
			}	
		}
	}
	
	public BigInteger calculateWeight() {
		//Create a Tree and add all these blocks.. then calculate the weight..
		BlockTree blktree = new BlockTree();
		
		//Is it empty..
		if(mNodes.size() == 0) {
			return BigInteger.ZERO;
		}
		
		//Drill down 
		for(SyncPacket spack : mNodes) {
			TxPoW txpow = spack.getTxPOW();
			MMRSet mmr  = spack.getMMRSet();
			boolean cascade = spack.isCascade();
			
			BlockTreeNode node = new BlockTreeNode(txpow);
			node.setCascade(cascade);
			node.setState(BlockTreeNode.BLOCKSTATE_VALID);
			
			//Sort the MMR..
			node.setMMRset(mmr);

			//Add it..
			blktree.hardAddNode(node, false);
			
			//Is this the cascade block
			if(txpow.getBlockNumber().isEqual(getCascadeNode())) {
				blktree.hardSetCascadeNode(node);
			}
		}
		
		//Now reset..
		CascadeTree casc = new CascadeTree(blktree);
		casc.cascadedTree();
		
		//Get the cascaded version..
		BlockTree newtree = casc.getCascadeTree();
		
		//Whats the weight..
		BigInteger totweight = newtree.getChainRoot().getTotalWeight();
		
		return totweight;
	}
	
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		//Write the details..
		MiniNumber len =  new MiniNumber(mNodes.size());
		len.writeDataStream(zOut);
		for(SyncPacket node : mNodes) {
			node.writeDataStream(zOut);
		}
		mCascadeNode.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mNodes = new ArrayList<>();
		MiniNumber nodelen = MiniNumber.ReadFromStream(zIn);
		int len = nodelen.getAsInt();
		
		MMRSet parentmmr = null;
		int tot = 0;
		int totduplicates = 0;
		int totentries    = 0;
		long totsize      = 0;
		for(int i=0;i<len;i++) {
			
			//Reda the Packet
			SyncPacket node = new SyncPacket();
			node.readDataStream(zIn);
			
			if(tot % 250 == 0) {
				MinimaLogger.memlog("Sync @ "+tot);
			}
			
//			//Check for copies of entries..
//			MMRSet mmr = node.getMMRSet();
//			if(mmr != null) {
//				MinimaLogger.log("Check for duplicates @"+mmr.getBlockTime()+" "+totduplicates+" / "+totentries);
//				//Set the Parent..
//				if(parentmmr!=null) {
//					mmr.setParent(parentmmr);
//				}
//				parentmmr = mmr;
//				
//				//MinimaLogger.log("Check duplicates in MMR "+mmr.getBlockTime());
//				ArrayList<MMREntry> peaks = mmr.getMMRPeaks();
//				Hashtable<String, MMREntry> setEntries = mmr.mSetEntries;
//				totentries += setEntries.size();
//				
//				Enumeration<MMREntry> entries = setEntries.elements();
//				while(entries.hasMoreElements()) {
//					MMREntry entry = entries.nextElement();
//				
//					//Check if there is another..
//					MMRSet pmmr = mmr.getParent();
//					
//					if(pmmr!=null) {
//						MMREntry pentry = pmmr.getEntry(entry.getRow(), entry.getEntryNumber());
//						if(!pentry.isEmpty() && pentry.getData().getFinalHash().isEqual(entry.getData().getFinalHash())) {
////							if(!pentry.isEmpty()) {
//							//Duplicate found!
//							totduplicates++;
////								MinimaLogger.log("@"+pmmr.getBlockTime()+" ["+totduplicates+"] Parent:"+pentry+" Base:"+entry);
//						
//							//Now reset the parent to that one..
//							pmmr = mmr.getParentAtTime(pentry.getBlockTime());
//							if(pmmr != null) {
//								pmmr = pmmr.getParent();
//							}
//						}else {
//							pmmr = null;
//						}
//					}
//				
//				}
//			}
			
			//Add to the list
//			if(tot<100) {
				mNodes.add(node);
				tot++;
				
//				MinimaLogger.log("TxPow Size : "+node.getTxPOW().getSizeinBytes());
				
				totsize += node.getTxPOW().getSizeinBytes();
//			}
				
				
		}
//		MinimaLogger.log(" Total Duplicates "+totduplicates+" / "+totentries);
		MinimaLogger.log(" Sync PacketTotal TxPoW Size  "+MiniFormat.formatSize(totsize));
		
		
		mCascadeNode = MiniNumber.ReadFromStream(zIn);
	}
	
	@Override
	public String toString() {
		String ret = "SyncPackage size : "+mNodes.size();
//		for(SyncPacket node : mNodes) {
//			ret += node+",";
//		}
		return ret;
	}
}
