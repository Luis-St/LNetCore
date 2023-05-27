package net.luis.netcore.connection.channel;

import io.netty.channel.*;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import net.luis.netcore.connection.Connection;
import net.luis.netcore.connection.util.ConnectionFactory;
import net.luis.netcore.instance.InternalConnection;
import net.luis.netcore.packet.PacketDecoder;
import net.luis.netcore.packet.PacketEncoder;
import net.luis.utils.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public final class SimpleChannelInitializer extends ChannelInitializer<Channel> {
	
	private final ConnectionFactory factory;
	
	public SimpleChannelInitializer(ConnectionFactory factory) {
		this.factory = Objects.requireNonNull(factory, "Factory must not be null");
	}
	
	@Override
	protected void initChannel(@NotNull Channel channel) {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("splitter", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("decoder", new PacketDecoder());
		pipeline.addLast("prepender", new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("encoder", new PacketEncoder());
		Pair<InternalConnection, Connection> pair = this.factory.create(channel);
		pipeline.addLast("internal", Objects.requireNonNull(pair.getFirst(), "Internal handler must not be null"));
		pipeline.addLast("handler", Objects.requireNonNull(pair.getSecond(), "Handler must not be null"));
	}
}
