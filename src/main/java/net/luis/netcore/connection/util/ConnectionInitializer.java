package net.luis.netcore.connection.util;

import net.luis.netcore.connection.Connection;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionInitializer {
	
	void initialize(Connection connection);
}
