package edu.hm.dako.chat.udp;

import java.net.InetAddress;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionFactory;
import edu.hm.dako.chat.udp.UdpSocket;

public class UdpClientConnectionFactory implements ConnectionFactory {

	// Maximale Wartezeit beim Empfang einer Nachricht in Millisekunden.
	// Wenn in dieser Zeit keine Nachricht kommt, wird das Empfangen abgebrochen.
	// Mit verschiedenen Einstellungen experimentieren.
	private final int defaultResponseTimeout = 5000;

	@Override
	public Connection connectToServer(String remoteServerAddress, int serverPort,
			int localPort, int sendBufferSize, int receiveBufferSize) throws Exception {

		UdpSocket udpSocket = new UdpSocket(localPort, sendBufferSize, receiveBufferSize);
		udpSocket.setRemoteAddress(InetAddress.getByName(remoteServerAddress));
		udpSocket.setRemotePort(serverPort);
		return new UdpClientConnection(udpSocket, defaultResponseTimeout);
	}
}
