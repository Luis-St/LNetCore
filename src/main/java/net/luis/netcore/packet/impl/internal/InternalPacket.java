package net.luis.netcore.packet.impl.internal;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public abstract sealed class InternalPacket extends Packet permits CloseConnectionPacket, CloseServerPacket, InitializeConnectionPacket, SyncServerDataPacket {
	
	InternalPacket() {
	
	}
	
	InternalPacket(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
	
	}
}
