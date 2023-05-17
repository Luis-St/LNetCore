package net.luis.netcore.network.connection;

import io.netty.channel.Channel;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionFactory {
	
	Connection create(Channel channel);
}
