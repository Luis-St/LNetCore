package net.luis.netcore.packet.listener;

import net.luis.netcore.connection.*;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.ApiStatus;
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
	
	private final ConnectionRegistry registry;
	private Class<? extends Packet> packetClass = Packet.class;
	private BiConsumer<Packet, ConnectionContext> consumer;
	protected PacketTarget target = PacketTarget.ANY;
	protected PacketPriority priority = PacketPriority.NORMAL;
	
	@ApiStatus.Internal
	public ListenerBuilder(ConnectionRegistry registry) {
		this.registry = Objects.requireNonNull(registry, "Connection registry must not be null");
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
	
	public @NotNull ListenerBuilder listener(BiConsumer<Packet, ConnectionContext> listener) {
		return this.listener(Packet.class, listener);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> @NotNull ListenerBuilder listener(Class<T> packetClass, BiConsumer<T, ConnectionContext> listener) {
		this.packetClass = Objects.requireNonNull(packetClass, "Packet class must not be null");
		this.consumer = (BiConsumer<Packet, ConnectionContext>) Objects.requireNonNull(listener, "Listener must not be null");
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public @NotNull UUID register() {
		return this.registry.registerListener((Class<Packet>) this.packetClass, this.target, this.priority, this.consumer);
	}
}
