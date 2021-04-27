package org.minima.system.network.p2p;

import org.minima.utils.messages.Message;
import org.minima.utils.messages.MessageProcessor;

public class DiscoveryManager extends MessageProcessor {

	public static final String P2P_INIT 		= "P2P_INIT";
	public static final String P2P_SHUTDOWN 	= "P2P_SHUTDOWN";
	
	
	public DiscoveryManager() {
		super("P2P_DISCOVERY");
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(P2P_INIT)) {
			//Load the current Dynamic List of peers..
			
			
		}else if(zMessage.getMessageType().equals(P2P_SHUTDOWN)) {
			//Save the current list of dynamic peers
			
			
			
		}
		
	}

}
