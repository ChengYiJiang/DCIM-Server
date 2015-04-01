/**
 * 
 */
package com.raritan.tdz.reports.generator;

import java.util.List;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

/**
 * @author prasanna
 *
 */
public class ReportGeneratorExecutor implements Runnable {
	IRunAndRenderTask birtTask;
	
	public ReportGeneratorExecutor(IRunAndRenderTask birtTask) {
		this.birtTask = birtTask;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			birtTask.run();
			
			@SuppressWarnings("rawtypes")
			List errors = birtTask.getErrors(); 
			if(!errors.isEmpty()) {
				for (Object error: errors) {
					System.out.println(error.toString());
					System.out.println("");
				}
				//there where exceptions! iterate over list to get them one after the other } task.close();
			}
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
