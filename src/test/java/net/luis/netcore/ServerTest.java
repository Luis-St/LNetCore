package net.luis.netcore;

import net.luis.netcore.connection.Connection;
import net.luis.netcore.network.ServerInstance;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.StringPacket;
import net.luis.netcore.packet.listener.PacketListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ServerTest {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
	
	public static void main(String[] args) {
		ServerInstance server = new ServerInstance("localhost", 8080, ServerTest::initializeConnection);
		server.open();
	}
	
	@PacketListener(target = 1, priority = 2)
	private static void initializeConnection(@NotNull Connection connection) {
		connection.addListener(ServerTest::logPacket);
		connection.addListener(0, packet -> LOGGER.debug("Received packet {} for target 0", packet));
		connection.addListener(1, (conn, packet) -> conn.send(4, new StringPacket("You sent packet " + packet + " to target 1")));
		connection.addListener(new Listener());
		LOGGER.debug("Server connection initialized");
	}
	
	private static void logPacket(Packet packet) {
		LOGGER.debug("Received packet: {}", packet);
	}
	
	public static class Listener {
		
		@PacketListener
		public void emptyListener() {
		
		}
		
		@PacketListener
		public void singleListener(Packet packet) {
		
		}
		
		@PacketListener
		public void singleListener(Connection connection) {
		
		}
		
		@PacketListener
		public void doubleListener(Connection connection, Packet packet) {
		
		}
		
	}
	
}
