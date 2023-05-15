package net.luis.netcore.packet;

import net.luis.netcore.buffer.Decodable;
import net.luis.netcore.buffer.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.listener.PacketTarget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public abstract class Packet implements Encodable, Decodable {
	
	private PacketTarget target = PacketTarget.ANY;
	
	public Packet() {
	
	}
	
	public Packet(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public abstract void encode(@NotNull FriendlyByteBuffer buffer);
	
	public boolean skippable() {
		return false;
	}
	
	public PacketTarget getTarget() {
		return this.target;
	}
	
	public Packet withTarget(PacketTarget target) {
		this.target = target;
		return this;
	}
	
	public Packet withTarget(int target) {
		return this.withTarget(PacketTarget.of(target));
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	//endregion
}
