package net.luis.netcore.network.instance;

import net.luis.netcore.connection.Connection;

import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ServerInstance {
	
	private final Runnable open;
	private final Supplier<List<Connection>> connections;
	private final Supplier<Boolean> isOpen;
	private final Runnable close;
	
	ServerInstance(Runnable open, Supplier<List<Connection>> connections, Supplier<Boolean> isOpen, Runnable close) {
		this.open = open;
		this.connections = connections;
		this.isOpen = isOpen;
		this.close = close;
	}
	
	public void open() {
		this.open.run();
	}
	
	public List<Connection> getConnections() {
		return this.connections.get();
	}
	
	public boolean isOpen() {
		return this.isOpen.get();
	}
	
	public void close() {
		this.close.run();
	}
	
}
