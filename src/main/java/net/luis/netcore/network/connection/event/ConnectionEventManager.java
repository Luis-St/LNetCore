package net.luis.netcore.network.connection.event;

import net.luis.netcore.network.instance.ClosingTrigger;
import net.luis.utils.event.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 *
 * @author Luis
 *
 */

public enum ConnectionEventManager {
	
	INSTANCE;
	
	private final EventDispatcher dispatcher = new EventDispatcher();
	
	public <T extends EventType<E>, E extends Event> UUID register(T type, EventListener<T, E> listener) {
		return this.dispatcher.register(type, listener);
	}
	
	public  <T extends EventType<E>, E extends Event> boolean remove(T type, UUID uniqueId) {
		return this.dispatcher.remove(type, uniqueId);
	}
	
	public <T extends EventType<E>, E extends Event> void removeAll(T type) {
		this.dispatcher.removeAll(type);
	}
	
	public <T extends EventType<E>, E extends Event> void dispatch(T type, E event) {
		this.dispatcher.dispatch(type, event);
	}
}
