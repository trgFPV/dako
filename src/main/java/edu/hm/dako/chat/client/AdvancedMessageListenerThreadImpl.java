package edu.hm.dako.chat.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.ChatPDU;
import edu.hm.dako.chat.common.ClientConversationStatus;
import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.connection.Connection;


/**
 * Thread wartet auf ankommende Nachrichten vom Server und bearbeitet diese.
 * 
 * @author Peter Mandl
 *
 */
public class AdvancedMessageListenerThreadImpl extends AbstractMessageListenerThread {

	private static Log log = LogFactory.getLog(AdvancedMessageListenerThreadImpl.class);

	public AdvancedMessageListenerThreadImpl(ClientUserInterface userInterface,
			Connection con, SharedClientData sharedData) {

		super(userInterface, con, sharedData);
	}

	@Override
	protected void loginResponseAction(ChatPDU receivedPdu) {

		if (receivedPdu.getErrorCode() == ChatPDU.LOGIN_ERROR) {
			// Login hat nicht funktioniert
			log.debug("Login-Response-PDU fuer Client " + receivedPdu.getUserName()
					+ " mit Login-Error empfangen");
			userInterface.setErrorMessage(
					"Chat-Server", "Anmelden beim Server nicht erfolgreich, Benutzer "
							+ receivedPdu.getUserName() + " vermutlich schon angemeldet",
					receivedPdu.getErrorCode());
			sharedClientData.status = ClientConversationStatus.UNREGISTERED;

			// Verbindung wird gleich geschlossen
			try {
				connection.close();
			} catch (Exception e) {
			}
		} else {
			// Login hat funktioniert
			sharedClientData.status = ClientConversationStatus.REGISTERED;
			try {
				userInterface.loginComplete();
			} catch (Exception e) {
			}

			Thread.currentThread().setName("Listener" + "-" + sharedClientData.userName);
			log.debug(
					"Login-Response-PDU fuer Client " + receivedPdu.getUserName() + " empfangen");
		}
	}

	@Override
	protected void loginEventAction(ChatPDU receivedPdu) {

		// Eventzaehler fuer Testzwecke erhoehen
		sharedClientData.eventCounter.getAndIncrement();
		int events = SharedClientData.loginEvents.incrementAndGet();

		log.debug(
				sharedClientData.userName + " erhaelt LoginEvent, LoginEventCounter: " + events);

		try {
			handleUserListEvent(receivedPdu);
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}

		// ADVANCED_CHAT: Bestaetigung senden
		confirmLoginEvent(receivedPdu);

		// ADVANCED_CHAT:ConfirmCounter erhoehen
		sharedClientData.confirmCounter.getAndIncrement();
	}

	@Override
	protected void logoutResponseAction(ChatPDU receivedPdu) {

		log.debug(sharedClientData.userName + " empfaengt Logout-Response-PDU fuer Client "
				+ receivedPdu.getUserName());
		sharedClientData.status = ClientConversationStatus.UNREGISTERED;

		userInterface.setSessionStatisticsCounter(sharedClientData.eventCounter.longValue(),
				sharedClientData.confirmCounter.longValue(), 0, 0, 0);

		log.debug("Vom Client gesendete Chat-Nachrichten:  "
				+ sharedClientData.messageCounter.get());

		finished = true;
		userInterface.logoutComplete();
	}

	@Override
	protected void logoutEventAction(ChatPDU receivedPdu) {

		log.debug(sharedClientData.userName + " empfaengt Logout-Event-PDU fuer Client "
				+ receivedPdu.getUserName());
		log.debug(sharedClientData.userName + ": Clientliste: " + receivedPdu.getClients());

		// Eventzaehler fuer Testzwecke erhoehen
		sharedClientData.eventCounter.getAndIncrement();
		int events = SharedClientData.logoutEvents.incrementAndGet();

		log.debug("LogoutEventCounter: " + events);

		try {
			handleUserListEvent(receivedPdu);
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}

		// ADVANCED_CHAT: Bestaetigung senden
		confirmLogoutEvent(receivedPdu);

		// ADVANCED_CHAT: Confirmation-Zaehler erhoehen
		sharedClientData.confirmCounter.getAndIncrement();
	}

	@Override
	protected void chatMessageResponseAction(ChatPDU receivedPdu) {

		log.debug("Sequenznummer der Chat-Response-PDU " + receivedPdu.getUserName() + ": "
				+ receivedPdu.getSequenceNumber() + ", Messagecounter: "
				+ sharedClientData.messageCounter.get());

		if (receivedPdu.getSequenceNumber() == sharedClientData.messageCounter.get()) {

			// Zuletzt gemessene Serverzeit fuer das Benchmarking
			// merken
			userInterface.setLastServerTime(receivedPdu.getServerTime());

			// Naechste Chat-Nachricht darf eingegeben werden
			userInterface.setLock(false);

			log.debug("Chat-Response-PDU fuer Client " + receivedPdu.getUserName()
					+ " empfangen, Serverbearbeitungszeit: "
					+ +receivedPdu.getServerTime() / 1000000 + " ms");

		} else {
			log.debug("Sequenznummer der Chat-Response-PDU " + receivedPdu.getUserName()
					+ " passt nicht: " + receivedPdu.getSequenceNumber() + "/"
					+ sharedClientData.messageCounter.get());
		}
	}

