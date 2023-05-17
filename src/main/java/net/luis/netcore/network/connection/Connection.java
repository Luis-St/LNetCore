package net.luis.netcore.network.connection;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public final class Connection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final UUID uniqueId = UUID.randomUUID();
	private final Map<UUID, Listener> listeners = Maps.newHashMap();
	private final Channel channel;
	private final Optional<Packet> handshake;
	
	public Connection(Channel channel) {
		this(channel, Optional.empty());
	}
	
	public Connection(Channel channel, Optional<Packet> handshake) {
		this.channel = channel;
		this.handshake = handshake;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	public void send(Packet packet) {
		this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		LOGGER.debug("Sent packet {}", packet.getClass().getSimpleName());
	}
	
	//region Netty overrides
	@Override
	public void channelActive(ChannelHandlerContext context) {
		this.handshake.ifPresent(handshake -> {
			this.channel.writeAndFlush(handshake.withTarget(PacketTarget.HANDSHAKE)).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			LOGGER.debug("Sent handshake {}", handshake.getClass().getSimpleName());
		});
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
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
	
	@Override
	public void channelInactive(ChannelHandlerContext context) {
		LOGGER.info("Channel inactive");
	}
	//endregion
	
	//region Register listeners overloads
	public @NotNull UUID registerListener(Runnable listener) {
		return this.registerListener(Packet.class, PacketTarget.ANY, PacketPriority.NORMAL, (packet, sender) -> listener.run());
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Consumer<Packet> listener) {
		return this.registerListener(Packet.class, PacketTarget.ANY, PacketPriority.NORMAL, listener);
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, Consumer<T> listener) {
		return this.registerListener(packetClass, PacketTarget.ANY, PacketPriority.NORMAL, listener);
	}
	
	public <T extends Packet> @NotNull UUID registerListener(PacketTarget target, PacketPriority priority, Consumer<Packet> listener) {
		return this.registerListener(Packet.class, target, priority, listener);
	}
	
	public @NotNull UUID registerListener(BiConsumer<Packet, Consumer<Packet>> listener) {
		return this.registerListener(Packet.class, PacketTarget.ANY, PacketPriority.NORMAL, listener);
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, BiConsumer<T, Consumer<Packet>> listener) {
		return this.registerListener(packetClass, PacketTarget.ANY, PacketPriority.NORMAL, listener);
	}
	
	public @NotNull UUID registerListener(PacketTarget target, PacketPriority priority, BiConsumer<Packet, Consumer<Packet>> listener) {
		return this.registerListener(Packet.class, target, priority, listener);
	}
	//endregion
	
	public void registerListener(PacketListener listener) {
		Objects.requireNonNull(listener, "Listener must not be null").initialize(this);
	}
	
	public @NotNull UUID registerListener(PacketTarget target, PacketPriority priority, Runnable listener) {
		return this.registerListener(Packet.class, target, priority, (packet, sender) -> listener.run());
	}
	
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketTarget target, PacketPriority priority, Consumer<T> listener) {
		return this.registerListener(packetClass, target, priority, (packet, sender) -> listener.accept(packet));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<T, Consumer<Packet>> listener) {
		UUID uniqueId = UUID.randomUUID();
		this.listeners.put(uniqueId, new Listener(uniqueId, packetClass, target, priority, (BiConsumer<Packet, Consumer<Packet>>) listener));
		return uniqueId;
	}
	
	private void callListeners(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		boolean handled = false;
		List<Listener> listeners = Lists.newArrayList(this.listeners.values());
		listeners.sort(Comparator.comparing(Listener::priority).reversed());
		for (Listener listener : listeners) {
			if (listener.shouldCall(packet)) {
				try {
					listener.call(packet, this::send);
					handled = true;
				} catch (Exception e) {
					LOGGER.warn("Caught exception while calling listener {}", listener.uniqueId(), e);
				}
			}
		}
		if (!handled) {
			LOGGER.warn("{} with target {} was not handled by any listener", packet, packet.getTarget());
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
	private record Listener(UUID uniqueId, Class<? extends Packet> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<Packet, Consumer<Packet>> listener) {
		
		public Listener {
			Objects.requireNonNull(uniqueId, "Unique id must not be null");
			Objects.requireNonNull(packetClass, "Packet class must not be null");
			Objects.requireNonNull(target, "Packet target must not be null");
			Objects.requireNonNull(priority, "Packet priority must not be null");
			Objects.requireNonNull(listener, "Listener must not be null");
		}
		
		public boolean shouldCall(Packet packet) {
			Objects.requireNonNull(packet, "Packet must not be null");
			if (!this.packetClass.isAssignableFrom(packet.getClass())) {
				return false;
			}
			return this.target.isAny() || this.target.equals(packet.getTarget());
		}
		
		public void call(Packet packet, Consumer<Packet> sender) {
			Objects.requireNonNull(packet, "Packet must not be null");
			Objects.requireNonNull(sender, "Sender must not be null");
			this.listener.accept(packet, sender);
		}
	}
	//endregion
}
