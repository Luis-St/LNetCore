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

public class DoublePacket extends Packet {
	
	private final double value;
	
	public DoublePacket(double value) {
		this.value = value;
	}
	
	public DoublePacket(@NotNull FriendlyByteBuffer buffer) {
		this.value = buffer.readDouble();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeDouble(this.value);
	}
	
	@PacketGetter("value")
	public double get() {
		return this.value;
	}
}
