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
public @interface PacketPriority {
	
	int value() default 0;
	
}
