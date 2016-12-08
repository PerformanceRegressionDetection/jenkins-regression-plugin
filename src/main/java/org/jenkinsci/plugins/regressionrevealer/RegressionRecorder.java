package org.jenkinsci.plugins.regressionrevealer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;
import org.jenkinsci.plugins.regressionrevealer.BuildMeasurementData.BuildMeas;
import org.jenkinsci.plugins.regressionrevealer.RegressionEnums.RegressionDirection;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Result;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

/**
 * @author zsolt.garda
 *
 */
public class RegressionRecorder extends Recorder {

	// ======================================================
	// ======================================================
	// VARIABLES FROM JENKINS PROJECT CONFIGURATION
	// ======================================================

	/**
	 * value comes from Jenkins project configuration relative path to the
	 * project workspace where regression measurements reside
	 */

	private String measurementDataRelativePath;

	public String getMeasurementDataRelativePath() {
		return measurementDataRelativePath;
	}
	
	private boolean usePerProjectSettings;
	
	public boolean isUsePerProjectSettings() {
		return usePerProjectSettings;
	}

	/**
	 * value comes from Jenkins project configuration how many previous
	 * measurement should count into the regression detection
	 */
	private int howManyPreviousBuildsToConsider;

	public int getHowManyPreviousBuildsToConsider() {
		return howManyPreviousBuildsToConsider;
	}

	/**
	 * The border in percents the regression detector allows a build to have
	 * while it not considers it regressed
	 */
	private double percentageLimit;

	public double getPercentageLimit() {
		return percentageLimit;
	}

	// ======================================================
	// OTHER VARIABLES
	// ======================================================

	/**
	 * Holds all the RegressionData lines from all the files processed This data
	 * will be processed by BuildMeasurementData.addMeasurements()
	 */
	private List<RegressionData> newMeasFromFiles = new ArrayList<RegressionData>();

	/**
	 * Holds all the scanned measurement data across the builds
	 */
	private Set<BuildMeasurementData> bmd = new HashSet<BuildMeasurementData>();

	/**
	 * Holds the regressed testcases to consume by the summary.jelly
	 */
	private List<RegressionData> regressedTests;

	public List<RegressionData> getRegressedTests() {
		return regressedTests;
	}
	
	private boolean haveEnoughPreviousBuild = true;

	// ======================================================
	// ======================================================
	// LOGIC
	// ======================================================

