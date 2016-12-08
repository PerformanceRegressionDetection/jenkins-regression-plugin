package org.jenkinsci.plugins.regressionrevealer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.ConversionException;
import org.jenkinsci.plugins.regressionrevealer.BuildMeasurementData.BuildMeas;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class AreaChart {
	
	public static JFreeChart createChart(BuildMeasurementData bmd, boolean regressed) {
		JFreeChart lineChartObject = null;
		DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
		ArrayList<BuildMeas> m = (ArrayList<BuildMeas>) bmd.getBuildMeas();
		
		for(int i=0; i<m.size(); ++i){			
			//if there are no measurement for a given build, the graph will show 0
			//for the value of the measurement BUT the regression detection
			//won't count in those 0-s, only the real measurements
			int buildNoDiff = 0;
			if(i!=0){
				buildNoDiff = m.get(i).getBuildNo() - m.get(i-1).getBuildNo(); //detecting if there are "holes" between the measurements
			}
			
			//adding "buildNoDiff - 1" pieces of 0 values to the graph
			if(buildNoDiff > 1){
				int baseBuildNo = m.get(i-1).getBuildNo();
				for(int j=baseBuildNo+1; j<baseBuildNo+1 + buildNoDiff-1; ++j){
					line_chart_dataset.addValue(0, bmd.getUnit().getShortForm(), String.valueOf(j));
				}
			}			
			double currentMeas = convertMeasToUnit(m.get(i).getMeas(), bmd.getUnit().getShortForm()); //converting the measurement to the appropriate format
			line_chart_dataset.addValue(currentMeas, bmd.getUnit().getShortForm(), Integer.toString(m.get(i).getBuildNo()));
		}
		
		lineChartObject = ChartFactory.createAreaChart(bmd.getMethodName() + " (" + bmd.getClassName() + ")",
				"buildNo", "[" + bmd.getUnit().getShortForm() + "]", line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);
		//if a test is regressed, keep the plot color to red, otherwise blue
		AreaRenderer ar = new AreaRenderer();
		CategoryPlot plot = lineChartObject.getCategoryPlot();
		Color plotColor;
		if(!regressed)
			plotColor = new Color(0, 0, 255, 120);
		 else 
			plotColor = new Color(255, 0, 0, 120);
		ar.setPaint(plotColor);
		plot.setRenderer(ar);
		
		return lineChartObject;
	}
	
	private static double convertMeasToUnit(double currentMeas, String unitShortForm){
		double converted = -1;
		switch (unitShortForm){
			case "ms": converted = currentMeas; break;
			case "s": converted = currentMeas / 1000; break;
			case "m": converted = currentMeas / (1000 * 60); break;
			case "h": converted = currentMeas / (1000 * 60 * 60); break;
			case "d": converted = currentMeas / (1000 * 60 *60 * 24); break;
			
			case "B": converted = currentMeas; break;
			case "KB": converted = currentMeas / 1000; break;
			case "MB": converted = currentMeas / (1000 * 1000); break;
			case "GB": converted = currentMeas / (1000 * 1000 * 1000); break;
		}
		return converted;
	}
}
