package org.minima.system.network.p2p;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.minima.objects.base.MiniByte;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.objects.greet.Greeting;
import org.minima.system.Main;
import org.minima.system.brains.ConsensusHandler;
import org.minima.system.network.NetworkHandler;
import org.minima.system.network.base.MinimaReader;
import org.minima.utils.MinimaLogger;
import org.minima.utils.messages.Message;

/**
 * Check if a peer is valid..
 * 
 * @author spartacusrex
 *
 */
public class PeerChecker implements Runnable{

	Peer mPeer;
	
	public PeerChecker(Peer zPeer) {
		mPeer = zPeer;
	}
	
	public void start() {
		Thread runner = new Thread(this);
		runner.start();
	}
	
	@Override
	public void run() {
		//The Socket
		Socket socket = new Socket();
		
		//Start a connection to a peer..
		try {
			MinimaLogger.log("CHECK PEER "+mPeer);
			
			//Connect with timeout
			socket.connect(new InetSocketAddress(mPeer.getHost(), mPeer.getPort()), 60000);
		
			//Now you are connected 
			DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			
			//What message type
			MiniByte msgtype = MiniByte.ReadFromStream(input);
			
			if(!msgtype.isEqual(MinimaReader.NETMESSAGE_GREETING)) {
				throw new Exception("Invalid message at startup.. "+msgtype);
			}
			
			//What length..
			int len = MiniNumber.ReadFromStream(input).getAsInt();
			
			//What message
			MiniData fullmsg = MiniData.ReadFromStream(input, len);
			
			//Now convert 
			ByteArrayInputStream bais   = new ByteArrayInputStream(fullmsg.getData());
			DataInputStream inputstream = new DataInputStream(bais);
			
			//Must be
			Greeting greet = Greeting.ReadFromStream(inputstream);
			
			MinimaLogger.log("Peer Checker Greeting SUCCESS : "+greet.getVersion()); 
			
			//Clean up..
			inputstream.close();
			bais.close();
			
			
		}catch (Exception e) {
			MinimaLogger.log("Error checking peer "+mPeer+" "+e);
			
			//Return false to Peer Manager
			//..
			
		}	
		
		//Close the socket
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
