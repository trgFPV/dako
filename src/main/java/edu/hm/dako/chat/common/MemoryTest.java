package edu.hm.dako.chat.common;

import java.text.NumberFormat;

/**
 * Testklasse fuer einen Heap-Test. Klasse dient nur zum Test und ist nicht fuer die
 * Studienarbeit relevant
 *
 * @author mandl
 */
public class MemoryTest {
  public static Runtime r = null;
  public static NumberFormat n = null;

  public static void main(String[] args) {

    try {
	r = Runtime.getRuntime();

	n = NumberFormat.getInstance();
	n.setMaximumFractionDigits(2);

	getMemoryOverview("Start");

	int anzahlArrays = 100000;
	int laengeArray = 100;

	Object[] array = new Object[anzahlArrays];

	for (int i = 0; i < anzahlArrays; i++) {

	  array[i] = new String[laengeArray][laengeArray];
	  // Ausgabe nur jedes 1000te Array
	  if (i % 1000 == 0) {
	    System.out.print(i + ": ");
	    printFreeMemory();
	  }
	}

	getMemoryOverview("Ende");

    } catch (OutOfMemoryError e) {
	System.out.println("OutOfMemory");
	getMemoryOverview("Exception");

	System.out.println(e);
    } catch (Exception e) {

	e.printStackTrace();
    }
  }

  private static void getMemoryOverview(String place) {
    System.out.println("-----Speicheruebersicht(" + place + ")------");
    System.out.println("Gesamt-Speicher: "
	  + n.format(inMBytes(r.totalMemory())) + " MB");
    System.out.println("freier Speicher: "
	  + n.format(inMBytes(r.freeMemory())) + " MB");
    System.out.println("maximaler Speicher: "
	  + n.format(inMBytes(r.maxMemory())) + " MB");

    System.out
	  .println("Verfuegbare Prozessoren: " + r.availableProcessors());
  }

  public static void printFreeMemory() {

    System.out.println("Maximaler moeglicher Heap-Speicher: "
	  + n.format(inMBytes(r.maxMemory())) + " MB, "
	  + "benutzter Heap-Speicher: "
	  + n.format(inMBytes(usedMemory(r))) + " MB, "
	  + "noch verfuegbar insgesamt: "
	  + n.format(inMBytes(availableMemory(r))) + " MB");
  }

  /**
   * Berechnet den tatsaechlich benutzten Heap-Speicher
   */
  public static long usedMemory(Runtime r) {

    long usedMemory = r.totalMemory() - r.freeMemory();

    return (usedMemory);
  }

  /**
   * Berechnet den tatsaechlich noch verfuegbaren Heap-Speicher
   */
  public static long availableMemory(Runtime r) {

    long availableMemory = r.maxMemory()
	  - (r.totalMemory() - r.freeMemory());

    return (availableMemory);
  }

  public static double inKBytes(double bytes) {
    return ((bytes / 1024));
  }

  public static double inMBytes(double bytes) {
    return ((inKBytes(bytes) / 1024));
  }
}