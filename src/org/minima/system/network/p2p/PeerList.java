package org.minima.system.network.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.minima.objects.base.MiniNumber;
import org.minima.utils.Streamable;

public class PeerList implements Streamable {

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

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		int len = mPeers.size();
		new MiniNumber(len).writeDataStream(zOut);
		for(Peer pp : mPeers) {
			pp.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		int len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			
		}
		
	}
	
}
