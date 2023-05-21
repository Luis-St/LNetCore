package net.luis.netcore.network.connection.event;

import net.luis.utils.collection.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public class OpenEvent implements ConnectionEvent {
	
	private final Registry<Runnable> actions = Registry.of();
	
	OpenEvent() {
	
	}
	
	public @NotNull UUID register(Runnable action) {
		return this.actions.register(action);
	}
	
	public boolean remove(UUID uniqueId) {
		return this.actions.remove(uniqueId);
	}
	
	public void trigger() {
		this.actions.getItems().forEach(Runnable::run);
	}
	
	//region Object overrides
	@Override
	public String toString() {
		return "OpenEvent";
	}
	//endregion
}
