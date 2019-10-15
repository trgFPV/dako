package edu.hm.dako.chat.connection;

/**
 * End of File bei Verbindung, verursacht durch einen Verbindungsabbau des Partners
 * 
 * @author mandl
 */
public class EndOfFileException extends Exception {

  private static final long serialVersionUID = 2L;

  public EndOfFileException(Exception e) {
    super("End of File Exception");
  }
}
