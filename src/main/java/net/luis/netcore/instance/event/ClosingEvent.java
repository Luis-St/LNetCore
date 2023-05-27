package net.luis.netcore.instance.event;

import com.google.common.collect.Maps;
import net.luis.netcore.connection.event.impl.ReceiveEvent;
import net.luis.netcore.connection.event.impl.SendEvent;
import net.luis.netcore.packet.Packet;
import net.luis.utils.event.Event;
import net.luis.utils.event.EventType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static net.luis.netcore.connection.event.ConnectionEventType.*;

/**
 *
 * @author Luis-St
 *
 */

public interface ClosingEvent<E extends Event> {
	
	//region Send closing triggers
	static @NotNull ClosingEvent<SendEvent> closeAfterSent() {
		return new ClosingEvent<SendEvent>() {
			@Override
			public @NotNull EventType<SendEvent> getEvent() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				return true;
			}
		};
	}
	
	static @NotNull ClosingEvent<SendEvent> closeAfterSent(Class<? extends Packet> packet) {
		return closeAfterSent(packet, 1);
	}
	
	static @NotNull ClosingEvent<SendEvent> closeAfterSent(int packets) {
		return closeAfterSent(Packet.class, packets);
	}
	
	static @NotNull ClosingEvent<SendEvent> closeAfterSent(Class<? extends Packet> packet, int packets) {
		return new ClosingEvent<SendEvent>() {
			private final Map<UUID, Integer> hits = Maps.newHashMap();
			
			@Override
			public @NotNull EventType<SendEvent> getEvent() {
				return SEND;
			}
			
			@Override
			public boolean shouldClose(SendEvent event) {
				if (packet.isInstance(event.getPacket())) {
					this.hits.compute(event.getUniqueId(), (uuid, integer) -> integer == null ? 1 : integer + 1);
					return this.hits.getOrDefault(event.getUniqueId(), 0) >= packets;
				}
				return false;
			}
		};
	}
	//endregion
	
	//region Receive closing triggers
	static @NotNull ClosingEvent<ReceiveEvent> closeAfterReceived() {
		return new ClosingEvent<ReceiveEvent>() {
			@Override
			public @NotNull EventType<ReceiveEvent> getEvent() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				return true;
			}
		};
	}
	
	static @NotNull ClosingEvent<ReceiveEvent> closeAfterReceived(Class<? extends Packet> packet) {
		return closeAfterReceived(packet, 1);
	}
	
	static @NotNull ClosingEvent<ReceiveEvent> closeAfterReceived(int packets) {
		return closeAfterReceived(Packet.class, packets);
	}
	
	static @NotNull ClosingEvent<ReceiveEvent> closeAfterReceived(Class<? extends Packet> packet, int packets) {
		return new ClosingEvent<ReceiveEvent>() {
			private final Map<UUID, Integer> hits = Maps.newHashMap();
			
			@Override
			public @NotNull EventType<ReceiveEvent> getEvent() {
				return RECEIVE;
			}
			
			@Override
			public boolean shouldClose(ReceiveEvent event) {
				if (packet.isInstance(event.getPacket())) {
					this.hits.compute(event.getUniqueId(), (uuid, integer) -> integer == null ? 1 : integer + 1);
					return this.hits.getOrDefault(event.getUniqueId(), 0) >= packets;
				}
				return false;
			}
		};
	}
	//endregion
	
	@NotNull EventType<E> getEvent();
	
	boolean shouldClose(E event);
}
