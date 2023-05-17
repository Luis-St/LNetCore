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
	
	protected boolean initialized = false;
	private EventLoopGroup group;
	private String host;
	private int port;
	
	protected final void initialize(String host, int port) {
		Objects.requireNonNull(host, "Host must not be null");
		if (host.isEmpty()) {
			throw new IllegalArgumentException("Host must not be empty");
		}
		this.host = "localhost".equalsIgnoreCase(host) ? "127.0.0.1" : host;
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port must be between 0 and 65535");
		}
		this.port = port;
	}
	
	public final @NotNull String getHost() {
		return Objects.requireNonNull(this.host, "Host has not been initialized");
	}
	
	public final int getPort() {
		return this.port;
	}
	
	protected final @NotNull EventLoopGroup buildGroup(String name) {
		return this.group = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat(name).setUncaughtExceptionHandler(new DefaultExceptionHandler()).build());
	}
	
	@Override
	public final boolean isOpen() {
		return this.initialized && this.group != null && !this.group.isShuttingDown();
	}
	
	@Override
	public void closeNow() {
		this.group.shutdownGracefully();
		this.group = null;
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractNetworkInstance that)) return false;
		
		if (this.port != that.port) return false;
		return this.host.equals(that.host);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.host, this.port);
	}
	//endregion
}
