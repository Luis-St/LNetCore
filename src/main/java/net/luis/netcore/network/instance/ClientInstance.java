package net.luis.netcore.network.instance;

import net.luis.netcore.connection.Connection;

import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ClientInstance {
	
	private final Runnable open;
	private final Supplier<Connection> connection;
	private final Supplier<Boolean> isOpen;
	private final Runnable close;
	
	ClientInstance(Runnable open, Supplier<Connection> connection, Supplier<Boolean> isOpen, Runnable close) {
		this.open = open;
		this.connection = connection;
		this.isOpen = isOpen;
		this.close = close;
	}
	
	public void open() {
		this.open.run();
	}
	
	public Connection getConnection() {
		return this.connection.get();
	}
	
	public boolean isOpen() {
		return this.isOpen.get();
	}
	
	public void close() {
		this.close.run();
	}
	
}
