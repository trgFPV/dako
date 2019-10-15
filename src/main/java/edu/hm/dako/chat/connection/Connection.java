package edu.hm.dako.chat.connection;

import java.io.Serializable;

/**
 * Wird vom Client und vom Server zur Kommunikation verwendet.
 */
public interface Connection {

  /**
   * Blockiert maximal eine angegebene Zeit in ms bis eine serialisierte Nachricht als
   * Java-Objekt eintrifft.
   *
   * @param timeout
   *          Maximale Wartezeit in ms
   * @return Die erhaltene Nachricht des Kommunikationspartners.
   * @throws Exception
   */
  public Serializable receive(int timeout) throws Exception,
	ConnectionTimeoutException;

  /**
   * Blockiert bis eine serialisierte Nachricht als Java-Objekt eintrifft.
   *
   * @return Die erhaltene Nachricht des Kommunikationspartners.
   * @throws Exception
   */
  public Serializable receive() throws Exception;

  /**
   * Sendet eine Nachricht an den Kommunikationspartner.
   *
   * @param message
   *          Die zu sendende Nachricht.
   * @throws Exception
   */
  public void send(Serializable message) throws Exception;

  /**
   * Baut die Verbindung zum Kommunikationspartner ab.
   *
   * @throws Exception
   */
  public void close() throws Exception;
}
