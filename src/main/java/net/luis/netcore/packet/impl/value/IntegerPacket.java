package net.luis.netcore.packet.impl.value;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketGetter;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class IntegerPacket extends Packet {
	
	private final int value;
	
	public IntegerPacket(int value) {
		this.value = value;
	}
	
	public IntegerPacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readInt();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeInt(this.value);
	}
	
	@PacketGetter(parameterName = "value")
	public int get() {
		return this.value;
	}
}
