package org.minima.system.network.p2p;

import org.minima.system.Main;
import org.minima.system.network.NetworkHandler;
import org.minima.system.network.base.MinimaClient;
import org.minima.utils.MinimaLogger;
import org.minima.utils.messages.Message;
import org.minima.utils.messages.MessageProcessor;
import org.minima.utils.messages.TimerMessage;

public class P2PManager extends MessageProcessor {
	
	public static final String P2P_INIT 		= "P2P_INIT";
	public static final String P2P_SHUTDOWN 	= "P2P_SHUTDOWN";

	public static final String P2P_REFRESHPEERS = "P2P_REFRESHPEERS";
	public static final String P2P_PEERSLIST 	= "P2P_PEERSLIST";

	public static final String P2P_DEFAULTCONNECT = "P2P_DEFAULTCONNECT";
	
	/**
	 * Seed Nodes
	 */
//	public static final String[] VALID_BOOTSTRAP_NODES = 
//		{"35.204.181.120",
//		 "35.204.119.15",
//		 "34.91.220.49",
//		 "35.204.62.177",
//		 "35.204.139.141",
//		 "35.204.194.45"};

	public static final String[] SEED_PEERS = {"127.0.0.1"};
	
	/**
	* Seed Peers are hard coded or added manually from the command line
	*/
	PeerList mSeedPeers;
	
	/**
	* This list is generated from other peers
	*/
	PeerList mValidPeers;
	
	/**
	* This list is generated from other peers
	*/
	PeerList mRecievedPeers;
	
	
	public P2PManager() {
		super("P2P_DISCOVERY");
		
		mLogON = true;
		
		mRecievedPeers = new PeerList();
		
		PostMessage(P2P_INIT);
		
		//Timer message to refresh the peers to those you are connected
		PostTimerMessage(new TimerMessage(30000, P2P_REFRESHPEERS));
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
	
	public boolean isSeedPeer(String zHost, int zPort) {
		return mSeedPeers.getPeer(zHost, zPort) != null;
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(P2P_INIT)) {
			//Default list..
			mSeedPeers = new PeerList();
			for(String host : SEED_PEERS) {
				mSeedPeers.addPeer(new Peer(host, 9001));
			}
			
			//Dynamic List
			mValidPeers = new PeerList();
			
			//Load the current Dynamic List of peers..
			
		
		}else if(zMessage.getMessageType().equals(P2P_DEFAULTCONNECT)) {
			//Get the Network Handler
			NetworkHandler netw = Main.getMainHandler().getNetworkHandler();
			
			//Connect normally to a seed peer..
			Peer seed = getSeedPeer();

			//Now connect
			Message connect  = new Message(NetworkHandler.NETWORK_CONNECT)
					.addInteger("port", seed.getPort())
					.addString("host", seed.getHost());
			netw.PostMessage(connect);
		
		}else if(zMessage.getMessageType().equals(P2P_REFRESHPEERS)) {
			//Send out peer list to out connected peers - if it has changed..
			Message netmsg  = new Message(MinimaClient.NETCLIENT_PEERS)
					.addObject("peers", mSeedPeers);
			Message netw    = new Message(NetworkHandler.NETWORK_SENDALL).addObject("message", netmsg);
			Main.getMainHandler().getNetworkHandler().PostMessage(netw);
			
			//Do this every 4 hours..
			PostTimerMessage(new TimerMessage(60 * 1000 * 1, P2P_REFRESHPEERS));
			
		}else if(zMessage.getMessageType().equals(P2P_PEERSLIST)) {
			PeerList peerlist = (PeerList)zMessage.getObject("peers");
			
			//Add to our Recieved
			mRecievedPeers.mergePeerList(peerlist);
			
			MinimaLogger.log("REC PEERS : "+mRecievedPeers);
			
		}else if(zMessage.getMessageType().equals(P2P_SHUTDOWN)) {
			
			//Save the current list of dynamic peers
			
			
			
			stopMessageProcessor();
		}
		
	}

}
