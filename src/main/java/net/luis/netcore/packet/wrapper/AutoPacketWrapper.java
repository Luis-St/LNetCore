package net.luis.netcore.packet.wrapper;

import com.google.common.collect.Lists;
import net.luis.netcore.packet.Packet;
import net.luis.utils.util.unsafe.Nullability;
import net.luis.utils.util.unsafe.classpath.ClassPathUtils;
import net.luis.utils.util.unsafe.info.ValueInfo;
import net.luis.utils.util.unsafe.reflection.ReflectionHelper;
import net.luis.utils.util.unsafe.reflection.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class AutoPacketWrapper<T> implements PacketWrapper<T> {
	
	private final Class<T> clazz;
	private final Constructor<T> constructor;
	private final Parameter[] parameters;
	
	@SuppressWarnings("unchecked")
	public AutoPacketWrapper(Class<T> clazz) {
		//region Validation
		Objects.requireNonNull(clazz, "Class must not be null");
		if (clazz.isAnnotation() || clazz.isInterface() || clazz.isEnum()) {
			throw new IllegalArgumentException("Class must be a class or a record");
		}
		if (clazz.getConstructors().length != 1) {
			throw new IllegalArgumentException("Class must have exactly one constructor");
		}
		//endregion
		this.clazz = clazz;
		this.constructor = (Constructor<T>) this.clazz.getConstructors()[0];
		this.parameters = this.constructor.getParameters();
	}
	
	@Override
	public T wrap(Packet packet) {
		if (this.parameters.length == 0) {
			return ReflectionHelper.newInstance(this.constructor);
		}
		Objects.requireNonNull(packet, "Packet must not be null");
		List<Method> getters = ClassPathUtils.getAnnotatedMethods(packet.getClass(), PacketGetter.class);
		if (getters.size() != this.parameters.length) {
			throw new IllegalArgumentException(packet.getClass().getSimpleName() + " must have the same amount of getters as the constructor of class " + this.clazz.getSimpleName() + " parameters");
		}
		return ReflectionHelper.newInstance(this.constructor, ReflectionUtils.getParameters(this.constructor, this.getGetterValues(packet, getters)));
	}
	
	private @NotNull List<ValueInfo> getGetterValues(Packet packet, List<Method> getters) {
		List<ValueInfo> values = Lists.newArrayList();
		for (Method method : Objects.requireNonNull(getters, "Getters must not be null")) {
			PacketGetter getter = method.getAnnotation(PacketGetter.class);
			if (method.getParameterCount() > 0) {
				throw new IllegalArgumentException("Getter " + method.getName() + " of packet " + packet.getClass().getSimpleName() + " must not have any parameters");
			}
			String name = getter.value();
			if (name.isBlank()) {
				throw new IllegalArgumentException("The name of the parameter must be specified in getter " + method.getName() + " of " + packet.getClass().getSimpleName());
			}
			values.add(new ValueInfo(ReflectionHelper.invoke(method, packet), name, Nullability.UNKNOWN));
		}
		if (values.size() != this.parameters.length) {
			throw new IllegalStateException("An error occurred while getting the values of the getters of " + packet.getClass().getSimpleName() + " (expected " + this.parameters.length + ", got " + values.size() + ")");
		}
		return values;
	}
}
