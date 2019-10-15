package edu.hm.dako.chat.udp;

import java.io.IOException;
import java.io.Serializable;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionTimeoutException;

public class UdpServerConnection implements Connection {

	private UdpSocket serverSocket;

	private UdpPseudoConnectionContext udpRemoteObject; // Empfangene Request-PDU

	public UdpServerConnection(UdpSocket serverSocket) throws Exception {
		this.serverSocket = serverSocket;
		udpRemoteObject = new UdpPseudoConnectionContext();
	}

	/*
	 * Der Empfang der Daten vom UDP-Client erfolgt bereits im Konstruktor. Diese
	 * Methode gibt nur die bereits empfangene Nachricht zurueck.
	 * 
	 * @see edu.hm.dako.echo.connection.Connection#receive()
	 */
	@Override
	public Serializable receive(int timeout) throws Exception, ConnectionTimeoutException {
		System.out.println("Echo-Request empfangen von " + serverSocket.getRemoteAddress()
				+ ":" + serverSocket.getRemotePort() + " ueber lokalen Port "
				+ serverSocket.getLocalPort());

		Object pdu = serverSocket.receive(timeout);
		udpRemoteObject = new UdpPseudoConnectionContext(serverSocket.getRemoteAddress(),
				serverSocket.getRemotePort(), pdu);
		return (Serializable) udpRemoteObject.getObject();
	}

	public Serializable receive() throws Exception {
		Object pdu = serverSocket.receive(0);
		udpRemoteObject = new UdpPseudoConnectionContext(serverSocket.getRemoteAddress(),
				serverSocket.getRemotePort(), pdu);
		return (Serializable) udpRemoteObject.getObject();
	}

	@Override
	public void send(Serializable message) throws Exception {
		serverSocket.send(udpRemoteObject.getRemoteAddress(), udpRemoteObject.getRemotePort(),
				message);
	}

	@Override
	/*
	 * Dies ist nur eine Dummy-Methode. Der ServerSocket darf nicht geschlossen
	 * werden, da der Server sonst keine Requests mehr entgegennehmen kann. Es
	 * gibt im Unterschied zu TCP-Sockets keine Verbindungssockets bei UDP,
	 * sondern nur ein UDP-Socket, ueber das alles empfangen wird.
	 */
	public void close() throws IOException {
	}
}
