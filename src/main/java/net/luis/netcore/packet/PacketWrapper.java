package net.luis.netcore.packet;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface PacketWrapper<T> {
	
	T wrap(Packet packet);
}
