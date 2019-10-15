package edu.hm.dako.chat.benchmarking;

import edu.hm.dako.chat.client.ClientUserInterface;
import edu.hm.dako.chat.common.SharedClientStatistics;
import edu.hm.dako.chat.connection.ConnectionFactory;
import edu.hm.dako.chat.connection.DecoratingConnectionFactory;
import edu.hm.dako.chat.tcp.TcpConnectionFactory;

/**
 * Uebernimmt die Konfiguration und die Erzeugung bestimmter Client-Typen fuer
 * das Benchmarking. Siehe
 * {@link edu.hm.dako.chat.benchmarking.NioImplementationType.benchmarking.UserInterfaceInputParameters.ImplementationType}
 * Dies beinhaltet die {@link ConnectionFactory}, die Adressen, Ports, Denkzeit
 * etc.
 */
public final class BenchmarkingClientFactory {

	private BenchmarkingClientFactory() {
	}

	public static Runnable getClient(ClientUserInterface userInterface,
			UserInterfaceInputParameters param, int numberOfClient,
			SharedClientStatistics sharedData,
			BenchmarkingClientUserInterface benchmarkingGui) {
		try {

			switch (param.getImplementationType()) {

			case TCPSimpleImplementation:
			case TCPAdvancedImplementation:

				BenchmarkingClientImpl impl = new BenchmarkingClientImpl(userInterface,
						benchmarkingGui, param.getImplementationType(), param.getRemoteServerPort(),
						param.getRemoteServerAddress(), numberOfClient, param.getMessageLength(),
						param.getNumberOfMessages(), param.getClientThinkTime(),
						param.getNumberOfRetries(), param.getResponseTimeout(), sharedData,
						getDecoratedFactory(new TcpConnectionFactory()));
				return impl;

			case UDPAdvancedImplementation:

				System.out.println("UDP noch nicht implementiert");

			default:
				throw new RuntimeException(
						"Unbekannter Implementierungstyp: " + param.getImplementationType());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ConnectionFactory getDecoratedFactory(
			ConnectionFactory connectionFactory) {
		return new DecoratingConnectionFactory(connectionFactory);
	}
}