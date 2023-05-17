package net.luis.netcore.packet.impl.data;

import com.google.common.collect.Lists;
import net.luis.netcore.buffer.decode.Decodable;
import net.luis.netcore.buffer.encode.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ListPacket extends Packet implements Supplier<List<Object>> {
	
	private final List<Object> values;
	
	public <T extends Encodable & Decodable> ListPacket(List<T> values) {
		this.values = Lists.newArrayList(values);
	}
	
	public ListPacket(@NotNull FriendlyByteBuffer buffer) {
		this.values = buffer.readList(buffer::readUnsafe);
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeList(this.values, buffer::writeUnsafe);
	}
	
	@Override
	public List<Object> get() {
		return this.values;
	}
	
	@SuppressWarnings("unchecked")
	public <X> List<X> getAs() {
		return (List<X>) this.values;
	}
}
