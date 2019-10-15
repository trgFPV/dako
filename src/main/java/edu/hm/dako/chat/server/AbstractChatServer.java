package edu.hm.dako.chat.server;

/**
 * Gemeinsame Attribute fuer alle Implementierungen
 * 
 * @author Peter Mandl
 *
 */
public abstract class AbstractChatServer implements ChatServerInterface {

	// Gemeinsam fuer alle Workerthreads verwaltete Liste aller eingeloggten
	// Clients
	protected SharedChatClientList clients;

	// Zaehler fuer Test
	protected SharedServerCounter counter;

	// Referenz auf Server GUI fuer die Meldung von Ereignissen
	protected ChatServerGuiInterface serverGuiInterface;
}
