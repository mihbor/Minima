package org.minima.objects.proofs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.minima.database.mmr.MMREntry;
import org.minima.objects.base.MiniByte;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.BaseConverter;
import org.minima.utils.Crypto;
import org.minima.utils.MinimaLogger;
import org.minima.utils.Streamable;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;

public class Proof implements Streamable {

	public class ProofChunk implements Streamable {
		MiniByte    mLeftRight;
		MiniData    mHash;
		MiniNumber  mValue;
		
		public ProofChunk() {}
		
		public ProofChunk(MiniByte zLeft, MiniData zHash, MiniNumber zValue) {
			mLeftRight 	= zLeft;
			mHash 		= zHash;
			mValue 		= zValue;
		}
		
		public MiniByte getLeft() {
			return mLeftRight;
		}
		
		public MiniData getHash() {
			return mHash;
		}
		
		public MiniNumber getValue() {
			return mValue;
		}
		
		public JSONObject toJSON() {
			JSONObject json = new JSONObject();
			
			json.put("left", getLeft().isTrue());
			json.put("hash", getHash().to0xString());
			json.put("value", getValue().toString());
			
			return json;
		}
		
		@Override
		public void writeDataStream(DataOutputStream zOut) throws IOException {
			mLeftRight.writeDataStream(zOut);
			mHash.writeDataStream(zOut);
			mValue.writeDataStream(zOut);
		}

		@Override
		public void readDataStream(DataInputStream zIn) throws IOException {
			mLeftRight 	= MiniByte.ReadFromStream(zIn);
			mHash      	= MiniData.ReadFromStream(zIn);
			mValue     	= MiniNumber.ReadFromStream(zIn);
		}
	}
	
	//The data you are trying to prove..
	protected MiniData   mData;
	protected MiniNumber mValue;
	
	//The Merkle Branch that when applied to the data gives the final proof;
	protected ArrayList<ProofChunk> mProofChain;
	
	//Calculate this once
	protected MiniData mFinalHash;
	protected MiniData mChainSHA;
	protected boolean mFinalized;
		
	private int HASH_BITS = 512;
	
	public Proof(){
		mProofChain = new ArrayList<>();
	}

	public void setData(MiniData zData) {
		setData(zData, MiniNumber.ZERO);
	}

	public void setData(MiniData zData, MiniNumber zValue) {
		mData  = zData;
		mValue = zValue;
	}
	
	public MiniData getData() {
		return mData;
	}
	
	public MiniNumber getValue() {
		return mValue;
	}
	
	public void setProof(MiniData zChainSHAProof) {
		mFinalized  = false;
		mProofChain = new ArrayList<>();
	
		ByteArrayInputStream bais = new ByteArrayInputStream(zChainSHAProof.getData());
		DataInputStream dis = new DataInputStream(bais);
		
		try {
			//The HASH_BITS is first
			int hb    = MiniByte.ReadFromStream(dis).getValue();
			HASH_BITS = hb * 32;
			
			while(dis.available()>0) {
				//Create the Proof Chunk
				ProofChunk chunk = new ProofChunk();
				chunk.readDataStream(dis);
				addProofChunk(chunk);
			}
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			MinimaLogger.log("setProof Error "+e);
			e.printStackTrace();
		}
		
		
		finalizeHash();
	}
	
	public void setHashBitLength(int zBitLength) {
		HASH_BITS = zBitLength;
	}
	
	public void addProofChunk(MiniByte zLeft, MiniData zHash) {
		addProofChunk(zLeft, zHash, MiniNumber.ZERO);
	}
	
	public void addProofChunk(MiniByte zLeft, MiniData zHash, MiniNumber zValue) {
		addProofChunk(new ProofChunk(zLeft, zHash, zValue));
	}
	
	public void addProofChunk(ProofChunk zChunk) {
		mProofChain.add(zChunk);
	}
	
	public int getProofLen() {
		return mProofChain.size();
	}
	
	public ProofChunk getProofChunk(int zNum) {
		return mProofChain.get(zNum);
	}
	
	public void finalizeHash() {
		//Reset so that can be recalculated
		mFinalized = false;
		
		//Recalculate
		mFinalHash = getFinalHash();
		mChainSHA  = getChainSHAProof();
		
		//Ok - it's done now..
		mFinalized = true;
	}
	
