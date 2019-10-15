package edu.hm.dako.chat.common;

/**
 * Testklasse fuer das Lesen eines Zeitstempels
 *
 * @author mandl
 */
public class TestNanoTime {

	public static void main(String[] args) {

		for (int i = 0; i < 10; i++) {

			// Zeitmessung fuer RTT starten
			long startTime = System.nanoTime();

			try {
				Thread.sleep((10000));
			} catch (Exception e) {
			}

			System.out.println(
					"Start time in ns: " + startTime + " = " + startTime / 1000000 + " ms");

			// Ende der Zeitmessung
			long endTime = System.nanoTime();

			System.out
					.println("End time in ns: " + endTime + " = " + endTime / 1000000 + " ms");
			long elapseTime = endTime - startTime;

			System.out.println(
					"Elapsed time in ns: " + elapseTime + " = " + elapseTime / 1000000 + " ms");
			System.out.println("");
		}
	}
}
