package net.luis.netcore.packet.listener;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.buffer.decode.Decodable;
import net.luis.netcore.buffer.encode.Encodable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public final class PacketTarget implements Encodable, Decodable {
	
	public static final PacketTarget ANY = new PacketTarget("any", -1);
	public static final PacketTarget HANDSHAKE = new PacketTarget("handshake", 0);
	
	private final String name;
	private final int target;
	
	public PacketTarget(String name, int target) {
		this.name = Objects.requireNonNull(name, "Name must not be null");
		this.target = target;
		if (this.target < -1) {
			throw new IllegalArgumentException("Target must be greater than -2");
		}
	}
	
	public PacketTarget(@NotNull FriendlyByteBuffer buffer) {
		this.name = buffer.readString();
		this.target = buffer.readInt();
	}
	
	public static @NotNull PacketTarget of(int target) {
		return of("custom-" + target, target);
	}
	
	public static @NotNull PacketTarget of(String name, int target) {
		return new PacketTarget(name, target);
	}
	
	public @NotNull String getName() {
		return this.name;
	}
	
	public int getTarget() {
		return this.target;
	}
	
	public boolean isAny() {
		return this.target == -1 || this == ANY;
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeString(this.name);
		buffer.writeInt(this.target);
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PacketTarget that)) return false;
		
		if (this.target != that.target) return false;
		return this.name.equals(that.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.target);
	}
	
	@Override
	public String toString() {
		return "PacketTarget{name='" + this.name + "', target=" + this.target + "}";
	}
	//endregion
}
