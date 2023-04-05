package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.utils.util.unsafe.StackTraceUtils;
import net.luis.utils.util.unsafe.classpath.ClassPathUtils;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import net.luis.utils.util.unsafe.reflection.ReflectionUtils;
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
	
	//region Netty overrides
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
			LOGGER.warn("Fail to handle {}", packet, e);
		}
	}
	
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
	//endregion
	
	//region Single parameter listeners
	public void addListener(@NotNull Consumer<Packet> listener) {
		this.addListener(getName(listener), DataHolder.of(), (connection, packet) -> listener.accept(packet));
	}
	
	public void addListener(int target, @NotNull Consumer<Packet> listener) {
		this.addListener(getName(listener), DataHolder.of(target), (connection, packet) -> listener.accept(packet));
	}
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, int target, @NotNull Consumer<T> listener) {
		this.addListener(getName(listener), DataHolder.of(packetClass, target), (connection, packet) -> listener.accept(packetClass.cast(packet)));
	}
	//endregion
	
	//region Bi parameter listeners
	public void addListener(@NotNull BiConsumer<Connection, Packet> listener) {
		this.addListener(getName(listener), DataHolder.of(), listener);
	}
	
	public void addListener(int target, @NotNull BiConsumer<Connection, Packet> listener) {
		this.addListener(getName(listener), DataHolder.of(target), listener);
	}
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, int target, @NotNull BiConsumer<Connection, T> listener) {
		this.addListener(getName(listener), DataHolder.of(packetClass, target), listener);
	}
	//endregion
	
	//region Instance listeners
	public void addListener(@NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		for (Method listener : ClassPathUtils.getAnnotatedMethods(clazz, PacketListener.class)) {
			String name = clazz.getSimpleName() + "#" + listener.getName();
			this.addListener(name, DataHolder.of(clazz, listener), (connection, packet) -> {
				ReflectionHelper.invoke(listener, listenerInstance, (Object[]) ReflectionUtils.getParameters(listener, getValues(connection, packet)));
			});
		}
	}
	
	public void addListener(int target, @NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		for (Method listener : ClassPathUtils.getAnnotatedMethods(clazz, PacketListener.class)) {
			String name = clazz.getSimpleName() + "#" + listener.getName();
			this.addListener(name, DataHolder.of(clazz, listener).withTarget(target), (connection, packet) -> {
				ReflectionHelper.invoke(listener, listenerInstance, (Object[]) ReflectionUtils.getParameters(listener, getValues(connection, packet)));
			});
		}
	}
	//endregion
	
	@SuppressWarnings("unchecked")
	private void addListener(String name, @NotNull DataHolder holder, @NotNull BiConsumer<Connection, ? extends Packet> listener) {
		int target = holder.target();
		this.listeners.add(new Listener(name, holder.packet(), target, (BiConsumer<Connection, Packet>) Objects.requireNonNull(listener), holder.priority()));
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
				if (listener.target() == PacketListener.ANY_TARGET || listener.target() == target) {
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
		if (this == o) return true;
		if (!(o instanceof Connection that)) return false;
		
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
	private record DataHolder(Class<? extends Packet> packet, int target, int priority) {
		
		public static @NotNull DataHolder of(@NotNull Class<?> clazz, @NotNull Method method) {
			Class<? extends Packet> packet = Packet.class;
			int target = PacketListener.ANY_TARGET;
			int priority = 0;
			for (PacketListener listener : new PacketListener[] {clazz.getAnnotation(PacketListener.class), method.getAnnotation(PacketListener.class)}) {
				if (listener == null) {
					continue;
				}
				if (listener.value() != packet) {
					packet = listener.value();
				}
				if (listener.target() != target) {
					target = Math.max(listener.target(), PacketListener.ANY_TARGET);
				}
				if (listener.priority() != priority) {
					priority = listener.priority();
				}
			}
			return new DataHolder(packet, target, priority);
		}
		
		public static @NotNull DataHolder of() {
			return of(StackTraceUtils.getCallingClass(2), StackTraceUtils.getCallingMethod(2));
		}
		
		public static @NotNull DataHolder of(Class<? extends Packet> packet) {
			return of(StackTraceUtils.getCallingClass(2), StackTraceUtils.getCallingMethod(2)).withPacket(packet);
		}
		
		public static @NotNull DataHolder of(int target) {
			return of(StackTraceUtils.getCallingClass(2), StackTraceUtils.getCallingMethod(2)).withTarget(target);
		}
		
		public static @NotNull DataHolder of(Class<? extends Packet> packet, int target) {
			return of(StackTraceUtils.getCallingClass(2), StackTraceUtils.getCallingMethod(2)).withPacket(packet).withTarget(target);
		}
		
		private @NotNull DataHolder withPacket(Class<? extends Packet> packet) {
			return new DataHolder(packet, this.target(), this.priority());
		}
		
		private @NotNull DataHolder withTarget(int target) {
			return new DataHolder(this.packet(), target, this.priority());
		}
		
	}
	
	private record Listener(String name, Class<? extends Packet> packet, int target, BiConsumer<Connection, Packet> listener, int priority) implements Comparable<Listener> {
		
		@Override
		public int compareTo(@NotNull Listener listener) {
			return Integer.compare(this.priority, listener.priority);
		}
		
	}
	//endregion
}
