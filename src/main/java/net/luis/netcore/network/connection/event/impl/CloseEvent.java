package net.luis.netcore.network.connection.event.impl;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public record CloseEvent(UUID getUniqueId) implements ConnectionEvent {
	
	//region Object overrides
	@Override
	public String toString() {
		return "CloseEvent";
	}
	//endregion
}

