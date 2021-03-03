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
		MiniData    mHash;
		MiniNumber  mValue;
		MiniByte    mLeftRight;
		MiniNumber  mRow;
		MiniNumber  mEntry;
		
		public ProofChunk() {}
		
		public ProofChunk(MiniByte zLeft, MiniData zHash, MiniNumber zValue, MiniNumber zRow, MiniNumber zEntry) {
			mLeftRight 	= zLeft;
			mHash 		= zHash;
			mValue 		= zValue;
			mRow 		= zRow;
			mEntry 		= zEntry;
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
		
		public MiniNumber getRow() {
			return mRow;
		}
		
		public MiniNumber getEntry() {
			return mEntry;
		}
		
		public JSONObject toJSON() {
			JSONObject json = new JSONObject();
			
			json.put("left", getLeft().isTrue());
			json.put("hash", getHash().to0xString());
			json.put("value", getValue().toString());
			json.put("row", getRow().toString());
			json.put("entry", getEntry().toString());
			
			return json;
		}
		
		@Override
		public void writeDataStream(DataOutputStream zOut) throws IOException {
			mLeftRight.writeDataStream(zOut);
			mHash.writeDataStream(zOut);
			mValue.writeDataStream(zOut);
			mRow.writeDataStream(zOut);
			mEntry.writeDataStream(zOut);
		}

		@Override
		public void readDataStream(DataInputStream zIn) throws IOException {
			mLeftRight 	= MiniByte.ReadFromStream(zIn);
			mHash      	= MiniData.ReadFromStream(zIn);
			mValue     	= MiniNumber.ReadFromStream(zIn);
			mRow		= MiniNumber.ReadFromStream(zIn);
			mEntry		= MiniNumber.ReadFromStream(zIn);
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
		addProofChunk(zLeft, zHash, MiniNumber.ZERO, 0, MiniNumber.ZERO);
	}
	
	public void addProofChunk(MiniByte zLeft, MiniData zHash, MiniNumber zValue, int zRow, MiniNumber zEntry) {
		addProofChunk(new ProofChunk(zLeft, zHash, zValue, new MiniNumber(zRow), zEntry));
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
		MiniData currentdata = mData;
		MiniNumber value = mValue;
		
		//And LEFT ENtry
		MMREntry currententry = null;
		
		int len = getProofLen();
		for(int i=0;i<len;i++) {
			ProofChunk chunk = mProofChain.get(i);
			
			//First Chunk sets the orientation..
			if(i==0) {
				MMREntry initialchunk = new MMREntry(chunk.getRow().getAsInt(), chunk.getEntry());
				
				if(initialchunk.getRow() == 0) {
					currententry = new MMREntry(0, initialchunk.getSibling());
				}else {
					//Tree size//
					MiniNumber treespan = new MiniNumber(2).pow(initialchunk.getRow());
					currententry = new MMREntry(0, treespan);
				}
				
				System.out.println("Initial Pos :"+currententry.getEntryNumber());
			}
			
			//What is the SUM
			value = value.add(chunk.getValue());
			
			if(chunk.getLeft().isTrue()) {
				
				currentdata = Crypto.getInstance().hashAllObjects(HASH_BITS, 
						chunk.getHash(), 
						currentdata, 
						value,
						chunk.getRow(),chunk.getEntry(),
						new MiniNumber(currententry.getRow()),currententry.getEntryNumber());

				//What is the position
				MiniNumber leftparentrow    = chunk.getRow().increment();
				MiniNumber leftparententry 	= chunk.getEntry().divRoundDownWhole(MiniNumber.TWO);

				currententry = new MMREntry(leftparentrow.getAsInt(), leftparententry);
				
//				System.out.println("HASH ["+chunk.getHash()+"|"+current+"]");
				
//				current = Crypto.getInstance().hashAllObjects(HASH_BITS, 
//						chunk.getHash(), 
//						current);
				
//				System.out.println("LEFT "+parentrow+" "+parententry);
				
			}else {
				
				currentdata = Crypto.getInstance().hashAllObjects(HASH_BITS, 
						currentdata, 
						chunk.getHash(), 
						value,
						new MiniNumber(currententry.getRow()),currententry.getEntryNumber(),
						chunk.getRow(),chunk.getEntry());

				//What is the position
				MiniNumber leftparentrow    = new MiniNumber(currententry.getRow()+1);
				MiniNumber leftparententry 	= currententry.getEntryNumber().divRoundDownWhole(MiniNumber.TWO);

				currententry = new MMREntry(leftparentrow.getAsInt(), leftparententry);

//				System.out.println("HASH ["+current+"|"+chunk.getHash()+"]");
				
//				current = Crypto.getInstance().hashAllObjects(HASH_BITS, 
//						current, 
//						chunk.getHash());
				
//				System.out.println("="+current);
//				System.out.println("RIGHT "+parentrow+" "+parententry);
				
			}
			
			
		}
		
		return currentdata;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		JSONArray proof = new JSONArray();
		int len = mProofChain.size();
		for(int i=0;i<len;i++){
			proof.add(mProofChain.get(i).toJSON());
		}
		
		json.put("data", mData.to0xString());
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
