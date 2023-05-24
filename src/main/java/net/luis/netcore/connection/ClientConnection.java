package net.luis.netcore.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.luis.netcore.connection.event.impl.HandshakeEvent;
import net.luis.netcore.connection.event.impl.OpenEvent;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.SyncServerDataPacket;
import net.luis.netcore.packet.listener.PacketTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static io.netty.channel.ChannelFutureListener.*;
import static net.luis.netcore.connection.event.ConnectionEventManager.*;
import static net.luis.netcore.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public final class ClientConnection extends Connection {
	
	private static final Logger LOGGER = LogManager.getLogger(ClientConnection.class);
	
	public ClientConnection(Channel channel, Optional<Packet> handshake) {
		super(channel, handshake);
	}
	
	//region Netty overrides
	@Override
	public void channelActive(ChannelHandlerContext context) {
	
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		if (packet instanceof SyncServerDataPacket serverData) {
			LOGGER.debug("Received server data");
			this.setUniqueId(serverData.getUniqueId());
			INSTANCE.dispatch(OPEN, new OpenEvent(this.getUniqueId()));
			this.handshake.filter(handshake -> !handshake.isInternal()).ifPresent(handshake -> {
				HandshakeEvent event = new HandshakeEvent(this.getUniqueId(), handshake);
				INSTANCE.dispatch(HANDSHAKE, event);
				if (!event.isCancelled()) {
					this.channel.writeAndFlush(event.getPacket().withTarget(PacketTarget.HANDSHAKE)).addListener(CLOSE_ON_FAILURE);
					LOGGER.debug("Sent handshake {}", event.getPacket().getClass().getSimpleName());
				}
			});
		} else {
			super.channelRead0(ctx, packet);
		}
	}
	//endregion
}
