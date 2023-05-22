package net.luis.netcore.network.connection.event.impl;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class ReceiveEvent implements ConnectionEvent {
	
	private final Packet packet;
	private boolean handled = false;
	
	public ReceiveEvent(Packet packet, boolean handled) {
		this.packet = Objects.requireNonNull(packet, "Packet must not be null");
		this.handled = handled;
	}
	
	public @NotNull Packet getPacket() {
		return this.packet;
	}
	
	public boolean isHandled() {
		return this.handled;
	}
	
	public void setHandled(boolean handled) {
		this.handled = handled;
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "ReceiveEvent";
	}
	//endregion
}
