package net.luis.netcore.buffer;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface Encodable {
	
	void encode(@NotNull FriendlyByteBuffer buffer);
}
