package net.luis.netcore.connection;

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
