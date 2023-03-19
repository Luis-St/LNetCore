package net.luis.netcore.packet;

import com.google.common.collect.Maps;
import net.luis.netcore.buffer.FriendlyByteBuffer;
import net.luis.netcore.exception.InvalidPacketException;
import net.luis.utils.util.reflection.ClassPathUtils;
import net.luis.utils.util.reflection.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;

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
	
	public static Class<? extends Packet> byId(int id) {
		Class<? extends Packet> clazz = PACKETS.get(id);
		if (clazz != null) {
			return clazz;
		}
		LOGGER.error("Failed to get packet for id {}", id);
		return null;
	}
	
	public static int getId(@NotNull Class<? extends Packet> clazz) {
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
					assert constructor != null;
					return constructor.newInstance(buffer);
				} else {
					LOGGER.error("Packet {} does not have a constructor with FriendlyByteBuffer as parameter", clazz.getSimpleName());
					throw new InvalidPacketException("Packet " + clazz.getSimpleName() + " does not have a FriendlyByteBuffer constructor");
				}
			} catch (Exception e) {
				LOGGER.error("Fail to create packet of type {} for id {}", clazz.getSimpleName(), id);
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public static void register(Class<? extends Packet> clazz) {
		if (PACKETS.containsValue(clazz)) {
			LOGGER.error("Packet {} is already registered", clazz.getSimpleName());
			return;
		}
		if (!ReflectionHelper.hasConstructor(clazz, FriendlyByteBuffer.class)) {
			LOGGER.error("Cannot register packet {} because it does not have a constructor with FriendlyByteBuffer as parameter", clazz.getSimpleName());
			return;
		}
		if (PACKETS.containsKey(index)) {
			throw new IllegalStateException("Cannot register packet " + clazz.getSimpleName() + " because the id " + index + " is already registered to packet " + PACKETS.get(index).getSimpleName());
		} else {
			PACKETS.put(index++, clazz);
		}
	}
	
	
	static {
		/* TODO
		register(HandshakePacket.class);
		register(IntegerPacket.class);
		register(LongPacket.class);
		register(DoublePacket.class);
		register(StringPacket.class);
		register(ObjectPacket.class);
		*/
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
