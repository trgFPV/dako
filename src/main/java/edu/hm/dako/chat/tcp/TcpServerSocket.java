package edu.hm.dako.chat.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;

/**
 * Server-Socket Implementierung auf TCP-Basis
 * 
 * @author Peter Mandl
 */
public class TcpServerSocket implements ServerSocketInterface {

	private static Log log = LogFactory.getLog(TcpServerSocket.class);

	private static java.net.ServerSocket serverSocket;
	int sendBufferSize;
	int receiveBufferSize;

	/**
	 * Erzeugt ein TCP-Serversocket und bindet es an einen Port.
	 *
	 * @param port
	 *          Portnummer, die verwendet werden soll
	 * @param sendBufferSize
	 *          Groesse des Sendepuffers in Byte
	 * @param receiveBufferSize
	 *          Groesse des Empfangspuffers in Byte
	 * @exception BindException
	 *              Port schon belegt
	 * @exception IOException
	 *              I/O-Fehler bei der Dovket-Erzeugung
	 */
	public TcpServerSocket(int port, int sendBufferSize, int receiveBufferSize)
			throws BindException, IOException {

		this.sendBufferSize = sendBufferSize;
		this.receiveBufferSize = receiveBufferSize;
		try {
			serverSocket = new java.net.ServerSocket();
		
			// Bind erst nach Setzen der SO_REUSEADDR Option, sonst wird die Option nicht angenommen
			serverSocket.setReuseAddress(true);
			InetSocketAddress socketAddress = new InetSocketAddress(port);
			serverSocket.bind(socketAddress);
			
		} catch (BindException e) {
			log.debug(
					"Port " + port + " auf dem Rechner schon in Benutzung, Bind Exception: " + e);
			throw e;
		} catch (IOException e) {
			log.debug("Schwerwiegender Fehler beim Anlegen eines TCP-Sockets mit Portnummer "
					+ port + ": " + e);
			throw e;
		}
	}

	@Override
	public Connection accept() throws IOException {
		return new TcpConnection(serverSocket.accept(), sendBufferSize, receiveBufferSize,
				false, true);
	}

	@Override
	public void close() throws IOException {
		log.debug(
				"Serversocket wird geschlossen, lokaler Port: " + serverSocket.getLocalPort());
		serverSocket.close();
	}

	@Override
	public boolean isClosed() {
		return serverSocket.isClosed();
	}
}
