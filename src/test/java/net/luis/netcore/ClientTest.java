package net.luis.netcore;

import net.luis.netcore.network.ClientInstance;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.IntegerPacket;
import net.luis.netcore.packet.impl.value.StringPacket;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;
import net.luis.utils.logging.LoggingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
		client.handshake(new IntegerPacket(10)).open();
	}
	
	private static void initializeConnection(@NotNull Connection connection) {
		connection.registerListener(PacketTarget.ANY, PacketPriority.HIGHEST, packet -> LOGGER.debug("Received packet: {}", packet));
		connection.registerListener(new Listener());
		LOGGER.info("Client connection initialized");
	}
	
	public static class Listener implements PacketListener {
		
		private static final Logger LOGGER = LogManager.getLogger(ClientTest.Listener.class);
		
		@Override
		public void initialize(Connection connection) {
			connection.registerListener(PacketTarget.ANY, PacketPriority.HIGH, this::emptyListener);
			connection.registerListener(this::doubleListener);
			connection.registerListener(StringPacket.class, (packet, sender) -> this.doubleListener(packet, packet.get()));
		}
		
		public void emptyListener() {
			LOGGER.debug("Empty listener");
		}
		
		public void doubleListener(Packet packet, Consumer<Packet> sender) {
			LOGGER.debug("Double listener with connection and packet");
		}
		
		public void doubleListener(Packet packet, String value) {
			LOGGER.debug("Double listener with packet and value");
			LOGGER.info("Received StringPacket with value: {}", value);
		}
	}
}
