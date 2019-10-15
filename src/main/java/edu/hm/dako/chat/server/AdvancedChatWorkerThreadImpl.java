package edu.hm.dako.chat.server;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.ChatPDU;
import edu.hm.dako.chat.common.ClientConversationStatus;
import edu.hm.dako.chat.common.ClientListEntry;
import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionTimeoutException;
import edu.hm.dako.chat.connection.EndOfFileException;

/**
 * Worker-Thread zur serverseitigen Bedienung einer Session mit einem Client.
 * Jedem Chat-Client wird serverseitig ein Worker-Thread zugeordnet.
 * 
 * @author Peter Mandl
 *
 */
public class AdvancedChatWorkerThreadImpl extends AbstractWorkerThread {

	private static Log log = LogFactory.getLog(AdvancedChatWorkerThreadImpl.class);

	public AdvancedChatWorkerThreadImpl(Connection con, SharedChatClientList clients,
			SharedServerCounter counter, ChatServerGuiInterface serverGuiInterface) {
		super(con, clients, counter, serverGuiInterface);
	}

	@Override
	public void run() {

		log.debug(
				"ChatWorker-Thread erzeugt, Threadname: " + Thread.currentThread().getName());
		while (!finished && !Thread.currentThread().isInterrupted()) {
			try {
				// Warte auf naechste Nachricht des Clients und fuehre
				// entsprechende Aktion aus
				handleIncomingMessage();
			} catch (Exception e) {
				log.debug("Exception waehrend der Nachrichtenverarbeitung");
				ExceptionHandler.logException(e);
			}
		}
		log.debug(Thread.currentThread().getName() + " beendet sich");
		closeConnection();
	}

	/**
	 * Senden eines Login-List-Update-Event an alle angemeldeten Clients
	 * 
	 * @param currentClientList
	 *          Liste der gerade angemeldeten Clients fuer die Uebertragung an
	 *          alle aktiven Clients
	 * @param pdu
	 *          Zu sendende PDU
	 */
	protected void sendLoginListUpdateEvent(Vector<String> currentClientList, ChatPDU pdu) {

		Vector<String> clientList = currentClientList;

		for (String s : new Vector<String>(clientList)) {

			ClientListEntry client = clients.getClient(s);
			try {
				if (client != null) {

					client.getConnection().send(pdu);
					log.debug("Login- oder Logout-Event-PDU an " + client.getUserName()
							+ ", ClientListe:  " + pdu.getClients());
					clients.incrNumberOfSentChatEvents(client.getUserName());
					eventCounter.getAndIncrement();
					log.debug(userName + ": EventCounter bei Login/Logout erhoeht = "
							+ eventCounter.get() + ", ConfirmCounter = " + confirmCounter.get());
				}
			} catch (Exception e) {
				log.debug(
						"Senden einer Login- oder Logout-Event-PDU an " + s + " nicht moeglich");
				ExceptionHandler.logException(e);
			}
		}
	}

	@Override
	protected void loginRequestAction(ChatPDU receivedPdu) {
		log.debug("Login Request");
		ChatPDU pdu = null;
		log.debug("Login-Request-PDU fuer " + receivedPdu.getUserName() + " empfangen");

		// Neuer Client moechte sich einloggen, Client in Client-Liste
		// eintragen
		if (!clients.existsClient(receivedPdu.getUserName())) {
			log.debug("User nicht in Clientliste: " + receivedPdu.getUserName());
			ClientListEntry client = new ClientListEntry(receivedPdu.getUserName(), connection);
			client.setLoginTime(System.nanoTime());
			clients.createClient(receivedPdu.getUserName(), client);
			clients.changeClientStatus(receivedPdu.getUserName(),
					ClientConversationStatus.REGISTERING);
			log.debug("User " + receivedPdu.getUserName() + " nun in Clientliste");
			userName = receivedPdu.getUserName();
			clientThreadName = receivedPdu.getClientThreadName();
			Thread.currentThread().setName(receivedPdu.getUserName());
			log.debug("Laenge der Clientliste: " + clients.size());
			serverGuiInterface.incrNumberOfLoggedInClients();

			// ADVANCED_CHAT: Event-Warteliste erzeugen
			Vector<String> waitList = clients.createWaitList(receivedPdu.getUserName());

			// Login-Event an alle Clients (auch an den gerade aktuell
			// anfragenden) senden
			pdu = ChatPDU.createLoginEventPdu(userName, waitList, receivedPdu);
			sendLoginListUpdateEvent(waitList, pdu);
		} else {
			// User bereits angemeldet, Fehlermeldung an Client senden,
			// Fehlercode an Client senden
			pdu = ChatPDU.createLoginErrorResponsePdu(receivedPdu, ChatPDU.LOGIN_ERROR);

			try {
				connection.send(pdu);
				log.debug("Login-Response-PDU an " + receivedPdu.getUserName()
						+ " mit Fehlercode " + ChatPDU.LOGIN_ERROR + " gesendet");
			} catch (Exception e) {
				log.debug("Senden einer Login-Response-PDU an " + receivedPdu.getUserName()
						+ " nicth moeglich");
				ExceptionHandler.logExceptionAndTerminate(e);
			}
		}
	}

