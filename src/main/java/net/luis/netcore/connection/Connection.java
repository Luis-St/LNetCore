package net.luis.netcore.connection;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.Target;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.utils.collection.SortedList;
import net.luis.utils.util.reflection.ClassPathUtils;
import net.luis.utils.util.reflection.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class Connection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final UUID uniqueId = UUID.randomUUID();
	private final Map<Class<? extends Packet>, SortedList<Listener>> listeners = Maps.newHashMap();
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
	protected void channelRead0(ChannelHandlerContext context, Packet packet) {
		try {
			LOGGER.debug("Received packet {}", packet.getClass().getSimpleName());
			this.callListeners(packet);
		} catch (Exception e) {
			LOGGER.warn("Fail to handle packet {}", packet.getClass().getSimpleName());
		}
	}
	
	public void send(Packet packet) {
		int target = this.getTarget(Thread.currentThread().getStackTrace()[2]);
		if (target > -1) {
			this.send(packet, target);
		} else if (this.channel.isOpen()) {
			this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			LOGGER.debug("Sent packet {}", packet.getClass().getSimpleName());
		}
	}
	
	private int getTarget(StackTraceElement stackTrace) {
		try {
			Class<?> clazz = ReflectionHelper.getClassForName(stackTrace.getClassName());
			Method method = ReflectionHelper.getMethod(clazz, stackTrace.getMethodName());
			if (method.isAnnotationPresent(Target.class)) {
				return method.getAnnotation(Target.class).value();
			} else if (clazz.isAnnotationPresent(Target.class)) {
				return clazz.getAnnotation(Target.class).value();
			} else {
				return -1;
			}
		} catch (Exception | Error e) {
			return -1;
		}
	}
	
	public void send(Packet packet, int target) {
		if (this.channel.isOpen()) {
			ReflectionHelper.set(Packet.class, "target", packet, Math.max(target, -1));
			this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			LOGGER.debug("Sent packet {} to target {}", packet.getClass().getSimpleName(), target);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		if (cause instanceof SkipPacketException) {
			LOGGER.info("Skipping packet");
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
	
	//region No parameter listeners
	//region Method overloads
	public void addListener(Runnable listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), Packet.class, -1, (connection, packet) -> listener.run(), 0);
	}
	
	public void addListener(int targetId, Runnable listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), Packet.class, targetId, (connection, packet) -> listener.run(), 0);
	}
	
	public void addListener(Class<? extends Packet> packetClass, Runnable listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), packetClass, -1, (connection, packet) -> listener.run(), 0);
	}
	//endregion
	
	public void addListener(Class<? extends Packet> packetClass, int targetId, Runnable listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), packetClass, targetId, (connection, packet) -> listener.run(), 0);
	}
	//endregion
	
	//region Single parameter listeners
	//region Method overloads
	public void addListener(Consumer<Packet> listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), Packet.class, -1, (connection, packet) -> listener.accept(packet), 0);
	}
	
	public void addListener(int targetId, Consumer<Packet> listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), Packet.class, targetId, (connection, packet) -> listener.accept(packet), 0);
	}
	
	public <T extends Packet> void addListener(Class<T> packetClass, Consumer<T> listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), packetClass, -1, (connection, packet) -> listener.accept(packetClass.cast(packet)), 0);
	}
	//endregion
	
	public <T extends Packet> void addListener(Class<T> packetClass, int targetId, Consumer<T> listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), packetClass, targetId, (connection, packet) -> listener.accept(packetClass.cast(packet)), 0);
	}
	//endregion
	
	//region Bi parameter listeners
	//region Method overloads
	public void addListener(BiConsumer<Connection, Packet> listener) {
		this.addListener(Packet.class, listener);
	}
	
	public void addListener(int targetId, BiConsumer<Connection, Packet> listener) {
		this.addListener(Packet.class, targetId, listener);
	}
	
	public <T extends Packet> void addListener(Class<T> packetClass, BiConsumer<Connection, T> listener) {
		this.addListener(packetClass, -1, listener);
	}
	//endregion
	
	public <T extends Packet> void addListener(Class<T> packetClass, int targetId, BiConsumer<Connection, T> listener) {
		this.addListener(listener.toString().split("/")[0].replace("$$", "$"), packetClass, targetId, listener, 0);
	}
	//endregion
	
	//region Instance listeners
	public void addListener(@NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		if (clazz.isAnnotationPresent(Target.class)) {
			int target = clazz.getAnnotation(Target.class).value();
			if (target == -1) {
				LOGGER.warn("If the listener instance {} is annotated with @ListenerTarget, the target should be explicitly specified", clazz.getSimpleName());
			}
			this.addListener(target, listenerInstance);
		} else {
			this.addListener(-1, listenerInstance);
		}
	}
	
	public void addListener(int targetId, @NotNull Object listenerInstance) {
		Class<?> clazz = listenerInstance.getClass();
		for (Method listener : ClassPathUtils.getAnnotatedMethods(clazz, PacketListener.class)) {
			int target = targetId;
			if (listener.isAnnotationPresent(Target.class)) {
				target = listener.getAnnotation(Target.class).value();
				if (target == -1) {
					LOGGER.warn("If the listener {}#{} is annotated with @ListenerTarget, the target should be explicitly specified", clazz.getSimpleName(), listener.getName());
				}
			}
			PacketListener packetListener = listener.getAnnotation(PacketListener.class);
			String name = listener.getDeclaringClass().getSimpleName() + "#" + listener.getName();
			this.addListener(name, packetListener.value(), target, (connection, packet) -> ReflectionHelper.invoke(listener, listenerInstance, ListenerHelper.getParameters(listener, connection, packet)), packetListener.priority());
		}
	}
	//endregion
	
	@SuppressWarnings("unchecked")
	private void addListener(String name, @NotNull Class<? extends Packet> packetClass, int targetId, @NotNull BiConsumer<Connection, ? extends Packet> listener, int priority) {
		int target = this.validateTargetId(name, targetId);
		LOGGER.debug("Adding listener {} for packet {} with target {} and priority {}", name, packetClass.getSimpleName(), target, priority);
		Listener listenerInstance = new Listener(name, target, (BiConsumer<Connection, Packet>) Objects.requireNonNull(listener), priority);
		this.listeners.putIfAbsent(packetClass, new SortedList<>(Comparator.comparingInt(Listener::priority)));
		this.listeners.get(packetClass).add(listenerInstance);
	}
	
	private int validateTargetId(String name, int target) {
		if (-1 > target) {
			LOGGER.warn("The target id of listener {} has no effect, it should be greater or equal than -1", name);
		}
		return Math.max(target, -1);
	}
	
	protected void callListeners(@NotNull Packet packet) {
		SortedList<Listener> listeners = this.listeners.getOrDefault(packet.getClass(), new SortedList<>());
		for (Listener listener : listeners) {
			if (listener.target() == -1 || listener.target() == (int) ReflectionHelper.get(Packet.class, "target", packet)) {
				listener.listener().accept(this, packet);
			}
		}
	}
	
	public void close() {
		this.listeners.clear();
		this.channel.close();
	}
	
	//region Object overrides
	@Override
	public boolean equals(@Nullable Object o) {
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
		return "Connection{uniqueId=" + this.uniqueId + "}";
	}
	//endregion
	
	//region Internal
	private record Listener(String name, int target, BiConsumer<Connection, Packet> listener, int priority) implements Comparable<Listener> {
		
		@Override
		public int compareTo(@NotNull Listener listener) {
			return Integer.compare(this.priority, listener.priority);
		}
		
	}
	//endregion
}
