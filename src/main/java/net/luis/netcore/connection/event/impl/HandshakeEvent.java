package net.luis.netcore.connection.event.impl;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public class HandshakeEvent implements ConnectionEvent {
	
	private final UUID uniqueId;
	private final Packet originalPacket;
	private Packet packet;
	
	public HandshakeEvent(UUID uniqueId, Packet packet) {
		this.uniqueId = Objects.requireNonNull(uniqueId, "UniqueId must not be null");
		this.originalPacket = Objects.requireNonNull(packet, "Packet must not be null");
		this.packet = this.originalPacket;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public @NotNull Packet getOriginalPacket() {
		return this.originalPacket;
	}
	
	public @NotNull Packet getPacket() {
		return this.packet;
	}
	
	public void setPacket(Packet packet) {
		this.packet = Objects.requireNonNull(packet, "Packet must not be null");
	}
	
	public boolean isCancelled() {
		return this.packet == null;
	}
	
	public void cancel() {
		this.packet = null;
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "HandshakeEvent";
	}
	//endregion
}
