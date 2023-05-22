package net.luis.netcore.network.connection.event;

/**
 *
 * @author Luis-St
 *
 */

public interface ConnectionEvent {
	

	
	
	
	
	
	OpenEvent OPEN = new OpenEvent();
	HandshakeEvent HANDSHAKE = new HandshakeEvent();
	SendEvent SEND = new SendEvent();
	ReceiveEvent RECEIVE = new ReceiveEvent();
	ExceptionEvent EXCEPTION = new ExceptionEvent();
	CloseEvent CLOSE = new CloseEvent();
}
