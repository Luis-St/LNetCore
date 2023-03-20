package net.luis.netcore.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.luis.netcore.connection.Connection;

/**
 *
 * @author Luis-St
 *
 */

public class ClientInstance extends AbstractNetworkInstance {
	
	private Connection connection;
	
	public ClientInstance(String host, int port) {
		super(host, port);
	}
	
	@Override
	public void open() {
		try {
			new Connection(new Bootstrap().group(this.buildGroup("client connection")).channel(NioSocketChannel.class).handler(new SimpleChannelInitializer(channel -> {
				this.connection = new Connection(channel);
				return this.connection;
			})).connect(this.getHost(), this.getPort()).syncUninterruptibly().channel());
		} catch (Exception e) {
			throw new RuntimeException("Fail to start client", e);
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
}
