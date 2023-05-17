package net.luis.netcore.buffer.encode;

import net.luis.netcore.buffer.FriendlyByteBuffer;
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
