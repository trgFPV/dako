package edu.hm.dako.chat.benchmarking;

import javafx.scene.control.ProgressBar;

import javax.swing.JProgressBar;

import org.apache.log4j.PropertyConfigurator;

import edu.hm.dako.chat.server.ChatServerGuiInterface;
import edu.hm.dako.chat.server.ServerStartData;

/**
 * Diese Klasse simuliert eine Benutzeroberflaeche.
 *
 * @author mandl
 */

public class BenchmarkingUserInterfaceSimulation implements
    BenchmarkingClientUserInterface, ChatServerGuiInterface {

  private int timeCounter = 0;

  @Override
  public void showStartData(UserInterfaceStartData data) {

    System.out.println("Testbeginn: " + data.getStartTime());
    System.out.println("Geplante Requests: " + data.getNumberOfRequests());
  }

  @Override
  public void showResultData(UserInterfaceResultData data) {

    System.out.println("Testende: " + data.getEndTime());
    System.out.println("Testdauer in s: " + data.getElapsedTime());

    System.out.println("Gesendete Requests: "
	  + data.getNumberOfSentRequests());
    System.out.println("Anzahl Responses: " + data.getNumberOfResponses());
    System.out.println("Anzahl verlorener Responses: "
	  + data.getNumberOfLostResponses());

    System.out.println("Mittlere RTT in ms: " + data.getMean());
    System.out.println("Maximale RTT in ms: " + data.getMaximum());
    System.out.println("Minimale RTT in ms: " + data.getMinimum());
    System.out.println("Mittlere Serverbearbeitungszeit in ms: "
	  + data.getAvgServerTime());

    System.out.println("Maximale Heap-Belegung in MByte: "
	  + data.getMaxHeapSize());
    System.out.println("Maximale CPU-Auslastung in %: "
	  + data.getMaxCpuUsage());
  }

  @Override
  public void setMessageLine(String message) {
    System.out.println("*** Meldung: " + message + " ***");
  }

  @Override
  public void addCurrentRunTime(long sec) {
    // Feld Testdauer um sec erhoehen
    timeCounter += sec;
    System.out.println("Laufzeitzaehler: " + timeCounter);
  }

  @Override
  public void resetCurrentRunTime() {
    // Feld Testdauer auf 0 setzen
    timeCounter = 0;
  }

  @Override
  public synchronized void testFinished() {
    System.out.println("Testlauf beendet");
  }

  @Override
  public JProgressBar getProgressBar() {
    return null;
  }

  @Override
  public ProgressBar getProgressBarFx() {
    return null;
  }

  @Override
  public void countUpProgressTask() {

  }

  /**
   * main
   *
   * @param args
   */
  public static void main(String args[]) {
    PropertyConfigurator.configureAndWatch("log4j.benchmarkingCient.properties",
	  60 * 1000);
    new BenchmarkingUserInterfaceSimulation().doWork();

  }

  public void doWork() {
    // Input-parameter aus GUI
    UserInterfaceInputParameters parm = new UserInterfaceInputParameters();

    // GUI sammmelt Eingabedaten ...

    // Benchmarking-Client instanzieren und Benchmark starten

    BenchmarkingClientCoordinator benchClient = new BenchmarkingClientCoordinator();
    benchClient.executeTest(parm, this);
  }

  @Override
  public void showStartData(ServerStartData data) {

  }

  @Override
  public void incrNumberOfLoggedInClients() {

  }

  @Override
  public void decrNumberOfLoggedInClients() {

  }

  @Override
  public void incrNumberOfRequests() {

  }
}