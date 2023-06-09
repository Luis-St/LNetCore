package net.luis.netcore.packet.impl.data;

import com.google.common.collect.Maps;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.buffer.decode.Decodable;
import net.luis.netcore.buffer.encode.Encodable;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.wrapper.PacketGetter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class MapPacket extends Packet implements Supplier<Map<Object, Object>> {
	
	private final Map<Object, Object> values;
	
	public <K extends Encodable & Decodable, V extends Encodable & Decodable> MapPacket(Map<K, V> values) {
		this.values = Maps.newHashMap(values);
	}
	
	public MapPacket(@NotNull FriendlyByteBuffer buffer) {
		this.values = buffer.readMap(buffer::readUnsafe, buffer::readUnsafe);
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeMap(this.values, buffer::writeUnsafe, buffer::writeUnsafe);
	}
	
	@Override
	@PacketGetter("values")
	public @NotNull Map<Object, Object> get() {
		return this.values;
	}
	
	@SuppressWarnings("unchecked")
	public <X, Y> Map<X, Y> getAs() {
		return (Map<X, Y>) this.values;
	}
}
