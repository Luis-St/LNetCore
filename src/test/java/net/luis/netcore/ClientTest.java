package net.luis.netcore;

import net.luis.netcore.connection.Connection;
import net.luis.netcore.connection.ConnectionContext;
import net.luis.netcore.instance.ClientInstance;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.CloseServerPacket;
import net.luis.netcore.packet.impl.value.IntegerPacket;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketTarget;
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
		ClientInstance client = new ClientInstance(ClientTest::initializeConnection);
		client.handshake(new IntegerPacket(10));
		client.open("localhost", 8080);
		//client.closeOn(ClosingEvent.closeAfterReceived(StringPacket.class));
	}
	
	private static void initializeConnection(@NotNull Connection connection) {
		connection.registerListener(new Listener());
		LOGGER.info("Initialized client connection {}", connection.getUniqueId());
	}
	
	public static class Listener implements PacketListener {
		
		private static final Logger LOGGER = LogManager.getLogger(ClientTest.Listener.class);
		
		@Override
		public void initialize(Connection connection) {
			connection.builder().target(PacketTarget.of(4)).listener(this::doubleListener).register();
		}
		
		public void doubleListener(Packet packet, ConnectionContext ctx) {
			ctx.sendPacket(new CloseServerPacket());
		}
	}
}
