package org.jenkinsci.plugins.regressionrevealer;

import java.util.StringTokenizer;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

@Extension
public class RegressionDescriptor extends BuildStepDescriptor<Publisher> {
		
	public RegressionDescriptor() {
		super(RegressionRecorder.class);
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "Regression Revealer";
	}
	
    /**
     * Validation of checkMeasurementHolderFilePath project config item
     */
    public FormValidation doCheckMeasurementDataRelativePath(@QueryParameter String value) {			
    	String[] tokens = value.split("\\.");
    	if(tokens[tokens.length - 1].equals("csv"))
    		return FormValidation.ok();
    	else
    		return FormValidation.error("No proper .csv file provided");
    }
    
    /**
     * Validation of howManyPreviousBuildsToConsider project config item
     */
    public FormValidation doCheckHowManyPreviousBuildsToConsider(@QueryParameter String value) {			
    	try{
    		int builds = Integer.parseInt(value);
    	} catch (NumberFormatException nfe) {
    		return FormValidation.error("Value is not an integer");
    	}
    	int builds = Integer.parseInt(value);
    	if(builds < 1)
    		return FormValidation.error("Cannot have less than 1 build");
    	else
    		return FormValidation.ok();
    }
    
    /**
     * Validation of PercentageLimit project config item
     */
    public FormValidation doCheckPercentageLimit(@QueryParameter String value) {
    	Double percent;
    	try{
    		percent = Double.parseDouble(value);
    	} catch (NumberFormatException nfe) {
    		return FormValidation.error("Value is not a double!");
    	}
    	if(percent < 0)
    		return FormValidation.error("No less than 0 percentage is allowed");
    	else if(percent <= 100)
    		return FormValidation.ok();
    	else
    		return FormValidation.warning("Percentage needed, isn't this value too big?");
    }
}
