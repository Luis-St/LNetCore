package net.luis.netcore.packet.impl.action;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ResponsePacket extends Packet implements Supplier<Object> {
	
	private final String message;
	private final Object response;
	
	public ResponsePacket(String message, Object response) {
		this.message = message;
		this.response = response;
	}
	
	public ResponsePacket(@NotNull FriendlyByteBuffer buffer) {
		this.message = buffer.readString();
		this.response = buffer.readUnsafe();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeString(this.message);
		buffer.writeUnsafe(this.response);
	}
	
	public @NotNull String getMessage() {
		return this.message;
	}
	
	@Override
	public @NotNull Object get() {
		return this.response;
	}
	
	@SuppressWarnings("unchecked")
	public <X> X getAs() {
		return (X) this.response;
	}
}
