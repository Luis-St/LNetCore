package net.luis.netcore.network.connection.event;

import net.luis.netcore.packet.Packet;
import net.luis.utils.collection.Registry;

import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ReceiveEvent implements ConnectionEvent {
	
	private final Registry<Consumer<Packet>> actions = Registry.of();
	
	ReceiveEvent() {
	
	}
	
	public void register(Consumer<Packet> action) {
		this.actions.register(action);
	}
	
	public boolean remove(UUID uniqueId) {
		return this.actions.remove(uniqueId);
	}
	
	public void trigger(Packet packet) {
		this.actions.getItems().forEach(action -> action.accept(packet));
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "ReceiveEvent";
	}
	//endregion
}
