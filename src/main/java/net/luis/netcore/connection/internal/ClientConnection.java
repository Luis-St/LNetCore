package net.luis.netcore.connection.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.connection.event.impl.HandshakeEvent;
import net.luis.netcore.connection.event.impl.OpenEvent;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.SyncServerDataPacket;
import net.luis.netcore.packet.listener.PacketTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.Optional;

import static io.netty.channel.ChannelFutureListener.*;
import static net.luis.netcore.connection.event.ConnectionEventManager.*;
import static net.luis.netcore.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

@ApiStatus.Internal
public final class ClientConnection extends Connection {
	
	private static final Logger LOGGER = LogManager.getLogger(ClientConnection.class);
	
	public ClientConnection(Channel channel, ConnectionInitializer initializer, Optional<Packet> handshake) {
		super(channel, initializer, handshake);
	}
	
	@ApiStatus.Internal
	public void initialize(SyncServerDataPacket packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		LOGGER.debug("Received server data");
		this.setUniqueId(packet.getUniqueId());
		if (this.getSettings().areEventsAllowed()) {
			this.initializeUsingEvents();
		} else {
			this.handshake.ifPresent(handshake -> {
				this.channel.writeAndFlush(handshake.withTarget(PacketTarget.HANDSHAKE)).addListener(CLOSE_ON_FAILURE);
				LOGGER.debug("Sent handshake {}", handshake.getClass().getSimpleName());
			});
		}
		
	}
	
	private void initializeUsingEvents() {
		INSTANCE.dispatch(OPEN, new OpenEvent(this.getUniqueId()));
		this.handshake.ifPresent(handshake -> {
			HandshakeEvent event = new HandshakeEvent(this.getUniqueId(), handshake);
			INSTANCE.dispatch(HANDSHAKE, event);
			if (!event.isCancelled()) {
				this.channel.writeAndFlush(event.getPacket().withTarget(PacketTarget.HANDSHAKE)).addListener(CLOSE_ON_FAILURE);
				LOGGER.debug("Sent handshake {}", event.getPacket().getClass().getSimpleName());
			}
		});
	}
	
	//region Netty overrides
	@Override
	public void channelActive(ChannelHandlerContext context) {
	
	}
	//endregion
}
