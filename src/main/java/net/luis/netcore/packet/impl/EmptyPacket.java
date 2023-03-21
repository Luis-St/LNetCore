package net.luis.netcore.packet.impl;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class EmptyPacket implements Packet {
	
	public EmptyPacket() {
	
	}
	
	public EmptyPacket(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
	
	}
}