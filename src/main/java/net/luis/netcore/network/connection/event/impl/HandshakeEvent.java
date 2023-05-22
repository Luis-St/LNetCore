package net.luis.netcore.network.connection.event.impl;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class HandshakeEvent implements ConnectionEvent {
	
	private final Packet originalPacket;
	private Packet packet;
	
	public HandshakeEvent(Packet packet) {
		this.originalPacket = Objects.requireNonNull(packet, "Packet must not be null");
		this.packet = this.originalPacket;
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
