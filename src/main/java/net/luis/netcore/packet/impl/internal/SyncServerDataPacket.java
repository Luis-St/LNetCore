package net.luis.netcore.packet.impl.internal;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

@ApiStatus.Internal
public final class SyncServerDataPacket extends InternalPacket {
	
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
