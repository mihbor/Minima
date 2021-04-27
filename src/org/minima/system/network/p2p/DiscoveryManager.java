package org.minima.system.network.p2p;

import org.minima.utils.messages.Message;
import org.minima.utils.messages.MessageProcessor;

public class DiscoveryManager extends MessageProcessor {
	
	public static final String P2P_INIT 		= "P2P_INIT";
	public static final String P2P_SHUTDOWN 	= "P2P_SHUTDOWN";

	public static final String[] VALID_BOOTSTRAP_NODES = 
		{"35.204.181.120",
		 "35.204.119.15",
		 "34.91.220.49",
		 "35.204.62.177",
		 "35.204.139.141",
		 "35.204.194.45"};

	/**
	* Seed Peers are hard coded or added manually from the command line
	*/
	PeerList mSeedPeers;
	
	/**
	* This list is generated from other peers
	*/
	PeerList mDynamicPeers;
	
	public DiscoveryManager() {
		super("P2P_DISCOVERY");
		
		PostMessage(P2P_INIT);
	}
	
	public void shutdown() {
		PostMessage(P2P_SHUTDOWN);
	}
	
	public void clearSeeds() {
		mSeedPeers.clear();
	}
	
	public Peer getSeedPeer() {
		return mSeedPeers.getRandomPeer();
	}
	
	public void addSeedPeer(String zHost, int zPort) {
		Peer pp = new Peer(zHost, zPort);
		mSeedPeers.addPeer(pp);
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(P2P_INIT)) {
			//Default list..
			mSeedPeers = new PeerList();
			for(String host : VALID_BOOTSTRAP_NODES) {
				mSeedPeers.addPeer(new Peer(host, 9001));
			}
			
			//Dynamic List
			mDynamicPeers = new PeerList();
			
			//Load the current Dynamic List of peers..
			
			
		}else if(zMessage.getMessageType().equals(P2P_SHUTDOWN)) {
			
			//Save the current list of dynamic peers
			
			
			
			stopMessageProcessor();
		}
		
	}

}
