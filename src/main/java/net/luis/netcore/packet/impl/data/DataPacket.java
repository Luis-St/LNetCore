package net.luis.netcore.packet.impl.data;

import net.luis.netcore.buffer.decode.Decodable;
import net.luis.netcore.buffer.encode.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class DataPacket extends Packet implements Supplier<Object[]> {
	
	private final Object[] values;
	
	@SafeVarargs
	public <T extends Encodable & Decodable> DataPacket(T... values) {
		this.values = values;
	}
	
	public DataPacket(@NotNull FriendlyByteBuffer buffer) {
		this.values = buffer.readList(buffer::readUnsafe).toArray();
	}
	
	@Override
	public void encode(@NotNull FriendlyByteBuffer buffer) {
		buffer.writeList(Arrays.asList(this.values), buffer::writeUnsafe);
	}
	
	@Override
	public Object[] get() {
		return this.values;
	}
	
	@SuppressWarnings("unchecked")
	public <X> X[] getAs() {
		return (X[]) this.values;
	}
}
