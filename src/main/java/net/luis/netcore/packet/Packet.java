package net.luis.netcore.packet;

import net.luis.netcore.buffer.Decodable;
import net.luis.netcore.buffer.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.listener.PacketListener;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public abstract class Packet implements Encodable, Decodable {
	
	private final int target = PacketListener.ANY_TARGET;
	
	public Packet() {
	
	}
	
	public Packet(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public abstract void encode(@NotNull FriendlyByteBuffer buffer);
	
	public boolean skippable() {
		return false;
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	//endregion
}
