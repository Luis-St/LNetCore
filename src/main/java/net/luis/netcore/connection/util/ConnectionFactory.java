package net.luis.netcore.connection.util;

import io.netty.channel.Channel;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.instance.InternalConnection;
import net.luis.utils.util.Pair;

/**
 *
 * @author Luis
 *
 */

@FunctionalInterface
public interface ConnectionFactory {
	
	Pair<InternalConnection, Connection> create(Channel channel);
}
