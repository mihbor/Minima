package org.minima.utils.mmr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.minima.objects.base.MiniNumber;
import org.minima.objects.proofs.Proof;
import org.minima.utils.json.JSONObject;

public class MMRProof extends Proof {
	
	/**
	 * The block time this proof points to
	 */
	MiniNumber mBlockTime = MiniNumber.ZERO;
	
	/**
	 * The Entry number in the MMR
	 */
	MiniNumber mEntryNumber = MiniNumber.ZERO;
	
	/**
	 * The Provable data
	 */
	MMRData mData;
	
	public MMRProof() {
		super();
	}
		
	public MMRProof(MiniNumber zEntryNumber, MMRData zInitialData, MiniNumber zBlockTime) {
		mEntryNumber = zEntryNumber;
		mData        = zInitialData;
		mBlockTime   = zBlockTime;
		
		setData(mData.getFinalHash(), mData.getValueSum());
	}
	
	public MiniNumber getBlockTime() {
		return mBlockTime;
	}
	
	public MiniNumber getEntryNumber() {
		return mEntryNumber;
	}
	
	public MMRData getMMRData() {
		return mData;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject(); 
		
		obj.put("blocktime", mBlockTime.toString());
		obj.put("entry", mEntryNumber.toString());
		obj.put("data", mData.toJSON());
		obj.put("proof",super.toJSON());
		
		return obj;
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mBlockTime.writeDataStream(zOut);
		mEntryNumber.writeDataStream(zOut);
		mData.writeDataStream(zOut);
		
		super.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mBlockTime   = MiniNumber.ReadFromStream(zIn);
		mEntryNumber = MiniNumber.ReadFromStream(zIn);
		mData        = MMRData.ReadFromStream(zIn);
		
		super.readDataStream(zIn);
	}
	
	public static MMRProof ReadFromStream(DataInputStream zIn) throws IOException{
		MMRProof proof = new MMRProof();
		proof.readDataStream(zIn);
		return proof;
	}
}
