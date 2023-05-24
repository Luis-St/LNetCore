package net.luis.netcore.packet.listener;

import net.luis.netcore.connection.Connection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 *
 * @author Luis
 *
 */

public class DefaultListenerBuilder extends ListenerBuilder {
	
	private final PacketTarget defaultTarget;
	private final PacketPriority defaultPriority;
	
	@ApiStatus.Internal
	public DefaultListenerBuilder(Connection connection, PacketTarget defaultTarget) {
		this(connection, defaultTarget, PacketPriority.NORMAL);
	}
	
	@ApiStatus.Internal
	public DefaultListenerBuilder(Connection connection, PacketPriority defaultPriority) {
		this(connection, PacketTarget.ANY, defaultPriority);
	}
	
	@ApiStatus.Internal
	public DefaultListenerBuilder(Connection connection, PacketTarget defaultTarget, PacketPriority defaultPriority) {
		super(connection);
		this.defaultTarget = defaultTarget;
		this.defaultPriority = defaultPriority;
		this.resetDefaults();
	}
	
	private void resetDefaults() {
		this.target = this.defaultTarget;
		this.priority = this.defaultPriority;
	}
	
	@Override
	public @NotNull UUID register() {
		UUID uniqueId = super.register();
		this.resetDefaults();
		return uniqueId;
	}
}
