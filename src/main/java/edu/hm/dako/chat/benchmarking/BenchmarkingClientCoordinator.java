package edu.hm.dako.chat.benchmarking;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.client.ClientUserInterface;
import edu.hm.dako.chat.common.CpuUtilisationWatch;
import edu.hm.dako.chat.common.DistributionMetrics;
import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.common.SharedClientStatistics;

/**
 * Basisklasse zum Starten eines Benchmarks
 *
 * @author Mandl
 */
public class BenchmarkingClientCoordinator extends Thread
		implements BenchmarkingStartInterface, ClientUserInterface {
	private static Log log = LogFactory.getLog(BenchmarkingClientCoordinator.class);

	// Daten aller Client-Threads zur Verwaltung der Statistik
	private SharedClientStatistics sharedData;
	private CpuUtilisationWatch cpuUtilisationWatch;

	// Kennzeichen, ob gerade ein Test laeuft (es darf nur einer zu einer Zeit
	// laufen)
	private boolean running = false;

	// Kennzeichen, ob Test in der GUI gestoppt wurde
	private boolean abortedFlag = false;

	// Uebergebene Parameter vom User-Interface
	UserInterfaceInputParameters parm;

	// GUI-Schnittstelle
	BenchmarkingClientUserInterface benchmarkingClientGui;

	// Anzahl aller Requests, die auszufuehren sind
	long numberOfAllRequests;

	// Startzeit des Tests
	long startTime;

	// Startzeit als String
	String startTimeAsString;

	// Kalender zur Umrechnung der Startzeit
	Calendar cal;

	// Thread zur Zeitzaehlung fuer die Dauer des Tests
	BenchmarkingTimeCounterThread timeCounterThread;

	/**
	 * Methode liefert die aktuelle Zeit als String
	 *
	 * @param cal
	 *          Kalender
	 * @return Zeit als String
	 */
	private String getCurrentTime(Calendar cal) {
		return new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSS").format(cal.getTime());
	}

	@Override
	public void executeTest(UserInterfaceInputParameters parm,
			BenchmarkingClientUserInterface clientGui) {

		this.parm = parm;
		this.benchmarkingClientGui = clientGui;

		clientGui
				.setMessageLine(parm.mapImplementationTypeToString(parm.getImplementationType())
						+ ": Benchmark gestartet");

		// Anzahl aller erwarteten Requests ermitteln

		numberOfAllRequests = parm.getNumberOfClients() * parm.getNumberOfMessages();

		// Gemeinsamen Datenbereich fuer alle Threads anlegen
		sharedData = new SharedClientStatistics(parm.getNumberOfClients(),
				parm.getNumberOfMessages(), parm.getClientThinkTime());

		// Berechnung aller Messages fuer Progress-Bar
		if (clientGui.getProgressBar() != null) {
			clientGui.getProgressBar()
					.setMaximum(parm.getNumberOfClients() * parm.getNumberOfMessages()
							+ parm.getNumberOfClients() + parm.getNumberOfClients());
		}

		/**
		 * Startzeit ermitteln
		 */
		startTime = 0;
		cal = Calendar.getInstance();
		startTime = cal.getTimeInMillis();
		startTimeAsString = getCurrentTime(cal);

		/**
		 * Laufzeitzaehler-Thread erzeugen
		 */
		timeCounterThread = new BenchmarkingTimeCounterThread(clientGui);
		timeCounterThread.start();

		cpuUtilisationWatch = new CpuUtilisationWatch();

		start();
	}

	/**
	 * Thread zur Entkoppelung des User-Interface von der Testausfuehrung, damit
	 * im User-Interface Eingaben moeglich sind, waehrend der Benchmark laeuft
	 * (z.B. Abbruch).
	 */
	@Override
	public void run() {

		// Test aktiv
		running = true;

		// Client-Threads in Abhaengigkeit des Implementierungstyps instanziieren
		// und starten
		ExecutorService executorService = Executors
				.newFixedThreadPool(parm.getNumberOfClients());

		for (int i = 0; i < parm.getNumberOfClients(); i++) {
			executorService.submit(
					BenchmarkingClientFactory.getClient(this, parm, i, sharedData, benchmarkingClientGui));

			// Warten, bis der Client seinen Login abgeschlossen hat. Damit erfolgt
			// eine Serialisierung der Logins, damit die Anzahl der Login-Events genau
			// berechnet werden kann.
			while (sharedData.getNumberOfLoggedInClients() != i + 1) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					ExceptionHandler.logException(e);
				}
			}
			log.debug("Client " + (i + 1) + " ist eingeloggt");
		}

		// Startwerte anzeigen
		UserInterfaceStartData startData = new UserInterfaceStartData();
		startData.setNumberOfRequests(numberOfAllRequests);
		startData.setStartTime(getCurrentTime(cal));

		/*
		 * Maximal moegliche Events = ChatMessage-Events + Anzahl an Login-Events
		 * (wenn alle Clients sich seriell hintereinander einloggen + die Anzahl an
		 * Logout-Events, wenn alle Clients bis zuum letzten Logout arbeiten.
		 */

		long numberOfPlannedLoginEvents = 0;
		for (int i = 1; i <= parm.getNumberOfClients(); i++) {
			numberOfPlannedLoginEvents += i;
		}
		log.debug("Anzahl geplanter LoginEvent-Nachrichten: " + numberOfPlannedLoginEvents);

		long numberOfPlannedMessagesEvents = numberOfAllRequests * parm.getNumberOfClients();
		log.debug(
				"Anzahl geplanter MessageEvent-Nachrichten: " + numberOfPlannedMessagesEvents);

		long numberOfPlannedLogoutEvents = parm.getNumberOfClients()
				* parm.getNumberOfClients();

		log.debug("Anzahl geplanter LogoutEvent-Nachrichten: " + numberOfPlannedLogoutEvents);

		startData.setNumberOfPlannedEventMessages(numberOfPlannedMessagesEvents
				+ numberOfPlannedLoginEvents + numberOfPlannedLogoutEvents);

		benchmarkingClientGui.showStartData(startData);

		benchmarkingClientGui.setMessageLine(
				"Alle " + parm.getNumberOfClients() + " Clients-Threads gestartet");

		// Auf das Ende aller Clients warten
		executorService.shutdown();

		try {
			executorService.awaitTermination(120000, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("Das Beenden des ExecutorService wurde unterbrochen");
			ExceptionHandler.logException(e);
		}

		// Laufzeitzaehler-Thread beenden
		timeCounterThread.stopThread();

		// Analyse der Ergebnisse durchfuehren, Statistikdaten berechnen und
		// ausgeben
		// sharedData.printStatistic();

		// Testergebnisse ausgeben
		benchmarkingClientGui.setMessageLine("Alle Clients-Threads beendet");

		UserInterfaceResultData resultData = getResultData(parm, startTime);

		benchmarkingClientGui.showResultData(resultData);
		benchmarkingClientGui
				.setMessageLine(parm.mapImplementationTypeToString(parm.getImplementationType())
						+ ": Benchmark beendet");

		benchmarkingClientGui.testFinished();

		log.debug(
				"Anzahl aller erneuten Sendungen wegen Nachrichtenverlust (Uebertragungswiederholungen): "
						+ sharedData.getSumOfAllRetries());

		// Datensatz fuer Benchmark-Lauf auf Protokolldatei schreiben
		sharedData.writeStatisticSet("Benchmarking-ChatApp-Protokolldatei",
				parm.mapImplementationTypeToString(parm.getImplementationType()),
				parm.mapMeasurementTypeToString(parm.getMeasurementType()), startTimeAsString,
				resultData.getEndTime(), cpuUtilisationWatch.getAverageCpuUtilisation());

		// In der GUI erneute Testlaeufe zulassen
		running = false;
	}

	@Override
	// Wird nicht genutzt, nur fuer ChatClientGUI relevant
	public synchronized void setUserList(Vector<String> names) {
	}

	@Override
	// Wird nicht genutzt, nur fuer ChatClientGUI relevant
	public synchronized void setMessageLine(String sender, String message) {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized void setLock(boolean lock) {
	}

	@Override
	// Wird nicht genutzt, nur fuer ChatClientGUI
	public void setErrorMessage(String sender, String errorMessage, long errorCode) {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public void loginComplete() {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public void logoutComplete() {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized boolean getLock() {
		return false;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized void setLastServerTime(long lastServerTime) {
	}

	@Override
	public synchronized void abortTest() {
		abortedFlag = true;
	}

	@Override
	public synchronized boolean isRunning() {
		return (this.running);
	}

	@Override
	public synchronized void releaseTest() {
		this.abortedFlag = false;
	}

	@Override
	public synchronized boolean isTestAborted() {
		return abortedFlag;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getLastServerTime() {
		return 0;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized void setSessionStatisticsCounter(long numberOfSentEvents,
			long numberOfReceivedConfirms, long numberOfLostConfirms, long numberOfRetries,
			long numberOfReceivedChatMessages) {
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getNumberOfSentEvents() {
		return 0;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getNumberOfReceivedConfirms() {
		return 0;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getNumberOfLostConfirms() {
		return 0;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getNumberOfRetries() {
		return 0;
	}

	@Override
	// Wird nicht genutzt, nur fuer BenchmarkingClientImpl relevant
	public synchronized long getNumberOfReceivedChatMessages() {
		return 0;
	}

	/**
	 * Ergebnisdaten des Tests aufbereiten
	 * 
	 * @param parm
	 *          Eingabedaten fuer die GUI
	 * @param startTime
	 *          Startzeit des Tests
	 * @return
	 */
	private UserInterfaceResultData getResultData(UserInterfaceInputParameters parm,
			long startTime) {

		Calendar cal;
		UserInterfaceResultData resultData = new UserInterfaceResultData();
		DistributionMetrics distributionMetrics = sharedData.calculateMetrics();

		resultData.setPercentile10(distributionMetrics.getPercentile10());
		resultData.setMean(distributionMetrics.getMean());
		resultData.setPercentile25(distributionMetrics.getPercentile25());
		resultData.setPercentile50(distributionMetrics.getPercentile50());
		resultData.setPercentile75(distributionMetrics.getPercentile75());
		resultData.setPercentile90(distributionMetrics.getPercentile90());
		resultData.setStandardDeviation(distributionMetrics.getStandardDeviation());
		resultData.setRange(distributionMetrics.getRange());
		resultData.setInterquartilRange(distributionMetrics.getInterquartilRange());
		resultData.setMinimum(distributionMetrics.getMinimum());
		resultData.setMaximum(distributionMetrics.getMaximum());

		resultData.setAvgServerTime(sharedData.getAverageServerTime() / 1000000.0);

		cal = Calendar.getInstance();
		resultData.setEndTime(getCurrentTime(cal));

		long elapsedTimeInSeconds = (cal.getTimeInMillis() - startTime) / 1000;
		resultData.setElapsedTime(elapsedTimeInSeconds);

		resultData.setMaxCpuUsage(cpuUtilisationWatch.getAverageCpuUtilisation());

		resultData.setMaxHeapSize(sharedData.getMaxHeapSize() / (1024 * 1024));

		resultData.setNumberOfResponses(sharedData.getSumOfAllReceivedMessages());
		resultData.setNumberOfSentRequests(sharedData.getNumberOfSentRequests());
		resultData.setNumberOfLostResponses(sharedData.getNumberOfLostResponses());
		resultData.setNumberOfRetries(sharedData.getSumOfAllRetries());
		resultData.setNumberOfSentEventMessages(sharedData.getSumOfAllSentEventMessages());
		resultData
				.setNumberOfReceivedConfirmEvents(sharedData.getSumOfAllReceivedConfirmEvents());
		resultData.setNumberOfLostConfirmEvents(sharedData.getSumOfAllLostConfirmEvents());
		resultData.setNumberOfRetriedEvents(sharedData.getSumOfAllRetriedEvents());
		return resultData;
	}
}