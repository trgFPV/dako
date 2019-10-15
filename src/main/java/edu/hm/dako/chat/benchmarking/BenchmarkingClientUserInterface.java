package edu.hm.dako.chat.benchmarking;

import javax.swing.JProgressBar;

/**
 * Schnittstelle zum Benchmarking-Client
 */

import javafx.scene.control.ProgressBar;

/**
 * Interface zur Uebergabe von Daten fuer die Ausgabe im Benchmarking-Gui-Client
 *
 * @author Mandl
 */
public interface BenchmarkingClientUserInterface {

	/**
	 * Uebergabe der Startdaten an die GUI
	 *
	 * @param data
	 *          Startdaten
	 */
	public void showStartData(UserInterfaceStartData data);

	/**
	 * Uebergabe der Ergebnisdaten an die GUI
	 *
	 * @param data
	 *          Testergebnisse
	 */
	public void showResultData(UserInterfaceResultData data);

	/**
	 * Uebergabe einer Nachricht an die GUI zur Ausgabe in der Messagezeile
	 *
	 * @param message
	 *          Nachrichtentext
	 */
	public void setMessageLine(String message);

	/**
	 * Zuruecksetzen des Laufzeitzaehlers auf 0
	 */
	public void resetCurrentRunTime();

	/**
	 * Erhoehung des Laufzeitzaehlers
	 *
	 * @param sec
	 *          Laufzeiterhoehung in Sekunden
	 */
	public void addCurrentRunTime(long sec);

	/**
	 * Dem User-Interface mitteilen, dass der Testlauf abgeschlossen ist
	 */
	public void testFinished();

	/**
	 * Uebergibt den Progressbar an die GUI
	 */
	public JProgressBar getProgressBar();

	public ProgressBar getProgressBarFx();

	/**
	 * Stellt Verarbeitungsfortschritt im Progressbar dar
	 */
	public void countUpProgressTask();

}