/**
 *
 * @author Luis-St
 *
 */

module net.luis.netcore {
	requires org.apache.logging.log4j;
	requires io.netty.all;
	requires org.jetbrains.annotations;
	requires com.google.common;
	requires net.luis.utils;
	
	exports net.luis.netcore.buffer;
	exports net.luis.netcore.buffer.decode;
	exports net.luis.netcore.buffer.encode;
	exports net.luis.netcore.connection;
	exports net.luis.netcore.connection.channel;
	exports net.luis.netcore.connection.event;
	exports net.luis.netcore.connection.event.impl;
	exports net.luis.netcore.connection.util;
	exports net.luis.netcore.exception;
	exports net.luis.netcore.instance;
	exports net.luis.netcore.instance.event;
	exports net.luis.netcore.packet;
	exports net.luis.netcore.packet.impl.action;
	exports net.luis.netcore.packet.impl.data;
	exports net.luis.netcore.packet.impl.message;
	exports net.luis.netcore.packet.impl.value;
	exports net.luis.netcore.packet.listener;
	exports net.luis.netcore.packet.permission;
	exports net.luis.netcore.packet.registry;
	exports net.luis.netcore.packet.util;
	exports net.luis.netcore.packet.wrapper;
}