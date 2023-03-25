package net.luis.netcore.packet.impl;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ErrorPacket extends Packet {
	
	private final String message;
	private final int errorCode;
	
	public ErrorPacket(String message, int errorCode) {
		this.message = message;
		this.errorCode = errorCode;
	}
	
	public ErrorPacket(@NotNull FriendlyByteBuffer buffer) {
		this.message = buffer.readString();
		this.errorCode = buffer.readInt();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeString(this.message);
		buffer.writeInt(this.errorCode);
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public int getErrorCode() {
		return this.errorCode;
	}
	
}
