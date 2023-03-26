package net.luis.netcore.exception;

/**
 *
 * @author Luis-St
 *
 */

public class InvalidPacketException extends RuntimeException {
	
	public InvalidPacketException() {
		super();
	}
	
	public InvalidPacketException(String message) {
		super(message);
	}
	
	public InvalidPacketException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
