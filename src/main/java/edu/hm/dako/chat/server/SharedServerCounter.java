package edu.hm.dako.chat.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p/>
 * Globale Zaehler fuer Logouts, gesendete Events und empfangene Confirms nur
 * zum Tests
 *
 * @author Peter Mandl
 */

public class SharedServerCounter {
	public AtomicInteger logoutCounter;
	public AtomicInteger eventCounter;
	public AtomicInteger confirmCounter;
}
