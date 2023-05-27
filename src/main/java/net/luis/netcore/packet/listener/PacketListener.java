package net.luis.netcore.packet.listener;

import net.luis.netcore.connection.ConnectionRegistry;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface PacketListener {
	
	void initialize(ConnectionRegistry registry);
}
