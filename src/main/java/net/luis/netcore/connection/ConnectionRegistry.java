package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.*;
import net.luis.utils.collection.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ConnectionRegistry {
	
	private final Registry<ConnectionListener> listeners = Registry.of();
	private final Supplier<UUID> uniqueId;
	
	@ApiStatus.Internal
	ConnectionRegistry(Supplier<UUID> uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId.get();
	}
	
	//region Listener builder
	public @NotNull ListenerBuilder builder() {
		return new ListenerBuilder(this);
	}
	
	public @NotNull ListenerBuilder builder(PacketTarget target) {
		return new DefaultListenerBuilder(this, target);
	}
	
	public @NotNull ListenerBuilder builder(PacketPriority priority) {
		return new DefaultListenerBuilder(this, priority);
	}
	
	public @NotNull ListenerBuilder builder(PacketTarget target, PacketPriority priority) {
		return new DefaultListenerBuilder(this, target, priority);
	}
	//endregion
	
	//region Instance listener
	public void registerListener(PacketListener listener) {
		Objects.requireNonNull(listener, "Listener must not be null").initialize(this);
	}
	//endregion
	
	//region Method listener
	public <T extends Packet> @NotNull UUID registerListener(Runnable listener) {
		return this.registerListener(Packet.class, PacketTarget.ANY, PacketPriority.NORMAL, (packet, ctx) -> listener.run());
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, BiConsumer<T, ConnectionContext> listener) {
		return this.registerListener(packetClass, PacketTarget.ANY, PacketPriority.NORMAL, listener);
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketTarget target, BiConsumer<T, ConnectionContext> listener) {
		return this.registerListener(packetClass, target, PacketPriority.NORMAL, listener);
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketPriority priority, BiConsumer<T, ConnectionContext> listener) {
		return this.registerListener(packetClass, PacketTarget.ANY, priority, listener);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<T, ConnectionContext> listener) {
		return this.listeners.register(uniqueId -> new ConnectionListener(uniqueId, packetClass, target, priority, (BiConsumer<Packet, ConnectionContext>) listener));
	}
	//endregion
	
	public boolean removeListener(UUID uniqueId) {
		return this.listeners.remove(uniqueId);
	}
	
	public @NotNull List<ConnectionListener> getListeners() {
		List<ConnectionListener> listeners = Lists.newArrayList(this.listeners.getItems());
		listeners.sort(Comparator.comparing(ConnectionListener::priority).reversed());
		return listeners;
	}
	
	public void close() {
		this.listeners.clear();
	}
}
