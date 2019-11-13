package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.ImplementationType;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionFactory;
import edu.hm.dako.chat.connection.LoggingConnectionDecorator;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import edu.hm.dako.chat.tcp.TcpServerSocket;
import edu.hm.dako.chat.udp.UdpServerSocket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;

/**
 * Uebernimmt die Konfiguration und Erzeugung bestimmter Server-Typen.
 *
 * @author Peter Mandl
 */
public final class AuditLogServerFactory {
  private static Log log = LogFactory.getLog(AuditLogServerFactory.class);

  // Connection Factory und Verbindung zum Server
  protected static ConnectionFactory connectionFactory;
  protected static Connection connection;


  private AuditLogServerFactory() {
  }

  /**
   * Erzeugt einen Server
   *
   */
  public static AuditLogServerImpl getServer(
          ImplementationType implType,
          final int serverPort,
          final int sendBufferSize,
          final int receiveBufferSize) throws Exception {

    // Logging
    log.debug("AuditLogServer (" + implType.toString() + ") wird gestartet, Serverport: "
            + serverPort + ", Sendepuffer: " + sendBufferSize + ", Empfangspuffer: "
            + receiveBufferSize);
    System.out.println("AuditLogServer (" + implType.toString()
            + ") wird gestartet, Listen-Port: " + serverPort + ", Sendepuffer: "
            + sendBufferSize + ", Empfangspuffer: " + receiveBufferSize);


    switch (implType) {

      case TCPAdvancedImplementation:

        try {

          TcpServerSocket tcpServerSocket =
                  new TcpServerSocket(
                          serverPort,
                          sendBufferSize,
                          receiveBufferSize);

          return new AuditLogServerImpl(
                          Executors.newCachedThreadPool(),
                          getDecoratedServerSocket(tcpServerSocket));

        } catch (Exception e) {
          throw new Exception(e);
        }

      case UDPAdvancedImplementation:

        try {
          UdpServerSocket udpServerSocket = new UdpServerSocket(serverPort, sendBufferSize,
                  receiveBufferSize);
          return new AuditLogServerImpl(Executors.newCachedThreadPool(),
                  getDecoratedServerSocket(udpServerSocket));
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
