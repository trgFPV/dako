package edu.hm.dako.chat.connection;

/**
 * Erstellt neue Instanzen von {@link Connection}.
 */
public interface ConnectionFactory {

  /**
   * Erstellt eine Verbindung zum Server.
   *
   * @param remoteServerAddress
   *          Die IP-Adresse des Servers (z.B. localhost bzw. 127.0.0.1 oder 192.168.2.41)
   * @param serverPort
   *          Der Port an dem der Server horcht.
   * @param localPort
   *          Der Port des Clients.
   * @param sendBufferSize
   *          Groesse des Sendepuffers in Byte.
   * @param receiveBufferSize
   *          Groesse des Empfangspuffers in Byte.
   * @return Connection-Objekt das eine Verbindung zum Server darstellt.
   * @throws Exception
   */
  public Connection connectToServer(String remoteServerAddress,
	int serverPort, int localPort, int sendBufferSize,
	int receiveBufferSize) throws Exception;

}
