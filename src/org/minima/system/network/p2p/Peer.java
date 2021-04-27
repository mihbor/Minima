package org.minima.system.network.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.minima.objects.base.MiniByte;
import org.minima.objects.base.MiniNumber;
import org.minima.objects.base.MiniString;
import org.minima.utils.Streamable;
import org.minima.utils.json.JSONObject;

public class Peer implements Comparable<Peer>, Streamable {

	/**
	 * Peer details
	 */
	public String 	mHost 		 = "";
	public int 		mPort 		 = -1;
	
	/**
	 * The Peer Info you receive once connected
	 */
	public boolean  mPeerInfoSet = false;
	public String 	mVersion 	 = "";
	public long 	mLastComms 	 = -1;
	public boolean 	mExternal	 = false;
	public boolean 	mInBound  	 = false;
	public boolean  mArchive 	 = false;
	
	private Peer() {}
	
	public Peer(String zHost, int zPort) {
		mHost = zHost;
		mPort = zPort;
	}
	
	public String getHost() {
		return mHost;
	}
	
	public int getPort() {
		return mPort;
	}

	public boolean isInbound() {
		return mInBound;
	}
	
	public long getLastComms() {
		return mLastComms;
	}
	
	public void setLastComms() {
		mLastComms = System.currentTimeMillis();
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("host", mHost);
		json.put("port", ""+mPort);
		
		json.put("version", mVersion);
		json.put("lastcomms", ""+mLastComms);
		json.put("inbound", mInBound);
		json.put("external", mExternal);
		json.put("archive", mArchive);
		
		return json;
	}
	
	public boolean isEqual(Peer zPeer) {
		return mHost==zPeer.getHost() && mPort==zPeer.getPort();
	}
	
	@Override
	public int compareTo(Peer zPeer) {
		if(mLastComms < zPeer.getLastComms()) {
			return -1;
		}else if(mLastComms < zPeer.getLastComms()) {
			return 1;
		}
		
		return 0;
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		new MiniString(mVersion).writeDataStream(zOut);
		new MiniString(mHost).writeDataStream(zOut);
		new MiniNumber(mPort).writeDataStream(zOut);
		new MiniNumber(mLastComms).writeDataStream(zOut);
		new MiniByte(mInBound).writeDataStream(zOut);
		new MiniByte(mExternal).writeDataStream(zOut);
		new MiniByte(mArchive).writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mVersion 	= MiniString.ReadFromStream(zIn).toString(); 
		mHost 		= MiniString.ReadFromStream(zIn).toString();
		mPort 		= MiniNumber.ReadFromStream(zIn).getAsInt();
		mLastComms 	= MiniNumber.ReadFromStream(zIn).getAsLong();
		mInBound 	= MiniByte.ReadFromStream(zIn).isTrue();
		mExternal 	= MiniByte.ReadFromStream(zIn).isTrue();
		mArchive 	= MiniByte.ReadFromStream(zIn).isTrue();
	}
	
	public static Peer ReadFromStream(DataInputStream zIn) throws IOException {
		Peer pp = new Peer();
		pp.readDataStream(zIn);
		return pp;
	}
}
