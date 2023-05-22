package net.luis.netcore.network.connection.event;

import net.luis.netcore.network.connection.event.impl.*;
import net.luis.utils.event.EventType;

/**
 *
 * @author Luis
 *
 */

public interface ConnectionEventType {

	EventType<OpenEvent> OPEN = new EventType<>("open");
	EventType<HandshakeEvent> HANDSHAKE = new EventType<>("handshake");
	EventType<SendEvent> SEND = new EventType<>("send");
	EventType<ReceiveEvent> RECEIVE = new EventType<>("receive");
	EventType<ExceptionEvent> EXCEPTION = new EventType<>("exception");
	EventType<CloseEvent> CLOSE = new EventType<>("close");
}
