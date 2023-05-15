package net.luis.netcore.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.packet.PacketDecoder;
import net.luis.netcore.packet.PacketEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Luis-St
 *
 */

public final class SimpleChannelInitializer extends ChannelInitializer<Channel> {
	
	private final Function<Channel, Connection> factory;
	
	public SimpleChannelInitializer(Function<Channel, Connection> factory) {
		this.factory = Objects.requireNonNull(factory, "Factory must not be null");
	}
	
	@Override
	protected void initChannel(@NotNull Channel channel) {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("splitter", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("decoder", new PacketDecoder());
		pipeline.addLast("prepender", new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("encoder", new PacketEncoder());
		pipeline.addLast("handler", this.factory.apply(channel));
	}
}
