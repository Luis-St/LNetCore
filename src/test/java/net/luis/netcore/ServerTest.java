package net.luis.netcore;

import net.luis.netcore.network.instance.ServerInstance;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.value.StringPacket;
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
		connection.builder().listener((packet, ctx) -> ctx.sendPacket(new StringPacket("You sent " + packet).withTarget(4))).register();
		LOGGER.info("Initialized server connection {}", connection.getUniqueId());
	}
}
