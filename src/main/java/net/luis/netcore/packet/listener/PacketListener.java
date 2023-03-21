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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketListener {
	
	@NotNull Class<? extends Packet> value() default Packet.class;

}
