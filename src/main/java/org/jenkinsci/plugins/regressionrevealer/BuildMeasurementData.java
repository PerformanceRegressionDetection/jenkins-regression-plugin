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

/**
 * Represents a line in the aggregated measurement results list file
 * Also has a List of 
 * @author zsolt.garda
 */
public class BuildMeasurementData {
	class BuildMeas {
		public int buildNo;

		public int getBuildNo() {
			return buildNo;
		}

		public double meas;

		public double getMeas() {
			return meas;
		}

		public BuildMeas(int buildNo, double meas) {
			this.buildNo = buildNo;
			this.meas = meas;
		}
	}

	private String methodName;
	private String className;
	private RegressionEnums.MeasurementUnit unit;
	double treshold;
	private RegressionEnums.RegressionDirection dir;
	List<BuildMeas> buildMeas; // list: item per build

	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}
	
	public RegressionEnums.MeasurementUnit getUnit() {
		return unit;
	}
	
	public double getTreshold() {
		return treshold;
	}
	
	public RegressionEnums.RegressionDirection getDir() {
		return dir;
	}

	public List<BuildMeas> getBuildMeas() {
		return buildMeas;
	}

	public BuildMeasurementData(String data) {
		buildMeas = new ArrayList<BuildMeas>();
		StringTokenizer tokenizer = new StringTokenizer(data, ",");
		methodName = tokenizer.nextToken();
		className = tokenizer.nextToken();
		unit = MeasurementUnit.resolveMeasurementUnitEnum(tokenizer.nextToken());
		treshold = Double.parseDouble(tokenizer.nextToken());
		dir = RegressionDirection.resolveRegressionDirectionEnum(tokenizer.nextToken());

		while (tokenizer.hasMoreTokens()) {
			BuildMeas newBuild = new BuildMeas(Integer.parseInt(tokenizer.nextToken()),
					Double.parseDouble(tokenizer.nextToken()));
			// adding to the buildMeas list
			buildMeas.add(newBuild);
		}
	}
	
	// this must only be used for constructing an object to use in equals() (and not even for that)
	@Deprecated
	public BuildMeasurementData(String methodName, String className){
		this.methodName = methodName;
		this.className = className;
	}

	/**
	 * Parses a given file for BuildMeasurementData lines and gives them back in
	 * a List
	 * 
	 * @param dataFile
	 *            Location of the CSV file with the RegressionData lines
	 * @return List of BuildMeasurementData if the file exists otherwise null
	 */
	public static Set<BuildMeasurementData> getBuildMeasurementDataFromFile(File dataFile) {
		try {
			if (dataFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(dataFile));
				HashSet<BuildMeasurementData> lines = new HashSet<BuildMeasurementData>();
				String line;
				while ((line = br.readLine()) != null) {
					BuildMeasurementData rd = new BuildMeasurementData(line);
					lines.add(rd);
				}
				br.close();
				return lines;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] getBuildMeasurementDataFromFile");
			e.printStackTrace();
		}
		return null;
	}
	
	public void addBuildMeas(int buildNo, double meas){
		buildMeas.add(new BuildMeas(buildNo, meas));
	}

	@Override
	public String toString() {
		String line =         methodName
					  + "," + className 
					  + "," + unit.getShortForm()
					  + "," + treshold
					  + "," + dir.getShortForm()
					  + ",";
		int i = 1;
		for (BuildMeas bm : buildMeas) {
			line += bm.getBuildNo();
			line += ",";
			line += bm.getMeas();
			if(!(i++ == buildMeas.size()))
				line += ",";
		}
		return line;
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
		if (object instanceof BuildMeasurementData) {
			BuildMeasurementData otherRegressionData = (BuildMeasurementData) object;
			result = (this.methodName.equals(otherRegressionData.methodName)
					&& this.className.equals(otherRegressionData.className));
		}
		return result;
	}
}
