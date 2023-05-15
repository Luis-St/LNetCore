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
	protected boolean initialized = false;
	private EventLoopGroup group;
	
	public AbstractNetworkInstance(String host, int port) {
		Objects.requireNonNull(host, "Host must not be null");
		if (host.isEmpty()) {
			throw new IllegalArgumentException("Host must not be empty");
		}
		this.host = "localhost".equalsIgnoreCase(host) ? "127.0.0.1" : host;
		this.port = port;
	}
	
	protected final @NotNull String getHost() {
		return this.host;
	}
	
	protected final int getPort() {
		return this.port;
	}
	
	protected final @NotNull EventLoopGroup buildGroup(String name) {
		this.group = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat(name).setUncaughtExceptionHandler(new DefaultExceptionHandler()).build());
		return this.group;
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
