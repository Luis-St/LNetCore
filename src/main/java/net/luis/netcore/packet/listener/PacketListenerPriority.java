package net.luis.netcore.packet.listener;

import java.lang.annotation.*;

/**
 *
 * @author Luis-St
 *
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketListenerPriority {
	
	int value() default 0;
	
}
