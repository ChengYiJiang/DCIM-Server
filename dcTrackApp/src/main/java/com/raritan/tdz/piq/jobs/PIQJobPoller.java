package com.raritan.tdz.piq.jobs;

import java.util.List;
import java.util.Map;

/**
 * A service which polls PIQ jobs and invokes call back methods
 * on PIQJobHandler instances provided by the client.
 * @author Andrew Cohen
 */
public interface PIQJobPoller {

	/**
	 * Add a PIQ job to be polled.
	 * This is a blocking call that will not return until the job is completed or aborted.
	 * @param jobId the job ID
	 * @param handlers A list of callback handlers to invoke
	 * @return job output data as a map
	 */
	public Map<String, Object> addJob(String jobId, List<PIQJobHandler> handlers, Map<String, Object> data);
	
	/**
	 * Add a PIQ job to be polled. 
	 * This is a blocking call that will not return until the job is completed or aborted.
	 * @param jobId the job ID
	 * @param ipAddress String ipAddress
	 * @param handler a callback handler to invoke
	 * @return job output data as a map
	 */
	public Map<String, Object> addJob(String jobId, PIQJobHandler handler, Map<String, Object> data);
	
	/**
	 * @return the number of active jobs being polled.
	 */
	public int getActiveJobs();
}