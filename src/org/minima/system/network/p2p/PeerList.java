package org.minima.system.network.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.minima.objects.base.MiniNumber;
import org.minima.utils.Streamable;

public class PeerList implements Streamable {

	ArrayList<Peer> mPeers;
	
	public PeerList() {
		mPeers = new ArrayList<Peer>();
	}
	
	public Peer getRandomPeer() {
		int rand = new Random().nextInt(size());
		return mPeers.get(rand);
	}
	
	public int size() {
		return mPeers.size();
	}
	
	public void clear() {
		mPeers.clear();
	}
	
	public void addPeer(Peer zPeer) {
		mPeers.add(zPeer);
	}

	public Peer getPeer(String zHost, int zPort) {
		for(Peer peer : mPeers) {
			if(peer.getHost().equals(zHost) && peer.getPort()==zPort) {
				return peer;
			}
		}
		
		return null;
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
		mPeers = new ArrayList<>();
		int len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			Peer pp = Peer.ReadFromStream(zIn);
			mPeers.add(pp);
		}
	}
	
}
