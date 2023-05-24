package net.luis.netcore.network.connection;

import com.google.common.collect.Lists;
import io.netty.channel.*;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.network.connection.event.impl.*;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.filter.PacketFilter;
import net.luis.netcore.packet.listener.*;
import net.luis.utils.collection.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

import static io.netty.channel.ChannelFutureListener.*;
import static net.luis.netcore.network.connection.event.ConnectionEventManager.*;
import static net.luis.netcore.network.connection.event.ConnectionEventType.CLOSE;
import static net.luis.netcore.network.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public final class Connection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final UUID uniqueId = UUID.randomUUID();
	private final Registry<ConnectionListener> listeners = Registry.of();
	private final Registry<PacketFilter> filters = Registry.of();
	private final Channel channel;
	private final Optional<Packet> handshake;
	
	@ApiStatus.Internal
	public Connection(Channel channel) {
		this(channel, Optional.empty());
	}
	
	@ApiStatus.Internal
	public Connection(Channel channel, Optional<Packet> handshake) {
		this.channel = Objects.requireNonNull(channel, "Channel must not be null");
		this.handshake = Objects.requireNonNull(handshake, "Handshake must not be null");
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	public void send(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (packet.bypassEvent(SEND)) {
			this.sendInternal(packet);
			return;
		}
		SendEvent event = new SendEvent(this.uniqueId, packet);
		INSTANCE.dispatch(SEND, event);
		if (!event.isCancelled()) {
			this.sendInternal(event.getPacket());
		}
	}
	
	private void sendInternal(Packet packet) {
		this.channel.writeAndFlush(packet).addListener(CLOSE_ON_FAILURE);
		LOGGER.debug("Sent {}", packet.getClass().getSimpleName());
	}
	
	//region Netty overrides
	@Override
	public void channelActive(ChannelHandlerContext context) {
		INSTANCE.dispatch(OPEN, new OpenEvent(this.uniqueId));
		this.handshake.filter(handshake -> !handshake.isInternal()).ifPresent(handshake -> {
			HandshakeEvent event = new HandshakeEvent(this.uniqueId, handshake);
			INSTANCE.dispatch(HANDSHAKE, event);
			if (!event.isCancelled()) {
				this.channel.writeAndFlush(event.getPacket().withTarget(PacketTarget.HANDSHAKE)).addListener(CLOSE_ON_FAILURE);
				LOGGER.debug("Sent handshake {}", event.getPacket().getClass().getSimpleName());
			}
		});
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		try {
			LOGGER.debug("Received {}", packet);
			if (!packet.isInternal() && this.filters.getItems().stream().anyMatch(filter -> filter.filter(packet))) {
				LOGGER.debug("{} with target '{}' was filtered", packet.getClass().getSimpleName(), packet.getTarget().getName());
			} else {
				this.callListeners(packet);
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
	
	@Override
	public void channelInactive(ChannelHandlerContext context) throws Exception {
		INSTANCE.dispatch(CLOSE, new CloseEvent(this.uniqueId));
	}
	//endregion
	
	//region Listener registration
	public @NotNull ListenerBuilder builder() {
		return new ListenerBuilder(this);
	}
	
	public void registerListener(PacketListener listener) {
		Objects.requireNonNull(listener, "Listener must not be null").initialize(this);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Packet> @NotNull UUID registerListener(Class<T> packetClass, PacketTarget target, PacketPriority priority, BiConsumer<T, ConnectionContext> listener) {
		return this.listeners.register(uniqueId -> new ConnectionListener(uniqueId, packetClass, target, priority, (BiConsumer<Packet, ConnectionContext>) listener));
	}
	
	public boolean removeListener(UUID uniqueId) {
		return this.listeners.remove(uniqueId);
	}
	//endregion
	
	//region Filter registration
	public @NotNull UUID registerFilter(PacketFilter filter) {
		return this.filters.register(Objects.requireNonNull(filter, "Filter must not be null"));
	}
	
	public boolean removeFilter(UUID uniqueId) {
		return this.filters.remove(uniqueId);
	}
	//endregion
	
	private void callListeners(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		boolean handled = false;
		List<ConnectionListener> listeners = Lists.newArrayList(this.listeners.getItems());
		listeners.sort(Comparator.comparing(ConnectionListener::priority).reversed());
		for (ConnectionListener listener : listeners) {
			if (listener.shouldCall(packet)) {
				try {
					listener.call(packet, new ConnectionContext(this.uniqueId, this::send));
					handled = true;
				} catch (Exception e) {
					LOGGER.warn("Caught exception while calling listener {}", listener.uniqueId(), e);
				}
			}
		}
		if (!packet.bypassEvent(RECEIVE)) {
			ReceiveEvent event = new ReceiveEvent(this.uniqueId, packet, handled);
			INSTANCE.dispatch(RECEIVE, event);
			if (!event.isHandled()) {
				LOGGER.warn("{} with target '{}' was not handled by any listener and event", packet, packet.getTarget().getName());
			}
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
}
