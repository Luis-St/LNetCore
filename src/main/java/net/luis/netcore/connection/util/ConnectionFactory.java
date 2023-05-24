package net.luis.netcore.connection.util;

import io.netty.channel.Channel;
import net.luis.netcore.connection.Connection;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionFactory {
	
	Connection create(Channel channel);
}
