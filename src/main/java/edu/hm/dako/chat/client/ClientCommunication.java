package edu.hm.dako.chat.client;

import java.io.IOException;

/**
 * Interface zur Kommunikation des Chat-Clients mit dem Chat-Server
 * 
 * @author Peter Mandl
 *
 */
public interface ClientCommunication {

	/**
	 * Login-Request an den Server senden
	 * 
	 * @param name
	 *          Username (Login-Kennung)
	 * @throws Exception
	 */
	public void login(String name) throws IOException;

	/**
	 * Logout-Request an den Server senden
	 * 
	 * @param name
	 *          Username (Login-Kennung)
	 * @throws Exception
	 */
	public void logout(String name) throws IOException;

	/**
	 * Senden einer Chat-Nachricht zur Verteilung an den Server
	 * 
	 * @param name
	 *          Username (Login-Kennung)
	 * @param text
	 *          Chat-Nachricht
	 */
	public void tell(String name, String text) throws IOException;

	/**
	 * Abbruch der Verbindung zum Server
	 */
	public void cancelConnection();

	/**
	 * Pruefen, ob Logout schon komplett vollzogen
	 * 
	 * @return boolean - true = Logout abgeschlossen
	 */
	public boolean isLoggedOut();
}
