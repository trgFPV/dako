package edu.hm.dako.chat.server;

/**
 * Einheitliche Schnittstelle aller Server
 * 
 * @author Peter Mandl
 */
public interface ChatServerInterface {

	/**
	 * Startet den Server
	 */
	void start();

	/**
	 * Stoppt den Server
	 *
	 * @throws Exception
	 */
	void stop() throws Exception;
}
