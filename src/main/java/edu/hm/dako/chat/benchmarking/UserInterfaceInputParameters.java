package edu.hm.dako.chat.benchmarking;

import edu.hm.dako.chat.common.ImplementationType;

/**
 * Konfigurationsparameter fuer Lasttest
 *
 * @author Mandl
 */
public class UserInterfaceInputParameters {

	// Anzahl zu startender Client-Threads
	private int numberOfClients;
	// Nachrichtenlaenge
	private int messageLength;
	// Denkzeit zwischen zwei Requests
	private int clientThinkTime;
	// Anzahl der Nachrichten pro Client-Thread
	private int numberOfMessages;

	// Maximale Anzahl an Uebertragungswiederholungen bei
	// verbindungslosen Prototokollen
	private int numberOfRetries;
	// Maximale Wartezeit in ms auf eine Antwort des Servers
	// bei verbindungslosen Protokollen
	// Typ der Implementierung
	private int responseTimeout;

	private ImplementationType implementationType;

	// Typ der Messung fuer das Messprotokoll
	private MeasurementType measurementType;
	private int remoteServerPort; // UDP- oder TCP-Port des Servers, Default:
																// 50001
	private String remoteServerAddress; // Server-IP-Adresse, Default: "127.0.0.1"

	/**
	 * Konstruktor Belegung der Inputparameter mit Standardwerten
	 */
	public UserInterfaceInputParameters() {
		numberOfClients = 2;
		clientThinkTime = 1;
		messageLength = 100;
		numberOfMessages = 5;
		remoteServerPort = 50001;
		remoteServerAddress = "127.0.0.1";
		implementationType = ImplementationType.TCPSimpleImplementation;
		measurementType = MeasurementType.VarThreads;
	}

	/**
	 * Abbildung der Implementierungstypen auf Strings
	 *
	 * @param type
	 *          Implementierungstyp
	 * @return Passender String fuer Implementierungstyp
	 */
	public String mapImplementationTypeToString(ImplementationType type) {
		String returnString = null;

		switch (type) {
		case TCPAdvancedImplementation:
			returnString = "TCPAdvanced-Implementation";
			break;
		case TCPSimpleImplementation:
			returnString = "TCPSimple-Implementation";
			break;
		case UDPAdvancedImplementation:
			returnString = "UDPAdvanced-Implementation";
			break;
		default:
			break;
		}

		return returnString;
	}

	/**
	 * Typen von unterstuetzten Messungen: Nur fuer die Unterscheidung der Messung
	 * im Benchmarking-Protokoll
	 *
	 * @author Mandl
	 */
	public enum MeasurementType {
		// Variation der Threadanzahl
		VarThreads,
		// Variation der Nachrichtenlaenge
		VarMsgLength
	}

	/**
	 * Abbildung der Messungstypen auf Strings
	 *
	 * @param type
	 *          Messungstyp
	 * @return Passender String fuer Messungstyp
	 */
	public String mapMeasurementTypeToString(MeasurementType type) {
		String returnString = null;

		switch (type) {
		case VarThreads:
			returnString = "VariationThreadanzahl";
			break;
		case VarMsgLength:
			returnString = "VariationNachrichtenlaenge";
			break;
		default:
			break;
		}

		return returnString;
	}

	public int getNumberOfClients() {
		return numberOfClients;
	}

	public void setNumberOfClients(int numberOfClients) {
		this.numberOfClients = numberOfClients;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	public int getClientThinkTime() {
		return clientThinkTime;
	}

	public void setClientThinkTime(int clientThinkTime) {
		this.clientThinkTime = clientThinkTime;
	}

	public int getNumberOfMessages() {
		return numberOfMessages;
	}

	public void setNumberOfMessages(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

	public ImplementationType getImplementationType() {
		return implementationType;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public int getResponseTimeout() {
		return responseTimeout;
	}

	public void setImplementationType(ImplementationType implementationType) {
		this.implementationType = implementationType;
	}

	public MeasurementType getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(MeasurementType measurementType) {
		this.measurementType = measurementType;
	}

	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public void setRemoteServerPort(int remoteServerPort) {
		this.remoteServerPort = remoteServerPort;
	}

	public String getRemoteServerAddress() {
		return remoteServerAddress;
	}

	public void setRemoteServerAddress(String remoteServerAddress) {
		this.remoteServerAddress = remoteServerAddress;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public void setResponseTimeout(int responseTimer) {
		this.responseTimeout = responseTimer;
	}
}