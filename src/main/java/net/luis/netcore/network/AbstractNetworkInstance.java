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
	
	/*
	 * TODO:
	 *  - Add closeOn(Event)
	 *  - Add closeOnDisconnect()
	 *  - Add closeOnResponse()
	 *  - Add closeAfterPackets(int)
	 *  - Add closeOnReceive(Class<? extends Packet>)
	 *  - Add closeOnTimeout(int)
	 *  - Implement these functions using a NetworkInstanceHandler (can handle multiple Events)
	 *  - Add EventSystem for NetworkInstance (ResponseEvent, TimeoutEvent, PacketEvent,
	 *  to Client and Server
	 */
	
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
	
	protected final @NotNull String getHost() {
		return this.host;
	}
	
	protected final int getPort() {
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
}
