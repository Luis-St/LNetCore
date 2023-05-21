package net.luis.netcore.packet.listener;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public final class PacketPriority implements Comparable<PacketPriority> {
	
	public static final PacketPriority LOWEST = new PacketPriority("lowest", -2);
	public static final PacketPriority LOW = new PacketPriority("low", -1);
	public static final PacketPriority NORMAL = new PacketPriority("normal", 0);
	public static final PacketPriority HIGH = new PacketPriority("high", 1);
	public static final PacketPriority HIGHEST = new PacketPriority("highest", 2);
	
	private final String name;
	private final int priority;
	
	private PacketPriority(String name, int priority) {
		this.name = Objects.requireNonNull(name, "Name must not be null");
		this.priority = priority;
	}
	
	public static @NotNull PacketPriority of(int priority) {
		return switch (priority) {
			case -2 -> LOWEST;
			case -1 -> LOW;
			case 0 -> NORMAL;
			case 1 -> HIGH;
			case 2 -> HIGHEST;
			default -> throw new IllegalArgumentException("No packet priority with priority " + priority + " found");
		};
	}
	
	public static @NotNull PacketPriority of(String name, int priority) {
		return new PacketPriority(name, priority);
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	@Override
	public int compareTo(@NotNull PacketPriority priority) {
		return Comparator.comparingInt(PacketPriority::getPriority).compare(this, priority);
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PacketPriority that)) return false;
		
		if (this.priority != that.priority) return false;
		return this.name.equals(that.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.priority);
	}
	
	@Override
	public String toString() {
		return "PacketPriority{name='" + this.name + '\'' + ", priority=" + this.priority + "}";
	}
	//endregion
}
