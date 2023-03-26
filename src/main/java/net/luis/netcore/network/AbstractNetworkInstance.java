package net.luis.netcore.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.luis.utils.util.DefaultExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

abstract class AbstractNetworkInstance implements NetworkInstance {
	
	private final String host;
	private final int port;
	private EventLoopGroup group;
	
	public AbstractNetworkInstance(String host, int port) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Host cannot be null or empty");
		}
		this.host = "localhost".equalsIgnoreCase(host) ? "127.0.0.1" : host;
		this.port = port;
	}
	
	protected @NotNull String getHost() {
		return this.host;
	}
	
	protected int getPort() {
		return this.port;
	}
	
	protected final @NotNull EventLoopGroup buildGroup(String nameFormat) {
		this.group = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat(nameFormat).setUncaughtExceptionHandler(new DefaultExceptionHandler()).build());
		return this.group;
	}
	
	@Override
	public final boolean isOpen() {
		return this.group != null && !this.group.isShuttingDown();
	}
	
	@Override
	public void close() {
		this.group.shutdownGracefully();
		this.group = null;
	}
	
}
