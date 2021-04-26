package org.minima.system.network.p2p;

public class Connect {

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
	
	public Connect() {
		//Default list..
		mSeedPeers = new PeerList();
		for(String host : VALID_BOOTSTRAP_NODES) {
			mSeedPeers.addPeer(new Peer(host, 9001));
		}
		
		//Dynamic List
		mDynamicPeers = new PeerList();
	}
	
	/**
	 * When a new connection comes in
	 * @param zPeer
	 */
	public void newConnection(Peer zPeer) {
		//Do we have it already..
		mDynamicPeers.updatePeer(zPeer);
		
	}
	
	
}
