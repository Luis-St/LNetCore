package net.luis.netcore.network.connection.event;

import net.luis.utils.collection.Registry;

import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ExceptionEvent implements ConnectionEvent {
	
	private final Registry<Consumer<Throwable>> actions = Registry.of();
	
	ExceptionEvent() {
	
	}
	
	public void register(Consumer<Throwable> action) {
		this.actions.register(action);
	}
	
	public boolean remove(UUID uniqueId) {
		return this.actions.remove(uniqueId);
	}
	
	public void trigger(Throwable exception) {
		this.actions.getItems().forEach(action -> action.accept(exception));
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "ExceptionEvent";
	}
	//endregion
}
