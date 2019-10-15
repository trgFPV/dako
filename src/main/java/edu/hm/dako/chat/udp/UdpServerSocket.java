package edu.hm.dako.chat.udp;

import java.io.IOException;
import java.net.SocketException;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;

public class UdpServerSocket implements ServerSocketInterface {

	private UdpSocket socket;

	public UdpServerSocket(int serverPort, int sendBufferSize,
			int receiveBufferSize) throws SocketException {
		this.socket = new UdpSocket(serverPort, sendBufferSize,
				receiveBufferSize);
	}

	@Override
	public Connection accept() throws Exception {
		return new UdpServerConnection(socket);
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}
}
