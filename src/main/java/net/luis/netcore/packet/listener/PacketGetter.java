package net.luis.netcore.packet.listener;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.annotation.Target;

/**
 *
 * @author Luis-St
 *
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketGetter {
	
	@NotNull String getterPrefix() default "";
	
	@NotNull String parameterName() default "";
	
}