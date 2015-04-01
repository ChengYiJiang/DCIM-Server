/**
 * 
 */
package com.raritan.tdz.reports.generator;

import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;

/**
 * @author prasanna
 *
 */
public class ReportGeneratorSetup {
	ReportEngine eng;
	String outputFormat;
	
	private ReportGeneratorProgressMonitor reportGeneratorProgressMonitor;
	
	public ReportGeneratorSetup(ReportEngine eng, String outputFormat){
		this.eng = eng;
		this.outputFormat = outputFormat;
	}
	
	
	
	public ReportGeneratorProgressMonitor getReportGeneratorProgressMonitor() {
		return reportGeneratorProgressMonitor;
	}



	public void setReportGeneratorProgressMonitor(
			ReportGeneratorProgressMonitor reportGeneratorProgressMonitor) {
		this.reportGeneratorProgressMonitor = reportGeneratorProgressMonitor;
	}



	public IRunAndRenderTask setup(String designPath, String outPutPath, Map params) throws EngineException{	
		IReportRunnable design = eng.openReportDesign(designPath);
		IRunAndRenderTask task = eng.createRunAndRenderTask(design);
		   
		IRenderOption options = new RenderOption();
		options.setOutputFileName(outPutPath);
		options.setOutputFormat(outputFormat);
		task.setRenderOption(options);
		task.setProgressMonitor(reportGeneratorProgressMonitor);
		task.setParameterValues(params);
		
		return task;
	}

}
