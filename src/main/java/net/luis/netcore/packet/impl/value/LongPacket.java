package net.luis.netcore.packet.impl.value;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.wrapper.PacketGetter;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class LongPacket extends Packet {
	
	private final long value;
	
	public LongPacket(long value) {
		this.value = value;
	}
	
	public LongPacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readLong();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeLong(this.value);
	}
	
	@PacketGetter("value")
	public long get() {
		return this.value;
	}
}
