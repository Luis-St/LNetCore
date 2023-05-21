package net.luis.netcore.network.connection;

import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

record ConnectionListener(Class<? extends Packet> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<Packet, Consumer<Packet>> listener) {
	
	public ConnectionListener {
		Objects.requireNonNull(packetClass, "Packet class must not be null");
		Objects.requireNonNull(target, "Packet target must not be null");
		Objects.requireNonNull(priority, "Packet priority must not be null");
		Objects.requireNonNull(listener, "ConnectionListener must not be null");
	}
	
	public boolean shouldCall(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (!this.packetClass.isAssignableFrom(packet.getClass())) {
			return false;
		}
		return this.target.isAny() || this.target.equals(packet.getTarget());
	}
	
	public void call(Packet packet, Consumer<Packet> sender) {
		Objects.requireNonNull(packet, "Packet must not be null");
		Objects.requireNonNull(sender, "Sender must not be null");
		this.listener.accept(packet, sender);
	}
}
