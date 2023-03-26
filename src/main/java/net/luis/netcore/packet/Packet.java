package net.luis.netcore.packet;

import net.luis.netcore.buffer.Decodable;
import net.luis.netcore.buffer.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.listener.PacketTarget;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public abstract class Packet implements Encodable, Decodable {
	
	private final int target = PacketTarget.ANY_TARGET;
	
	public Packet() {
	
	}
	
	public Packet(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public abstract void encode(@NotNull FriendlyByteBuffer buffer);
	
	public boolean skippable() {
		return false;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
