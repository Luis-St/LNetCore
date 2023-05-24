package net.luis.netcore.network.instance;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.luis.netcore.network.SimpleChannelInitializer;
import net.luis.netcore.network.connection.Connection;
import net.luis.netcore.network.connection.ConnectionInitializer;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.action.CloseConnectionPacket;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.utils.event.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

import static net.luis.netcore.network.connection.event.ConnectionEventManager.*;

/**
 *
 * @author Luis-St
 *
 */

public class ClientInstance extends AbstractNetworkInstance {
	
	/**
	 * TODO:<br>
	 *  - add better test
	 *  - add permissions for special packets (CloseServerPacket, CloseConnectionPacket, etc.)
	 */
	
	private static final Logger LOGGER = LogManager.getLogger(ClientInstance.class);
	
	private final ConnectionInitializer initializer;
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
			LOGGER.info("Starting client");
			new Bootstrap().group(this.buildGroup("client connection")).channel(NioSocketChannel.class).handler(new SimpleChannelInitializer(channel -> {
				this.connection = new Connection(channel, Optional.ofNullable(this.handshake));
				this.connection.registerListener(new InternalListener(this));
				this.initializer.initialize(this.connection);
				this.initialized = true;
				return this.connection;
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
		if (this.connection == null) {
			LOGGER.warn("Client has already been closed");
			return;
		}
		this.connection.send(new CloseConnectionPacket());
		this.closeInternal();
		LOGGER.info("Client closed");
	}
	
	private void closeInternal() {
		this.connection.close();
		this.connection = null;
		super.closeNow();
	}
	
	public <E extends Event> void closeOn(ClosingTrigger<E> trigger) {
		Objects.requireNonNull(trigger, "Closing trigger must not be null");
		INSTANCE.register(trigger.getTrigger(), event -> {
			if (trigger.shouldClose(event)) {
				this.closeNow();
			}
		});
	}
	
	//region Internal listener
	private static record InternalListener(ClientInstance instance) implements PacketListener {
		
		@Override
		public void initialize(Connection connection) {
			connection.builder().listener(CloseConnectionPacket.class, (packet) -> this.closeConnection()).register();
		}
		
		private void closeConnection() {
			this.instance.closeInternal();
		}
	}
	//endregion
	
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
		return "ClientInstance " + this.connection.getUniqueId();
	}
	//endregion
}
