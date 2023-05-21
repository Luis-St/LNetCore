package net.luis.netcore;

import net.luis.netcore.network.instance.ClientInstance;
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
		ClientInstance client = new ClientInstance(ClientTest::initializeConnection);
		client.handshake(new IntegerPacket(10));
		client.open("localhost", 8080);
	}
	
	private static void initializeConnection(@NotNull Connection connection) {
		connection.builder().target(PacketTarget.ANY).priority(PacketPriority.HIGHEST).listener(packet -> LOGGER.debug("Received packet: {}", packet)).register();
		connection.registerListener(new Listener());
		LOGGER.info("Client connection initialized");
	}
	
	public static class Listener implements PacketListener {
		
		private static final Logger LOGGER = LogManager.getLogger(ClientTest.Listener.class);
		
		@Override
		public void initialize(Connection connection) {
			connection.builder().target(PacketTarget.ANY).priority(PacketPriority.HIGH).listener(this::emptyListener).register();
			connection.builder().listener(this::doubleListener).register();
			connection.builder().priority(2).listener(StringPacket.class, (packet, sender) -> this.doubleListener(packet, packet.get())).register();
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