	@Override
	protected void logoutRequestAction(ChatPDU receivedPdu) {
		ChatPDU pdu;
		logoutCounter.getAndIncrement();
		log.debug("Logout-Request von " + receivedPdu.getUserName() + ", LogoutCount = "
				+ logoutCounter.get());

		log.debug("Logout-Request-PDU von " + receivedPdu.getUserName() + " empfangen");

		if (!clients.existsClient(userName)) {
			log.debug("User nicht in Clientliste: " + receivedPdu.getUserName());
		} else {
			// ADVANCED_CHAT: Event-Warteliste erzeugen
			Vector<String> waitList = clients.createWaitList(receivedPdu.getUserName());

			// Client, der sich gerade ausloggt soll nicht in der Userliste im Client
			// erscheinen, er wird daher entfernt. Ein LogoutEvent muss er aber
			// erhalten
			Vector<String> waitListWithoutLoggingOutClient = new Vector<String>();
			waitListWithoutLoggingOutClient = (Vector<String>) waitList.clone();
			waitListWithoutLoggingOutClient.remove(receivedPdu.getUserName());
			log.debug(
					"Warteliste ohne sich ausloggenden Client: " + waitListWithoutLoggingOutClient);

			pdu = ChatPDU.createLogoutEventPdu(userName, waitListWithoutLoggingOutClient,
					receivedPdu);

			// Event an Client versenden
			log.debug("Warteliste mit sich ausloggenden Client: " + waitList);
			clients.changeClientStatus(receivedPdu.getUserName(),
					ClientConversationStatus.UNREGISTERING);
			sendLoginListUpdateEvent(waitList, pdu);

			// ADVANCED_CHAT: Logout-Response darf hier noch nicht gesendet werden
			// (erst nach Empfang aller Confirms)

			serverGuiInterface.decrNumberOfLoggedInClients();
		}
	}

	@Override
	protected void chatMessageRequestAction(ChatPDU receivedPdu) {

		log.debug("Chat-Message-Request-PDU von " + receivedPdu.getUserName()
				+ " mit Sequenznummer " + receivedPdu.getSequenceNumber() + " empfangen");
		serverGuiInterface.incrNumberOfRequests();

		if (!clients.existsClient(receivedPdu.getUserName())) {
			log.debug("User nicht in Clientliste: " + receivedPdu.getUserName());
		} else {

			// ADVANCED_CHAT: Event-Warteliste erzeugen
			Vector<String> waitList = clients.createWaitList(receivedPdu.getUserName());

			log.debug("Warteliste: " + waitList);
			log.debug("Anzahl der User in der Warteliste: " + waitList.size());

			ChatPDU pdu = ChatPDU.createChatMessageEventPdu(userName, receivedPdu);

			// Event an Clients senden
			for (String s : new Vector<String>(waitList)) {

				ClientListEntry client = clients.getClient(s);
				try {
					if ((client != null)
							&& (client.getStatus() != ClientConversationStatus.UNREGISTERED)) {
						pdu.setUserName(client.getUserName());
						client.getConnection().send(pdu);
						log.debug("Chat-Event-PDU an " + client.getUserName() + " gesendet");
						clients.incrNumberOfSentChatEvents(client.getUserName());
						eventCounter.getAndIncrement();
						log.debug(userName + ": EventCounter erhoeht = " + eventCounter.get()
								+ ", Aktueller ConfirmCounter = " + confirmCounter.get()
								+ ", Anzahl gesendeter ChatMessages von dem Client = "
								+ receivedPdu.getSequenceNumber());
					}
				} catch (Exception e) {
					log.debug("Senden einer Chat-Event-PDU an " + client.getUserName()
							+ " nicht moeglich");
					ExceptionHandler.logException(e);
				}
			}

			log.debug("Aktuelle Laenge der Clientliste: " + clients.size());

			// Statistikdaten aktualisieren
			clients.incrNumberOfReceivedChatMessages(receivedPdu.getUserName());
			clients.setRequestStartTime(receivedPdu.getUserName(), startTime);
		}
	}