	@Override
	protected void chatMessageEventAction(ChatPDU receivedPdu) {

		log.debug(
				"Chat-Message-Event-PDU von " + receivedPdu.getEventUserName() + " empfangen");

		// Eventzaehler erhoehen
		sharedClientData.eventCounter.getAndIncrement();
		int events = SharedClientData.messageEvents.incrementAndGet();

		log.debug("MessageEventCounter: " + events);

		// ADVANCED_CHAT:Chat-Message-Event bestaetigen
		confirmChatMessageEvent(receivedPdu);

		// ADVANCED_CHAT:ConfirmCounter erhoehen
		sharedClientData.confirmCounter.getAndIncrement();

		// Empfangene Chat-Nachricht an User Interface zur
		// Darstellung uebergeben
		userInterface.setMessageLine(receivedPdu.getEventUserName(),
				(String) receivedPdu.getMessage());
	}

	/**
	 * ADVANCED_CHAT: Bestaetigung fuer eine Chat-Event-Message-PDU an Server
	 * senden
	 * 
	 * @param receivedPdu
	 *          Empfangene Chat-Event-Message-PDU
	 * @throws Exception
	 */
	private void confirmChatMessageEvent(ChatPDU receivedPdu) {

		ChatPDU responsePdu = ChatPDU.createChatMessageEventConfirm(sharedClientData.userName,
				receivedPdu);

		try {
			connection.send(responsePdu);
			log.debug("Chat-Message-Event-Confirm-PDU fuer " + receivedPdu.getUserName()
					+ " bzgl. eines urspruenglichen Events von " + receivedPdu.getEventUserName()
					+ " an den Server gesendet");
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * ADVANCED_CHAT:Bestaetigung fuer eine Login-Event-PDU an Server senden
	 * 
	 * @param receivedPdu
	 *          Empfangene Chat-Event-Message-PDU
	 * @throws Exception
	 */
	private void confirmLoginEvent(ChatPDU receivedPdu) {

		ChatPDU responsePdu = ChatPDU.createLoginEventConfirm(sharedClientData.userName,
				receivedPdu);

		try {
			connection.send(responsePdu);
			log.debug("Login-Event-Confirm-PDU fuer " + receivedPdu.getUserName()
					+ " bzgl. eines urspruenglichen Events von " + receivedPdu.getEventUserName()
					+ " an den Server gesendet");
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * ADVANCED_CHAT: Bestaetigung fuer eine Logout-Event-PDU an Server senden
	 * 
	 * @param receivedPdu
	 *          Empfangene Chat-Event-Message-PDU
	 * @throws Exception
	 */
	private void confirmLogoutEvent(ChatPDU receivedPdu) {

		ChatPDU responsePdu = ChatPDU.createLogoutEventConfirm(sharedClientData.userName,
				receivedPdu);

		try {
			connection.send(responsePdu);
			log.debug("Logout-Event-Confirm-PDU fuer " + receivedPdu.getUserName()
					+ " bzgl. eines urspruenglichen Events von " + receivedPdu.getEventUserName()
					+ " an den Server gesendet");
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
	}

	/**
	 * Bearbeitung aller vom Server gesendeten Nachrichten
	 */
	public void run() {

		ChatPDU receivedPdu = null;

		while (!finished) {

			try {
				// Naechste ankommende Nachricht empfangen
				log.debug("Auf die naechste Nachricht vom Server warten");
				receivedPdu = receive();
				log.debug("Nach receive Aufruf, ankommende PDU mit PduType = "
						+ receivedPdu.getPduType());
			} catch (Exception e) {
				finished = true;
			}

			if (receivedPdu != null) {

				switch (sharedClientData.status) {

				case REGISTERING:

					switch (receivedPdu.getPduType()) {

					case LOGIN_RESPONSE:
						// Login-Bestaetigung vom Server angekommen
						loginResponseAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);

						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);

						break;

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
					}
					break;

				case REGISTERED:

					switch (receivedPdu.getPduType()) {

					case CHAT_MESSAGE_RESPONSE:

						// Die eigene zuletzt gesendete Chat-Nachricht wird vom
						// Server bestaetigt.
						chatMessageResponseAction(receivedPdu);
						break;

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);
						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);
						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
					}
					break;

				case UNREGISTERING:

					switch (receivedPdu.getPduType()) {

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;

					case LOGOUT_RESPONSE:
						// Bestaetigung des eigenen Logout
						logoutResponseAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);
						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);
						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
						break;
					}
					break;

				case UNREGISTERED:
					log.debug(
							"Ankommende PDU im Zustand " + sharedClientData.status + " wird verworfen");
					break;

				default:
					log.debug("Unzulaessiger Zustand " + sharedClientData.status);
				}
			}
		}

		// Verbindung noch schliessen
		try {
			connection.close();
		} catch (Exception e) {
			ExceptionHandler.logException(e);
		}
		log.debug("Ordnungsgemaesses Ende des AdvancedMessage-Listener-Threads fuer User"
				+ sharedClientData.userName + ", Status: " + sharedClientData.status);
	} // run
}