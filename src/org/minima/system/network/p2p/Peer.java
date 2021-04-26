package org.minima.system.network.p2p;

public class Peer implements Comparable<Peer>{

	public String 	mHost;
	public int 		mPort;
	
	public long 	mLastComms;
	
	public Peer(String zHost, int zPort) {
		mHost = zHost;
		mPort = zPort;
		
		setLastComms();
	}
	
	public String getHost() {
		return mHost;
	}
	
	public int getPort() {
		return mPort;
	}

	public long getLastComms() {
		return mLastComms;
	}
	
	public void setLastComms() {
		mLastComms = System.currentTimeMillis();
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
	
}
