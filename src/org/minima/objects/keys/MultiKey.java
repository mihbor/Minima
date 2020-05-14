package org.minima.objects.keys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.minima.database.mmr.MMRSet;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniInteger;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.BaseConverter;
import org.minima.utils.Crypto;

public class MultiKey extends BaseKey {
	
	//The Leaf Node Keys..
	SingleKey[] mSingleKeys;
	
	//The Current Leaf Node being used..
	int mCurrentLeaf = -1;
	
	//The Current MultiKey for the leaf node..
	MultiKey mCurrentChildTree = null;
	
	//The signature of the Root of the current base
	MiniData mCurrentPublicKey = null;
	MiniData mCurrentSignature = null;
	MiniData mCurrentProof     = null;
	
	//The MMR tree of keys
	MMRSet mMMR;
	
	/**
	 * For verification only can start like this..
	 */
	public MultiKey() {}

	public MultiKey(MiniData zPrivateSeed, MiniNumber zKeysPerLevel, MiniNumber zLevel) {
		super();
		mMaxUses  = zKeysPerLevel;
		mLevel    = zLevel;
		mUses     = MiniNumber.ZERO;
		initKeys(zPrivateSeed);
	}
	
	private void initKeys(MiniData zPrivateSeed) {
		//Can calculate from the Private Seed
		mBitLength = new MiniNumber(zPrivateSeed.getLength()*8);
		
		//Store it
		mPrivateSeed = zPrivateSeed;
		
		//Create the Key Tree
		mSingleKeys = new SingleKey[mMaxUses.getAsInt()];
		
		//Now create the MMR tree
		mMMR = new MMRSet(mBitLength.getAsInt());
				
		//Create all the keys..
		int len = mMaxUses.getAsInt();
		for(int i=0;i<len;i++) {
			//Create the Key
			mSingleKeys[i] = new SingleKey(mBitLength.getAsInt());	
			
			//Add to the tree
			mMMR.addLeafNode(mSingleKeys[i].getPublicKey());
		}
		
		//Finalise the set
		mMMR.finalizeSet();
		
		//Get the root of the tree..
		mPublicKey = mMMR.getMMRRoot().getFinalHash();
	}
	
	@Override
	public MiniNumber getTotalAllowedUses() {
		return mMaxUses.pow(mLevel.getAsInt());
	}
	
	@Override
	public MiniData sign(MiniData zData) {
		//Which key are we on..
		int keynum = getUses().getAsInt();
		
		//Once used you cannot use it again..
		incrementUses();
		
		//How many signatures per leaf..
		int perleaf = mMaxUses.pow(mLevel.decrement().getAsInt()).getAsInt();
		
		//Which leaf node are we using..
		int leafnode = (int)(keynum / perleaf);
		
//		System.out.println("LEVEL:"+mLevels+" KEY:"+keynum+" LEAF:"+leafnode);
		
		if(leafnode>=getMaxUses().getAsInt()) {
			System.out.println("ERROR Key "+getPublicKey().to0xString()
					+" used too many times! MAX LEAF NODES:"+getMaxUses()+" ALLOWED:"+getTotalAllowedUses());
			//Create an INVALID multi sig..
			MiniData zero = new MiniData("0x0000");
			MultiSig sig  = new MultiSig(zero,zero, zero);
			return sig.getCompleteSig();
		}
		
		//Are we top level
		if(getLevel().isEqual(MiniNumber.ONE)) {
			//Sign the data with this key
			signWithLeafNode(zData, leafnode);
			
			//Create a multi sig.. no child signature
			MultiSig sig = new MultiSig(mCurrentPublicKey, mCurrentProof, mCurrentSignature);
			
			//Return this..
			return sig.getCompleteSig();
		}
		
		//Are we on the same leaf or a new one..
		if(leafnode != mCurrentLeaf) {
			//Store
			mCurrentLeaf = leafnode;
			
			//Create a private seed based of this see..
			MiniData leafnumdata = new MiniData(BaseConverter.numberToHex(leafnode));
			
			//Now create the private seed..
			MiniData leafpriv = new MiniData( Crypto.getInstance().hashData(mPrivateSeed.concat(leafnumdata).getData(), 160) );
			
			//Create a new Multi Key at this leaf position..
			mCurrentChildTree = new MultiKey(leafpriv, getMaxUses(), getLevel().decrement()); 
			
			//Get the Base..
			MiniData rootkey = mCurrentChildTree.getPublicKey();
			
			//Sign that..
			signWithLeafNode(rootkey, mCurrentLeaf);
		}	
		
		//Use the current base 
		MiniData childsignature = mCurrentChildTree.sign(zData);
		
		//Create a multi sig..
		MultiSig sig = new MultiSig(mCurrentPublicKey, mCurrentProof, mCurrentSignature, childsignature);
		
		//Return this..
		return sig.getCompleteSig();
	}
	
