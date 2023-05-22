package net.luis.netcore.network.connection.event.impl;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class SendEvent implements ConnectionEvent {
	
	private final Packet packet;
	private boolean cancelled = false;
	
	public SendEvent(Packet packet) {
		this.packet = Objects.requireNonNull(packet, "Packet must not be null");
		this.cancelled = false;
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
