package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;
import net.luis.utils.collection.SortedList;
import net.luis.utils.util.reflection.ClassPathUtils;
import net.luis.utils.util.reflection.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.luis.netcore.connection.ListenerHelper.*;

/**
 *
 * @author Luis-St
 *
 */

public class Connection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final UUID uniqueId = UUID.randomUUID();
	private final List<Listener> listeners = Lists.newArrayList();
	private final List<Integer> registeredTargets = Lists.newArrayList();
	private final Channel channel;
	
	public Connection(Channel channel) {
		this.channel = channel;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, @NotNull Packet packet) {
		try {
			LOGGER.debug("Received packet {}", packet);
			int target = (int) ReflectionHelper.get(Packet.class, "target", packet);
			if (target == -1 || this.registeredTargets.contains(target)) {
				this.callListeners(packet, target);
			} else if (packet.skippable()) {
				LOGGER.warn("Received packet {} with target {} but no listener is registered for this target", packet, target);
				throw new SkipPacketException();
			} else {
				throw new IllegalStateException("Received packet " + packet + " with target " + target + " but no listener is registered for this target");
			}
		} catch (Exception e) {
			LOGGER.warn("Fail to handle packet {}", packet);
		}
	}
	
	//region Sending packets
	public void send(Packet packet) {
		int target = getTarget();
		if (target > -1) {
			this.send(target, packet);
		} else if (this.channel.isOpen()) {
			this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			LOGGER.debug("Sent packet {}", packet.getClass().getSimpleName());
		}
	}
	
	public void send(int target, Packet packet) {
		if (this.channel.isOpen()) {
			ReflectionHelper.set(Packet.class, "target", packet, Math.max(target, -1));
			this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			LOGGER.debug("Sent packet {} to target {}", packet.getClass().getSimpleName(), target);
		}
	}
	//endregion
	
	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		if (cause instanceof SkipPacketException e) {
			LOGGER.info("Skipping packet", e);
		} else if (this.channel.isOpen()) {
			LOGGER.error("Caught Exception", cause);
			if (cause instanceof TimeoutException) {
				throw new IOException("Timeout", cause);
			} else {
				throw new IOException("Internal exception", cause);
			}
		} else {
			LOGGER.error("Caught exception while channel is closed", cause);
		}
	}
	
	//region Single parameter listeners
	public void addListener(@NotNull Consumer<Packet> listener) {
		this.addListener(getName(listener), Packet.class, -1, (connection, packet) -> listener.accept(packet), getPriority());
	}
	
	public void addListener(int targetId, @NotNull Consumer<Packet> listener) {
		this.addListener(getName(listener), Packet.class, targetId, (connection, packet) -> listener.accept(packet), getPriority());
	}
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, int targetId, @NotNull Consumer<T> listener) {
		this.addListener(getName(listener), packetClass, targetId, (connection, packet) -> listener.accept(packetClass.cast(packet)), getPriority());
	}
	//endregion
	
	//region Bi parameter listeners
	public void addListener(@NotNull BiConsumer<Connection, Packet> listener) {
		this.addListener(getName(listener), Packet.class, PacketTarget.ANY_TARGET, listener, getPriority());
	}
	
	public void addListener(int targetId, @NotNull BiConsumer<Connection, Packet> listener) {
		this.addListener(getName(listener), Packet.class, targetId, listener, getPriority());
	}
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, int targetId, @NotNull BiConsumer<Connection, T> listener) {
		this.addListener(getName(listener), packetClass, targetId, listener, getPriority());
	}
	//endregion
	
	//region Instance listeners
	public void addListener(@NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		int target = -1;
		if (clazz.isAnnotationPresent(PacketTarget.class)) {
			target = clazz.getAnnotation(PacketTarget.class).value();
			if (target == -1) {
				LOGGER.warn("If the listener instance {} is annotated with @ListenerTarget, the target should be explicitly specified", clazz.getSimpleName());
			}
		}
		this.addListener(target, listenerInstance);
	}
	
	public void addListener(int target, @NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		int priority = 0;
		if (clazz.isAnnotationPresent(PacketPriority.class)) {
			priority = clazz.getAnnotation(PacketPriority.class).value();
		}
		for (Method listener : ClassPathUtils.getAnnotatedMethods(clazz, PacketListener.class)) {
			if (listener.isAnnotationPresent(PacketTarget.class)) {
				target = listener.getAnnotation(PacketTarget.class).value();
				if (target == -1) {
					LOGGER.warn("If the listener {}#{} is annotated with @ListenerTarget, the target should be explicitly specified", clazz.getSimpleName(), listener.getName());
				}
			}
			if (listener.isAnnotationPresent(PacketPriority.class)) {
				priority = listener.getAnnotation(PacketPriority.class).value();
			}
			PacketListener packetListener = listener.getAnnotation(PacketListener.class);
			String name = listener.getDeclaringClass().getSimpleName() + "#" + listener.getName();
			this.addListener(name, packetListener.value(), target, (connection, packet) -> ReflectionHelper.invoke(listener, listenerInstance, ListenerHelper.getParameters(listener, connection, packet)), priority);
		}
	}
	//endregion
	
	@SuppressWarnings("unchecked")
	private void addListener(String name, @NotNull Class<? extends Packet> packetClass, int target, @NotNull BiConsumer<Connection, ? extends Packet> listener, int priority) {
		if (-1 > target) {
			LOGGER.warn("The target id of listener {} has no effect, it should be greater or equal than -1", name);
		}
		target = Math.max(target, PacketTarget.ANY_TARGET);
		if (packetClass == Packet.class) {
			LOGGER.debug("Adding listener {} with target {} and priority {}", name, -1 == target ? "any" : target, priority);
		} else {
			LOGGER.debug("Adding listener {} for {} with target {} and priority {}", name, packetClass.getSimpleName(), -1 == target ? "any" : target, priority);
		}
		this.listeners.add(new Listener(name, packetClass, target, (BiConsumer<Connection, Packet>) Objects.requireNonNull(listener), priority));
		if (!this.registeredTargets.contains(target)) {
			this.registeredTargets.add(target);
		}
	}
	
	private void callListeners(@NotNull Packet packet, int target) {
		boolean handled = false;
		this.listeners.sort(Comparator.comparingInt(Listener::priority));
		Collections.reverse(this.listeners);
		for (Listener listener : this.listeners) {
			if (listener.packet().isAssignableFrom(packet.getClass())) {
				if (listener.target() == PacketTarget.ANY_TARGET || listener.target() == target) {
					listener.listener().accept(this, packet);
					handled = true;
				}
			}
		}
		if (!handled) {
			LOGGER.warn("{} with target {} was not handled by any listener", packet, target);
		}
	}
	
	public void close() {
		this.listeners.clear();
		this.channel.close();
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Connection that))
			return false;
		
		return this.uniqueId.equals(that.uniqueId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.uniqueId);
	}
	
	@Override
	public String toString() {
		return "Connection " + this.uniqueId;
	}
	//endregion
	
	//region Internal
	private record Listener(String name, Class<? extends Packet> packet, int target, BiConsumer<Connection, Packet> listener, int priority) implements Comparable<Listener> {
		
		@Override
		public int compareTo(@NotNull Listener listener) {
			return Integer.compare(this.priority, listener.priority);
		}
		
	}
	//endregion
}
