package net.luis.netcore.packet.util;

import net.luis.netcore.packet.Packet;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface PacketWrapper<T> {
	
	T wrap(Packet packet);
}
