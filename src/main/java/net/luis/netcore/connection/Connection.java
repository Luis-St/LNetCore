package net.luis.netcore.connection;

import io.netty.channel.*;
import io.netty.handler.timeout.TimeoutException;
import net.luis.netcore.connection.event.impl.*;
import net.luis.netcore.connection.internal.*;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.InternalPacket;
import net.luis.netcore.packet.permission.PermissionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static io.netty.channel.ChannelFutureListener.*;
import static net.luis.netcore.connection.event.ConnectionEventManager.*;
import static net.luis.netcore.connection.event.ConnectionEventType.CLOSE;
import static net.luis.netcore.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public sealed abstract class Connection extends SimpleChannelInboundHandler<Packet> permits ClientConnection, ServerConnection {
	
	private static final Logger LOGGER = LogManager.getLogger(Connection.class);
	
	private final ConnectionRegistry registry = new ConnectionRegistry(() -> this.uniqueId);
	private final ConnectionSettings settings = new ConnectionSettings(() -> this.uniqueId);
	private final ConnectionInitializer initializer;
	protected final Channel channel;
	protected final Optional<Packet> handshake;
	private UUID uniqueId = null;
	
	protected Connection(Channel channel, ConnectionInitializer initializer, Optional<Packet> handshake) {
		this.channel = Objects.requireNonNull(channel, "Channel must not be null");
		this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
		this.handshake = Objects.requireNonNull(handshake, "Handshake must not be null");
	}
	
	protected Connection(UUID uniqueId, Channel channel, ConnectionInitializer initializer) {
		this.channel = Objects.requireNonNull(channel, "Channel must not be null");
		this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
		this.handshake = Optional.empty();
		this.setUniqueId(uniqueId);
	}
	
	public @NotNull ConnectionRegistry getRegistry() {
		return this.registry;
	}
	
	public @NotNull ConnectionSettings getSettings() {
		return this.settings;
	}
	
	public @NotNull UUID getUniqueId() {
		return Objects.requireNonNull(this.uniqueId, "Unique id has not been initialized yet");
	}
	
	@ApiStatus.Internal
	public void setUniqueId(UUID uniqueId) {
		if (this.uniqueId != null) {
			throw new IllegalStateException("Unique id is already set");
		}
		this.uniqueId = Objects.requireNonNull(uniqueId, "Unique id must not be null");
		this.initializer.initialize(this.registry, this.settings);
	}
	
	public boolean isInitialized() {
		return this.uniqueId != null;
	}
	
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	//region Sending packets
	public <T> void send(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (packet instanceof InternalPacket || packet.getTarget().isInternal()) {
			throw new IllegalArgumentException("Internal packets must not be sent using this connection");
		}
		if (!this.hasPermission(packet)) {
			return;
		}
		if (this.settings.areEventsAllowed()) {
			SendEvent event = new SendEvent(this.uniqueId, packet);
			INSTANCE.dispatch(SEND, event);
			if (!event.isCancelled()) {
				this.sendInternal(packet);
			}
		} else {
			this.sendInternal(packet);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> boolean hasPermission(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (packet.requiresPermission()) {
			PermissionHandler<T> handler = (PermissionHandler<T>) this.settings.getPermissionHandler();
			if (handler == null) {
				throw new IllegalStateException(packet + " requires permission but no permission handler is set");
			}
			return handler.hasPermission(handler.mapUser(this.uniqueId), packet);
		}
		return true;
	}
	
	private void sendInternal(Packet packet) {
		if (this.channel.isOpen()) {
			this.channel.writeAndFlush(packet).addListener(CLOSE_ON_FAILURE);
			LOGGER.debug("Sent {} with target '{}'", packet.getClass().getSimpleName(), packet.getTarget().getName());
		}
	}
	//endregion
	
	//region Netty overrides
	@Override
	public abstract void channelActive(ChannelHandlerContext ctx);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (packet instanceof InternalPacket || packet.getTarget().isInternal()) {
			throw new IllegalStateException("Internal packets must not be received using this connection");
		}
		try {
			LOGGER.debug("Received {}", packet);
			this.callListeners(packet);
		} catch (Exception e) {
			LOGGER.warn("Fail to handle {}", packet, e);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
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
	public void channelInactive(ChannelHandlerContext ctx) {
		if (this.settings.areEventsAllowed()) {
			INSTANCE.dispatch(CLOSE, new CloseEvent(this.uniqueId));
		}
	}
	//endregion
	
	//region Listener registration

	//endregion
	
	private void callListeners(Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		boolean handled = false;
		for (ConnectionListener listener : this.registry.getListeners()) {
			if (listener.shouldCall(packet)) {
				try {
					listener.call(packet, new ConnectionContext(this.uniqueId, this::send));
					handled = true;
				} catch (Exception e) {
					LOGGER.warn("Caught exception while calling listener {}", listener.uniqueId(), e);
				}
			}
		}
		if (this.settings.areEventsAllowed()) {
			ReceiveEvent event = new ReceiveEvent(this.uniqueId, packet, handled);
			INSTANCE.dispatch(RECEIVE, event);
			if (!event.isHandled()) {
				LOGGER.warn("{} with target '{}' was not handled by any listener or event", packet, packet.getTarget().getName());
			}
		} else if (!handled) {
			LOGGER.warn("{} with target '{}' was not handled by any listener", packet, packet.getTarget().getName());
		}
	}
	
	public void close() {
		this.registry.close();
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
