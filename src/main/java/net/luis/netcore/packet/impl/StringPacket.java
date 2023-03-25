package net.luis.netcore.packet.impl;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class StringPacket extends Packet {
	
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
	
	public String get() {
		return this.value;
	}
}
