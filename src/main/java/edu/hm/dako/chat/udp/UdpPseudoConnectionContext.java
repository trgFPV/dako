package edu.hm.dako.chat.udp;

import java.net.InetAddress;

/**
 * UDP ist ein zustandloses Protokoll, in dem es eigentlich keine Verbindung
 * gibt. Hier werden Verbindungsinformationen der ankommenden Datagramme
 * gespeichert, damit der Server seine Antwort an den richtigen Client schicken
 * kann.
 */
public class UdpPseudoConnectionContext {

	// IP-Adresse
	private InetAddress remoteAddress;

	// Entfernter UDP-Port (Partnerport)
	private int remotePort;

	// Empfangenes Objekt
	private Object object;

	public UdpPseudoConnectionContext() {
		this.remoteAddress = null;
		this.remotePort = 0;
		this.object = null;
	}

	public UdpPseudoConnectionContext(InetAddress remoteAddress, int remotePort,
			Object object) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.object = object;
	}

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}