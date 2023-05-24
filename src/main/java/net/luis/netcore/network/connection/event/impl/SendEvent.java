package net.luis.netcore.network.connection.event.impl;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public class SendEvent implements ConnectionEvent {
	
	private final UUID uniqueId;
	private final Packet packet;
	private boolean cancelled = false;
	
	public SendEvent(UUID uniqueId, Packet packet) {
		this.uniqueId = Objects.requireNonNull(uniqueId, "UniqueId must not be null");
		this.packet = Objects.requireNonNull(packet, "Packet must not be null");
		this.cancelled = false;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public @NotNull Packet getPacket() {
		return this.packet;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	public void cancel() {
		this.cancelled = true;
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "SendEvent";
	}
	//endregion
}
