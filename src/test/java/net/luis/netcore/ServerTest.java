package net.luis.netcore;

import net.luis.netcore.network.ServerInstance;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.StringPacket;
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

public class ServerTest {
	
	private static final Logger LOGGER = LogManager.getLogger(ServerTest.class);
	
	public static void main(String[] args) {
		LoggingUtils.enableConsoleDebug();
		ServerInstance server = new ServerInstance(ServerTest::initializeConnection);
		server.open("localhost", 8080);
	}
	
	private static void initializeConnection(@NotNull Connection connection) {
		connection.registerListener(ServerTest::logPacket);
		connection.registerListener((packet, sender) -> sender.accept(new StringPacket("You sent " + packet).withTarget(4)));
		LOGGER.info("Server connection initialized");
	}
	
	private static void logPacket(Packet packet) {
		LOGGER.debug("Received packet: {}", packet);
	}
}
