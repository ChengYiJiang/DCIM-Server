/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.domain.Report;

import flex.messaging.log.Log;

/**
 * This is the BIRT ReportTask progress manager
 * @author prasanna
 *
 */
public class BIRTReportTaskProgressManager extends ReportTaskProgressManagerBase implements ReportTaskProgressManager, ReportTaskProgressHandlerFactory {
	
	private String userSessionId;
	
	private List<String> outputPaths = new ArrayList<>();
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	@Autowired
	private ReportsDAO reportDao;
	
	public BIRTReportTaskProgressManager(String userSessionId){
		this.userSessionId = userSessionId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportTaskProgressManager#createProgressHandler(long)
	 */
	@Override
	public ReportTaskProgressHandler createProgressHandler(long reportId, Object additionalArgs) {
		ReportTaskProgressHandler handler = progressHandlerMap.get(reportId);
		if (handler == null || handler.getPercentComplete() == 100){
			List args = (List) additionalArgs;
			handler = new BIRTTaskProgressHandler((IRunAndRenderTask)args.get(0));
			outputPaths.add((String)args.get(1));
			progressHandlerMap.put(reportId, handler);
		}
		return handler;
	}
	
	public void destroy(){
		//Remove all files associated with the user context as user logged out.
		deleteReports();
		cleanupProgressHandlers();
	}
	
	private void cleanupProgressHandlers() {
		for (Map.Entry<Long, ReportTaskProgressHandler> progressHandlerEntry:progressHandlerMap.entrySet()){
			progressHandlerEntry.getValue().cleanup();
		}
	}
	
	private void deleteReports(){
		for (String outputPath: outputPaths){
			try {
				Files.delete(Paths.get(outputPath));
			} catch (IOException e) {
				if (Log.isDebug())
					e.printStackTrace();
			}
		}
		outputPaths.clear();
	}
}
