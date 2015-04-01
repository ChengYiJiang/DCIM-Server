package com.raritan.tdz.vbjavabridge.jobs;

import java.util.LinkedHashMap;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.apache.log4j.Logger;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.vbjavabridge.home.LNHome;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;

/**
 * A service which periodically runs to fetch any updates that the classic Client updates
 * and calls the appropritate dcTrackListenNotifyEvent handlers.
 * The details of the scheduling are defined in the "jobs.xml" spring configuration file.
 * 
 * 
 * @author Prasanna Nageswar
*/
public class LNPoller extends QuartzJobBean {

	private Logger log = Logger.getLogger(this.getClass());

	private LNHome notifyHome = null;
	

	public void subscribe(LksData operationLks, String tableName, LNEventSubscriber eventHandler){
		if (notifyHome != null)
		{
			notifyHome.subscribe(operationLks, tableName, eventHandler);
		}
	}
	

	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		if (notifyHome != null)
			notifyHome.processEvents();
	}



	public LNHome getNotifyHome() {
		return notifyHome;
	}



	public void setNotifyHome(LNHome notifyHome) {
		this.notifyHome = notifyHome;
	}
}
