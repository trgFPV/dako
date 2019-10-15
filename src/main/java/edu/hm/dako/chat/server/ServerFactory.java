package edu.hm.dako.chat.server;

import java.util.concurrent.Executors;

import edu.hm.dako.chat.common.AuditLogImplementationType;
import edu.hm.dako.chat.connection.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.ImplementationType;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.LoggingConnectionDecorator;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import edu.hm.dako.chat.tcp.TcpServerSocket;

/**
 * Uebernimmt die Konfiguration und Erzeugung bestimmter Server-Typen.
 * 
 * @author Peter Mandl
 */
public final class ServerFactory {
	private static Log log = LogFactory.getLog(ServerFactory.class);

	// Connection Factory und Verbindung zum Server
	protected static ConnectionFactory connectionFactory;
	protected static Connection connection;
	protected static AuditLogConnection auditLogConnection = null;

	private ServerFactory() {
	}

	/**
	 * Erzeugt einen Chat-Server
	 * 
	 * @param implType
	 *          Implementierungytyp des Servers
	 * @param serverPort
	 *          Listenport
	 * @param sendBufferSize
	 *          Groesse des Sendepuffers in Byte
	 * @param receiveBufferSize
	 *          Groesse des Empfangspuffers in Byte
	 * @param serverGuiInterface
	 *          Referenz auf GUI fuer Callback
	 * @return
	 * @throws Exception
	 */
	public static ChatServerInterface getServer(ImplementationType implType, int serverPort,
			int sendBufferSize, int receiveBufferSize,
			ChatServerGuiInterface serverGuiInterface) throws Exception {
		log.debug("ChatServer (" + implType.toString() + ") wird gestartet, Serverport: "
				+ serverPort + ", Sendepuffer: " + sendBufferSize + ", Empfangspuffer: "
				+ receiveBufferSize);
		System.out.println("ChatServer (" + implType.toString()
				+ ") wird gestartet, Listen-Port: " + serverPort + ", Sendepuffer: "
				+ sendBufferSize + ", Empfangspuffer: " + receiveBufferSize);
		
		switch (implType) {

		case TCPSimpleImplementation:

			try {
				TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
						receiveBufferSize);
				return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
						getDecoratedServerSocket(tcpServerSocket), serverGuiInterface);
			} catch (Exception e) {
				throw new Exception(e);
			}

		case TCPAdvancedImplementation:

			try {
				TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
						receiveBufferSize);
				return new AdvancedChatServerImpl(Executors.newCachedThreadPool(),
						getDecoratedServerSocket(tcpServerSocket), serverGuiInterface);
			} catch (Exception e) {
				throw new Exception(e);
			}

			/*
			 * return new AdvancedChatServerImpl(Executors.newCachedThreadPool(),
			 * getDecoratedServerSocket( new TcpServerSocket(serverPort,
			 * sendBufferSize, receiveBufferSize)), serverGuiInterface);
			 */
		default:
			System.out.println("Dezeit nur TCP implementiert!");
			throw new RuntimeException("Unknown type: " + implType);
		}
	}

	public static ChatServerInterface getServerWithAuditLog(ImplementationType implType, int serverPort,
														   int sendBufferSize, int receiveBufferSize,
														   ChatServerGuiInterface serverGuiInterface,
														   AuditLogImplementationType auditLogImplementationType,
														   String auditLogServerHostnameOrIP, int auditLogServerPort) throws Exception {
		
		// Zunaechst Verbindung zum AuditLog-Server aufbauen
		log.debug("ChatServer wird mit AuditLogServer gestartet, ChatServer Port: " + serverPort + ", Sendepuffer: " + sendBufferSize + ", Empfangspuffer: "
					+ receiveBufferSize + ", AuditLogServer Port: " + auditLogServerPort + ", AuditLogServer Hostname or IP: "
					+ auditLogServerHostnameOrIP);
		
		// Verbindung zum AuditLog-Server aufbauen
		
		int tcpOrUdp; 
			
		if (auditLogImplementationType == AuditLogImplementationType.AuditLogServerTCPImplementation) {
			tcpOrUdp = AuditLogConnection.AUDITLOG_CONNECTION_TYPE_TCP;
		} else {
			tcpOrUdp = AuditLogConnection.AUDITLOG_CONNECTION_TYPE_UDP;			
		}			
		
		try {				
			auditLogConnection = new AuditLogConnection(tcpOrUdp, auditLogServerHostnameOrIP, auditLogServerPort);					
			auditLogConnection.connectToAuditLogServer();					
				log.debug("Verbindung zum AuditLog Server aufgebaut");
		} catch (Exception e) {
			// AuditLogServer nicht ordentlich initialisiert
			auditLogConnection = null;
			//ExceptionHandler.logException(e);
			log.debug("Verbindung zum AuditLog-Server konnte nicht aufgebaut werden");
			
			// Server arbeitet ohne AuditLog-Server
		}
		
		// Dann Chat-Server mit Verbindungsendpunkt erzeugen
		
		switch (implType) {

		case TCPSimpleImplementation:

			try {
				TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
						receiveBufferSize);
				return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
						getDecoratedServerSocket(tcpServerSocket), serverGuiInterface, auditLogConnection);
			} catch (Exception e) {
				throw new Exception(e);
			}

		case TCPAdvancedImplementation:

			// Geht derzeit nur ohne AuditLog
			try {
				TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
						receiveBufferSize);
				return new AdvancedChatServerImpl(Executors.newCachedThreadPool(),
						getDecoratedServerSocket(tcpServerSocket), serverGuiInterface);
			} catch (Exception e) {
				throw new Exception(e);
			}
			
		default:
			System.out.println("Dezeit nur TCP implementiert!");
			throw new RuntimeException("Unknown type: " + implType);
		}
	}

	/**
	 * Dekoratiert ServerSocket mit Logging-Funktionalitaet
	 * 
	 * @param serverSocket
	 * @return
	 */
	private static ServerSocketInterface getDecoratedServerSocket(
			ServerSocketInterface serverSocket) {
		return new DecoratingServerSocket(serverSocket);
	}

	/**
	 * Pruefe, ob AuditLog-Server verbunden ist
	 * 
	 * @return Verbindung aufgebaut = true
	 */
	public static boolean isAuditLogServerConnected () {
		if (auditLogConnection == null) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * Dekoriert Server-Socket mit Logging-Funktionalitaet
	 * 
	 * @author mandl
	 *
	 */
	private static class DecoratingServerSocket implements ServerSocketInterface {

		private final ServerSocketInterface wrappedServerSocket;

		DecoratingServerSocket(ServerSocketInterface wrappedServerSocket) {
			this.wrappedServerSocket = wrappedServerSocket;
		}

		@Override
		public Connection accept() throws Exception {
			return new LoggingConnectionDecorator(wrappedServerSocket.accept());
		}

		@Override
		public void close() throws Exception {
			wrappedServerSocket.close();
		}

		@Override
		public boolean isClosed() {
			return wrappedServerSocket.isClosed();
		}
	}
}
