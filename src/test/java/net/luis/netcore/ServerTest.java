package net.luis.netcore;

import net.luis.netcore.connection.ConnectionRegistry;
import net.luis.netcore.connection.ConnectionSettings;
import net.luis.netcore.instance.ServerInstance;
import net.luis.netcore.packet.impl.value.StringPacket;
import net.luis.utils.logging.LoggingUtils;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ServerTest {
	
	private static final Logger LOGGER;
	
	public static void main(String[] args) {
		LoggingUtils.enableConsole(Level.DEBUG);
		ServerInstance server = new ServerInstance(ServerTest::initializeConnection);
		server.open("localhost", 8080);
	}
	
	private static void initializeConnection(@NotNull ConnectionRegistry registry, @NotNull ConnectionSettings settings) {
		registry.builder().listener((packet, ctx) -> ctx.sendPacket(new StringPacket("You sent " + packet).withTarget(4))).register();
		LOGGER.info("Initialized server connection {}", registry.getUniqueId());
	}
	
	static {
		LoggingUtils.initialize();
		LOGGER = LogManager.getLogger(ServerTest.class);
	}
}
