package net.luis.netcore.packet;

/**
 *
 * @author Luis-St
 *
 */

public interface PacketWrapper<T> {
	
	T wrap(Packet packet);
}
