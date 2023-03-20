package net.luis.netcore.network;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.luis.netcore.connection.Connection;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance extends AbstractNetworkInstance {
	
	private final List<Connection> connections = Lists.newArrayList();
	
	public ServerInstance(String host, int port) {
		super(host, port);
	}
	
	@Override
	public void open() {
		try {
			new ServerBootstrap().group(this.buildGroup("server connection #%d")).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInitializer(channel -> {
				Connection connection = new Connection(channel);
				this.connections.add(connection);
				return connection;
			})).localAddress(this.getHost(), this.getPort()).bind().syncUninterruptibly().channel();
		} catch (Exception e) {
			throw new RuntimeException("Fail to start server", e);
		}
	}
	
	public List<Connection> getConnections() {
		return this.connections;
	}
}
