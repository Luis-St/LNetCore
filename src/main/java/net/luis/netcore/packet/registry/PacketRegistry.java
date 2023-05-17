package net.luis.netcore.packet.registry;

import com.google.common.collect.Maps;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.exception.InvalidPacketException;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.impl.action.ActionPacket;
import net.luis.netcore.packet.impl.action.CloseConnectionPacket;
import net.luis.netcore.packet.impl.action.InitializeConnectionPacket;
import net.luis.netcore.packet.impl.data.DataPacket;
import net.luis.netcore.packet.impl.data.ListPacket;
import net.luis.netcore.packet.impl.data.MapPacket;
import net.luis.netcore.packet.impl.message.ErrorPacket;
import net.luis.netcore.packet.impl.message.InfoPacket;
import net.luis.netcore.packet.impl.value.*;
import net.luis.utils.util.unsafe.classpath.ClassPathUtils;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

@SuppressWarnings("unchecked")
public class PacketRegistry {
	
	private static final Logger LOGGER = LogManager.getLogger(PacketRegistry.class);
	private static final Map<Integer, Class<? extends Packet>> PACKETS = Maps.newHashMap();
	private static int index;
	
	public static void initialize() {
	
	}
	
	public static Class<? extends Packet> byId(int id) {
		Class<? extends Packet> clazz = PACKETS.get(id);
		if (clazz != null) {
			return clazz;
		}
		LOGGER.error("Failed to get packet for id {}", id);
		return null;
	}
	
	public static int getId(Class<? extends Packet> clazz) {
		Objects.requireNonNull(clazz, "Packet class must not be null");
		for (Map.Entry<Integer, Class<? extends Packet>> entry : PACKETS.entrySet()) {
			if (entry.getValue() == clazz) {
				return entry.getKey();
			}
		}
		LOGGER.error("Failed to get packet id for packet of type {}", clazz.getSimpleName());
		return -1;
	}
	
	public static Packet getPacket(int id, @NotNull FriendlyByteBuffer buffer) {
		Class<? extends Packet> clazz = byId(id);
		if (clazz != null) {
			try {
				if (ReflectionHelper.hasConstructor(clazz, FriendlyByteBuffer.class)) {
					Constructor<? extends Packet> constructor = ReflectionHelper.getConstructor(clazz, FriendlyByteBuffer.class);
					return Objects.requireNonNull(constructor).newInstance(buffer);
				} else {
					LOGGER.error("Packet {} does not have a constructor with FriendlyByteBuffer as parameter", clazz.getSimpleName());
					throw new InvalidPacketException("Packet " + clazz.getSimpleName() + " does not have a FriendlyByteBuffer constructor");
				}
			} catch (Exception e) {
				if (e instanceof InvalidPacketException) {
					throw (InvalidPacketException) e;
				} else {
					LOGGER.error("Fail to create packet of type {} for id {}", clazz.getSimpleName(), id);
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	public static void register(Class<? extends Packet> clazz) {
		Objects.requireNonNull(clazz, "Cannot register packet because the class is null");
		if (PACKETS.containsValue(clazz)) {
			LOGGER.error("Packet {} is already registered", clazz.getSimpleName());
			return;
		}
		if (!ReflectionHelper.hasConstructor(clazz, FriendlyByteBuffer.class)) {
			LOGGER.error("Cannot register packet {} because it does not have a constructor with FriendlyByteBuffer as parameter", clazz.getSimpleName());
			return;
		}
		if (!clazz.getSimpleName().endsWith("Packet")) {
			LOGGER.warn("A packet should end with 'Packet' but {} does not", clazz.getSimpleName());
		}
		if (PACKETS.containsKey(index)) {
			throw new IllegalStateException("Cannot register packet " + clazz.getSimpleName() + " because the id " + index + " is already registered to packet " + PACKETS.get(index).getSimpleName());
		} else {
			PACKETS.put(index++, clazz);
		}
	}
	
	static {
		register(ActionPacket.class);
		register(CloseConnectionPacket.class);
		register(InitializeConnectionPacket.class);
		register(DataPacket.class);
		register(ListPacket.class);
		register(MapPacket.class);
		register(InfoPacket.class);
		register(ErrorPacket.class);
		register(IntegerPacket.class);
		register(BooleanPacket.class);
		register(DoublePacket.class);
		register(IntegerPacket.class);
		register(LongPacket.class);
		register(ObjectPacket.class);
		register(StringPacket.class);
		ClassPathUtils.getAnnotatedClasses(AutoPacket.class).stream().filter(clazz -> {
			if (Packet.class.isAssignableFrom(clazz)) {
				return true;
			} else {
				LOGGER.error("Class {} is annotated with AutoPacket but is not a packet", clazz.getSimpleName());
				return false;
			}
		}).peek(clazz -> {
			LOGGER.debug("Automatically registering packet {}", clazz.getSimpleName());
		}).forEach(clazz -> register((Class<? extends Packet>) clazz));
	}
}
