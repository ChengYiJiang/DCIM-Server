package com.raritan.tdz.reports.generator;

import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;


public interface GenerateReport {

	/**
	 * perform cleanup operations
	 */
	public void cleanup();

	/**
	 * generate the report using the report design file in the format asked and put the report in the output file
	 * @param reportDesignFile
	 * @param reportOutputFile
	 * @param reportFormat
	 * @param reportParameters TODO
	 * @throws EngineException 
	 */
	void generateReport(String reportDesignFile, String reportOutputFile,
			String reportFormat, Map<String, Object> reportParameters) throws Throwable;
	
}
