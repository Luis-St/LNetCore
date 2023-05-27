package net.luis.netcore.connection.util;

import net.luis.netcore.connection.ConnectionRegistry;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionInitializer {
	
	void initialize(ConnectionRegistry registry, ConnectionSettings settings);
}
