package edu.hm.dako.chat.server;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.ChatPDU;
import edu.hm.dako.chat.common.ExceptionHandler;

import edu.hm.dako.chat.udp.UdpClientConnection;
import edu.hm.dako.chat.udp.UdpClientConnectionFactory;


import edu.hm.dako.chat.tcp.TcpConnection;
import edu.hm.dako.chat.tcp.TcpConnectionFactory;

import edu.hm.dako.chat.common.AuditLogPDU;
import edu.hm.dako.chat.common.AuditLogPduType;

/**
 * Verwaltet eine logische Verbindung zum AuditLog-Server ueber UDP oder TCP
 * 
 * @author mandl
 *
 */
public class AuditLogConnection {

	private static Log log = LogFactory.getLog(AuditLogConnection.class);
	
	// Verbindung zum AuditLog-Server und Verbindungsparameter
	private UdpClientConnectionFactory udpFactory = null;
	protected UdpClientConnection udpConnectionToAuditLogServer = null;
	
	private TcpConnectionFactory tcpFactory = null;
	protected TcpConnection tcpConnectionToAuditLogServer = null;
	
	// Puffergroessen
	static final int DEFAULT_SENDBUFFER_AUDITLOG_SIZE = 400000;
	static  final int DEFAULT_RECEIVEBUFFER_AUDITLOG_SIZE = 40000;
	
	String auditLogServer = null;
	int auditLogPort;
	
	// Zaehlt abgehende AuditLog-Saetze
	private long counter = 0;	
	
	// Verbindungstyp
	private int connectionType; // UDP oder TCP
	public static final int AUDITLOG_CONNECTION_TYPE_TCP = 1;
	public static final int AUDITLOG_CONNECTION_TYPE_UDP = 2;
	
	/**
	 * Konstruktor
	 */
	public AuditLogConnection(int connectionType, String auditLogServer, int auditLogPort) {
		
		this.auditLogServer = auditLogServer;
		this.auditLogPort = auditLogPort;
		
		if ((connectionType != AUDITLOG_CONNECTION_TYPE_TCP) && 
				(connectionType != AUDITLOG_CONNECTION_TYPE_UDP)) {
			this.connectionType = AUDITLOG_CONNECTION_TYPE_TCP;
		} else {
			this.connectionType = connectionType;
		}
	}
	
	/**
	 * Logische Verbindung zum AuditLog-Server aufbauen
	 * 
	 * @throws Exception
	 */
	public void connectToAuditLogServer() throws Exception {
		try {
			
			if (connectionType == AUDITLOG_CONNECTION_TYPE_UDP) {
				udpFactory = new UdpClientConnectionFactory();
				udpConnectionToAuditLogServer = (UdpClientConnection) udpFactory.connectToServer(auditLogServer, 
						auditLogPort, 0, DEFAULT_SENDBUFFER_AUDITLOG_SIZE, DEFAULT_RECEIVEBUFFER_AUDITLOG_SIZE);
			
			} else {
			
				tcpFactory = new TcpConnectionFactory();
				tcpConnectionToAuditLogServer = (TcpConnection) tcpFactory.connectToServer(auditLogServer, 
					auditLogPort, 0, DEFAULT_SENDBUFFER_AUDITLOG_SIZE, DEFAULT_RECEIVEBUFFER_AUDITLOG_SIZE);
			
			}
			System.out.println("Verbindung zu AuditLog-Server steht");
		} catch (Exception e) {
			log.error("Exception bei Verbindungsaufbau zum Auditlog-Server");
			//ExceptionHandler.logExceptionAndTerminate(e);
			throw new Exception();
		}
	}
	
	/**
	 * Senden eines AuditLog-Satzes zum AuditLog-Server
	 * 
	 * @param pdu Chat-PDU zum Entnehmen von Parametern für den AuditLog-Satz
	 * @param type Typ der AuditLog-PDU, der zu senden ist
	 * 
	 * @throws Exception
	 */
	public synchronized void send(ChatPDU pdu, AuditLogPduType type) throws Exception {
		
		// AuditLog-Satz erzeugen
		AuditLogPDU auditLogPdu = createAuditLogPdu(pdu);
		auditLogPdu.setPduType(type);
		auditLogPdu.setMessage(pdu.getMessage());
		
		// AuditLog-Satz senden
		try {			
			if (connectionType == AUDITLOG_CONNECTION_TYPE_UDP) {
				udpConnectionToAuditLogServer.send(auditLogPdu);
			} else {
				tcpConnectionToAuditLogServer.send(auditLogPdu);
			}
			counter++;
			log.trace("AuditLog-Satz gesendet: " + counter);
			
		} catch (Exception e) {
			System.out.println("Fehler beim Senden eines AuditLog-Satzes");
			ExceptionHandler.logException(e);
			throw new Exception();
		}
	}
	
	/**
	 * Schliessen der Verbindung zum AuditLog-Server
	 * 
	 * @throws Exception
	 */
	public synchronized void close() throws Exception {
		try {			
			if (connectionType == AUDITLOG_CONNECTION_TYPE_UDP) {
				udpConnectionToAuditLogServer.close();
			} else {
				tcpConnectionToAuditLogServer.close();
			}
			System.out.println("Verbindung zum AuditLog-Server beendet, Gesendete AuditLog-Saetze: " + counter);
					
		} catch (Exception e) {
			System.out.println("Fehler beim Schliessen der Verbindung zum AuditLog-Server");
			ExceptionHandler.logException(e);
			throw new Exception();
		}
	}
	
	/**
	 * AuditLog-PDU erzeugen
	 * 
	 * @return PDU
	 */
	private AuditLogPDU createAuditLogPdu(ChatPDU chatPdu) {

		AuditLogPDU pdu = new AuditLogPDU();
		pdu.setPduType(AuditLogPduType.UNDEFINED);
		pdu.setAuditTime(System.currentTimeMillis());
		pdu.setUserName(chatPdu.getUserName());
		pdu.setClientThreadName(chatPdu.getClientThreadName());
		pdu.setServerThreadName(chatPdu.getServerThreadName());
		pdu.setMessage(null);
		return (pdu);
	}
}