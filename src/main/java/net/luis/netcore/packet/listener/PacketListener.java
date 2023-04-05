package net.luis.netcore.packet.listener;

import net.luis.netcore.packet.Packet;
import org.jetbrains.annotations.NotNull;

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
public @interface PacketListener {
	
	int ANY_TARGET = -1;
	
	@NotNull Class<? extends Packet> value() default Packet.class;
	
	int target() default ANY_TARGET;
	
	int priority() default 0;
	
}
