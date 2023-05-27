package net.luis.netcore.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.packet.impl.internal.SyncServerDataPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

import static io.netty.channel.ChannelFutureListener.*;

/**
 *
 * @author Luis-St
 *
 */

@ApiStatus.Internal
public final class ServerConnection extends Connection {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerConnection.class);
	
	public ServerConnection(Channel channel, ConnectionInitializer initializer) {
		super(UUID.randomUUID(), channel, initializer);
	}
	
	//region Netty overrides
	@Override
	public void channelActive(ChannelHandlerContext context) {
		this.channel.writeAndFlush(new SyncServerDataPacket(this.getUniqueId())).addListener(CLOSE_ON_FAILURE);
		LOGGER.debug("Synced server data");
	}
	//endregion
}
