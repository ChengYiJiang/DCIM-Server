/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IProgressMonitor;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.springframework.validation.Errors;

/**
 * @author prasanna
 *
 */
public class BIRTTaskProgressHandler implements ReportTaskProgressHandler,
		IProgressMonitor {
	
	private int completionCounter = 0;
	private boolean statusUpdate = false;
	private IRunAndRenderTask task;
	
	private static String URL_PREFIX = "/dcTrackReports"; 

	public BIRTTaskProgressHandler(IRunAndRenderTask task) {
		this.task = task;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.engine.api.IProgressMonitor#onProgress(int, int)
	 */
	@Override
	public void onProgress(int type, int value) {
	if(type == IProgressMonitor.START_TASK){
			
			switch(value){
				
			case IEngineTask.TASK_RUNANDRENDER : 
			case IEngineTask.TASK_RUN : 
			case IEngineTask.TASK_RENDER :
									completionCounter = 10;
									statusUpdate=true;
								
								break;
			case IEngineTask.TASK_DATAEXTRACTION :
			case IEngineTask.TASK_GETPARAMETERDEFINITION :
			case IEngineTask.TASK_UNKNOWN :
			default :
			}
		}
		
		else if(type == IProgressMonitor.END_TASK){
			this.completionCounter = 100;
			statusUpdate = true;
		}
		else if(type == IProgressMonitor.FETCH_ROW){

			//System.out.print("fetch row" + completionCounter);
			if(completionCounter<15)
				completionCounter = 30;
			if(completionCounter<75)
				completionCounter++;				
			
			statusUpdate=true;
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskProgressHandler#getPercentComplete()
	 */
	@Override
	public int getPercentComplete() {
		return completionCounter;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskProgressHandler#getURL()
	 */
	@Override
	public String getURL() {
		String filePath = task.getRenderOption().getOutputFileName();
		return completionCounter == 100 && filePath != null ? URL_PREFIX + filePath.substring( filePath.contains("/") ? filePath.lastIndexOf("/") : 0) : "";
	}
	
	@Override
	public void cancel() {
		task.cancel();
	}
	
	@Override
	public Errors getErrors() {
		return (Errors) task.getAppContext().get(BIRTReportTaskSetup.ERRORS_OBJECT);
	}
	
	@Override
	public Boolean isRunning() {
		return task.getStatus() == IEngineTask.STATUS_RUNNING;
	}
	
	@Override
	public void cleanup() {
		this.task.close();
	}

}
