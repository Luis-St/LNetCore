package net.luis.netcore.packet.listener;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.buffer.encode.Encodable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public final class PacketTarget implements Encodable {
	
	public static final PacketTarget INTERNAL = new PacketTarget("internal", -1);
	public static final PacketTarget ANY = new PacketTarget("any", 0);
	public static final PacketTarget HANDSHAKE = new PacketTarget("handshake", 1);
	
	private final String name;
	private final int target;
	
	private PacketTarget(String name, int target) {
		this.name = Objects.requireNonNull(name, "Name must not be null");
		this.target = target;
	}
	
	public static @NotNull PacketTarget of(int target) {
		return of("custom-" + target, target);
	}
	
	public static @NotNull PacketTarget of(String name, int target) {
		if (2 > target) {
			throw new IllegalArgumentException("Target must be greater than or equal to 2");
		}
		return new PacketTarget(name, target);
	}
	
	public static @NotNull PacketTarget of(FriendlyByteBuffer buffer) {
		Objects.requireNonNull(buffer, "Buffer must not be null");
		int target = buffer.readInt();
		String name = buffer.readString();
		return switch (target) {
			case -1 -> INTERNAL;
			case 0 -> ANY;
			case 1 -> HANDSHAKE;
			default -> of(name, target);
		};
	}
	
	public @NotNull String getName() {
		return this.name;
	}
	
	public int getTarget() {
		return this.target;
	}
	
	public boolean isInternal() {
		return this.target == -1 || this == INTERNAL;
	}
	
	public boolean isAny() {
		return this.target == 0 || this == ANY;
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
