package net.luis.netcore.network;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.luis.netcore.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance extends AbstractNetworkInstance {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerInstance.class);
	
	private final List<Connection> connections = Lists.newArrayList();
	private final Consumer<Connection> initializeConnection;
	
	public ServerInstance(int port) {
		this("localhost", port);
	}
	
	public ServerInstance(String host, int port) {
		this(host, port, (connection) -> { });
	}
	
	public ServerInstance(String host, int port, Consumer<Connection> initializeConnection) {
		super(host, port);
		this.initializeConnection = initializeConnection;
	}
	
	@Override
	public void open() {
		try {
			LOGGER.info("Starting server");
			new ServerBootstrap().group(this.buildGroup("server connection #%d")).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInitializer(channel -> {
				Connection connection = new Connection(channel);
				this.initializeConnection.accept(connection);
				this.connections.add(connection);
				LOGGER.debug("Client connected with address {} using connection {}", channel.remoteAddress().toString().replace("/", ""), connection.getUniqueId());
				return connection;
			})).localAddress(this.getHost(), this.getPort()).bind().syncUninterruptibly().channel();
			LOGGER.info("Server successfully started on {}:{}", this.getHost(), this.getPort());
		} catch (Exception e) {
			throw new RuntimeException("Fail to start server", e);
		}
	}
	
	public @NotNull List<Connection> getConnections() {
		return this.connections;
	}
	
	@Override
	public void close() {
		this.connections.forEach(Connection::close);
		this.connections.clear();
		super.close();
		LOGGER.info("Server closed");
	}
	
}
