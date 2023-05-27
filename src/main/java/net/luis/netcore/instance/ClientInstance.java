package net.luis.netcore.instance;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.luis.netcore.connection.*;
import net.luis.netcore.connection.channel.SimpleChannelInitializer;
import net.luis.netcore.connection.util.ConnectionInitializer;
import net.luis.netcore.instance.event.ClosingEvent;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.internal.CloseConnectionPacket;
import net.luis.netcore.packet.listener.PacketTarget;
import net.luis.utils.event.Event;
import net.luis.utils.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.Optional;

import static net.luis.netcore.connection.event.ConnectionEventManager.*;

/**
 *
 * @author Luis-St
 *
 */

public class ClientInstance extends AbstractNetworkInstance {
	
	/**
	 * TODO:<br>
	 *  - add permissions for special packets (CloseServerPacket, CloseConnectionPacket, etc.) -> @RequirePermission<br>
	 *  - avoid exposing the connection to the initializer<br>
	 *  - ClientInstance#direct -> should open a connection sending only one packet waits for response and closes the connection -> returns the response<br>
	 *  - add way to disable events in connection -> e.g. ConnectionSettings<br>
	 */
	
	private static final Logger LOGGER = LogManager.getLogger(ClientInstance.class);
	
	private final ConnectionInitializer initializer;
	private InternalConnection internalConnection;
	private Connection connection;
	private Packet handshake;
	
	public ClientInstance() {
		this((connection) -> {});
	}
	
	public ClientInstance(ConnectionInitializer initializer) {
		this.initializer = initializer;
	}
	
	public void handshake(Packet handshake) {
		this.handshake = handshake;
	}
	
	@Override
	public void open(String host, int port) {
		this.initialize(host, port);
		try {
			LOGGER.debug("Starting client");
			new Bootstrap().group(this.buildGroup("client connection")).channel(NioSocketChannel.class).handler(new SimpleChannelInitializer(channel -> {
				this.connection = new ClientConnection(channel, this.initializer, Optional.ofNullable(this.handshake));
				this.internalConnection = new InternalConnection(this, this.connection, channel);
				this.initialized = true;
				return Pair.of(this.internalConnection, this.connection);
			})).connect(this.getHost(), this.getPort()).syncUninterruptibly().channel();
			LOGGER.info("Client successfully started on {}:{}", this.getHost(), this.getPort());
		} catch (Exception e) {
			throw new RuntimeException("Fail to start client", e);
		}
	}
	
	@Override
	public void send(Packet packet) {
		this.connection.send(packet);
	}
	
	@Override
	public void closeNow() {
		if (this.connection == null || this.internalConnection == null) {
			LOGGER.warn("Client has already been closed");
			return;
		}
		this.internalConnection.send(new CloseConnectionPacket());
		this.closeInternal();
	}
	
	@ApiStatus.Internal
	void closeInternal() {
		LOGGER.debug("Closing client");
		this.internalConnection.close();
		this.internalConnection = null;
		this.connection.close();
		this.connection = null;
		super.closeNow();
		LOGGER.info("Client closed");
	}
	
	public <E extends Event> void closeOn(ClosingEvent<E> event) {
		Objects.requireNonNull(event, "Closing event must not be null");
		INSTANCE.register(event.getEvent(), evt -> {
			if (event.shouldClose(evt)) {
				this.closeNow();
			}
		});
	}
	
	//region Object overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClientInstance that)) return false;
		if (!super.equals(o)) return false;
		
		return Objects.equals(this.connection, that.connection);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.connection);
	}
	
	@Override
	public String toString() {
		return "ClientInstance";
	}
	//endregion
}
