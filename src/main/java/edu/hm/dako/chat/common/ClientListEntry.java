package edu.hm.dako.chat.common;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.connection.Connection;

/**
 * Eintrag in der serverseitigen Clientliste zur Verwaltung der angemeldeten
 * User inkl. des Conversation-Status.
 *
 * @author Peter Mandl
 *
 */
public class ClientListEntry {
	private static Log log = LogFactory.getLog(ClientListEntry.class);

	// Login-Name des Clients
	private String userName;

	// Verbindungs-Handle fuer Transportverbindung zum Client
	private Connection con;

	// Kennzeichen zum Beenden des Worker-Threads
	boolean finished;

	// Login-Zeitpunkt
	private long loginTime;

	// Ankunftszeit einer Chat-Message fuer die Serverzeit-Messung
	private long startTime;

	// Conversation-Status des Clients
	private ClientConversationStatus status;

	// Anzahl der verarbeiteten Chat-Nachrichten des Clients (Sequenznummer)
	private long numberOfReceivedChatMessages;

	// Anzahl gesendeter Events (ChatMessageEvents, LoginEvents, LogoutEvents),
	// die der Server fuer den Client sendet
	private long numberOfSentEvents;

	// Anzahl aller empfangenen Confirms (ChatMessageConfirm, LoginConfirm,
	// LogoutConfirm) fuer den Client
	private long numberOfReceivedEventConfirms;

	// Anzahl nicht erhaltener Bestaetigungen (derzeit nicht genutzt)
	private long numberOfLostEventConfirms;

	// Anzahl an Nachrichtenwiederholungen (derzeit nicht genutzt)
	private long numberOfRetries;

	// Liste, die auf alle Clients verweist, die noch kein Event-Confirm fuer
	// einen konkret laufenden Request gesendet haben
	private Vector<String> waitList;

	public ClientListEntry(String userName, Connection con) {
		this.userName = userName;
		this.con = con;
		this.finished = false;
		this.loginTime = 0;
		this.startTime = 0;
		this.status = ClientConversationStatus.UNREGISTERED;
		this.numberOfReceivedChatMessages = 0;
		this.numberOfSentEvents = 0;
		this.numberOfReceivedEventConfirms = 0;
		this.numberOfLostEventConfirms = 0;
		this.numberOfRetries = 0;
		this.waitList = new Vector<String>();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder
				.append("ChatClientListEntry+++++++++++++++++++++++++++++++++++++++++++++");
		stringBuilder.append("UserName: " + this.userName);
		stringBuilder.append("\n");
		stringBuilder.append("Connection: " + this.con);
		stringBuilder.append("\n");
		stringBuilder.append("Status: " + this.status);
		stringBuilder.append("\n");
		stringBuilder
				.append("+++++++++++++++++++++++++++++++++++++++++++++ChatClientListEntry");

		return stringBuilder.toString();
	}

	public synchronized void setUserName(String userName) {
		this.userName = userName;
	}

	public synchronized String getUserName() {
		return userName;
	}

	public synchronized void setConnection(Connection con) {
		this.con = con;
	}

	public synchronized Connection getConnection() {
		return (con);
	}

	public synchronized void setLoginTime(long time) {
		this.loginTime = time;
	}

	public synchronized void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public synchronized long getLoginTime() {
		return (loginTime);
	}

	public synchronized long getStartTime() {
		return (startTime);
	}

	public synchronized void setNumberOfReceivedChatMessages(long nr) {
		this.numberOfReceivedChatMessages = nr;
	}

	public synchronized long getNumberOfReceivedChatMessages() {
		return (numberOfReceivedChatMessages);
	}

	public synchronized void setNumberOfSentEvents(long nr) {
		this.numberOfSentEvents = nr;
	}

	public synchronized long getNumberOfSentEvents() {
		return (numberOfSentEvents);
	}

	public synchronized void setNumberOfReceivedEventConfirms(long nr) {
		this.numberOfReceivedEventConfirms = nr;
	}

	public synchronized long getNumberOfReceivedEventConfirms() {
		return (numberOfReceivedEventConfirms);
	}

	public synchronized void setNumberOfLostEventConfirms(long nr) {
		this.numberOfLostEventConfirms = nr;
	}

	public synchronized long getNumberOfLostEventConfirms() {
		return (numberOfLostEventConfirms);
	}

	public synchronized void setNumberOfRetries(long nr) {
		this.numberOfRetries = nr;
	}

	public synchronized long getNumberOfRetries() {
		return (numberOfRetries);
	}

	public synchronized ClientConversationStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(ClientConversationStatus status) {
		this.status = status;
	}

	public synchronized boolean isFinished() {
		return finished;
	}

	public synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}

	public synchronized void incrNumberOfSentEvents() {
		this.numberOfSentEvents++;
	}

	public synchronized void incrNumberOfReceivedEventConfirms() {
		this.numberOfReceivedEventConfirms++;
	}

	public synchronized void incrNumberOfLostEventConfirms() {
		this.numberOfLostEventConfirms++;
	}

	public synchronized void incrNumberOfReceivedChatMessages() {
		this.numberOfReceivedChatMessages++;
	}

	public synchronized void incrNumberOfRetries() {
		this.numberOfRetries++;
	}

	public synchronized void setWaitList(Vector<String> list) {
		this.waitList = list;
		log.debug("Warteliste von " + this.userName + ": " + waitList);
	}

	public synchronized void addWaitListEntry(String userName) {
		this.waitList.add(userName);
		log.debug("Warteliste von " + this.userName + " ergaenzt um " + userName);
	}

	public synchronized Vector<String> getWaitList() {
		return waitList;
	}

	public synchronized void clearWaitList() {
		waitList.clear();
	}
}
