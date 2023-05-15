package net.luis.netcore.packet.listener;

import net.luis.netcore.network.connection.Connection;

/**
 *
 * @author Luis-St
 *
 */

public interface PacketListener {
	
	void initialize(Connection connection);
}
