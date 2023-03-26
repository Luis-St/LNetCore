package net.luis.netcore.connection;

import com.google.common.collect.Lists;
import net.luis.netcore.packet.Packet;
import net.luis.netcore.packet.listener.PacketGetter;
import net.luis.netcore.packet.listener.PacketListener;
import net.luis.netcore.packet.listener.PacketPriority;
import net.luis.netcore.packet.listener.PacketTarget;
import net.luis.utils.util.reflection.ClassPathUtils;
import net.luis.utils.util.reflection.Nullability;
import net.luis.utils.util.reflection.ReflectionHelper;
import net.luis.utils.util.reflection.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
		StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
		try {
			Class<?> clazz = ReflectionHelper.getClassForName(stackTrace.getClassName());
			Method method = ReflectionHelper.getMethod(clazz, stackTrace.getMethodName());
			if (method.isAnnotationPresent(PacketTarget.class)) {
				return method.getAnnotation(PacketTarget.class).value();
			} else if (clazz.isAnnotationPresent(PacketTarget.class)) {
				return clazz.getAnnotation(PacketTarget.class).value();
			} else {
				return -1;
			}
		} catch (Exception | Error e) {
			return -1;
		}
	}
	
	static int getPriority() {
		StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
		try {
			Class<?> clazz = ReflectionHelper.getClassForName(stackTrace.getClassName());
			List<Method> methods = getMethodsForName(clazz, stackTrace.getMethodName()).stream().filter(method -> method.isAnnotationPresent(PacketPriority.class)).toList();
			if (methods.size() == 1 && methods.get(0).isAnnotationPresent(PacketPriority.class)) {
				return methods.get(0).getAnnotation(PacketPriority.class).value();
			} else if (clazz.isAnnotationPresent(PacketPriority.class)) {
				return clazz.getAnnotation(PacketPriority.class).value();
			}
		} catch (Exception | Error ignored) {
		
		}
		return 0;
	}
	
	private static @NotNull List<Method> getMethodsForName(@NotNull Class<?> clazz, String name) {
		List<Method> methods = Lists.newArrayList();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(name)) {
				methods.add(method);
			}
		}
		return methods;
	}
	
	static @NotNull Object[] getParameters(@NotNull Method listener, @NotNull Connection connection, @NotNull Packet packet) {
		Parameter[] parameters = listener.getParameters();
		Object[] arguments = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if (parameter.getType().isAssignableFrom(Connection.class)) {
				arguments[i] = connection;
			} else if (parameter.getType().isAssignableFrom(packet.getClass())) {
				arguments[i] = packet;
			} else if (listener.getAnnotation(PacketListener.class).value() == packet.getClass()) {
				GetterInfo getter = findParameter(listener, packet, parameter);
				arguments[i] = ReflectionHelper.invoke(getter.getter(), packet);
			} else {
				throw new IllegalStateException("No getter for parameter " + parameter.getName() + " in packet " + packet + " found");
			}
		}
		return arguments;
	}
	
	private static @NotNull GetterInfo findParameter(@NotNull Method listener, @NotNull Packet packet, @NotNull Parameter parameter) {
		List<GetterInfo> getters = disassemblePacket(packet.getClass());
		for (GetterInfo getter : getters) {
			boolean duplicate = hasDuplicates(getters);
			if (parameter.getType().isAssignableFrom(getter.valueClass())) {
				if (!duplicate) {
					return checkNullability(parameter, getter);
				} else if (!parameter.isNamePresent()) {
					throw new IllegalArgumentException("The parameter " + parameter.getName() + " of method " + listener.getDeclaringClass().getSimpleName() + "#" + listener.getName() + " is ambiguous");
				} else if (parameter.getName().equalsIgnoreCase(getter.parameterName())) {
					return checkNullability(parameter, getter);
				}
			}
		}
		throw new IllegalArgumentException("No getter for parameter " + parameter.getName() + " in packet " + packet + " found");
	}
	
	private static @NotNull List<GetterInfo> disassemblePacket(@NotNull Class<? extends Packet> clazz) {
		List<GetterInfo> getters = Lists.newArrayList();
		for (Method annotatedMethod : ClassPathUtils.getAnnotatedMethods(clazz, PacketGetter.class)) {
			PacketGetter getter = annotatedMethod.getAnnotation(PacketGetter.class);
			if (annotatedMethod.getParameters().length > 0) {
				LOGGER.warn("The method {}#{} is annotated with @PacketGetter but requires parameters, which is not allowed", clazz.getSimpleName(), annotatedMethod.getName());
				continue;
			}
			String name = ReflectionUtils.getRawName(annotatedMethod, getter.getterPrefix());
			String parameterName = getter.parameterName().isEmpty() ? name.substring(0, 1).toLowerCase() + name.substring(1) : getter.parameterName();
			getters.add(new GetterInfo(annotatedMethod, annotatedMethod.getReturnType(), parameterName, ReflectionUtils.getNullability(annotatedMethod.getAnnotatedReturnType())));
		}
		return getters;
	}
	
	private static boolean hasDuplicates(@NotNull List<GetterInfo> getters) {
		List<Class<?>> classes = Lists.newArrayList();
		for (GetterInfo getter : getters) {
			if (classes.contains(getter.valueClass())) {
				return true;
			}
			classes.add(getter.valueClass());
		}
		return false;
	}
	
	private static @NotNull GetterInfo checkNullability(@NotNull Parameter parameter, @NotNull GetterInfo getter) {
		String parameterName = parameter.getDeclaringExecutable().getDeclaringClass().getSimpleName() + "#" + parameter.getDeclaringExecutable().getName() + "#" + parameter.getName();
		String getterName = getter.getter().getDeclaringClass().getSimpleName() + "#" + getter.getter().getName();
		if (getter.nullability() == Nullability.NULLABLE) {
			if (!parameter.isAnnotationPresent(Nullable.class)) {
				LOGGER.warn("Parameter {} is not annotated with @Nullable but the getter {} can return null", parameterName, getterName);
			}
			if (parameter.isAnnotationPresent(NotNull.class)) {
				throw new IllegalArgumentException("Parameter " + parameterName + " is annotated with @NotNull but the getter " + getterName + " can return null");
			}
		}
		return getter;
	}
	
	private record GetterInfo(Method getter, Class<?> valueClass, String parameterName, Nullability nullability) {
	
	}
	
}
