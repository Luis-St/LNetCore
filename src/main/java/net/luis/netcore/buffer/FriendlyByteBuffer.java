package net.luis.netcore.buffer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.luis.netcore.buffer.decode.Decodable;
import net.luis.netcore.buffer.decode.Decoder;
import net.luis.netcore.buffer.encode.Encodable;
import net.luis.netcore.buffer.encode.Encoder;
import net.luis.utils.util.Utils;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public final class FriendlyByteBuffer {
	
	private final ByteBuf buffer;
	
	public FriendlyByteBuffer() {
		this(Unpooled.buffer());
	}
	
	public FriendlyByteBuffer(ByteBuf buffer) {
		this.buffer = Objects.requireNonNull(buffer, "Initial buffer cannot be null");
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
	
	public <T extends Enum<T>> void writeEnum(T value) {
		this.writeInt(Objects.requireNonNull(value, "Enum must not be null").ordinal());
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
	
	public <T extends Enum<T>> @NotNull T readEnum(Class<T> clazz) {
		int ordinal = this.readInt();
		return Objects.requireNonNull(clazz, "Enum class must not be null").getEnumConstants()[ordinal];
	}
	//endregion
	
	//region Write built-in types
	public void writeString(String value) {
		Objects.requireNonNull(value, "String must not be null");
		this.buffer.writeInt(value.length());
		this.buffer.writeCharSequence(value, Charset.defaultCharset());
	}
	
	public void writeUUID(UUID value) {
		Objects.requireNonNull(value, "UUID must not be null");
		this.writeLong(value.getMostSignificantBits());
		this.writeLong(value.getLeastSignificantBits());
	}
	
	public <T> void writeList(List<T> list, Encoder<T> encoder) {
		Objects.requireNonNull(list, "List must not be null");
		this.writeInt(list.size());
		for (T t : list) {
			Objects.requireNonNull(encoder, "Encoder must not be null").encode(t);
		}
	}
	
	public <K, V> void writeMap(Map<K, V> map, Encoder<K> keyEncoder, Encoder<V> valueEncoder) {
		Objects.requireNonNull(map, "Map must not be null");
		this.writeInt(map.size());
		for (Map.Entry<K, V> entry : map.entrySet()) {
			Objects.requireNonNull(keyEncoder, "Key encoder must not be null").encode(entry.getKey());
			Objects.requireNonNull(valueEncoder, "Value encoder must not be null").encode(entry.getValue());
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
	
	public <T> @NotNull List<T> readList(Decoder<T> decoder) {
		List<T> list = Lists.newArrayList();
		int size = this.readInt();
		for (int i = 0; i < size; i++) {
			list.add(Objects.requireNonNull(decoder, "Decoder must not be null").decode());
		}
		return list;
	}
	
	public <K, V> @NotNull Map<K, V> readMap(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
		Map<K, V> map = Maps.newHashMap();
		int size = this.buffer.readInt();
		for (int i = 0; i < size; i++) {
			K key = Objects.requireNonNull(keyDecoder, "Key decoder must not be null").decode();
			V value = Objects.requireNonNull(valueDecoder, "Value decoder must not be null").decode();
			map.put(key, value);
		}
		return map;
	}
	//endregion
	
	//region Write objects
	public <T extends Encodable> void write(T object) {
		Objects.requireNonNull(object, "Object must not be null");
		if (object instanceof Decodable decodable) {
			decodable.validate();
		}
		object.encode(this);
	}
	
	public void writeUnsafe(Object object) {
		Objects.requireNonNull(object, "Object must not be null");
		this.writeString(object.getClass().getName());
		this.write((Encodable) object);
	}
	
	public <T extends Encodable> void writeInterface(T value) {
		Objects.requireNonNull(value, "Value must not be null");
		this.writeString(value.getClass().getName());
		this.write(value);
	}
	//endregion
	
	//region Read objects
	public <T extends Encodable & Decodable> T read(Class<T> clazz) {
		return Decodable.decode(Objects.requireNonNull(clazz, "Class must not be null"), this);
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
