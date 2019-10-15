package edu.hm.dako.chat.common;

/**
 * Metriken zur Bewertung der Verteilung der gemessenen RTTs
 * 
 * @author mandl
 */
public class DistributionMetrics {

	// Minimum
	double minimum;

	// Maximum
	double maximum;

	// 10 % Percentile, alle RTT-Werte, die zu den kleinsten 10 % gehoeren
	double percentile10;

	// 10 % Percentile = 25%-Quartil, alle RTT-Werte, die zu den kleinsten 25 % gehoeren
	double percentile25;

	// Median = 50%-Quartil
	double percentile50;

	// 75 % percentile
	double percentile75;

	// 90 % Percentile
	double percentile90;

	// Stannweite (zwischen Minimum und Maximum)
	double range;

	// Interquartilsabstand, Wertebereich, in dem sich die mittleren 50 % der gemessenen RTT-Werte
	// befinden (IQR)
	double interquartilRange;

	// Arithmetisches Mittel
	double mean;

	// Varianz
	double variance;

	// Standardabweichung
	double standardDeviation;

	public DistributionMetrics() {
		minimum = 0;
		maximum = 0;
		percentile10 = 0;
		percentile25 = 0;
		percentile50 = 0;
		percentile75 = 0;
		percentile90 = 0;
		interquartilRange = 0;
		range = 0;
		mean = 0;
		variance = 0;
		standardDeviation = 0;
	}

	public double getMinimum() {
		return minimum;
	}

	public double getMaximum() {
		return maximum;
	}

	public double getPercentile10() {
		return percentile10;
	}

	public double getPercentile25() {
		return percentile25;
	}

	public double getPercentile50() {
		return percentile50;
	}

	public double getPercentile75() {
		return percentile75;
	}

	public double getPercentile90() {
		return percentile90;
	}

	public double getInterquartilRange() {
		return interquartilRange;
	}

	public double getRange() {
		return range;
	}

	public double getMean() {
		return mean;
	}

	public double getVariance() {
		return variance;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	public void setPercentile10(double percentile10) {
		this.percentile10 = percentile10;
	}

	public void setPercentile25(double percentile25) {
		this.percentile25 = percentile25;
	}

	public void setPercentile50(double percentile50) {
		this.percentile50 = percentile50;
	}

	public void setPercentile75(double percentile75) {
		this.percentile75 = percentile75;
	}

	public void setPercentile90(double percentile90) {
		this.percentile90 = percentile90;
	}

	public void setInterquartilRange(double interquartilRange) {
		this.interquartilRange = interquartilRange;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
}
