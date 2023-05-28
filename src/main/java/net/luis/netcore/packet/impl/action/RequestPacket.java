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

public class RequestPacket extends Packet implements Supplier<Object> {
	
	private final Object request;
	
	public RequestPacket(Object request) {
		this.request = request;
	}
	
	public RequestPacket(@NotNull FriendlyByteBuffer buffer) {
		this.request = buffer.readUnsafe();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeUnsafe(this.request);
	}
	
	@Override
	public Object get() {
		return this.request;
	}
	
	@SuppressWarnings("unchecked")
	public <X> X getAs() {
		return (X) this.request;
	}
}
