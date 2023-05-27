package net.luis.netcore.instance;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.luis.netcore.connection.*;
import net.luis.netcore.connection.channel.SimpleChannelInitializer;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.CloseConnectionPacket;
import net.luis.utils.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance extends AbstractNetworkInstance {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerInstance.class);
	
	private final Map<UUID, InternalConnection> internalConnections = Maps.newHashMap();
	private final Map<UUID, Connection> connections = Maps.newHashMap();
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
				Connection connection = new ServerConnection(channel, this.initializer);
				InternalConnection internalConnection = new InternalConnection(this, connection, channel);
				this.internalConnections.put(internalConnection.getUniqueId(), internalConnection);
				this.connections.put(connection.getUniqueId(), connection);
				LOGGER.info("Client connected with address {} using connection {}", channel.remoteAddress().toString().replace("/", ""), connection.getUniqueId());
				LOGGER.debug("Internal connection {} created", internalConnection.getUniqueId());
				return Pair.of(internalConnection, connection);
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
		this.connections.get(uniqueId).send(packet);
	}
	
	@Override
	public void closeNow() {
		LOGGER.debug("Closing server");
		this.connections.keySet().forEach(this::closeConnection);
		LOGGER.info("Closed connections");
		super.closeNow();
		LOGGER.info("Server closed");
	}
	
	public void closeConnection(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "Unique id must not be null");
		this.internalConnections.get(uniqueId).send(new CloseConnectionPacket());
		this.closeConnectionInternal(uniqueId);
	}
	
	void closeConnectionInternal(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "Unique id must not be null");
		LOGGER.debug("Try closing connection {}", uniqueId);
		this.internalConnections.remove(uniqueId).close();
		this.connections.remove(uniqueId).close();
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
}
