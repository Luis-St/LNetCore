package net.luis.netcore.network.connection;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionInitializer {
	
	void initialize(Connection connection);
}
