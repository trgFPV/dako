package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.AuditLogPDU;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.tcp.TcpServerSocket;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;


/**
 * AuditLog Server fuer die Protokollierung von Chat-Nachrichten eines Chat-Servers.
 * Implementierung auf Basis von TCP.
 *
 * @author mandl
 */
public class AuditLogTcpServer {
    private static Logger log = Logger.getLogger(AuditLogTcpServer.class);

    // Serverport fuer AuditLog-Service
    static final int AUDIT_LOG_SERVER_PORT = 40001;

    // Standard-Puffergroessen fuer Serverport in Bytes
    static final int DEFAULT_SENDBUFFER_SIZE = 30000;
    static final int DEFAULT_RECEIVEBUFFER_SIZE = 800000;

    // Name der AuditLog-Datei
    static final String auditLogFile = new String("ChatAuditLog_TCP.c" +
            "sv");
    static final int PORT = 50001;

    // Zaehler fuer ankommende AuditLog-PDUs
    protected long counter = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Thread auditLogTcpServerThread = new Thread(new AuditLogTcpServerThread());
        auditLogTcpServerThread.start();
        System.out.println("Type anything + Enter to exit");
        scanner.next();
        System.exit(0);


    }

    static class AuditLogTcpServerThread implements Runnable {

        @Override
        public void run() {
            PropertyConfigurator.configureAndWatch("log4j.auditLogServer_tcp.properties", 60 * 1000);
            System.out.println("AuditLog-TcpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);

            //TODO: Implementierung des AuditLogServers auf TCP-Basis hier ergaenzen
            try {
                TcpServerSocket socket = new TcpServerSocket(AUDIT_LOG_SERVER_PORT, DEFAULT_SENDBUFFER_SIZE, DEFAULT_RECEIVEBUFFER_SIZE);
                Connection connection = socket.accept();

                CSVAuditLogWriter calw = new CSVAuditLogWriter(auditLogFile);

                System.out.println("Recieved connection from Chat-Server");

                while (true) {
                    AuditLogPDU recievedPdu = (AuditLogPDU) connection.receive();
                    calw.writeAuditLogPDU(recievedPdu);
                    log.info("CSV-Line written");
                }

            } catch (BindException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Lost Connection to Chatserver, Exiting");
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
