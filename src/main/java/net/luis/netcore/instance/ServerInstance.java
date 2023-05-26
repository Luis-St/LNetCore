package net.luis.netcore.instance;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.connection.ServerConnection;
import net.luis.netcore.connection.channel.SimpleChannelInitializer;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.CloseConnectionPacket;
import net.luis.netcore.packet.impl.internal.CloseServerPacket;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.*;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance extends AbstractNetworkInstance {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerInstance.class);
	
	private final Map<UUID, ServerConnection> connections = Maps.newHashMap();
	private final ConnectionInitializer initializer;
	
	public ServerInstance() {
		this((connection) -> {});
	}
	
	public ServerInstance(ConnectionInitializer initializer) {
		this.initializer = initializer;
		this.initialized = true;
	}
	
	@Override
	public void open(String host, int port) {
		this.initialize(host, port);
		try {
			LOGGER.debug("Starting server");
			new ServerBootstrap().group(this.buildGroup("server connection #%d")).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInitializer(channel -> {
				ServerConnection connection = new ServerConnection(channel, this.initializer);
				connection.registerListener(new InternalListener(this, channel.remoteAddress(), connection.getUniqueId()));
				this.connections.put(connection.getUniqueId(), connection);
				LOGGER.debug("Client connected with address {} using connection {}", channel.remoteAddress().toString().replace("/", ""), connection.getUniqueId());
				return connection;
			})).localAddress(this.getHost(), this.getPort()).bind().syncUninterruptibly().channel();
			LOGGER.info("Server successfully started on {}:{}", this.getHost(), this.getPort());
		} catch (Exception e) {
			throw new RuntimeException("Fail to start server", e);
		}
	}
	
	@Override
	public void send(Packet packet) {
		this.connections.values().forEach(connection -> connection.send(packet));
	}
	
	public void send(UUID uniqueId, Packet packet) {
		Connection connection = this.connections.get(uniqueId);
		if (connection != null) {
			connection.send(packet);
		}
	}
	
	@Override
	public void closeNow() {
		LOGGER.debug("Closing server");
		this.connections.values().forEach(Connection::close);
		this.connections.clear();
		LOGGER.info("Closed connections");
		super.closeNow();
		LOGGER.info("Server closed");
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ServerInstance that)) return false;
		if (!super.equals(o)) return false;
		
		return this.connections.equals(that.connections);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.connections);
	}
	
	@Override
	public String toString() {
		return "ServerInstance";
	}
	//endregion
	
	//region Internal listener
	private static record InternalListener(ServerInstance instance, SocketAddress address, UUID uniqueId) implements PacketListener {
		
		@Override
		public void initialize(Connection connection) {
			connection.builder().listener(CloseConnectionPacket.class, (packet) -> this.closeConnection()).register();
			connection.builder().listener(CloseServerPacket.class, (packet) -> this.closeServer()).register();
		}
		
		private void closeConnection() {
			Connection connection = this.instance.connections.get(this.uniqueId);
			connection.close();
			this.instance.connections.remove(this.uniqueId);
			LOGGER.debug("Client disconnected with address {} using connection {}", this.address.toString().replace("/", ""), this.uniqueId);
		}
		
		private void closeServer() {
			for (Connection connection : this.instance.connections.values()) {
				connection.send(new CloseConnectionPacket().withTarget(PacketTarget.INTERNAL));
				connection.close();
			}
			this.instance.closeNow();
		}
	}
	//endregion
}
