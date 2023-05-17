package net.luis.netcore.network;

import net.luis.netcore.packet.Packet;

/**
 *
 * @author Luis-St
 *
 */

public interface NetworkInstance {
	
	default void open(int port) {
		this.open("localhost", port);
	}
	
	void open(String host, int port);
	
	boolean isOpen();
	
	void send(Packet packet);
	
	void closeNow();
}
