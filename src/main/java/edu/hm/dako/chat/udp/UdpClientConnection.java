package edu.hm.dako.chat.udp;

import java.io.IOException;
import java.io.Serializable;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.udp.UdpSocket;

public class UdpClientConnection implements Connection {

	// UDP-Socket der Verbindung
	private UdpSocket clientSocket;

	/**
	 * Timeout fuer {@link UdpSocket#receive(int)} Das ist die maximale Wartezeit
	 * beim Empfang von UDP-Datagrammen
	 */

	private final int receivingTimeout;

	public UdpClientConnection(UdpSocket clientSocket, int receivingTimeout)
			throws Exception {
		this.clientSocket = clientSocket;
		this.receivingTimeout = receivingTimeout;
	}

	@Override
	public Serializable receive(int timeout) throws Exception {
		try {
			return (Serializable) clientSocket.receive(timeout);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Serializable receive() throws Exception {
		try {
			return (Serializable) clientSocket.receive(receivingTimeout);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void send(Serializable message) throws Exception {
		clientSocket.send(clientSocket.getRemoteAddress(), clientSocket.getRemotePort(),
				message);
	}

	@Override
	public void close() throws IOException {
		clientSocket.close();
	}
}