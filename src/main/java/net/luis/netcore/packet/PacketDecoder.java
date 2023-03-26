package net.luis.netcore.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.registry.PacketRegistry;
import net.luis.utils.util.reflection.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public class PacketDecoder extends ByteToMessageDecoder {
	
	private static final Logger LOGGER = LogManager.getLogger(PacketDecoder.class);
	
	@Override
	protected void decode(@NotNull ChannelHandlerContext context, @NotNull ByteBuf input, @NotNull List<Object> output) throws Exception {
		int i = input.readableBytes();
		if (i != 0) {
			FriendlyByteBuffer buffer = new FriendlyByteBuffer(input);
			int id = buffer.readInt();
			int target = buffer.readInt();
			Packet packet = PacketRegistry.getPacket(id, buffer);
			if (packet == null) {
				LOGGER.error("Failed to get packet for id {}", id);
				throw new IllegalStateException("Failed to get packet for id " + id);
			} else {
				int readableBytes = buffer.readableBytes();
				if (readableBytes > 0) {
					if (packet.skippable()) {
						LOGGER.warn("Packet was too big than expected, found {} extra bytes while reading packet {} with id {}", readableBytes, packet, id);
						throw new SkipPacketException();
					} else {
						throw new IOException("Packet was too big than expected, found " + readableBytes + " extra bytes while reading packet " + packet + " with id " + id);
					}
				} else {
					ReflectionHelper.set(Packet.class, "target", packet, target);
					output.add(packet);
				}
			}
		}
	}
	
	@Override
	public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
		LOGGER.error("Caught an exception while decoding a packet", cause);
	}
	
}
