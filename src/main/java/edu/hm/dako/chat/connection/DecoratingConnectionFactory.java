package edu.hm.dako.chat.connection;

/**
 * Stellt beim Verbindungsaufbau sicher, dass eine {@link Connection} um
 * Logging-Funktionen erweitert wird.
 */
public class DecoratingConnectionFactory implements ConnectionFactory {

  private final ConnectionFactory wrappedFactory;

  public DecoratingConnectionFactory(ConnectionFactory wrappedFactory) {
    this.wrappedFactory = wrappedFactory;
  }

  @Override
  public Connection connectToServer(String remoteServerAddress,
	int serverPort, int localPort, int sendBufferSize,
	int receiveBufferSize) throws Exception {
    Connection wrappedConnection = wrappedFactory.connectToServer(
	  remoteServerAddress, serverPort, localPort, sendBufferSize,
	  receiveBufferSize);
    return new LoggingConnectionDecorator(wrappedConnection);
  }
}
