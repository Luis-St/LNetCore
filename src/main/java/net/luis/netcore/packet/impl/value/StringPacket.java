package net.luis.netcore.packet.impl.value;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.wrapper.PacketGetter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class StringPacket extends Packet implements Supplier<String> {
	
	private final String value;
	
	public StringPacket(@NotNull String value) {
		this.value = value;
	}
	
	public StringPacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readString();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeString(this.value);
	}
	
	@Override
	@PacketGetter("value")
	public String get() {
		return this.value;
	}
}
