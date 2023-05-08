package net.luis.netcore.buffer;

import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public interface Decodable {
	
	static <T extends Decodable> T decode(@NotNull Class<T> clazz, @NotNull FriendlyByteBuffer buffer) {
		return ReflectionHelper.newInstance(clazz, buffer);
	}
	
	default void validate() {
		if (!ReflectionHelper.hasConstructor(this.getClass(), FriendlyByteBuffer.class)) {
			throw new IllegalArgumentException("Class " + this.getClass().getName() + " has no constructor with FriendlyByteBuffer as parameter");
		}
	}
}
