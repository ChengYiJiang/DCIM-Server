package com.raritan.tdz.piq.jobs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQRestClientBase;
import com.raritan.tdz.piq.json.JobJSON;
import com.raritan.tdz.piq.json.JobJSON.Job;
import com.raritan.tdz.piq.json.JobJSON.JobStatus;
import com.raritan.tdz.piq.json.JobMessagesJSON;
import com.raritan.tdz.piq.json.JobMessagesJSON.JobMessage;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * PIQ Job Poller which is backed by a ScheduledThreadPoolExecutor.
 * @author Andrew Cohen
 */
@Transactional
public class PIQJobPollerImpl extends PIQRestClientBase implements PIQJobPoller, RejectedExecutionHandler {
	
	private long maxTries;
	private long delaySeconds;
	private long periodSeconds;
	private ScheduledThreadPoolExecutor scheduler;
	
	private EventHome eventHome; // The event log for logging job errors
	
	public PIQJobPollerImpl(ApplicationSettings settings, EventHome eventHome) throws DataAccessException {
		
		super( settings );
		
		this.eventHome = eventHome;
		
		this.scheduler = new ScheduledThreadPoolExecutor(5, (RejectedExecutionHandler)this);
		this.scheduler.setThreadFactory( new JobPollerThreadFactory() );
		this.scheduler.setKeepAliveTime(60, TimeUnit.SECONDS);
		this.scheduler.allowCoreThreadTimeOut( true );
	}
	
	@Override
	public Map<String, Object> addJob(String jobId, List<PIQJobHandler> handlers, Map<String, Object> data) {
		if (jobId == null)
			throw new IllegalArgumentException("jobId cannot be null!");
		if (handlers == null)
			throw new IllegalArgumentException("handlers cannot be null!");
		
		JobPollerTask task = new JobPollerTask(jobId, handlers, data);
		scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
		
		// Block the calling thread until the job is completed.
		synchronized(task) {
			try {
				log.debug("Waiting until job is complete...");
				task.wait(500000);
				log.debug("Job is complete!");
			}
			catch (InterruptedException e) {
				log.error("", e);
			}
		}
		
		return task.data;
	}

	@Override
	public Map<String, Object>  addJob(String jobId, PIQJobHandler handler, Map<String, Object> data) {
		List<PIQJobHandler> handlers = new LinkedList<PIQJobHandler>();
		handlers.add( handler );
		return addJob( jobId, handlers, data );
	}
	
	@Override
	public int getActiveJobs() {
		return scheduler.getActiveCount();
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		JobPollerTask task = (JobPollerTask)r;
		// TODO: Log to system event log
		log.error("Rejected PIQ Job poller task for job " + task.jobId);
	}

	public void setDelaySeconds(long delay) {
		this.delaySeconds = delay;
	}
	
	public void setPeriodSeconds(long period) {
		this.periodSeconds = period;
	}
	
	public void setMaxTries(int maxTries) {
		this.maxTries = maxTries;
	}
	
	//
	// Start private methods and classes
	//
	
	private Job getJobStatus(String jobId) {
		Job job = null;
		try {
			ResponseEntity<?> resp = doRestGet(jobId, JobJSON.class);
			if (resp != null) {
				JobJSON jobResp = (JobJSON)resp.getBody();
				if (jobResp != null) {
					job = jobResp.getJob();
				}
			}
		}
		catch (RemoteDataAccessException e) {
			job = null;
			log.error("Error fetching job status for jobId=" + jobId, e);
		}
		return job;
	}
	
	/**
	 * A scheduled task for polling a particular PIQ job.
	 */
	private class JobPollerTask implements Runnable {
		private String jobId;
		private Map<String, Object> data;
		private List<PIQJobHandler> handlers;
		private int numTries;
		
		JobPollerTask(String jobId, List<PIQJobHandler> handlers, Map<String, Object> data) {
			this.jobId = jobId;
			this.handlers = handlers;
			this.numTries = 0;
			this.data = new HashMap<String, Object>( data );
		}
		
		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Polling PIQ job ID " + jobId);
			}
			
			if (numTries > maxTries) {
				log.warn("Giving up polling PIQ job with ID " + jobId + ", exceeded max polling attempts (" + maxTries + ")");
				return;
			}
			
			boolean jobCompleted = false;
			
			try {
				Job job = getJobStatus( jobId );
				if (job == null) {
					log.warn("No PIQ job status for job ID " + jobId);
					numTries++;
					return;
				}
				
				JobStatus status = job.getJobStatus();
				
				if (status == JobStatus.COMPLETED) {
					jobCompleted = true;
					handleJobErrors( job );
				}
				else if (status == JobStatus.ABORTED) {
					jobCompleted = true;
				}
				
				handleJobStatusUpdate(job, data);
				
				if (!jobCompleted) {
					// Reschedule the polling task until Job is completed or aborted
					scheduler.schedule(this, periodSeconds, TimeUnit.SECONDS);
				}
			}
			catch (Throwable t) {
				t.printStackTrace();
				log.error("Error polling PIQ job status for job ID " + jobId);
				jobCompleted = true;
			}
			finally {
				numTries++;
				if (jobCompleted) {
					synchronized(this) {
						this.notifyAll();
					}
				}
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj instanceof JobPollerTask) {
				return this.jobId.equals(((JobPollerTask)obj).jobId);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return jobId.hashCode();
		}
		
		/**
		 * Handle job errors by querying for the job messages and logging them in the in system event log.
		 * @param job
		 */
		private void handleJobErrors(Job job) {
			if (!job.getHasErrors()) return;
			
			List<JobMessage> messages = null;
			try {
				ResponseEntity<?> resp = doRestGet("v2/job_messages", "?job_id_eq=" +  job.getId(), JobMessagesJSON.class);
				
				if (resp != null) {
					JobMessagesJSON list = (JobMessagesJSON)resp.getBody();
					
					if (list != null) {
						messages = list.getJobMessages();
						
						if (messages != null) {
							// Log error to system event log
							Event event = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
							event.setSummary(eventSource + " Job " + jobId + " completed with errors: " + job.getLastMessage());
							event.addParam("Job ID", jobId);
							
							for (JobMessage msg : messages) {
								event.addParam(eventSource + " " + msg.getLevel(),  msg.getMessage());
							}
							
							eventHome.saveEvent( event );
							
							// Save this error event in the data map so that job handlers
							// can add or update more specific information about this event.
							data.put("errorEventId",  event.getId());
						}
					}
				}
			}
			catch (RemoteDataAccessException e) {
				log.error("Error retrieving PIQ job messages for job "  + jobId);
			}
			catch (DataAccessException e) {
				log.error("Error logging PIQ job messages to event log "  + jobId);
			}
		}
		
		/**
		 * Sequentially invoke each handler with the updated job status.
		 * We can multi-thread this later if we need to for performance.
		 * @param job
		 * @param pduId
		 */
		private void handleJobStatusUpdate(Job job, Map<String, Object> data) {
			JobStatus status = job.getJobStatus();
			
			for (PIQJobHandler handler: handlers) {
				switch( status ){
					case ABORTED:
						handler.onJobError( job, data );
						break;
					case ACTIVE:
						handler.onJobUpdate( job, data );
						break;
					case COMPLETED:
						if (job.getHasErrors()) {
							handler.onJobError( job, data);
						}
						else {
							handler.onJobComplete( job, data );
						}
						break;
				}
			}
		}
	}
	
	private static class JobPollerThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        JobPollerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "PIQ Job Poller " +
                          poolNumber.getAndIncrement();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
