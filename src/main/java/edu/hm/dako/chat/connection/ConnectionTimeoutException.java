package edu.hm.dako.chat.connection;

/**
 * Timeout-Exception bei Verbindung
 * 
 * @author mandl
 */
public class ConnectionTimeoutException extends Exception {

  private static final long serialVersionUID = 1L;

  public ConnectionTimeoutException(Exception e) {
    super("Timeout bei Verbindung aufgetreten");
  }
}
