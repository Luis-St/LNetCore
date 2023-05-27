package net.luis.netcore.packet.permission;

import net.luis.netcore.packet.Packet;

import java.util.UUID;

/**
 *
 * @author Luis-St
 *
 */

public interface PermissionHandler<T> {
	
	T mapUser(UUID uniqueId);
	
	boolean hasPermission(T user, Packet packet);
}
