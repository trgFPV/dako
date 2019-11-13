package edu.hm.dako.chat.AuditLogServer;

import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import javafx.concurrent.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ExecutorService;

public class AuditLogServerImpl implements GenericServerInterface {

  private static Log log = LogFactory.getLog(AuditLogServerImpl.class);

  private final ExecutorService executorService;
  private ServerSocketInterface socket;
  /*private ChatServerConnection ChatServerConnection;*/

  /**
   * Default ctor
   * @param executorService
   * @param socket
   */
  public AuditLogServerImpl(
          final ExecutorService executorService,
          final ServerSocketInterface socket) {

    this.executorService = executorService;
    this.socket = socket;
    /*this.ChatServerConnection = null;*/

  }

  @Override
  public void start() {

    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {

        System.out.println("I am finally here :)");

        // Solange der Thread nicht unterbrochen wurde und der Socket nicht geschlossen ist
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
          try {
            // Auf eingehende PDU nachrichten warten
            System.out.println(
                    "AuditLogServer wartet auf eingehende Verbindungsanfrage vom ChatServer...");

            Connection connection = socket.accept();
            log.debug("Verbindungsanfrage vom ChatServer empfangen");

          } catch (Exception e) {
            if (socket.isClosed()) {
              log.debug("Socket wurde geschlossen");
            } else {
              log.error(
                      "Exception beim Entgegennehmen von Verbindungsaufbauwuenschen: " + e);
              ExceptionHandler.logException(e);
            }
          }
        }
        return null;
      }
    };
    Thread th = new Thread(task);
    th.setDaemon(true);
    th.start();
  }

  @Override
  public void stop() throws Exception {

    // Cut connection to ChatServer
    Thread.currentThread().interrupt();
    socket.close();
    log.debug("Listen-Socket geschlossen");

    // Verbindung zu Chat-Server schliessen
    /*if (chatServerConnection != null) {
      chatServerConnection.close();
      log.debug("ChatServer Connection closed");
    }*/

    // Threadpool schliessen
    executorService.shutdown();
    log.debug("Threadpool freigegeben");

    System.out.println("AuditLogServer beendet sich");
  }
}
