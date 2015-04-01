package com.raritan.tdz.reports.generator;

import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IProgressMonitor;

public class ReportGeneratorProgressMonitor implements IProgressMonitor {
	
	private long completionCounter = 0;
	private boolean statusUpdate = false;

	public void onProgress(int type, int value) {
	if(type == IProgressMonitor.START_TASK){
			
			switch(value){
				
			case IEngineTask.TASK_RUNANDRENDER : 
			case IEngineTask.TASK_RUN : 
			case IEngineTask.TASK_RENDER : //System.out.println("TASK RUN/RENDER " + (new Date()).toString());
									completionCounter = 10;
									statusUpdate=true;
								
								break;
			case IEngineTask.TASK_DATAEXTRACTION : //System.out.println("TASK_DATAEXTRACTION " + (new Date()).toString());break;
			case IEngineTask.TASK_GETPARAMETERDEFINITION : //System.out.println("TASK_GETPARAMETERDEFINITION " + (new Date()).toString());break;
			case IEngineTask.TASK_UNKNOWN : //System.out.print("TASK_UNKNOWN " + (new Date()).toString());break;
			default : //System.out.println("invalid VAL ::::::::: " + task + " val " + val); break;
			}
		}
		
		else if(type == IProgressMonitor.END_TASK){
			
			System.out.println("task  ENDDDDDDDDDDDD" + value);
			this.completionCounter = 100;
		    
			statusUpdate = true;
		}
			
		//else if(task == IProgressMonitor.START_PAGE)
			//	System.out.println("starting page " + val);

	//	else if(task == IProgressMonitor.END_PAGE)
//				System.out.println("end page " + val);

//		else if(task == IProgressMonitor.START_QUERY)
	//			System.out.println("start query for element " + val);

		//else if(task == IProgressMonitor.END_QUERY)
		//		System.out.println("end query for element " + val);

		else if(type == IProgressMonitor.FETCH_ROW){

			//System.out.print("fetch row" + completionCounter);
			if(completionCounter<15)
				completionCounter = 30;
			if(completionCounter<75)
				completionCounter++;				
			
			statusUpdate=true;
		}
		
		//else
			//System.out.println(" unknown TASK " + task);
		
		try{

			if(statusUpdate){
				System.out.println("Counter : " + completionCounter);
	
				//this.reportService.updateCustomReport(customReport);
			}
		}catch(Exception sle){
			
			sle.printStackTrace();
			//LogManager.error(sle.getExceptionContext(), this.appLogger);
		}
		
		

	}

}
