package net.luis.netcore.network.instance;

import net.luis.netcore.network.connection.event.impl.ReceiveEvent;
import net.luis.netcore.network.connection.event.impl.SendEvent;
import net.luis.netcore.packet.Packet;
import net.luis.utils.event.Event;
import net.luis.utils.event.EventType;
import org.jetbrains.annotations.NotNull;

import static net.luis.netcore.network.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public interface ClosingTrigger<E extends Event> {
	
	@NotNull EventType<E> getTrigger();
	
	boolean shouldClose(E event);
	
	//region Send closing triggers
	static @NotNull ClosingTrigger<SendEvent> closeAfterSent() {
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
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSent(Class<? extends Packet> packet) {
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
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSent(int packets) {
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
	
	static @NotNull ClosingTrigger<SendEvent> closeAfterSent(Class<? extends Packet> packet, int packets) {
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
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceived() {
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
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceived(Class<? extends Packet> packet) {
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
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceived(int packets) {
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
	
	static @NotNull ClosingTrigger<ReceiveEvent> closeAfterReceived(Class<? extends Packet> packet, int packets) {
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
}
