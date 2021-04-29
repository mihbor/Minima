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
	
	public static final String P2P_PEERCHECK 	= "P2P_PEERCHECK";
	
	public static final String P2P_DEFAULTCONNECT = "P2P_DEFAULTCONNECT";
	
	/**
	 * Seed Nodes
	 */
//	public static final String[] SEED_PEERS = 
//		{"35.204.181.120",
//		 "35.204.119.15",
//		 "34.91.220.49",
//		 "35.204.62.177",
//		 "35.204.139.141",
//		 "35.204.194.45"};

//	public static final String[] SEED_PEERS = {"127.0.0.1"};
	public static final String[] SEED_PEERS = {};
	
	/**
	* This list is generated from other peers
	*/
	PeerList mValidPeers;
	
	/**
	* This list is generated from other peers
	*/
	PeerList mRecievedPeers;
	
	/**
	* This list is generated from other peers
	*/
	PeerList mCurrentPeers;
	
	
	public P2PManager() {
		super("P2P_DISCOVERY");
		
		mLogON = true;
		
		mRecievedPeers 	= new PeerList();
		mValidPeers    	= new PeerList();
		mCurrentPeers   = new PeerList();
		
		PostMessage(P2P_INIT);
		
		//Timer message to refresh the peers to those you are connected
		PostTimerMessage(new TimerMessage(30000, P2P_REFRESHPEERS));
	}
	
	public void shutdown() {
		PostMessage(P2P_SHUTDOWN);
	}
	
	/**
	 * When you connect to a peer it is valid..
	 */
	public void addValidPeer(Peer zPeer) {
		mValidPeers.addPeer(zPeer);
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(P2P_INIT)) {
			//Default list..
			for(String host : SEED_PEERS) {
				mValidPeers.addPeer(new Peer(host, 9001));
			}
			
		}else if(zMessage.getMessageType().equals(P2P_DEFAULTCONNECT)) {
			//Get the Network Handler
			NetworkHandler netw = Main.getMainHandler().getNetworkHandler();
			
			//Connect normally to a peer..
			Peer seed = mValidPeers.getRandomPeer();

			//Now connect
			Message connect  = new Message(NetworkHandler.NETWORK_CONNECT)
					.addInteger("port", seed.getPort())
					.addString("host", seed.getHost());
			netw.PostMessage(connect);
		
		}else if(zMessage.getMessageType().equals(P2P_REFRESHPEERS)) {
			//Get all valid outbound peers
			PeerList validout = new PeerList();
			for(Peer pp : mValidPeers.getAllPeers()) {
				if(!pp.isInbound()) {
					validout.addPeer(pp);
				}
			}
			
			//Send out peer list to out connected peers - if it has changed..
			Message netmsg  = new Message(MinimaClient.NETCLIENT_PEERS)
					.addObject("peers", validout);
			Message netw    = new Message(NetworkHandler.NETWORK_SENDALL).addObject("message", netmsg);
			Main.getMainHandler().getNetworkHandler().PostMessage(netw);
			
			//Do this every 4 hours..
			PostTimerMessage(new TimerMessage(60 * 1000 * 1, P2P_REFRESHPEERS));
			
		}else if(zMessage.getMessageType().equals(P2P_PEERSLIST)) {
			//This one is sent to us..
			PeerList peerlist = (PeerList)zMessage.getObject("peers");
			
			//These are unknown to us
			PeerList newlist  = new PeerList();
			
			//Check if we already have these peers..
			for(Peer pp : peerlist.getAllPeers()) {
				if(!mValidPeers.hasPeer(pp)) {
					newlist.addPeer(pp);
				}
			}
			MinimaLogger.log("NEW PEERS : "+newlist);
			
			//Add to our Received
//			mRecievedPeers.mergePeerList(newlist);
			
			//Check Peers..
			for(Peer pp : newlist.getAllPeers()) {
				CheckPeer check = new CheckPeer(pp);
				check.start();
			}
			
		}else if(zMessage.getMessageType().equals(P2P_PEERCHECK)) {
			MinimaLogger.log(zMessage.toString());
		
			if(zMessage.getBoolean("success")) {
				Peer peer = (Peer) zMessage.getObject("peer");
				
				//Add this peer to valid peers
				mValidPeers.addPeer(peer);
			}
			
		}else if(zMessage.getMessageType().equals(P2P_SHUTDOWN)) {
			
			//Save the current list of dynamic peers
			
			
			
			stopMessageProcessor();
		}
		
	}

}
