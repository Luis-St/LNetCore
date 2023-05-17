package net.luis.netcore.packet.listener;

import net.luis.netcore.network.connection.Connection;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface PacketListener {
	
	void initialize(Connection connection);
}