	/**
	 * ADVANCED_CHAT: ChatMessageEvent bestaetigen
	 * 
	 * @param receivedPdu
	 *          . Empfangene PDU
	 */
	private void chatMessageEventConfirmAction(ChatPDU receivedPdu) {

		log.debug("Chat-Message-Event-Confirm-PDU von " + receivedPdu.getUserName()
				+ " fuer initierenden Client " + receivedPdu.getEventUserName() + " empfangen");

		String eventInitiatorClient;
		String confirmSenderClient;

		// Empfangene Confirms hochzaehlen
		clients.incrNumberOfReceivedChatEventConfirms(receivedPdu.getEventUserName());
		confirmCounter.getAndIncrement();
		log.debug(userName + ": ConfirmCounter fuer ChatMessage erhoeht = "
				+ confirmCounter.get() + ", Aktueller EventCounter = " + eventCounter.get()
				+ ", Anzahl gesendeter ChatMessages von dem Client = "
				+ receivedPdu.getSequenceNumber());

		// Chat-Response-PDU fuer den initiierenden Client aufbauen und
		// senden, sofern alle Events-Confirms
		// der anderen aktiven Clients eingesammelt wurden

		try {
			eventInitiatorClient = receivedPdu.getEventUserName();
			confirmSenderClient = receivedPdu.getUserName();
			if ((clients.deleteWaitListEntry(eventInitiatorClient, confirmSenderClient) == 0)) {

				// Der User, der die Chat-Nachricht gesendet hatte, muss ermittelt
				// werden, da an ihn eine Response-PDU gesendet werden muss
				ClientListEntry client = clients.getClient(receivedPdu.getEventUserName());

				if (client != null) {
					ChatPDU responsePdu = ChatPDU.createChatMessageResponsePdu(
							receivedPdu.getEventUserName(), 0, 0, 0, 0,
							client.getNumberOfReceivedChatMessages(), receivedPdu.getClientThreadName(),
							(System.nanoTime() - client.getStartTime()));

					if (responsePdu.getServerTime() / 1000000 > 100) {
						log.debug(Thread.currentThread().getName()
								+ ", Benoetigte Serverzeit vor dem Senden der Response-Nachricht > 100 ms: "
								+ responsePdu.getServerTime() + " ns = "
								+ responsePdu.getServerTime() / 1000000 + " ms");
					}

					try {
						client.getConnection().send(responsePdu);
						log.debug("Chat-Message-Response-PDU an " + receivedPdu.getEventUserName()
								+ " gesendet");
					} catch (Exception e) {
						log.debug("Senden einer Chat-Message-Response-PDU an " + client.getUserName()
								+ " nicht moeglich");
						ExceptionHandler.logExceptionAndTerminate(e);
					}
				}
			}
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * ADVANCED_CHAT: LoginEvent bestaetigen
	 * 
	 * @param receivedPdu
	 *          . Empfangene PDU
	 */
	private void loginEventConfirmAction(ChatPDU receivedPdu) throws Exception {

		String eventInitiatorClient;
		String confirmSenderClient;

		log.debug("Login-Event-Confirm-PDU von Client " + receivedPdu.getUserName()
				+ " fuer initierenden " + receivedPdu.getEventUserName() + " empfangen");

		// Empfangene Confirms hochzaehlen
		clients.incrNumberOfReceivedChatEventConfirms(receivedPdu.getEventUserName());
		confirmCounter.getAndIncrement();
		log.debug(userName + ": ConfirmCounter fuer Login erhoeht = " + confirmCounter.get()
				+ ", Aktueller EventCounter = " + eventCounter.get());

		eventInitiatorClient = receivedPdu.getEventUserName();
		confirmSenderClient = receivedPdu.getUserName();
		log.debug("Login-EventConfirm: Event-Initiator: " + eventInitiatorClient
				+ ", Confirm-Sender: " + confirmSenderClient);

		try {
			log.debug(confirmSenderClient + " aus der Warteliste von " + eventInitiatorClient
					+ " austragen");

			if ((clients.deleteWaitListEntry(eventInitiatorClient, confirmSenderClient) == 0)) {
				log.debug("Warteliste von " + eventInitiatorClient
						+ " ist nun leer, alle Login-Event-Confirms erhalten");

				if (clients.getClient(eventInitiatorClient)
						.getStatus() == ClientConversationStatus.REGISTERING) {

					// Der initiierende Client ist im Login-Vorgang

					ChatPDU responsePdu = ChatPDU.createLoginResponsePdu(eventInitiatorClient,
							receivedPdu);

					// Login-Response senden

					try {
						clients.getClient(eventInitiatorClient).getConnection().send(responsePdu);
					} catch (Exception e) {
						log.debug("Senden einer Login-Response-PDU an " + eventInitiatorClient
								+ " fehlgeschlagen");
						log.debug("Exception Message: " + e.getMessage());
						throw e;
					}

					log.debug("Login-Response-PDU an Client " + eventInitiatorClient + " gesendet");
					clients.changeClientStatus(eventInitiatorClient,
							ClientConversationStatus.REGISTERED);
				}
			} else {
				log.debug("Warteliste von " + eventInitiatorClient + " enthaelt noch "
						+ clients.getWaitListSize(eventInitiatorClient) + " Eintraege");
			}

		} catch (Exception e) {
			log.debug("Login-Event-Confirm-PDU fuer nicht vorhandenen Client erhalten: "
					+ eventInitiatorClient);
		}
	}

	/**
	 * ADVANCED_CHAT: LogoutEvent bestaetigen
	 * 
	 * @param receivedPdu
	 *          . Empfangene PDU
	 */
	private void logoutEventConfirmAction(ChatPDU receivedPdu) {

		String eventInitiatorClient;
		String confirmSenderClient;

		log.debug("Logout-Event-Confirm-PDU von " + receivedPdu.getUserName()
				+ " fuer initierenden Client " + receivedPdu.getEventUserName() + " empfangen");

		// Empfangene Confirms hochzaehlen
		clients.incrNumberOfReceivedChatEventConfirms(receivedPdu.getEventUserName());
		confirmCounter.getAndIncrement();
		log.debug(userName + ": ConfirmCounter fuer Logout erhoeht = " + confirmCounter.get()
				+ ", Aktueller EventCounter = " + eventCounter.get());

		eventInitiatorClient = receivedPdu.getEventUserName();
		confirmSenderClient = receivedPdu.getUserName();
		log.debug("Logout-EventConfirm: Event-Initiator: " + eventInitiatorClient
				+ ", Confirm-Sender: " + confirmSenderClient);

		log.debug(confirmSenderClient + " aus der Warteliste von " + eventInitiatorClient
				+ " austragen");

		try {
			if ((clients.deleteWaitListEntry(eventInitiatorClient, confirmSenderClient) == 0)) {
				// Wenn der letzte Logout-Confirm ankommt, muss auch ein
				// Logout-Response gesendet werden

				log.debug("Warteliste von " + eventInitiatorClient
						+ " ist nun leer, alle Confirms fuer Logout erhalten");
				sendLogoutResponse(eventInitiatorClient);

				log.debug(
						eventInitiatorClient + ": EventCounter beim Logout = " + eventCounter.get()
								+ ", ConfirmCounter beim Logout-Response = " + confirmCounter.get());

				// Worker-Thread des Clients, der den Logout-Request gesendet
				// hat, auch gleich zum Beenden markieren
				clients.finish(eventInitiatorClient);
				log.debug("Laenge der Clientliste beim Vormerken zum Loeschen von "
						+ eventInitiatorClient + ": " + clients.size());
			}
		} catch (Exception e) {
			log.error("Logout-Event-Confirm-PDU fuer nicht vorhandenen Client erhalten: "
					+ receivedPdu.getEventUserName());
		}
	}

	/**
	 * Verbindung zu einem Client ordentlich abbauen
	 */
	private void closeConnection() {

		log.debug("Schliessen der Chat-Connection zum " + userName);

		// Bereinigen der Clientliste falls erforderlich

		if (clients.existsClient(userName)) {
			log.debug("Close Connection fuer " + userName
					+ ", Laenge der Clientliste vor dem bedingungslosen Loeschen: "
					+ clients.size());
			log.debug("Laenge der Warteliste des bedingungslos zu loeschenden Clients "
					+ userName + ": " + clients.getWaitListSize(userName));
			clients.deleteClientWithoutCondition(userName);
			log.debug("Laenge der Clientliste nach dem bedingungslosen Loeschen von " + userName
					+ ": " + clients.size());
		}

		try {
			connection.close();
		} catch (Exception e) {
			log.debug("Exception bei close");
			// ExceptionHandler.logException(e);
		}
	}

	/**
	 * Antwort-PDU fuer den initiierenden Client aufbauen und senden
	 * 
	 * @param eventInitiatorClient
	 *          Name des Clients
	 */
	private void sendLogoutResponse(String eventInitiatorClient) {

		ClientListEntry client = clients.getClient(eventInitiatorClient);

		if (client != null) {
			ChatPDU responsePdu = ChatPDU.createLogoutResponsePdu(eventInitiatorClient, 0, 0, 0,
					0, client.getNumberOfReceivedChatMessages(), clientThreadName);

			log.debug(eventInitiatorClient + ": Anzahl gesendeter Events aus Clientlist-Entry: "
					+ client.getNumberOfSentEvents() + ": ReceivedConfirms aus Clientliste: "
					+ client.getNumberOfReceivedEventConfirms());
			try {
				clients.getClient(eventInitiatorClient).getConnection().send(responsePdu);
			} catch (Exception e) {
				log.debug("Senden einer Logout-Response-PDU an " + eventInitiatorClient
						+ " fehlgeschlagen");
				log.debug("Exception Message: " + e.getMessage());
			}

			log.debug("Logout-Response-PDU an Client " + eventInitiatorClient + " gesendet");
		}
	}

	/**
	 * Prueft, ob Clients aus der Clientliste geloescht werden koennen.
	 * 
	 * @return boolean, true: Client geloescht, false: Client nicht geloescht
	 */
	private boolean checkIfClientIsDeletable() {

		ClientListEntry client;

		// Worker-Thread beenden, wenn sein Client schon abgemeldet ist
		if (userName != null) {
			client = clients.getClient(userName);
			if (client != null) {
				if (client.isFinished()) {
					// Loesche den Client aus der Clientliste
					// Ein Loeschen ist aber nur zulaessig, wenn der Client
					// nicht mehr in einer anderen Warteliste ist
					log.debug("Laenge der Clientliste vor dem Entfernen von " + userName + ": "
							+ clients.size());
					if (clients.deleteClient(userName) == true) {
						// Jetzt kann auch Worker-Thread beendet werden

						log.debug("Laenge der Clientliste nach dem Entfernen von " + userName + ": "
								+ clients.size());
						log.debug("Worker-Thread fuer " + userName + " zum Beenden vorgemerkt");
						return true;
					}
				}
			}
		}

		// Garbage Collection in der Clientliste durchfuehren
		Vector<String> deletedClients = clients.gcClientList();
		if (deletedClients.contains(userName)) {
			log.debug("Ueber Garbage Collector ermittelt: Laufender Worker-Thread fuer "
					+ userName + " kann beendet werden");
			return true;
		}
		return false;
	}

	@Override
	protected void handleIncomingMessage() throws Exception {

		if (checkIfClientIsDeletable() == true) {
			log.debug("User kann entfernt werden, checkIfClientIsDeletable liefert true fuer "
					+ userName);
			finished = true;
			return;
		}

		// Warten auf naechste Nachricht
		ChatPDU receivedPdu = null;
		// Wenn ein Client im Zustand UNREGISTERING ist, aber keine Nachricht mehr
		// bekommt wird die Verbindung nach einem Timeout serverseitig abgebrochen.
		final int RECEIVE_TIMEOUT = 120000;

		try {
			receivedPdu = (ChatPDU) connection.receive(RECEIVE_TIMEOUT);
			// Nachricht empfangen
			// Zeitmessung fuer Serverbearbeitungszeit starten
			startTime = System.nanoTime();

		} catch (ConnectionTimeoutException e) {

			// Wartezeit beim Empfang abgelaufen, pruefen, ob der Client
			// ueberhaupt noch etwas sendet
			log.error("Timeout beim Empfangen, " + RECEIVE_TIMEOUT
					+ " ms ohne Nachricht vom Client fuer User: " + userName);

			if (clients.getClient(userName) != null) {
				if (clients.getClient(userName)
						.getStatus() == ClientConversationStatus.UNREGISTERING) {
					// Worker-Thread wartet auf eine Nachricht vom Client, aber es
					// kommt nichts mehr an
					log.error(
							"Client ist im Zustand UNREGISTERING und bekommt aber keine Nachricht mehr");
					// Zur Sicherheit eine Logout-Response-PDU an Client senden und
					// dann Worker-Thread beenden
					sendLogoutResponse(userName);
					finished = true;
				}
			}
			return;

		} catch (EndOfFileException e) {
			log.debug("End of File beim Empfang, vermutlich Verbindungsabbau des Partners fuer "
					+ userName);
			finished = true;
			return;

		} catch (java.net.SocketException e) {
			log.error("Verbindungsabbruch beim Empfang der naechsten Nachricht vom Client "
					+ userName);
			finished = true;
			return;

		} catch (Exception e) {
			log.error(
					"Empfang einer Nachricht fehlgeschlagen, Workerthread fuer User: " + userName);
			ExceptionHandler.logException(e);
			finished = true;
			return;
		}

		// Empfangene Nachricht bearbeiten
		try {
			switch (receivedPdu.getPduType()) {

			case LOGIN_REQUEST:
				// Login-Request vom Client empfangen
				loginRequestAction(receivedPdu);
				break;

			case CHAT_MESSAGE_REQUEST:
				// Chat-Nachricht angekommen, an alle verteilen
				chatMessageRequestAction(receivedPdu);
				break;

			case LOGOUT_REQUEST:
				// Logout-Request vom Client empfangen
				logoutRequestAction(receivedPdu);
				break;

			case LOGIN_EVENT_CONFIRM:
				// Bestaetigung eines Login-Events angekommen
				try {
					loginEventConfirmAction(receivedPdu);
				} catch (Exception e) {
					ExceptionHandler.logException(e);
				}
				break;

			case LOGOUT_EVENT_CONFIRM:
				// Bestaetigung eines Logout-Events angekommen
				logoutEventConfirmAction(receivedPdu);
				break;

			case CHAT_MESSAGE_EVENT_CONFIRM:
				// Bestaetigung eines Chat-Events angekommen
				chatMessageEventConfirmAction(receivedPdu);
				break;

			default:
				log.debug("Falsche PDU empfangen von Client: " + receivedPdu.getUserName()
						+ ", PduType: " + receivedPdu.getPduType());
				break;
			}
		} catch (Exception e) {
			log.error("Exception bei der Nachrichtenverarbeitung");
			ExceptionHandler.logExceptionAndTerminate(e);
		}
	}
}
