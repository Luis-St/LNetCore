package net.luis.netcore;

import net.luis.netcore.connection.Connection;
import net.luis.netcore.network.ServerInstance;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.StringPacket;
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
		//LoggingUtils.enableConsoleDebug();
		ServerInstance server = new ServerInstance("localhost", 8080, ServerTest::initializeConnection);
		server.open();
	}
	
	private static void initializeConnection(@NotNull Connection connection) {
		connection.addListener(ServerTest::logPacket);
		connection.addListener(packet -> LOGGER.debug("Received packet {}", packet));
		connection.addListener((conn, packet) -> conn.send(4, new StringPacket("You sent " + packet)));
		LOGGER.info("Server connection initialized");
	}
	
	private static void logPacket(Packet packet) {
		LOGGER.debug("Received packet: {}", packet);
	}
}
