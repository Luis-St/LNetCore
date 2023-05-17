package net.luis.netcore.buffer.encode;

/**
 *
 * @author Luis
 *
 */

public interface Encoder<T> {
	
	void encode(T object);
}
