package net.luis.netcore.connection;

import net.luis.netcore.packet.permission.PermissionHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 * @author Luis-St
 *
 */

public class ConnectionSettings {
	
	private final Supplier<UUID> uniqueId;
	private boolean allowEvents = true;
	private PermissionHandler<?> permissionHandler;
	
	@ApiStatus.Internal
	ConnectionSettings(Supplier<UUID> uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public @NotNull UUID getUniqueId() {
		return this.uniqueId.get();
	}
	
	public boolean areEventsAllowed() {
		return this.allowEvents;
	}
	
	public void setEventsAllowed(boolean allowEvents) {
		this.allowEvents = allowEvents;
	}
	
	public PermissionHandler<?> getPermissionHandler() {
		return this.permissionHandler;
	}
	
	public void setPermissionHandler(PermissionHandler<?> permissionHandler) {
		this.permissionHandler = permissionHandler;
	}
}
