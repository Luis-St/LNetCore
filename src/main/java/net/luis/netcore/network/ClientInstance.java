package net.luis.netcore.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.luis.netcore.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ClientInstance extends AbstractNetworkInstance {
	
	private static final Logger LOGGER = LogManager.getLogger(ClientInstance.class);
	
	private final Consumer<Connection> initializeConnection;
	private Connection connection;
	
	public ClientInstance(String host, int port) {
		this(host, port, (connection) -> {
		});
	}
	
	public ClientInstance(String host, int port, Consumer<Connection> initializeConnection) {
		super(host, port);
		this.initializeConnection = initializeConnection;
	}
	
	@Override
	public void open() {
		try {
			LOGGER.info("Starting client");
			new Bootstrap().group(this.buildGroup("client connection")).channel(NioSocketChannel.class).handler(new SimpleChannelInitializer(channel -> {
				this.connection = new Connection(channel);
				this.initializeConnection.accept(this.connection);
				return this.connection;
			})).connect(this.getHost(), this.getPort()).syncUninterruptibly().channel();
			LOGGER.info("Client successfully started on {}:{}", this.getHost(), this.getPort());
		} catch (Exception e) {
			throw new RuntimeException("Fail to start client", e);
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	@Override
	public void closeNow() {
		this.connection.close();
		this.connection = null;
		super.closeNow();
		LOGGER.info("Client closed");
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClientInstance that)) return false;
		if (!super.equals(o)) return false;
		
		return Objects.equals(this.connection, that.connection);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.connection);
	}
	
	@Override
	public String toString() {
		return "ClientInstance " + this.connection.getUniqueId();
	}
	//endregion
}
