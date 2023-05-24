package net.luis.netcore.network.instance;

import net.luis.netcore.packet.Packet;
import net.luis.utils.event.Event;

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
