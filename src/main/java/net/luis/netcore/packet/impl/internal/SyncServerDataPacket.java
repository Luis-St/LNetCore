package net.luis.netcore.packet.impl.internal;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import net.luis.utils.annotation.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

@Internal
public class SyncServerDataPacket extends Packet {
	
	private final UUID uniqueId;
	
	public SyncServerDataPacket(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public SyncServerDataPacket(@NotNull FriendlyByteBuffer buffer) {
		this.uniqueId = buffer.readUniqueId();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeUniqueId(this.uniqueId);
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
}
