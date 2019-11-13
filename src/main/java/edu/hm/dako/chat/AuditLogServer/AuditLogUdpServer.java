package edu.hm.dako.chat.AuditLogServer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * UDP Version of AuditLogServer
 */
public class AuditLogUdpServer {

	private static Logger log = Logger.getLogger(AuditLogUdpServer.class);

	private static GenericServerInterface genericServerInterface;
	private static final int AUDIT_LOG_SERVER_PORT = 40001;
	static final int DEFAULT_SENDBUFFER_SIZE = 30000;
	static final int DEFAULT_RECEIVEBUFFER_SIZE = 800000;
	static final String auditLogFile = new String("ChatAuditLog.dat");
	protected long incomingPduCounter = 0;

	public static void main(String[] args) throws Exception {

		PropertyConfigurator.configureAndWatch("log4j.auditLogServer_udp.properties", 60 * 1000);
		System.out.println("AuditLog-UdpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);

		// TODO needs implementation. look at the TCP version

		log.info("AuditLog-UdpServer gestartet, Port" + AUDIT_LOG_SERVER_PORT);
	}

}
