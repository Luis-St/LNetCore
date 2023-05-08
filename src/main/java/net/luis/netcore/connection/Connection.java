package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.utils.util.unsafe.StackTraceUtils;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import net.luis.utils.util.unsafe.reflection.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
		this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		LOGGER.debug("Sent packet {}", packet.getClass().getSimpleName());
	}
	
	public void send(int target, Packet packet) {
		if (this.channel.isOpen()) {
			packet.setTarget(target);
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
			this.callListeners(packet);
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
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, @NotNull Consumer<T> listener) {
		this.addListener(getName(listener), DataHolder.of(packetClass), (connection, packet) -> listener.accept(packetClass.cast(packet)));
	}
	//endregion
	
	//region Bi parameter listeners
	public void addListener(@NotNull BiConsumer<Connection, Packet> listener) {
		this.addListener(getName(listener), DataHolder.of(), listener);
	}
	
	public <T extends Packet> void addListener(@NotNull Class<T> packetClass, @NotNull BiConsumer<Connection, T> listener) {
		this.addListener(getName(listener), DataHolder.of(packetClass), listener);
	}
	//endregion
	
	//region Instance listeners
	public void addListener(@NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		for (Method listener : clazz.getDeclaredMethods()) {
			if (!Modifier.isPublic(listener.getModifiers())) {
				continue;
			}
			String name = clazz.getSimpleName() + "#" + listener.getName();
			this.addListener(name, DataHolder.of(clazz, listener), (connection, packet) -> {
				try {
					ReflectionHelper.invoke(listener, listenerInstance, Connection.this.getParameters(listener, connection, packet));
				} catch (IllegalArgumentException ignored) {
				
				} catch (Exception e) {
					if (!packet.skippable()) {
						LOGGER.warn("Fail to invoke listener", e);
					}
				}
			});
		}
	}
	
	private Object[] getParameters(Method listener, @NotNull Connection connection, @NotNull Packet packet) {
		return ReflectionUtils.getParameters(listener, getValues(connection, packet));
	}
	//endregion
	
	@SuppressWarnings("unchecked")
	private void addListener(String name, @NotNull DataHolder holder, @NotNull BiConsumer<Connection, ? extends Packet> listener) {
		this.listeners.add(new Listener(name, holder.packet(), (BiConsumer<Connection, Packet>) Objects.requireNonNull(listener), holder.priority()));
	}
	
	private void callListeners(@NotNull Packet packet) {
		boolean handled = false;
		this.listeners.sort(Comparator.comparingInt(Listener::priority));
		Collections.reverse(this.listeners);
		for (Listener listener : this.listeners) {
			if (listener.packet().isAssignableFrom(packet.getClass())) {
				listener.listener().accept(this, packet);
				handled = true;
			}
		}
		if (!handled) {
			LOGGER.warn("{} was not handled by any listener", packet);
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
	private record DataHolder(Class<? extends Packet> packet, int priority) {
		
		public static @NotNull DataHolder of(@NotNull Class<?> clazz, @NotNull Method method) {
			Class<? extends Packet> packet = Packet.class;
			int priority = 0;
			for (PacketPriority listener : new PacketPriority[] {clazz.getAnnotation(PacketPriority.class), method.getAnnotation(PacketPriority.class)}) {
				if (listener == null) {
					continue;
				}
				if (listener.priority() != priority) {
					priority = listener.priority();
				}
			}
			return new DataHolder(packet, priority);
		}
		
		public static @NotNull DataHolder of() {
			return of(StackTraceUtils.getCallingClass(1), StackTraceUtils.getCallingMethod(1));
		}
		
		public static @NotNull DataHolder of(Class<? extends Packet> packet) {
			return of(StackTraceUtils.getCallingClass(2), StackTraceUtils.getCallingMethod(2)).withPacket(packet);
		}
		
		private @NotNull DataHolder withPacket(Class<? extends Packet> packet) {
			return new DataHolder(packet, this.priority());
		}
	}
	
	private record Listener(String name, Class<? extends Packet> packet, BiConsumer<Connection, Packet> listener, int priority) {
	
	}
	//endregion
}
