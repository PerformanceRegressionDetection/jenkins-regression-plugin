package org.jenkinsci.plugins.regressionrevealer;

public class RegressionEnums {
	enum MeasurementUnit{
		MILLISECOND("ms"), //for time measurement
		SECOND("s"),
		MINUTE("m"),
		HOUR("h"),
		DAY("d"),
		BYTE("B"), //for memory consumption measurement
		KILOBYTE("KB"),
		MEGABYTE("MB"),
		GIGABYTE("GB");
		
		private String shortForm;

		private MeasurementUnit(String shortForm) {
			this.shortForm = shortForm;
		}
		
		public String getShortForm() {
			return shortForm;
		}
		
		public static RegressionEnums.MeasurementUnit resolveMeasurementUnitEnum(String shortName){
			RegressionEnums.MeasurementUnit u = null;
			switch (shortName){
				case "ms": u = RegressionEnums.MeasurementUnit.MILLISECOND; break;
				case "s": u = RegressionEnums.MeasurementUnit.SECOND; break;
				case "m": u = RegressionEnums.MeasurementUnit.MINUTE; break;
				case "h": u = RegressionEnums.MeasurementUnit.HOUR; break;
				case "d": u = RegressionEnums.MeasurementUnit.DAY; break;
				
				case "B": u = RegressionEnums.MeasurementUnit.BYTE; break;
				case "KB": u = RegressionEnums.MeasurementUnit.KILOBYTE; break;
				case "MB": u = RegressionEnums.MeasurementUnit.MEGABYTE; break;
				case "GB": u = RegressionEnums.MeasurementUnit.GIGABYTE; break;
			}
			return u;
		}
	}
	
	enum RegressionDirection{
		MORE("more"),
		LESS("less");
		
		private String direction;

		private RegressionDirection(String shortForm) {
			this.direction = shortForm;
		}
		
		public String getShortForm() {
			return direction;
		}
		
		public static RegressionEnums.RegressionDirection resolveRegressionDirectionEnum(String shortName){
			RegressionEnums.RegressionDirection d = null;
			switch (shortName){
				case "more": d = RegressionEnums.RegressionDirection.MORE; break;
				case "less": d = RegressionEnums.RegressionDirection.LESS; break;
			}
			return d;
		}
	}
}
