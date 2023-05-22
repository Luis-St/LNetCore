package net.luis.netcore.network.connection.event.impl;

/**
 *
 * @author Luis-St
 *
 */

public record ExceptionEvent() implements ConnectionEvent {
	
	//region Object overrides
	@Override
	public String toString() {
		return "ExceptionEvent";
	}
	//endregion
}
