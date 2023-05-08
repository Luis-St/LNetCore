package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketGetter;
import net.luis.utils.util.unsafe.Nullability;
import net.luis.utils.util.unsafe.classpath.ClassPathUtils;
import net.luis.utils.util.unsafe.info.ValueInfo;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import net.luis.utils.util.unsafe.reflection.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

class ListenerHelper {
	
	private static final Logger LOGGER = LogManager.getLogger(ListenerHelper.class);
	
	static @NotNull String getName(@NotNull Object listener) {
		return listener.toString().split("/")[0].replace("$$", "$");
	}
	
	static @NotNull List<ValueInfo> getValues(@NotNull Connection connection, @NotNull Packet packet) {
		List<ValueInfo> values = Lists.newArrayList();
		values.add(new ValueInfo(connection, "connection", Nullability.NOT_NULL));
		values.add(new ValueInfo(packet, "packet", Nullability.NOT_NULL));
		values.add(new ValueInfo(packet.getTarget(), "target", Nullability.NOT_NULL));
		for (Method method : ClassPathUtils.getAnnotatedMethods(packet.getClass(), PacketGetter.class)) {
			PacketGetter getter = method.getAnnotation(PacketGetter.class);
			Object value = null;
			if (method.getParameters().length > 0) {
				if (method.getParameters().length > 1) {
					LOGGER.warn("The method {}#{} is annotated with @PacketGetter but requires multiple parameters, which is not allowed", packet.getClass().getSimpleName(), method.getName());
					continue;
				} else if (!method.getParameters()[0].getType().isAssignableFrom(Connection.class)) {
					LOGGER.warn("The method {}#{} is annotated with @PacketGetter but requires a parameter of type {}, but only the Connection is allowed as parameter", packet.getClass().getSimpleName(), method.getName(),
							method.getParameters()[0].getType().getSimpleName());
					continue;
				} else {
					value = ReflectionHelper.invoke(method, packet, connection);
				}
			}
			if (value == null) {
				value = ReflectionHelper.invoke(method, packet);
			}
			String name = ReflectionUtils.getRawName(method, getter.getterPrefix());
			String parameterName = getter.parameterName().isEmpty() ? name.substring(0, 1).toLowerCase() + name.substring(1) : getter.parameterName();
			values.add(new ValueInfo(value, parameterName, ReflectionUtils.getNullability(method.getAnnotatedReturnType())));
		}
		return values;
	}
}
