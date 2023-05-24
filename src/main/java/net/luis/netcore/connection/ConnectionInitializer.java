package net.luis.netcore.connection;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionInitializer {
	
	void initialize(Connection connection);
}
