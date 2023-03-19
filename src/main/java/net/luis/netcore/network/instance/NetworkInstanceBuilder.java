package net.luis.netcore.network.instance;

import net.luis.netcore.network.instance.AbstractNetwork.ClientNetwork;
import net.luis.netcore.network.instance.AbstractNetwork.ServerNetwork;

/**
 *
 * @author Luis-St
 *
 */

public class NetworkInstanceBuilder {
	
	public static ClientBuilder client(String host, int port) {
		return new ClientBuilder(host, port);
	}
	
	public static ServerBuilder server(String host, int port) {
		return new ServerBuilder(host, port);
	}
	
	public static class ClientBuilder {
		
		private final ClientNetwork client = new ClientNetwork();
		private final String host;
		private final int port;
		
		public ClientBuilder(String host, int port) {
			this.host = host;
			this.port = port;
		}
		
		public ClientInstance build() {
			return new ClientInstance(() -> this.client.open(this.host, this.port), this.client::getConnection, this.client::isOpen, this.client::close);
		}
		
	}
	
	public static class ServerBuilder {
		
		private final ServerNetwork server = new ServerNetwork();
		private final String host;
		private final int port;
		
		public ServerBuilder(String host, int port) {
			this.host = host;
			this.port = port;
		}
		
		public ServerInstance build() {
			return new ServerInstance(() -> this.server.open(this.host, this.port), this.server::getConnections, this.server::isOpen, this.server::close);
		}
		
	}
	
}
