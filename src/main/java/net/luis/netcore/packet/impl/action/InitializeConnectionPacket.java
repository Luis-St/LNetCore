package net.luis.netcore.packet.impl.action;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import net.luis.utils.annotation.Internal;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@Internal
public class InitializeConnectionPacket extends Packet {
	
	public InitializeConnectionPacket() {
	
	}
	
	public InitializeConnectionPacket(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
	
	}
}
