package net.luis.netcore.instance;

import io.netty.channel.*;
import net.luis.netcore.connection.internal.ClientConnection;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.*;
import net.luis.netcore.packet.listener.PacketTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.UUID;

import static io.netty.channel.ChannelFutureListener.*;

/**
 *
 * @author Luis-St
 *
 */

@ApiStatus.Internal
public final class InternalConnection extends SimpleChannelInboundHandler<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(InternalConnection.class);
	
	private final NetworkInstance instance;
	private final Connection connection;
	private final Channel channel;
	
	InternalConnection(NetworkInstance instance, Connection connection, Channel channel) {
		this.instance = Objects.requireNonNull(instance, "Instance must not be null");
		this.connection = Objects.requireNonNull(connection, "Connection must not be null");
		this.channel = Objects.requireNonNull(channel, "Channel must not be null");
	}
	
	public UUID getUniqueId() {
		if (this.connection.isInitialized()) {
			return this.connection.getUniqueId();
		}
		return null;
	}
	
	public void send(InternalPacket packet) {
		if (this.channel.isOpen()) {
			Objects.requireNonNull(packet, "Packet must not be null");
			this.channel.writeAndFlush(packet.withTarget(PacketTarget.INTERNAL)).addListener(CLOSE_ON_FAILURE);
			LOGGER.debug("Sent internal {}", packet.getClass().getSimpleName());
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
		Objects.requireNonNull(packet, "Packet must not be null");
		if (!(packet instanceof InternalPacket internalPacket)) {
			if (packet.getTarget().isInternal()) {
				throw new IllegalStateException("Received packet with target 'internal' but packet is not internal");
			}
			LOGGER.debug("Skipping non-internal {} with target '{}'", packet.getClass().getSimpleName(), packet.getTarget().getName());
			ctx.fireChannelRead(packet);
			return;
		}
		LOGGER.debug("Received internal {}", internalPacket);
		if (this.instance instanceof ClientInstance client) {
			this.handleClient(client, internalPacket);
		} else if (this.instance instanceof ServerInstance server) {
			this.handleServer(server, internalPacket);
		} else {
			throw new IllegalStateException("Instance is neither client nor server");
		}
	}
	
	private void handleClient(ClientInstance instance, InternalPacket packet) {
		if (packet instanceof SyncServerDataPacket syncPacket) {
			((ClientConnection) this.connection).initialize(syncPacket);
		} else if (packet instanceof CloseConnectionPacket) {
			instance.closeInternal();
		}
	}
	
	private void handleServer(ServerInstance instance, InternalPacket packet) {
		if (packet instanceof CloseConnectionPacket) {
			instance.closeConnectionInternal(this.getUniqueId());
			LOGGER.info("Client disconnected with address {} using connection {}", this.channel.remoteAddress().toString().replace("/", ""), this.getUniqueId());
		} else if (packet instanceof CloseServerPacket) {
			instance.closeNow();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof SkipPacketException e) {
			LOGGER.info("Skipping packet", e);
		} else if (this.channel.isOpen()) {
			LOGGER.error("Caught Exception", cause);
		} else {
			LOGGER.error("Caught exception while channel is closed", cause);
		}
	}
	
	public void close() {
		this.channel.close();
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof InternalConnection that)) return false;
		
		if (!this.instance.equals(that.instance)) return false;
		if (!this.connection.equals(that.connection)) return false;
		return this.channel.equals(that.channel);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.instance, this.connection, this.channel);
	}
	
	@Override
	public String toString() {
		return "InternalConnection " + this.getUniqueId();
	}
	//endregion
}
