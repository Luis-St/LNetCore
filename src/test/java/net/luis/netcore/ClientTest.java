package net.luis.netcore;

import net.luis.netcore.connection.Connection;
import net.luis.netcore.network.ClientInstance;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.StringPacket;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;
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
		ClientInstance client = new ClientInstance("localhost", 8080, ClientTest::initializeConnection);
		client.open();
		while (true) {
			if (client.getConnection() == null) {
				continue;
			}
			client.getConnection().send(1, new StringPacket("Hello World!"));
			break;
		}
	}
	
	@PacketTarget(2)
	private static void initializeConnection(@NotNull Connection connection) {
		connection.addListener(packet -> LOGGER.debug("Received packet: {}", packet));
		connection.addListener(0, packet -> LOGGER.debug("Received packet {} for target 0", packet));
		connection.addListener(1, (conn, packet) -> conn.send(new StringPacket("You sent packet " + packet + " to target 1")));
		connection.addListener(new Listener());
		LOGGER.debug("Client connection initialized");
	}
	
	@PacketPriority(2)
	public static class Listener {
		
		private static final Logger LOGGER = LogManager.getLogger(ClientTest.Listener.class);
		
		@PacketListener
		@PacketPriority(10)
		public void emptyListener() {
			LOGGER.debug("Empty listener, called first");
		}
		
		@PacketListener
		@PacketPriority(9)
		public void singleListener(Packet packet) {
			LOGGER.debug("Single listener with packet, called second");
		}
		
		@PacketListener
		public void singleListener(Connection connection) {
			LOGGER.debug("Single listener with connection, called last");
		}
		
		@PacketListener
		@PacketPriority(8)
		public void doubleListener(Connection connection, Packet packet) {
			LOGGER.debug("Double listener with connection and packet, called third");
		}
		
		@PacketListener(StringPacket.class)
		public void doubleListener(Connection connection, String value) {
			LOGGER.debug("Double listener with connection and value, called last");
		}
		
		@PacketTarget(4)
		@PacketPriority(7)
		@PacketListener(StringPacket.class)
		public void doubleListener(Packet packet, String value) {
			LOGGER.debug("Double listener with packet and value, called fourth");
			LOGGER.info("Received StringPacket with value: {}", value);
		}
		
		@PacketPriority(3)
		@PacketListener(StringPacket.class)
		public void tripleListener(Connection connection, Packet packet, String value) {
			LOGGER.debug("Triple listener with connection, packet and value, called sixth");
		}
		
		@PacketPriority(6)
		@PacketListener(StringPacket.class)
		public void tripleListener(String value, Connection connection, Packet packet) {
			LOGGER.debug("Triple listener with value, connection and packet, called fifth");
		}
		
	}
	
}
