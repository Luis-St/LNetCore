package net.luis.netcore.network;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.network.connection.ConnectionInitializer;
import net.luis.netcore.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance extends AbstractNetworkInstance {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerInstance.class);
	
	private final List<Connection> connections = Lists.newArrayList();
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
			LOGGER.info("Starting server");
			new ServerBootstrap().group(this.buildGroup("server connection #%d")).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInitializer(channel -> {
				Connection connection = new Connection(channel);
				this.initializer.initialize(connection);
				this.connections.add(connection);
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
		this.connections.forEach(connection -> connection.send(packet));
	}
	
	public void send(UUID uniqueId, Packet packet) {
		this.connections.stream().filter(connection -> connection.getUniqueId().equals(uniqueId)).forEach(connection -> connection.send(packet));
	}
	
	@Override
	public void closeNow() {
		this.connections.forEach(Connection::close);
		this.connections.clear();
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
}
