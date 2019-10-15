package edu.hm.dako.chat.common;

import com.sun.management.OperatingSystemMXBean;

//import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

/**
 * Ermitteln der durchschnittlich verbrauchten CPU-Zeit eines Prozesses
 *
 */
public class CpuUtilisationWatch {

	private static final OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory
		.getOperatingSystemMXBean();
	
	
	private static final int nCPUs = osbean.getAvailableProcessors();

	private Long startWallclockTime;
	private Long startCpuTime;

	public CpuUtilisationWatch() {
		startWallclockTime = System.nanoTime();
		startCpuTime = osbean.getProcessCpuTime();
	}

	public float getAverageCpuUtilisation() {
		float wallclockTimeDelta = System.nanoTime() - startWallclockTime;
		float cpuTimeDelta = osbean.getProcessCpuTime() - startCpuTime;
		cpuTimeDelta = Math.max(cpuTimeDelta, 1);

		return (cpuTimeDelta / (float) nCPUs) / wallclockTimeDelta;
	}
}
