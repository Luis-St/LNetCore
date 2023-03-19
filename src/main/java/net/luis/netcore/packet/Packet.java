package net.luis.netcore.packet;

import net.luis.netcore.buffer.Decodable;
import net.luis.netcore.buffer.Encodable;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public interface Packet extends Encodable, Decodable {
	
	@Override
	void encode(@NotNull FriendlyByteBuffer buffer);
	
	default boolean skippable() {
		return false;
	}
	
}