	public MiniData getChainSHAProof() {
		if(mFinalized) {
			return mChainSHA;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			//First write out the HASH_BITS
			MiniByte hb = new MiniByte(HASH_BITS / 32);
			hb.writeDataStream(dos);
			
			//Now write out the data..
			int len = mProofChain.size();
			for(int i=0;i<len;i++){
				ProofChunk chunk = mProofChain.get(i);
				chunk.writeDataStream(dos);
			}
			
			dos.close();
			baos.close();
			
		} catch (IOException e) {
			MinimaLogger.log("getChainSHAProof Error "+e);
			e.printStackTrace();
		}
		
		//Convert to MiniData..
		return new MiniData(baos.toByteArray());
	}
	
	public MiniData getFinalHash() {
		if(mFinalized) {
			return mFinalHash;
		}
		
		//Get the Final Hash of the Data
		MiniData current = mData;
		MiniNumber value = mValue;
		
		//And LEFT ENtry
		MMREntry currententry = null;
		
		int len = getProofLen();
		for(int i=0;i<len;i++) {
			ProofChunk chunk = mProofChain.get(i);
			
			//What is the SUM
			value = value.add(chunk.getValue());
			
			//Hash in the correct order
			if(chunk.getLeft().isTrue()) {
//				current = Crypto.getInstance().hashAllObjects(HASH_BITS, chunk.getHash(), current,value);
				current = Crypto.getInstance().hashAllObjects(HASH_BITS, chunk.getHash(), current);
			}else {
//				current = Crypto.getInstance().hashAllObjects(HASH_BITS, current, chunk.getHash(),value);
				current = Crypto.getInstance().hashAllObjects(HASH_BITS, current, chunk.getHash());
			}
		}
		
		return current;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		JSONArray proof = new JSONArray();
		int len = mProofChain.size();
		for(int i=0;i<len;i++){
			proof.add(mProofChain.get(i).toJSON());
		}
		
		json.put("data", mData.to0xString());
		json.put("value", mValue.toString());
		json.put("hashbits", HASH_BITS);
		json.put("proofchain", proof);
		json.put("chainsha", getChainSHAProof().to0xString());
		
		json.put("finalhash", getFinalHash().to0xString());
		
		return json;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniByte hb = new MiniByte(HASH_BITS / 32);
		hb.writeDataStream(zOut);
		
		mData.writeDataStream(zOut);
		mValue.writeDataStream(zOut);
		
		MiniNumber mlen = new MiniNumber(mProofChain.size());
		mlen.writeDataStream(zOut);
		int len = mlen.getAsInt();
		for(int i=0;i<len;i++) {
			ProofChunk chunk = mProofChain.get(i);
			chunk.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		MiniByte hb = MiniByte.ReadFromStream(zIn);
		HASH_BITS   = hb.getValue() * 32;
		
		mData  = MiniData.ReadFromStream(zIn);
		mValue = MiniNumber.ReadFromStream(zIn);
		
		mProofChain = new ArrayList<>();
		MiniNumber mlen = MiniNumber.ReadFromStream(zIn);
		int len = mlen.getAsInt();
		for(int i=0;i<len;i++) {
			ProofChunk chunk = new ProofChunk();
			chunk.readDataStream(zIn);
			mProofChain.add(chunk);
		}
		
		finalizeHash();
	}
	
	public static Proof ReadFromStream(DataInputStream zIn) throws IOException{
		Proof proof = new Proof();
		proof.readDataStream(zIn);
		return proof;
	}
	
	public static int getChainSHABits(String zChainSHA) throws Exception {
		
		//Get the first 4 digits..
		String bits = zChainSHA.substring(0, 4);
		
		//Convert to Decimal.
		int dec = BaseConverter.hexToNumber(bits);
		
		if(dec<5 || dec>16) {
			//ERROR
			throw new Exception("Invalid ChainSHA.. must be 160, 192, 224, 256, 288, 320, 384, 416, 448, 480 or 512");	
		}
		
		//And multiply by 32..
		return dec * 32;
	}
}
