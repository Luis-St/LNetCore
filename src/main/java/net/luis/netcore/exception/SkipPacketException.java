package net.luis.netcore.exception;

import io.netty.handler.codec.EncoderException;

/**
 *
 * @author Luis-St
 *
 */

public class SkipPacketException extends EncoderException {
	
	public SkipPacketException() {
	
	}
	
	public SkipPacketException(String message) {
		super(message);
	}
	
	public SkipPacketException(Throwable cause) {
		super(cause);
	}
	
	public SkipPacketException(String message, Throwable cause) {
		super(message, cause);
	}
}
