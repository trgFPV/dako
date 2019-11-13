package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.ImplementationType;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * AuditLog Server fuer die Protokollierung von Chat-Nachrichten eines Chat-Servers. 
 * Implementierung auf Basis von TCP.
 */
public class AuditLogTcpServer {

	private static Logger log = Logger.getLogger(AuditLogTcpServer.class);
	private static final int AUDIT_LOG_SERVER_PORT = 40001;
	private static final int DEFAULT_SENDBUFFER_SIZE = 30000;
	private static final int DEFAULT_RECEIVEBUFFER_SIZE = 800000;
	static final String auditLogFile = new String("ChatAuditLog.dat");
	protected long incomingPduCounter = 0;

	public static void main(String[] args) {

		// Logging
		PropertyConfigurator.configureAndWatch(
						"log4j.auditLogServer_tcp.properties",
						60 * 1000
		);


		// Getting the server
		try {

			AuditLogServerImpl adlTcpServer = AuditLogServerFactory.getServer(
							ImplementationType.TCPAdvancedImplementation,
							40001,
							DEFAULT_SENDBUFFER_SIZE,
							DEFAULT_RECEIVEBUFFER_SIZE);

			adlTcpServer.start();

			System.out.println("AuditLog-TcpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);
			log.info("AuditLog-TcpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
