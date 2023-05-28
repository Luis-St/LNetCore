package net.luis.netcore.packet.impl.message;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class InfoPacket extends Packet {
	
	private final String message;
	
	public InfoPacket(String message) {
		this.message = message;
	}
	
	public InfoPacket(@NotNull FriendlyByteBuffer buffer) {
		this.message = buffer.readString();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeString(this.message);
	}
	
	public @NotNull String getMessage() {
		return this.message;
	}
}
