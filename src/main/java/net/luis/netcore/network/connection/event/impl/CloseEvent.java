package net.luis.netcore.network.connection.event.impl;

/**
 *
 * @author Luis-St
 *
 */

public record CloseEvent() implements ConnectionEvent {
	
	//region Object overrides
	@Override
	public String toString() {
		return "CloseEvent";
	}
	//endregion
}

