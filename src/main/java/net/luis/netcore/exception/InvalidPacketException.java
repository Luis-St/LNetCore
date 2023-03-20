package net.luis.netcore.exception;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class InvalidPacketException extends RuntimeException {
	
	public InvalidPacketException() {
		super();
	}
	
	public InvalidPacketException(@NotNull String message) {
		super(message);
	}
	
	public InvalidPacketException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}
	
}
