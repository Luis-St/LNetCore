package net.luis.netcore.network.instance;

import net.luis.netcore.network.connection.event.impl.ReceiveEvent;
import net.luis.netcore.network.connection.event.impl.SendEvent;
import net.luis.netcore.packet.Packet;
import net.luis.utils.event.Event;
import net.luis.utils.event.EventType;
import net.luis.utils.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;

import static net.luis.netcore.network.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public interface ClosingTrigger<T extends Event> {
	
	@NotNull EventType<T> getTrigger();
	
	boolean shouldClose(T event);
	
	//region Send closing triggers
	static @NotNull ClosingTrigger<SendEvent> closeAfterSend() {
		return new ClosingTrigger<SendEvent>() {
			@Override
			public @NotNull EventType<SendEvent> getTrigger() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				return true;
			}
		};
	}
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSend(Class<? extends Packet> packet) {
		return new ClosingTrigger<SendEvent>() {
			@Override
			public @NotNull EventType<SendEvent> getTrigger() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				return packet.isAssignableFrom(event.getPacket().getClass());
			}
		};
	}
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSends(int packets) {
		return new ClosingTrigger<SendEvent>() {
			private int hits = 0;
			
			@Override
			public @NotNull EventType<SendEvent> getTrigger() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				return ++this.hits >= packets;
			}
		};
	}
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSends(Class<? extends Packet> packet, int packets) {
		return new ClosingTrigger<SendEvent>() {
			private int hits = 0;
			
			@Override
			public @NotNull EventType<SendEvent> getTrigger() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				if (packet.isAssignableFrom(event.getPacket().getClass())) {
					return ++this.hits >= packets;
				}
				return false;
			}
		};
	}
	//endregion
	
	//region Receive closing triggers
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceive() {
		return new ClosingTrigger<ReceiveEvent>() {
			@Override
			public @NotNull EventType<ReceiveEvent> getTrigger() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				return true;
			}
		};
	}
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceive(Class<? extends Packet> packet) {
		return new ClosingTrigger<ReceiveEvent>() {
			@Override
			public @NotNull EventType<ReceiveEvent> getTrigger() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				return packet.isAssignableFrom(event.getPacket().getClass());
			}
		};
	}
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceives(int packets) {
		return new ClosingTrigger<ReceiveEvent>() {
			private int hits = 0;
			
			@Override
			public @NotNull EventType<ReceiveEvent> getTrigger() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				return ++this.hits >= packets;
			}
		};
	}
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceives(Class<? extends Packet> packet, int packets) {
		return new ClosingTrigger<ReceiveEvent>() {
			private int hits = 0;
			
			@Override
			public @NotNull EventType<ReceiveEvent> getTrigger() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				if (packet.isAssignableFrom(event.getPacket().getClass())) {
					return ++this.hits >= packets;
				}
				return false;
			}
		};
	}
	//endregion
	
	//region Timeout closing triggers
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterTimeout(Duration duration) {
		return new ClosingTrigger<ReceiveEvent>() {
			private final ScheduledExecutorService executor = Utils.make(Executors.newSingleThreadScheduledExecutor());
			
			private void scheduleTimeout() {
				ScheduledExecutorService executor = ;
				this.executor.scheduleAtFixedRate(() -> {
				
				}, 0, 1, TimeUnit.MILLISECONDS);
			}
			
			@Override
			public @NotNull EventType<ReceiveEvent> getTrigger() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				return true;
			}
		};
	}
	//endregion
}
