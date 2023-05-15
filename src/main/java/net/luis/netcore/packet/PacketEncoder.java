package net.luis.netcore.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.exception.SkipPacketException;
import net.luis.netcore.packet.registry.PacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * @author Luis-St
 *
 */

public final class PacketEncoder extends MessageToByteEncoder<Packet> {
	
	private static final Logger LOGGER = LogManager.getLogger(PacketEncoder.class);
	
	@Override
	protected void encode(@NotNull ChannelHandlerContext context, @NotNull Packet packet, @NotNull ByteBuf output) throws Exception {
		int id = PacketRegistry.getId(packet.getClass());
		if (id == -1) {
			LOGGER.error("Can not encode packet {}", packet);
			throw new IOException("Can not encode packet " + packet + " because it is not registered");
		} else {
			FriendlyByteBuffer buffer = new FriendlyByteBuffer(output);
			buffer.writeInt(id);
			buffer.write(packet.getTarget());
			try {
				int startSize = buffer.writerIndex();
				packet.encode(buffer);
				int size = buffer.writerIndex() - startSize;
				if (size > 8000000) {
					LOGGER.error("Packet {} is too big", packet.getClass().getSimpleName());
					throw new IllegalArgumentException("Packet " + packet.getClass().getSimpleName() + " is too big, it should be less than 8MB, but it is " + size);
				}
			} catch (Exception e) {
				if (packet.skippable()) {
					LOGGER.warn("Fail to encode packet {} with id {}", packet, id);
					throw new SkipPacketException(e);
				} else {
					LOGGER.error("Fail to encode packet {} with id {}, since it is not skippable", packet, id);
					throw new IOException("Fail to encode packet " + packet, e);
				}
			}
			
		}
	}
	
	@Override
	public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
		LOGGER.error("Caught an exception while encoding a packet", cause);
	}
}
