package edu.hm.dako.chat.client;

import java.util.Vector;

/**
 * Interface zur Ausfuehrung von Aktionen ueber die Praesentationslogik
 *
 * @author Mandl
 */
public interface ClientUserInterface {

	/**
	 * Uebergabe der Startdaten an die GUI
	 *
	 * @param userList
	 *          Liste der aktuell angemeldeten User
	 */
	public void setUserList(Vector<String> userList);

	/**
	 * Uebergabe einer Nachricht zur Ausgabe in der Messagezeile
	 *
	 * @param sender
	 *          Absender der Nachricht
	 * @param message
	 *          Nachrichtentext
	 */
	public void setMessageLine(String sender, String message);

	/**
	 * Sperren bzw. Entsperren der Eingabe von Chat-Nachrichten an der GUI
	 *
	 * @param lock
	 *          true, wenn Client warten muss, sonst false
	 */
	public void setLock(boolean lock);

	/**
	 * Lesen der Sperre
	 * 
	 * @param lock
	 *          true, wenn Client warten muss, sonst false
	 */
	public boolean getLock();

	/**
	 * Abfragen, ob Benutzer den Chat stoppen will
	 */
	public boolean isTestAborted();

	/**
	 * Stoppen des laufenden Chats
	 */
	public void abortTest();

	/**
	 * (Rueck)setzen des Stop-Flags
	 */
	public void releaseTest();

	/**
	 * Puefen, ob Test gerade laeuft
	 */
	public boolean isRunning();

	/**
	 * Serverbearbeitungszeit des letzten Chat-Message-Requests merken
	 * 
	 * @param lastServerTime
	 *          Zuletzt gemessene Serverbearbeitungszeit
	 */
	public void setLastServerTime(long lastServerTime);

	/**
	 * Auslesen der zuletzt benoetigten Zeit fuer die Bearbeitung eines
	 * Chat-Message-Requests im Server
	 * 
	 * @return
	 */
	public long getLastServerTime();

	/**
	 * Zaehler einer Chat-Session setzen (Zaehlung erfolgt im Server)
	 * 
	 * @param numberOfSentEvents
	 *          Waehrend der Session gesendete Events
	 * @param numberOfReceivedConfirms
	 *          Waehrend der Session gesendete/empfangene Confirms
	 * @param numberOfLostConfirms
	 *          Waehrend der Session verlorene Confirms
	 * @param numberOfRetries
	 *          Waehrend der Session gesendete Wiederholungen
	 */
	public void setSessionStatisticsCounter(long numberOfSentEvents,
			long numberOfReceivedConfirms, long numberOfLostConfirms, long numberOfRetries,
			long numberOfReceivedChatMessages);

	public long getNumberOfSentEvents();

	public long getNumberOfReceivedConfirms();

	public long getNumberOfLostConfirms();

	public long getNumberOfRetries();

	public long getNumberOfReceivedChatMessages();

	/**
	 * Uebergabe einer Fehlermeldung
	 *
	 * @param sender
	 *          Absender der Fehlermeldung
	 * @param errorMessage
	 *          Fehlernachricht
	 * @param errorCode
	 *          Error Code
	 */

	public void setErrorMessage(String sender, String errorMessage, long errorCode);

	/**
	 * Login vollstaendig und Chat-GUI kann angezeigt werden
	 */
	public void loginComplete();

	/**
	 * Logout vollstaendig durchgefuehrt
	 */
	public void logoutComplete();
}