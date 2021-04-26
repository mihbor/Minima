package org.minima.system.network.p2p;

import java.util.ArrayList;

public class PeerList {

	ArrayList<Peer> mPeers;
	
	public PeerList() {
		mPeers = new ArrayList<Peer>();
	}
	
	public PeerList getRandomSample() {
		return new PeerList();
	}
	
	public int size() {
		return mPeers.size();
	}
	
	public void addPeer(Peer zPeer) {
		mPeers.add(zPeer);
	}

	public void updatePeer(Peer zPeer) {
		for(Peer peer : mPeers) {
			if(peer.isEqual(zPeer)) {
				peer.setLastComms();
				return;
			}
		}
		
		//We don't have it add to the list..
		addPeer(zPeer);
		zPeer.setLastComms();
	}
	
}
