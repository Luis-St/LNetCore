package net.luis.netcore.packet;

import net.luis.netcore.buffer.Decodable;
import net.luis.netcore.buffer.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public abstract class Packet implements Encodable, Decodable {
	
	private int target = -1;
	
	public Packet() {
	
	}
	
	public Packet(@NotNull FriendlyByteBuffer buffer) {
	
	}
	
	@Override
	public abstract void encode(@NotNull FriendlyByteBuffer buffer);
	
	public boolean skippable() {
		return false;
	}
	
	public int getTarget() {
		return this.target;
	}
	
	@ApiStatus.Internal
	public void setTarget(int target) {
		this.target = Math.max(target, -1);
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	//endregion
}
