package net.luis.netcore;

import net.luis.netcore.connection.Connection;
import net.luis.netcore.network.ClientInstance;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.IntegerPacket;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.utils.logging.LoggingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ClientTest {
	
	private static final Logger LOGGER = LogManager.getLogger(ClientTest.class);
	
	public static void main(String[] args) {
		LoggingUtils.enableConsoleDebug();
		ClientInstance client = new ClientInstance("localhost", 8080, ClientTest::initializeConnection);
		client.open();
		while (true) {
			if (client.getConnection() == null) {
				continue;
			}
			client.getConnection().send(1, new IntegerPacket(10));
			break;
		}
	}
	
	@PacketPriority(priority = 11)
	private static void initializeConnection(@NotNull Connection connection) {
		connection.addListener(packet -> LOGGER.debug("Received packet: {}", packet));
		connection.addListener(packet -> LOGGER.debug("Received packet {} for target", packet));
		connection.addListener(new Listener());
		LOGGER.info("Client connection initialized");
	}
	
	@PacketPriority(priority = 99)
	public static class Listener {
		
		private static final Logger LOGGER = LogManager.getLogger(ClientTest.Listener.class);
		
		@PacketPriority(priority = 10)
		public void emptyListener() {
			LOGGER.debug("Empty listener");
		}
		
		public void singleListener(Integer target) {
			LOGGER.debug("Single listener with target {}", target);
		}
		
		public void doubleListener(Connection connection, Packet packet) {
			LOGGER.debug("Double listener with connection and packet");
		}
		
		public void doubleListener(Connection connection, String value) {
			LOGGER.debug("Double listener with connection and value");
		}
		
		public void doubleListener(Packet packet, String value) {
			LOGGER.debug("Double listener with packet and value");
			LOGGER.info("Received StringPacket with value: {}", value);
		}
		
		@PacketPriority(priority = 9)
		public void tripleListener(Connection connection, Packet packet, String value) {
			LOGGER.debug("Triple listener with connection, packet and value");
		}
		
		@PacketPriority(priority = 8)
		public void tripleListener(String value, Connection connection, Packet packet) {
			LOGGER.debug("Triple listener with value, connection and packet");
		}
	}
}
