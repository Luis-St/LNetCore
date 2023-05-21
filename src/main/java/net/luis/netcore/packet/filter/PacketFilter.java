package net.luis.netcore.packet.filter;

import net.luis.netcore.packet.Packet;

/**
 *
 * @author Luis-St
 *
 */

public interface PacketFilter {
	
	boolean filter(Packet packet);
}
