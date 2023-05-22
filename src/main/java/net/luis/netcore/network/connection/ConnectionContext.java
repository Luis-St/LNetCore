package net.luis.netcore.network.connection;

import net.luis.netcore.packet.Packet;

import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 * @author Luis
 *
 */

public class ConnectionContext {
	
	private final UUID uniqueId;
	private final Consumer<Packet> sender;
	
	public ConnectionContext(UUID uniqueId, Consumer<Packet> sender) {
		this.uniqueId = uniqueId;
		this.sender = sender;
	}
	
	public UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public void sendPacket(Packet packet) {
		this.sender.accept(packet);
	}
}
