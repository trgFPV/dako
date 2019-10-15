package edu.hm.dako.chat.benchmarking;


/**
 * Schnittstelle zum Starten eines Benchmarks
 *
 * @author Mandl
 */

public interface BenchmarkingStartInterface {

  /**
   * Methode fuehrt den Benchmark aus
   *
   * @param parm
   *          Input-Parameter
   * @param clientGui
   *          Schnittstelle zur GUI
   */
  public void executeTest(UserInterfaceInputParameters parm,
                          BenchmarkingClientUserInterface clientGui);
}
