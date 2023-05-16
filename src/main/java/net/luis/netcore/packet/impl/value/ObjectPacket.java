package net.luis.netcore.packet.impl.value;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ObjectPacket extends Packet implements Supplier<Object> {
	
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
	
	@Override
	public Object get() {
		return this.value;
	}
	
	@SuppressWarnings("unchecked")
	public <X> X getAs() {
		return (X) this.value;
	}
}
