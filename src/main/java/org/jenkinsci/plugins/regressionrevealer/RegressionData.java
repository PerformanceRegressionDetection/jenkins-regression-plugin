package org.jenkinsci.plugins.regressionrevealer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jenkinsci.plugins.regressionrevealer.RegressionEnums.MeasurementUnit;
import org.jenkinsci.plugins.regressionrevealer.RegressionEnums.RegressionDirection;

public class RegressionData {
	private String methodName;
	private String className;
	private int repetition;
	private RegressionEnums.MeasurementUnit unit;
	private double treshold;
	private RegressionEnums.RegressionDirection dir;
	private double measurementInMillis;

	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	public int getRepetition() {
		return repetition;
	}

	public RegressionEnums.MeasurementUnit getMeasurementUnit() {
		return unit;
	}

	public double getTreshold() {
		return treshold;
	}

	public RegressionEnums.RegressionDirection getDir() {
		return dir;
	}
	
	public double getMeasurementInMillis() {
		return measurementInMillis;
	}

	public void setMeasurementInNanos(long meas) {
		measurementInMillis = meas;
	}

	public RegressionData(String data) {
		StringTokenizer tokenizer = new StringTokenizer(data, ",");
		methodName = tokenizer.nextToken();
		className = tokenizer.nextToken();
		repetition = Integer.parseInt(tokenizer.nextToken());
		unit = MeasurementUnit.resolveMeasurementUnitEnum(tokenizer.nextToken());
		treshold = Double.parseDouble(tokenizer.nextToken());
		dir = RegressionDirection.resolveRegressionDirectionEnum(tokenizer.nextToken());
		if(tokenizer.hasMoreTokens()){
			measurementInMillis = Double.parseDouble(tokenizer.nextToken());
		}
	}

	public RegressionData(String mN, String pN) {
		methodName = mN;
		className = pN;
	}

	public RegressionData(String mN, String pN, int rep, double meas) {
		methodName = mN;
		className = pN;
		repetition = rep;
		measurementInMillis = meas;
	}

	/**
	 * Parses a given file for RegressionData lines and gives them back in a Set
	 * 
	 * @param dataFile
	 *            Location of the CSV file with the RegressionData lines
	 * @return Set of Regression if the file exists otherwise null
	 */
	public static List<RegressionData> getRegressionDataFromFile(File dataFile) {
		try {
			if (dataFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(dataFile));
				ArrayList<RegressionData> lines = new ArrayList<RegressionData>();
				String line;
				while ((line = br.readLine()) != null) {
					RegressionData rd = new RegressionData(line);
					lines.add(rd);
				}
				return lines;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] getRegressionDataFromFile");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return new String(methodName + "," + className + "," + repetition);
	}

	// writes data without repetition -> only names and the measurement is
	// important in the measurement output
	public String toStringWithMeasurement() {
		return new String(methodName + "," + className + "," + measurementInMillis);
	}

	public String toStringAllWithRepetition(){
		return new String(        methodName
						  + "," + className
						  + "," + repetition
						  + "," + unit.getShortForm()
						  + "," + treshold
						  + "," + dir.getShortForm()
						  + "," + measurementInMillis
						  );
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;

		result = prime * result + methodName.hashCode();
		result = prime * result + className.hashCode();
		// result = prime * result + repetition;
		return result;
	}

	@Override
	public boolean equals(Object object) {
		boolean result = false;
		if (object instanceof RegressionData) {
			RegressionData otherRegressionData = (RegressionData) object;
			result = (this.methodName.equals(otherRegressionData.methodName)
					&& this.className.equals(otherRegressionData.className));
		}
		return result;
	}
}