	@DataBoundConstructor
	public RegressionRecorder(String measurementDataRelativePath,
			int howManyPreviousBuildsToConsider, boolean usePerProjectSettings, double percentageLimit) {
		this.measurementDataRelativePath = measurementDataRelativePath;
		this.howManyPreviousBuildsToConsider = howManyPreviousBuildsToConsider;
		this.usePerProjectSettings = usePerProjectSettings;
		this.percentageLimit = percentageLimit;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public final Action getProjectAction(final AbstractProject<?, ?> project) {
		return null;
	}

	@Override
	public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {

		String wsPath = build.getWorkspace().getRemote();

		// getting all the paths that contain measurement data #2
		DirectoryScanner ds = new DirectoryScanner();
		String[] includes = { measurementDataRelativePath };
		ds.setIncludes(includes);		// ds.setExcludes( excludes );
		ds.setBasedir(new File(wsPath));
		ds.setCaseSensitive(true);
		ds.scan();

		String[] files = ds.getIncludedFiles();

		collectMeasDataBetweenBuilds(build, wsPath, files);

		// detect regression and put the regressed testcases into a List
		detectRegression();

		// set build result to UNSTABLE if there are test regressions
		if (regressedTests.size() > 0)
			build.setResult(Result.UNSTABLE);

		build.getActions().add(new RegressionSummary(bmd, regressedTests, haveEnoughPreviousBuild));
		return true;
	}

	/**
	 * Collects measurement data of all the annotated methods across the builds
	 * retrospectively. Scales the measurements "vertically", adds new methods
	 * from classes if necessary. Scales also "horizontally" if given method has
	 * new measurement information from the current build.
	 */
	private void collectMeasDataBetweenBuilds(final AbstractBuild<?, ?> build, String wsPath, String[] filePaths) {
		// creating all the Files from witch to read data
		ArrayList<File> files = new ArrayList<File>();
		for (int i = 0; i < filePaths.length; ++i) {
			files.add(new File(wsPath + "\\" + filePaths[i]));
		}

		// getting previous (aggregated) data from the csv file (into bmd)
		try {
			File holder = new File(wsPath + "\\..\\builds\\" + (build.number - 1) + "\\regressionMeasurementData.csv");
			if (holder.exists())
				bmd = BuildMeasurementData
						.getBuildMeasurementDataFromFile(new File(
								wsPath + "\\..\\builds\\" + (build.number - 1) + "\\regressionMeasurementData.csv"));
		} catch (Exception e) {
			System.out.println("[ERROR] Reading from measurement holder file.");
			e.printStackTrace();
		}

		// collecting RegressionData lines from the "files" (into
		// newMeasFromFiles)
		newMeasFromFiles = new ArrayList<RegressionData>();
		for (File f : files) {
			ArrayList<RegressionData> temp;
			temp = (ArrayList<RegressionData>) RegressionData.getRegressionDataFromFile(f);
			for (RegressionData rd : temp) {
				newMeasFromFiles.add(rd);
			}
		}
		addMeasurementsToBMD(build, wsPath);
	}

	private void addMeasurementsToBMD(final AbstractBuild<?, ?> build, String wsPath) {
		for (RegressionData newline : newMeasFromFiles) {
			BuildMeasurementData lineFromThisBuild = new BuildMeasurementData(newline.getMethodName()
																			+ "," + newline.getClassName()
																			+ "," + newline.getMeasurementUnit().getShortForm()
																			+ "," + newline.getTreshold()
																			+ "," + newline.getDir().getShortForm()
																			+ "," + build.number
																			+ "," + newline.getMeasurementInMillis());
			if (!bmd.contains(lineFromThisBuild)) {
				bmd.add(lineFromThisBuild);
			} else {
				for (BuildMeasurementData b : bmd) {
					if (b.equals(lineFromThisBuild))
						b.addBuildMeas(build.number, newline.getMeasurementInMillis());
				}
			}
		}

		// writing bmd to file
		try (FileWriter writer = new FileWriter(
				wsPath + "\\..\\builds\\" + (build.number) + "\\regressionMeasurementData.csv")) {
			for (BuildMeasurementData b : bmd) {
				writer.write(b.toString() + System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void detectRegression(){
		regressedTests = new ArrayList<RegressionData>();
		
		//traversing all the measured methods to find regressed measurements
		for(BuildMeasurementData b : bmd){
			double avg = 0; //the average of the measurements of the previous builds
			//getting all the measurements in order to produce average
			int i = 0;
			int size = b.getBuildMeas().size();
			for(BuildMeas bm : b.getBuildMeas()){
				// only count in the appropriate builds
				// lower bound : begin from the end - howMany.. and -1 for the indexing from 0
				// upper bound : -1 for the indexing from 0 and -1 to not consider the last meas
				if(i>=(size - howManyPreviousBuildsToConsider - 1) && i<=(size - 1 - 1)){
					avg += bm.getMeas();
				}
				++i;
			}
			avg /= howManyPreviousBuildsToConsider; //this holds the average we should compare to
			// --------------------
			// regression detection
			double extra;
			if(usePerProjectSettings == true)
				extra = avg * (percentageLimit / 100); 
			else 
				extra = avg * ((double)b.getTreshold() / 100);
			if(size > howManyPreviousBuildsToConsider){
				haveEnoughPreviousBuild = true;
				if((b.getDir() == RegressionDirection.MORE && (b.getBuildMeas().get(size - 1).getMeas()) > (avg + extra)) //more than enough
						|| (b.getDir() == RegressionDirection.LESS && (b.getBuildMeas().get(size - 1).getMeas()) < (avg - extra))){ //less than expected
					regressedTests.add(new RegressionData(b.getMethodName(), b.getClassName()));
				}
			} else {
				haveEnoughPreviousBuild = false;
			}
			// ---- DEBUG ----
			//System.out.println("AVG: " + avg);
			//System.out.println("extra: " + extra);
			//System.out.println("last build meas: " + b.getBuildMeas().get(size - 1).getMeas());
			//System.out.println("*-*-*-*- haveEnoughPreviousBuild *-*-*-" + haveEnoughPreviousBuild);
			//System.out.println("*- SIZE : " + size + "*- HOWMANYTOCONSIDER : " + howManyPreviousBuildsToConsider);
			
		}
		//System.out.println("REGRESSED TESTS: " + regressedTests.toString());
	}

}
