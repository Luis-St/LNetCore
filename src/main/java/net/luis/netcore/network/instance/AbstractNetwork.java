package net.luis.netcore.network.instance;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.network.SimpleChannelInitializer;
import net.luis.utils.util.DefaultExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

abstract class AbstractNetwork {
	
	private EventLoopGroup group;
	
	protected final EventLoopGroup buildGroup(String nameFormat) {
		this.group = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat(nameFormat).setUncaughtExceptionHandler(new DefaultExceptionHandler()).build());
		return this.group;
	}
	
	public abstract void open(@NotNull String host, int port);
	
	public final boolean isOpen() {
		return this.group != null && !this.group.isShuttingDown();
	}
	
	public final void close() {
		this.group.shutdownGracefully();
		this.group = null;
	}
	
	static final class ServerNetwork extends AbstractNetwork {
		
		private final List<Connection> connections = Lists.newArrayList();
		
		@Override
		public void open(@NotNull String host, int port) {
			try {
				new ServerBootstrap().group(this.buildGroup("server connection #%d")).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInitializer(channel -> {
					Connection connection = new Connection(channel);
					this.connections.add(connection);
					return connection;
				})).localAddress(host, port).bind().syncUninterruptibly().channel();
			} catch (Exception e) {
				throw new RuntimeException("Fail to start server", e);
			}
		}
		
		public List<Connection> getConnections() {
			return this.connections;
		}
	}
	
	static final class ClientNetwork extends AbstractNetwork {
		
		private Connection connection;
		
		@Override
		public void open(@NotNull String host, int port) {
			try {
				new Connection(new Bootstrap().group(this.buildGroup("client connection")).channel(NioSocketChannel.class).handler(new SimpleChannelInitializer(channel -> {
					this.connection = new Connection(channel);
					return this.connection;
				})).connect(host, port).syncUninterruptibly().channel());
			} catch (Exception e) {
				throw new RuntimeException("Fail to start client", e);
			}
		}
		
		public Connection getConnection() {
			return this.connection;
		}
	}
	
}
