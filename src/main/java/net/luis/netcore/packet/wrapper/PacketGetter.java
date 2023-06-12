package net.luis.netcore.packet.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 *
 * @author Luis-St
 *
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketGetter {
	
	@NotNull String value() default "";
}
