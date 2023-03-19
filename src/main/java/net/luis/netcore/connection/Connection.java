package net.luis.netcore.connection;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 *
 * @author Luis-St
 *
 */

public class Connection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final UUID uniqueId = UUID.randomUUID();
	private final Map<String, Listener> listeners = Maps.newHashMap();
	private final Channel channel;
	
	public Connection(Channel channel) {
		this.channel = channel;
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
		if (this.channel.isOpen()) {
			this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
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
	
	//region Listeners
	protected void callListeners(@NotNull Packet packet) {
		this.listeners.values().stream().filter(listener -> listener.packetClass.isAssignableFrom(packet.getClass())).forEach(listener -> listener.listener.accept(this, packet));
	}
	
	public void addListener(@NotNull String name, @NotNull BiConsumer<Connection, Packet> listener) {
		this.listeners.put(name, new Listener(Packet.class, listener));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> void addListener(@NotNull String name, @NotNull Class<T> packetClass, @NotNull BiConsumer<Connection, T> listener) {
		this.listeners.put(name, new Listener(packetClass, (BiConsumer<Connection, Packet>) listener));
	}
	
	public void removeListener(@NotNull String name) {
		this.listeners.remove(name);
	}
	//endregion
	
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public void close() {
		this.channel.close();
	}
	
	//region Object overrides
	@Override
	public boolean equals(@Nullable Object o) {
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
		return "Connection{uniqueId=" + this.uniqueId + "}";
	}
	//endregion
	
	private record Listener(Class<? extends Packet> packetClass, BiConsumer<Connection, Packet> listener) {
	
	}
	
}
