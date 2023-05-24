package net.luis.netcore.connection.event.impl;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public record ExceptionEvent(UUID getUniqueId) implements ConnectionEvent {
	
	//region Object overrides
	@Override
	public String toString() {
		return "ExceptionEvent";
	}
	//endregion
}
