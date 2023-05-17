package net.luis.netcore.buffer.decode;

import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;

/**
 *
 * @author Luis-St
 *
 */

public interface Decodable {
	
	static <T extends Decodable> T decode(Class<T> clazz, FriendlyByteBuffer buffer) {
		return ReflectionHelper.newInstance(clazz, buffer);
	}
	
	default void validate() {
		if (!ReflectionHelper.hasConstructor(this.getClass(), FriendlyByteBuffer.class)) {
			throw new IllegalArgumentException("Class " + this.getClass().getName() + " has no constructor with FriendlyByteBuffer as parameter");
		}
	}
}
