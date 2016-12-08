package org.jenkinsci.plugins.regressionrevealer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.ProminentProjectAction;
import hudson.util.Area;
import hudson.util.ChartUtil;

public final class RegressionSummary implements Action {
	
	/**
	 * holds the tests that are detected to have been regressed
	 */
	private List<RegressionData> regressedTests;
	public List<RegressionData> getRegressedTests() {
		return regressedTests;
	}
	
	public int getRegressedTestsSize(){
		return regressedTests.size();
	}

	public Set<BuildMeasurementData> bmd;
	
	private boolean haveEnoughPreviousBuild;
	public boolean getHaveEnoughPreviousBuild(){
		return haveEnoughPreviousBuild;
	}

	public RegressionSummary(Set<BuildMeasurementData> bmd, List<RegressionData> regressedTests, boolean haveEnoughPreviousBuild) {
		super();
		this.bmd = bmd;
		this.regressedTests = regressedTests;
		this.haveEnoughPreviousBuild = haveEnoughPreviousBuild;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "RegressionRevealer test results";
	}

	@Override
	public String getUrlName() {
		return "regression";
	}

	@SuppressWarnings("deprecation")
	public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException{		
		String methodName = String.valueOf(req.getParameter("methodName"));
		String className = String.valueOf(req.getParameter("className"));
		
		//filtering the bmd to the currently requested method
		BuildMeasurementData tmp = new BuildMeasurementData(methodName, className);
		BuildMeasurementData current = null; //the one record that belongs to the given method + class name
		for(BuildMeasurementData b : bmd){
			if(b.equals(tmp)){
				current = b;
			}
		}
		
		// ---- DEBUG ----
		//System.out.println("STAPLER ARGS: " + methodName + " + " +  className);
		
		//is the current record regressed?
		boolean currentRegressed = false;
		for(RegressionData r : regressedTests){
			if(r.getClassName().equals(current.getClassName()) && r.getMethodName().equals(current.getMethodName()))
				currentRegressed = true;
		}
		
		int chartWidth;
		int buildMeasSize = current.getBuildMeas().size();
		if(buildMeasSize < 10){
			chartWidth = 500;
		} else if(buildMeasSize >= 10 && buildMeasSize < 20){
			chartWidth = 1000;
		} else {
			chartWidth = 1450;
		}
		ChartUtil.generateGraph(req, rsp, AreaChart.createChart(current, currentRegressed), chartWidth, 350);
	}
}
