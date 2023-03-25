package net.luis.netcore.packet.impl;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ObjectPacket extends Packet {
	
	private final Object value;
	
	public ObjectPacket(Object value) {
		this.value = value;
	}
	
	public ObjectPacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readUnsafe();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeUnsafe(this.value);
	}
	
	public Object get() {
		return this.value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAs() {
		return (T) this.value;
	}
	
}
