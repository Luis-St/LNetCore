package net.luis.netcore.network;

/**
 *
 * @author Luis-St
 *
 */

interface NetworkInstance {
	
	void open();
	
	boolean isOpen();
	
	void closeNow();
}
