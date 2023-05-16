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
	
	public boolean get() {
		return this.value;
	}
}
