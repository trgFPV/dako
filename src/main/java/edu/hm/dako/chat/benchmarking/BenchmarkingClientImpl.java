package edu.hm.dako.chat.benchmarking;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p/>
 * Benchmarking-Client: Simuliert einen Chat-User
 *
 * @author Mandl
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.client.AbstractChatClient;
import edu.hm.dako.chat.client.AdvancedMessageListenerThreadImpl;
import edu.hm.dako.chat.client.ClientImpl;
import edu.hm.dako.chat.client.ClientUserInterface;
import edu.hm.dako.chat.client.SimpleMessageListenerThreadImpl;
import edu.hm.dako.chat.common.ClientConversationStatus;
import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.common.ImplementationType;
import edu.hm.dako.chat.common.SharedClientStatistics;
import edu.hm.dako.chat.connection.ConnectionFactory;

public class BenchmarkingClientImpl extends AbstractChatClient
		implements Runnable, ClientUserInterface {

	private static Log log = LogFactory.getLog(ClientImpl.class);

	/*
	 * Parameter fuer den Benchmarking-Lauf
	 */
	protected int clientNumber;
	protected int messageLength;
	protected int numberOfMessagesToSend;

	protected int responseTimeout;
	protected int nrOfRetries;
	protected int clientThinkTime;

	protected ImplementationType implementationType;

	// Schnittstelle zur BenchmarkingGui, um den Progressbar zu veraendern
	protected BenchmarkingClientUserInterface benchmarkingGui;

	// Kennzeichen, ob zuletzt erwartete Chat-Response-PDU des Clients
	// angekommen ist
	private AtomicBoolean chatResponseReceived = new AtomicBoolean();

	// Serverzeit des letzten Chat-Message-Requests
	private AtomicLong lastServerTime = new AtomicLong(0);

	/*
	 * Statistikzaehlerstaende fuer eine beendete Chat-Session
	 */
	private long numberOfSentEvents;
	private long numberOfReceivedConfirms;
	private long numberOfLostConfirms;
	private long numberOfRetries;
	private long numberOfReceivedChatMessages;

	// Gemeinsame Daten aller Threads zur Erfassung statistischer Daten
	protected SharedClientStatistics sharedStatistics;

	/**
	 * Konstruktor fuer Benchmarking
	 * 
	 * @param userInterface
	 *          Schnittstelle zur GUI
	 * @param benchmarkingGui
	 *          Schnittstelle zur BenchmarkingGUI
	 * @param implementationType
	 *          Typ der Implementierung
	 * @param serverPort
	 *          Port des Servers
	 * @param remoteServerAddress
	 *          Hostadresse des Servers
	 * @param numberOfClient
	 *          Anzahl der zu simulierenden Clients
	 * @param messageLength
	 *          Laenge der Chat-Nachrichten
	 * @param numberOfMessages
	 *          Anzahl der Nachrichten pro Client
	 * @param clientThinkTime
	 *          Maximale Denkzeit zwischen zwei Chat-Requests
	 * @param numberOfRetries
	 *          Anzahl Wiederholungen bei Nachrichtenverlust
	 * @param responseTimeout
	 *          Timeout bei Uebrwachung der Bestaetigungen
	 * @param sharedStatistics
	 *          Statistikdaten
	 * @param connectionFactory
	 *          Connection Fabrik
	 */
	public BenchmarkingClientImpl(ClientUserInterface userInterface,
			BenchmarkingClientUserInterface benchmarkingGui,
			ImplementationType implementationType, int serverPort, String remoteServerAddress,
			int numberOfClient, int messageLength, int numberOfMessages, int clientThinkTime,
			int numberOfRetries, int responseTimeout, SharedClientStatistics sharedStatistics,
			ConnectionFactory connectionFactory) {

		super(userInterface, serverPort, remoteServerAddress);

		this.benchmarkingGui = benchmarkingGui;
		this.implementationType = implementationType;
		this.clientNumber = numberOfClient;
		this.messageLength = messageLength;
		this.numberOfMessagesToSend = numberOfMessages;
		this.clientThinkTime = clientThinkTime;
		this.nrOfRetries = numberOfRetries;
		this.responseTimeout = responseTimeout;
		this.sharedStatistics = sharedStatistics;
		startMessageListenerThread();
	}

	/**
	 * Start des Message-Listener-Threads zur Bearbeitung von empfangenen
	 * Server-Nachrichten
	 */
	private void startMessageListenerThread() {

		switch (implementationType) {

		case TCPSimpleImplementation:
			try {
				messageListenerThread = new SimpleMessageListenerThreadImpl(this, connection,
						sharedClientData);
				messageListenerThread.start();
			} catch (Exception e) {
				ExceptionHandler.logException(e);
			}
			break;

		case TCPAdvancedImplementation:
			try {

				messageListenerThread = new AdvancedMessageListenerThreadImpl(this, connection,
						sharedClientData);
				messageListenerThread.start();
			} catch (Exception e) {
				ExceptionHandler.logException(e);
			}
			break;

		default:
			break;
		}

		Thread.currentThread().setName("Client-Thread-" + String.valueOf(clientNumber + 1));
		threadName = Thread.currentThread().getName();
		messageListenerThread.setName("MessageListener-Thread-" + clientNumber);
		log.debug("Message-Processing-Thread gestartet: " + messageListenerThread.getName());
	}

	/**
	 * Thread zur Simulation eines Chat-Users: User wird beim Server registriert,
	 * alle Requests werden gesendet, Antworten werden gelesen und am Ende wird
	 * ein Logout ausgefuehrt. Der Vorgang wird abprupt abgebrochen, wenn dies
	 * ueber die GUI gewuenscht wird.
	 */
	@Override
	public void run() {

		try {
			// Login ausfuehren und warten, bis Server bestaetigt
			this.login(threadName);

			while (sharedClientData.status != ClientConversationStatus.REGISTERED) {

				Thread.sleep(1);
				if (sharedClientData.status == ClientConversationStatus.UNREGISTERED) {
					// Fehlermeldung vom Server beim Login-Vorgang
					log.debug("User " + userName + " schon im Server angemeldet");
					return;
				}
			}

			sharedStatistics.incrNumberOfLoggedInClients();

			// Derzeit werden in der Progressbar Login und Logout nicht
			// beruecksichtigt
			// benchmarkingGui.countUpProgressTask();

			log.debug("User " + userName + " beim Server angemeldet");

			// Warten, bis alle Clients eingeloggt sind
			waitForLoggedInClients();

			// Alle Chat-Nachrichten senden
			int i = 0;
			while ((i < numberOfMessagesToSend) && (!userInterface.isTestAborted())) {

				sendMessageAndWaitForAck(i);
				try {
					// Zufaellige Zeit, aber maximal die angegebene Denkzeit
					// warten
					int randomThinkTime = (int) (Math.random() * clientThinkTime) + 1;
					Thread.sleep(randomThinkTime);
				} catch (Exception e) {
					ExceptionHandler.logException(e);
				}

				i++;
				log.debug("Gesendete Chat-Nachrichten von " + userName + ": " + i);
			}

			// Warten, bis alle Clients bereit zum Ausloggen sind (alle Clients
			// haben alle Chat-Nachrichten gesendet)
			waitForLoggingOutClients();

			// Logout ausfuehren und warten, bis Server bestaetigt
			this.logout(threadName);
			while (sharedClientData.status != ClientConversationStatus.UNREGISTERED) {
				Thread.sleep(1);
			}

			sharedStatistics.incrNumberOfLoggedOutClients();

			// Derzeit werden in der Progressbar Loging und Logout nicht 
			// beruecksichtigt
			// benchmarkingGui.countUpProgressTask();

			log.debug(
					"Anzahl gesendeter Requests: " + sharedStatistics.getNumberOfSentRequests());
			log.debug("Anzahl empfangener Responses: "
					+ sharedStatistics.getSumOfAllReceivedMessages());
			log.debug(
					"Anzahl vom Server empfangener Events: " + sharedClientData.eventCounter.get());
			log.debug("Anzahl an Server gesendeter Confirms: "
					+ sharedClientData.confirmCounter.get());
			log.debug("Durchschnittliche Serverbearbeitungszeit in ns: "
					+ sharedStatistics.getAverageServerTime() + ", = "
					+ sharedStatistics.getAverageServerTime() / 1000000 + " ms");

			// Nachbearbeitung fuer die Statistik
			postLogout();
			log.debug("User " + userName + " beim Server abgemeldet");

			// Transportverbindung zum Server abbauen
			connection.close();

		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * Warten, bis Server einen Chat-Response als Antwort auf den letzten
	 * Chat-Request gesendet hat (nur fuer Benchmarking)
	 */
	private void waitUntilChatResponseReceived() {

		setLock(true);
		try {
			while (getLock() == true) {
				log.debug(userName + " wartet auf Chat-Message-Response-PDU");
				Thread.sleep(1);
				// Durch den Sleep wird die RTT beim Benchmark ein wenig verfaelscht
			}
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}

		return;
	}

	/**
	 * Chat-Nachricht an den Server senden und auf Antwort warten. Methode wird
	 * nur von Benchmarking-Client genutzt
	 * 
	 * @param i
	 *          Nummer des Clients
	 * @throws Exception
	 */
	private void sendMessageAndWaitForAck(int i) throws Exception {

		// Dummy-Nachricht zusammenbauen
		String chatMessage = "";
		for (int j = 0; j < messageLength; j++) {
			chatMessage += "+";
		}

		// Senden der Nachricht und warten, bis Bestaetigung vom Server da ist
		try {

			sharedStatistics.incrSentMsgCounter(clientNumber);

			// RTT-Startzeit ermitteln
			long rttStartTime = System.nanoTime();
			tell(userName, chatMessage);

			// Warten, bis Chat-Response empfangen wurde, dann erst naechsten
			// Chat Request senden
			waitUntilChatResponseReceived();

			// Response in Statistik aufnehmen
			long rtt = System.nanoTime() - rttStartTime;
			postReceive(i, getLastServerTime(), rtt);

		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * Synchronisation mit allen anderen Client-Threads: Warten, bis alle Clients
	 * angemeldet sind und dann erst mit der Lasterzeugung beginnen
	 *
	 * @throws InterruptedException
	 *           falls sleep unterbrochen wurde
	 */
	private void waitForLoggedInClients() throws InterruptedException {
		sharedStatistics.getLoginSignal().countDown();
		sharedStatistics.getLoginSignal().await();
	}

	/**
	 * Synchronisation mit allen anderen Client-Threads: Warten, bis alle Clients
	 * angemeldet sind und dann erst mit der Lasterzeugung beginnen
	 *
	 * @throws InterruptedException
	 *           falls sleep unterbrochen wurde
	 */
	private void waitForLoggingOutClients() throws InterruptedException {
		sharedStatistics.getLogoutSignal().countDown();
		sharedStatistics.getLogoutSignal().await();
		log.debug("Client " + threadName + " kann beendet werden");
	}

	/**
	 * Nacharbeit nach Empfang einer PDU vom Server
	 *
	 * @param messageNumber
	 *          Fortlaufende Nachrichtennummer
	 * @param serverTime
	 *          Zeit, die der Server fuer die Bearbeitung des
	 *          Chat-Message-Requests benoetigt
	 * @param rtt
	 *          Round Trip Time fuer den Request
	 */
	private void postReceive(int messageNumber, long serverTime, long rtt) {

		// Response-Zaehler und Serverbearbeitungszeit erhoehen
		sharedStatistics.incrReceivedMsgCounter(clientNumber, rtt, serverTime);

		// Progressbar weiterschreiben
		benchmarkingGui.countUpProgressTask();

		if (rtt <= serverTime) {
			// Test, ob Messung plausibel ist, rtt muss groesser als serverTime sein
			log.error(threadName + ": RTT fuer Request " + (messageNumber + 1) + ": " + rtt
					+ " ns = " + (rtt / 1000000) + " ms,  benoetigte Serverzeit: " + serverTime
					+ " ns = " + (serverTime / 1000000) + " ms");
		}
	}

	/**
	 * Nacharbeit nach Logout
	 *
	 * @param receivedPdu
	 *          letzte empfangene PDU
	 */
	private void postLogout() {

		// Zaehler fuer Statistik eintragen
		sharedStatistics.setNumberOfSentEventMessages(clientNumber, getNumberOfSentEvents());
		sharedStatistics.setNumberOfReceivedConfirmEvents(clientNumber,
				getNumberOfReceivedConfirms());
		sharedStatistics.setNumberOfLostConfirmEvents(clientNumber,
				getNumberOfLostConfirms());
		sharedStatistics.setNumberOfRetriedEvents(clientNumber, getNumberOfRetries());

		log.debug(
				"Vom Server verarbeitete Chat-Nachrichten: " + getNumberOfReceivedChatMessages());
		log.debug("Vom Server gesendete Event-Nachrichten: " + getNumberOfSentEvents());
		log.debug("Dem Server bestaetigte Event-Nachrichten (Confirms): "
				+ getNumberOfReceivedConfirms());
		log.debug("Im Server nicht empfangene Bestaetigungen: " + getNumberOfLostConfirms());
		log.debug("Vom Server initiierte Wiederholungen: " + getNumberOfRetries());
	}

	@Override
	// Wird nicht genutzt, nur fuer ClientGui relevant
	public void setUserList(Vector<String> names) {
	}

	@Override
	// Wird nicht genutzt, nur fuer ClientGui relevant
	public void setMessageLine(String sender, String message) {
	}

	@Override
	// Wird nicht genutzt, nur fuer ClientGui relevant
	public void setErrorMessage(String sender, String errorMessage, long errorCode) {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientCoordinator relevant
	public void loginComplete() {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientCoordinator relevant
	public void logoutComplete() {
	}

	@Override
	public synchronized void setLock(boolean lock) {
		chatResponseReceived.getAndSet(lock);
		if (chatResponseReceived.get() == false) {
			log.debug(Thread.currentThread().getName() + " sendet notify");
			// Antwort auf letzten Request erhalten, naechster Request kann gesendet
			// werden
			notifyAll();
		}
	}

	@Override
	public synchronized boolean getLock() {
		if (chatResponseReceived.get() == true) {
			log.debug(Thread.currentThread().getName() + " wartet auf notify");
			try {
				wait();
				return false;
			} catch (Exception e) {
				return false;
			}
		} else {
			log.error(
					Thread.currentThread().getName() + " muss nicht auf notify warten, Lock frei");
			return false;
		}
	}

	@Override
	public void setLastServerTime(long lastServerTime) {
		this.lastServerTime.getAndSet(lastServerTime);
	}

	@Override
	public void abortTest() {
		userInterface.abortTest();
	}

	@Override
	public boolean isRunning() {
		return userInterface.isRunning();
	}

	@Override
	public void releaseTest() {
		userInterface.releaseTest();
	}

	@Override
	public boolean isTestAborted() {
		return userInterface.isTestAborted();
	}

	@Override
	public synchronized long getLastServerTime() {
		long time = lastServerTime.get();
		return time;
	}

	@Override
	public void setSessionStatisticsCounter(long numberOfSentEvents,
			long numberOfReceivedConfirms, long numberOfLostConfirms, long numberOfRetries,
			long numberOfReceivedChatMessages) {

		this.numberOfSentEvents = numberOfSentEvents;
		this.numberOfReceivedConfirms = numberOfReceivedConfirms;
		this.numberOfLostConfirms = numberOfLostConfirms;
		this.numberOfRetries = numberOfRetries;
		this.numberOfReceivedChatMessages = numberOfReceivedChatMessages;
	}

	@Override
	public long getNumberOfSentEvents() {
		return this.numberOfSentEvents;
	}

	@Override
	public long getNumberOfReceivedConfirms() {
		return this.numberOfReceivedConfirms;
	}

	@Override
	public long getNumberOfLostConfirms() {
		return this.numberOfLostConfirms;
	}

	@Override
	public long getNumberOfRetries() {
		return this.numberOfRetries;
	}

	@Override
	public long getNumberOfReceivedChatMessages() {
		return this.numberOfReceivedChatMessages;
	}
}