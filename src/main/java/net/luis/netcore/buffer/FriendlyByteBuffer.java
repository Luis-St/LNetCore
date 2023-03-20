package net.luis.netcore.buffer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.luis.utils.util.Utils;
import net.luis.utils.util.reflection.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class FriendlyByteBuffer {
	
	private final ByteBuf buffer;
	
	public FriendlyByteBuffer() {
		this(Unpooled.buffer());
	}
	
	public FriendlyByteBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}
	
	public int readableBytes() {
		return this.buffer.readableBytes();
	}
	
	public int writerIndex() {
		return this.buffer.writerIndex();
	}
	
	//region Write primitives
	public void writeInt(int value) {
		this.buffer.writeInt(value);
	}
	
	public void writeLong(long value) {
		this.buffer.writeLong(value);
	}
	
	public void writeDouble(double value) {
		this.buffer.writeDouble(value);
	}
	
	public void writeBoolean(boolean value) {
		this.buffer.writeBoolean(value);
	}
	
	public <T extends Enum<T>> void writeEnum(@NotNull T value) {
		this.writeInt(value.ordinal());
	}
	//endregion
	
	//region Read primitives
	public int readInt() {
		return this.buffer.readInt();
	}
	
	public long readLong() {
		return this.buffer.readLong();
	}
	
	public double readDouble() {
		return this.buffer.readDouble();
	}
	
	public boolean readBoolean() {
		return this.buffer.readBoolean();
	}
	
	public <T extends Enum<T>> @NotNull T readEnum(@NotNull Class<T> clazz) {
		int ordinal = this.readInt();
		return clazz.getEnumConstants()[ordinal];
	}
	//endregion
	
	//region Write built-in types
	public void writeString(@NotNull String value) {
		this.buffer.writeInt(value.length());
		this.buffer.writeCharSequence(value, StandardCharsets.UTF_8);
	}
	
	public void writeUUID(@NotNull UUID value) {
		this.writeLong(value.getMostSignificantBits());
		this.writeLong(value.getLeastSignificantBits());
	}
	
	public <T> void writeList(@NotNull List<T> list, @NotNull Consumer<T> encoder) {
		this.writeInt(list.size());
		for (T t : list) {
			encoder.accept(t);
		}
	}
	
	public <K, V> void writeMap(@NotNull Map<K, V> map, @NotNull Consumer<K> keyEncoder, @NotNull Consumer<V> valueEncoder) {
		this.writeInt(map.size());
		for (Map.Entry<K, V> entry : map.entrySet()) {
			keyEncoder.accept(entry.getKey());
			valueEncoder.accept(entry.getValue());
		}
	}
	//endregion
	
	//region Read built-in types
	public @NotNull String readString() {
		int length = this.buffer.readInt();
		return this.buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
	}
	
	public @NotNull UUID readUUID() {
		long most = this.readLong();
		long least = this.readLong();
		UUID uuid = new UUID(most, least);
		return uuid.equals(Utils.EMPTY_UUID) ? Utils.EMPTY_UUID : uuid;
	}
	
	public <T> @NotNull List<T> readList(@NotNull Supplier<T> decoder) {
		List<T> list = Lists.newArrayList();
		int size = this.readInt();
		for (int i = 0; i < size; i++) {
			list.add(decoder.get());
		}
		return list;
	}
	
	public <K, V> @NotNull Map<K, V> readMap(@NotNull Supplier<K> keyDecoder, @NotNull Supplier<V> valueDecoder) {
		Map<K, V> map = Maps.newHashMap();
		int size = this.buffer.readInt();
		for (int i = 0; i < size; i++) {
			K key = keyDecoder.get();
			V value = valueDecoder.get();
			map.put(key, value);
		}
		return map;
	}
	//endregion
	
	//region Write objects
	public <T extends Encodable & Decodable> void write(@NotNull T object) {
		object.validate();
		object.encode(this);
	}
	
	public void writeUnsafe(@NotNull Object object) {
		this.writeString(object.getClass().getName());
		this.write((Encodable & Decodable) object);
	}
	
	public <T extends Encodable & Decodable> void writeInterface(@NotNull T value) {
		this.writeString(value.getClass().getName());
		this.write(value);
	}
	//endregion
	
	//region Read objects
	public <T extends Encodable & Decodable> T read(@NotNull Class<T> clazz) {
		return Decodable.decode(clazz, this);
	}
	
	@SuppressWarnings("unchecked")
	public <T, V extends Encodable & Decodable> T readUnsafe() {
		Class<V> clazz = (Class<V>) ReflectionHelper.getClassForName(this.readString());
		return (T) this.read(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Encodable & Decodable> T readInterface() {
		Class<T> clazz = (Class<T>) ReflectionHelper.getClassForName(this.readString());
		return Decodable.decode(clazz, this);
	}
	//endregion
	
	public @NotNull ByteBuf toBuffer() {
		return this.buffer;
	}
	
}
