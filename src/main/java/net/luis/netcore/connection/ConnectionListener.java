package net.luis.netcore.connection;

import net.luis.netcore.connection.util.ConnectionContext;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 *
 * @author Luis-St
 *
 */

record ConnectionListener(UUID uniqueId, Class<? extends Packet> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<Packet, ConnectionContext> listener) {
	
	public ConnectionListener {
		Objects.requireNonNull(uniqueId, "Unique id must not be null");
		Objects.requireNonNull(packetClass, "Packet class must not be null");
		Objects.requireNonNull(target, "Packet target must not be null");
		Objects.requireNonNull(priority, "Packet priority must not be null");
		Objects.requireNonNull(listener, "Listener must not be null");
	}
	
	public boolean shouldCall(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (!this.packetClass.isAssignableFrom(packet.getClass())) {
			return false;
		}
		return this.target.isAny() || this.target.equals(packet.getTarget());
	}
	
	public void call(Packet packet, ConnectionContext ctx) {
		Objects.requireNonNull(packet, "Packet must not be null");
		Objects.requireNonNull(ctx, "Connection context must not be null");
		this.listener.accept(packet, ctx);
	}
}