	private void signWithLeafNode(MiniData zData, int zLeaf) {
		//Get that Key..
		mCurrentPublicKey  = mSingleKeys[zLeaf].getPublicKey();
		mCurrentSignature  = mSingleKeys[zLeaf].sign(zData);
		mCurrentProof      = mMMR.getFullProofToRoot(new MiniInteger(zLeaf)).getChainSHAProof();
	}

	@Override
	public boolean verify(MiniData zData, MiniData zMultiSignature) {
		//Convert into a MultiSig Structure
		MultiSig sigdata = new MultiSig(zMultiSignature);
		
		//First check the Public Key is correct
		if(!sigdata.getRootKey().isEqual(mPublicKey)) {
			return false;
		}
		
		//Create a Single Key
		SingleKey skey = new SingleKey();
		skey.setPublicKey(sigdata.getPublicKey());
		
		//And now check the children
		if(sigdata.hasChildSignature()) {
			//The Child Signature
			MiniData childsig = sigdata.getChildSignature();
			
			//Convert to a multisig
			MultiSig msig = new MultiSig(childsig);
			
			//Now check the Signature was used to sign the root of the child tree
			if(!skey.verify(msig.getRootKey(), sigdata.getSignature())) {
				return false;
			}

			//Create a new MultiKey for the child tree
			MultiKey child = new MultiKey();
			child.setPublicKey(msig.getRootKey());
			
			//Now check the child signed this data
			return child.verify(zData, childsig);
		}
		
		//Just check the data was signed 
		return skey.verify(zData, sigdata.getSignature());
	}
	
	
	public static void main(String[] zargs) {		
		
		//get some data
		MiniData privseed = MiniData.getRandomData(20);
				
		//Create a new key
		System.out.println("MAKE KEY Start");
		MultiKey mkey = new MultiKey(privseed, new MiniNumber("32"), new MiniNumber("2"));
		System.out.println(mkey.toJSON().toString());
		
		//get some data
		MiniData data = MiniData.getRandomData(20);
		System.out.println("Data    : "+data);
		System.out.println();
		
		//Sign it..
		for(int i=0;i<2;i++) {
			MiniData sig = mkey.sign(data);
			System.out.println(i+")\tSigLength:"
					+sig.getLength()+"\thash:"
					+Crypto.getInstance().hashObject(sig,160).to0xString()
					+"\tVerify  : "+mkey.verify(data, sig));
			System.out.println(mkey.toJSON().toString());
		}
		
		System.out.println();
		System.out.println("Now read it in..");
		
		//Now write the key our..
		MultiKey lodkey = new MultiKey(); 
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			
			mkey.writeDataStream(dos);
			
			byte[] mdata = baos.toByteArray();
			
			dos.close();
			
			//Now read it back in..
			ByteArrayInputStream bais = new ByteArrayInputStream(mdata);
			DataInputStream dis = new DataInputStream(bais);
			
			lodkey.readDataStream(dis);
			
			dis.close();
			
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		
		System.out.println(lodkey.toJSON().toString());
		
		for(int i=0;i<3;i++) {
			MiniData sig = mkey.sign(data);
			System.out.println(i+")\tSigLength:"
					+sig.getLength()+"\thash:"
					+Crypto.getInstance().hashObject(sig,160).to0xString()
					+"\tVerify  : "+mkey.verify(data, sig));
			System.out.println(mkey.toJSON().toString());
		}
		
	}

	
}