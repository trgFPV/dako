package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.AuditLogPDU;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AuditLogUdpServer {

	private static Logger log = Logger.getLogger(AuditLogUdpServer.class);

	// UDP-Serverport fuer AuditLog-Service
	static final int AUDIT_LOG_SERVER_PORT = 40001;

	// Standard-Puffergroessen fuer Serverport in Bytes
	static final int DEFAULT_SENDBUFFER_SIZE = 30000;
	static final int DEFAULT_RECEIVEBUFFER_SIZE = 800000;

	// Name der AuditLog-Datei
	static final String auditLogFile = new String("ChatAuditLog.dat");

	// Zaehler fuer ankommende AuditLog-PDUs
	protected long counter = 0;

	public static void main(String[] args) {
		PropertyConfigurator.configureAndWatch("log4j.auditLogServer_udp.properties", 60 * 1000);
		System.out.println("AuditLog-UdpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);
		log.info("AuditLog-UdpServer gestartet, Port" + AUDIT_LOG_SERVER_PORT);

		//TODO: Implementierung des AuditLogServers auf UDP-Basis hier ergaenzen
	}
}