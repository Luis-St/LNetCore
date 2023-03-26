package net.luis.netcore.packet.listener;

import java.lang.annotation.*;

/**
 *
 * @author Luis-St
 *
 */

@Documented
@Target({
		ElementType.METHOD, ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketTarget {
	
	int ANY_TARGET = -1;
	
	int value() default ANY_TARGET;
	
}
