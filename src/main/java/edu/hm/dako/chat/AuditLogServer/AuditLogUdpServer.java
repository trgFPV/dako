package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.AuditLogPDU;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.udp.UdpServerSocket;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

public class AuditLogUdpServer {

  private static Logger log = Logger.getLogger(AuditLogUdpServer.class);

  // UDP-Serverport fuer AuditLog-Service
  static final int AUDIT_LOG_SERVER_PORT = 40001;

  // Standard-Puffergroessen fuer Serverport in Bytes
  static final int DEFAULT_SENDBUFFER_SIZE = 30000;
  static final int DEFAULT_RECEIVEBUFFER_SIZE = 800000;

  // Name der AuditLog-Datei
  static final String auditLogFile = new String("ChatAuditLog_UDP.csv");

  // Zaehler fuer ankommende AuditLog-PDUs
  protected long counter = 0;

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Thread auditLogUdpServerThread = new Thread(new AuditLogUdpServerThread());
    auditLogUdpServerThread.start();
    System.out.println("Type anything + Enter to exit");
    scanner.next();
    System.exit(0);
  }

  static class AuditLogUdpServerThread implements Runnable {

    @Override
    public void run() {
      PropertyConfigurator.configureAndWatch("log4j.auditLogServer_udp.properties", 60 * 1000);
      System.out.println("AuditLog-UdpServer gestartet, Port: " + AUDIT_LOG_SERVER_PORT);


      try {
        UdpServerSocket socket = new UdpServerSocket(AUDIT_LOG_SERVER_PORT, DEFAULT_SENDBUFFER_SIZE, DEFAULT_RECEIVEBUFFER_SIZE);
        Connection udpConnection = socket.accept();

        CSVAuditLogWriter calw = new CSVAuditLogWriter(auditLogFile);

        log.info("AuditLog-UdpServer gestartet, Port" + AUDIT_LOG_SERVER_PORT);

        while (true) {
          AuditLogPDU recievedPdu = (AuditLogPDU) udpConnection.receive();
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
