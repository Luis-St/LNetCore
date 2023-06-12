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

public class BooleanPacket extends Packet {
	
	private final boolean value;
	
	public BooleanPacket(boolean value) {
		this.value = value;
	}
	
	public BooleanPacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readBoolean();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeBoolean(this.value);
	}
	
	@PacketGetter("value")
	public boolean get() {
		return this.value;
	}
}
