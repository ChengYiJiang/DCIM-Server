package com.raritan.tdz.piq.jobs;

import java.util.Map;

import com.raritan.tdz.piq.json.JobJSON.Job;

/**
 * A call back interface to asynchronously handle PIQ Job status updates from the PIQJobPoller.
 * @author Andrew Cohen
 */
public interface PIQJobHandler {

	/**
	 * A call back invoked when a Job has successfully completed.
	 * The job details and associated PDU ID are provided.
	 * @param job job details
	 * @param pduId ID of the newly added PDU
	 */
	public void onJobComplete(Job job, Map<String, Object> data);
	
	/**
	 * A call back invoked when a Job completed with errors.
	 * @param job job details
	 */
	public void onJobError(Job job, Map<String, Object> data);
	
	/**
	 * Method callback after Job status was polled and Job is still active.
	 * @param job job details
	 */
	public void onJobUpdate(Job job, Map<String, Object> data);
}
