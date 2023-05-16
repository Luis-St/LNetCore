package net.luis.netcore.network;

import net.luis.netcore.packet.Packet;

/**
 *
 * @author Luis-St
 *
 */

public interface NetworkInstance {
	
	NetworkInstance handshake(Packet packet);
	
	void open();
	
	boolean isOpen();
	
	void send(Packet packet);
	
	void closeNow();
}
