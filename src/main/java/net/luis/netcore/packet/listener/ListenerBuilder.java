package net.luis.netcore.packet.listener;

import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ListenerBuilder {
	
	private final Connection connection;
	private PacketTarget target = PacketTarget.ANY;
	private PacketPriority priority = PacketPriority.NORMAL;
	private Class<? extends Packet> packetClass = Packet.class;
	private BiConsumer<Packet, Consumer<Packet>> consumer;
	
	public ListenerBuilder(Connection connection) {
		this.connection = Objects.requireNonNull(connection, "Connection must not be null");
	}
	
	public @NotNull ListenerBuilder target(PacketTarget target) {
		this.target = Objects.requireNonNull(target, "Target must not be null");
		return this;
	}
	
	public @NotNull ListenerBuilder target(String name, int target) {
		return this.target(PacketTarget.of(name, target));
	}
	
	public @NotNull ListenerBuilder priority(PacketPriority priority) {
		this.priority = Objects.requireNonNull(priority, "Priority must not be null");
		return this;
	}
	
	public @NotNull ListenerBuilder priority(int priority) {
		return this.priority(PacketPriority.of(priority));
	}
	
	public @NotNull ListenerBuilder priority(String name, int priority) {
		return this.priority(PacketPriority.of(name, priority));
	}
	
	public @NotNull ListenerBuilder listener(Runnable listener) {
		Objects.requireNonNull(listener, "Listener must not be null");
		return this.listener(Packet.class, (packet, sender) -> listener.run());
	}
	
	public @NotNull ListenerBuilder listener(Consumer<Packet> listener) {
		Objects.requireNonNull(listener, "Listener must not be null");
		return this.listener(Packet.class, (packet, sender) -> listener.accept(packet));
	}
	
	public <T extends Packet> @NotNull ListenerBuilder listener(Class<T> packetClass, Consumer<T> listener) {
		Objects.requireNonNull(listener, "Listener must not be null");
		return this.listener(packetClass, (packet, sender) -> listener.accept(packet));
	}
	
	public @NotNull ListenerBuilder listener(BiConsumer<Packet, Consumer<Packet>> listener) {
		return this.listener(Packet.class, listener);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> @NotNull ListenerBuilder listener(Class<T> packetClass, BiConsumer<T, Consumer<Packet>> listener) {
		this.packetClass = Objects.requireNonNull(packetClass, "Packet class must not be null");
		this.consumer = (BiConsumer<Packet, Consumer<Packet>>) Objects.requireNonNull(listener, "Listener must not be null");
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public @NotNull UUID register() {
		return this.connection.registerListener((Class<Packet>) this.packetClass, this.target, this.priority, this.consumer);
	}
}
