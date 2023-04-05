package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketGetter;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.utils.util.unsafe.Nullability;
import net.luis.utils.util.unsafe.StackTraceUtils;
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
	
	static int getTarget() {
		try {
			Class<?> clazz = StackTraceUtils.getCallingClass(1);
			return StackTraceUtils.getCallingMethodSafe().filter(method -> method.isAnnotationPresent(PacketListener.class)).map(method -> method.getAnnotation(PacketListener.class).target()).orElseGet(() -> {
				if (clazz.isAnnotationPresent(PacketListener.class)) {
					return clazz.getAnnotation(PacketListener.class).target();
				}
				return -1;
			});
		} catch (Exception | Error ignored) {
		
		}
		return -1;
	}
	
	static List<ValueInfo> getValues(@NotNull Connection connection, @NotNull Packet packet) {
		List<ValueInfo> values = Lists.newArrayList();
		values.add(new ValueInfo(connection, "connection", Nullability.NOT_NULL));
		values.add(new ValueInfo(packet, "packet", Nullability.NOT_NULL));
		for (Method method : ClassPathUtils.getAnnotatedMethods(packet.getClass(), PacketGetter.class)) {
			PacketGetter getter = method.getAnnotation(PacketGetter.class);
			if (method.getParameters().length > 0) {
				LOGGER.warn("The method {}#{} is annotated with @PacketGetter but requires parameters, which is not allowed", packet.getClass().getSimpleName(), method.getName());
				continue;
			}
			String name = ReflectionUtils.getRawName(method, getter.getterPrefix());
			String parameterName = getter.parameterName().isEmpty() ? name.substring(0, 1).toLowerCase() + name.substring(1) : getter.parameterName();
			values.add(new ValueInfo(ReflectionHelper.invoke(method, packet), parameterName, ReflectionUtils.getNullability(method.getAnnotatedReturnType())));
		}
		return values;
	}
	
}
