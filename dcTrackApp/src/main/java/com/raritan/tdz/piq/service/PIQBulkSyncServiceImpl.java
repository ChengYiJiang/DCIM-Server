package com.raritan.tdz.piq.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.exception.DataException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.StringUtils;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventStatus;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.dto.PIQBulkSyncStatusDTO;
import com.raritan.tdz.piq.home.PIQRestClient;
import com.raritan.tdz.piq.jobs.listener.PIQBulkSyncJobListener;
import com.raritan.tdz.piq.jobs.listener.PIQBulkSyncStepListener;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author Andrew Cohen
 */
public class PIQBulkSyncServiceImpl implements PIQBulkSyncService {

	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	private JobLauncher piqSyncJobLauncher;
	private Job piqSyncJob;
	private JobExecution piqJobExecution;
	private PIQBulkSyncStepListener piqLocSyncListener;
	private PIQBulkSyncStepListener piqCabSyncListener;
	private PIQBulkSyncStepListener piqDevSyncListener;
	private PIQBulkSyncStepListener piqRPDUSyncListener;
	private PIQBulkSyncStepListener piqPowerConnSyncListener;
	private PIQBulkSyncJobListener piqJobListener;
	private PIQBulkSyncStatusDTO lastStatus;
	private String piqHost;
	
	private EventHome eventHome;
	
	public PIQBulkSyncServiceImpl() {
	}

	public void setPiqSyncJobLauncher(JobLauncher piqSyncJobLauncher) {
		this.piqSyncJobLauncher = piqSyncJobLauncher;
	}

	public void setPiqSyncJob(Job piqSyncJob) {
		this.piqSyncJob = piqSyncJob;
	}
	
	public void setPiqLocSyncListener(PIQBulkSyncStepListener piqLocSyncListener) {
		this.piqLocSyncListener = piqLocSyncListener;
	}

	public void setPiqCabSyncListener(PIQBulkSyncStepListener piqCabSyncListener) {
		this.piqCabSyncListener = piqCabSyncListener;
	}

	public void setPiqDevSyncListener(PIQBulkSyncStepListener piqDevSyncListener) {
		this.piqDevSyncListener = piqDevSyncListener;
	}

	public void setPiqRPDUSyncListener(PIQBulkSyncStepListener piqRPDUSyncListener) {
		this.piqRPDUSyncListener = piqRPDUSyncListener;
	}
	
	public void setPiqPowerConnSyncListener(PIQBulkSyncStepListener piqPowerConnSyncListener) {
		this.piqPowerConnSyncListener = piqPowerConnSyncListener;
	}

	public void setPiqJobListener(PIQBulkSyncJobListener piqJobListener) {
		this.piqJobListener = piqJobListener;
	}
	
	public void setEventHome(EventHome eventHome) {
		this.eventHome = eventHome;
	}

	@Override
	public synchronized PIQBulkSyncStatusDTO updatePIQData(String ipAddress) throws ServiceLayerException {
		lastStatus = null;
//		piqJobExecution = null;
//		if (piqJobExecution != null && piqJobExecution.isRunning()) {
//			throw new ServiceLayerException(new ExceptionContext(ApplicationCodesEnum.PIQ_UPDATE_RUNNING, this.getClass(), null));
//		}
		
		if (piqJobExecution != null && piqJobExecution.isRunning()){
			PIQBulkSyncStatusDTO status = getPIQUpdateDataStatus(ipAddress);
			return status;
		}
		
		try {
			Map<String, JobParameter> params = new HashMap<String, JobParameter>();
			Calendar now = Calendar.getInstance();
			params.put("startTime", new JobParameter(now.getTime()));
			resetCurrentCounts();
			piqJobExecution = piqSyncJobLauncher.run(piqSyncJob, new JobParameters(params));
		}  
		catch (JobExecutionAlreadyRunningException e) {
			// TODO: Messages in ApplicationCodesEnum
			throw new ServiceLayerException(new ExceptionContext(ApplicationCodesEnum.PIQ_UPDATE_RUNNING, this.getClass(), e));
		} 
		catch (JobRestartException e) {
			throw new ServiceLayerException(new ExceptionContext(ApplicationCodesEnum.PIQ_UPDATE_CANNOT_RESTART, this.getClass(), e));
		} 
		catch (JobInstanceAlreadyCompleteException e) {
			throw new ServiceLayerException(new ExceptionContext(ApplicationCodesEnum.PIQ_UPDATE_ALREADY_COMPLETED, this.getClass(), e));
		} 
		catch (JobParametersInvalidException e) {
			throw new ServiceLayerException(new ExceptionContext(ApplicationCodesEnum.PIQ_UPDATE_INVALID, this.getClass(), e));
		}
		
		PIQBulkSyncStatusDTO status = getPIQUpdateDataStatus(ipAddress);
		
		// Clear current counts
		status.setCurrentLocations(0);
		status.setCurrentCabinets(0);
		status.setCurrentDevices(0);
		status.setCurrentRPDUs(0);
		status.setCurrentPowerConns(0);
		
		return status;
	}

	@Override
	public synchronized PIQBulkSyncStatusDTO getPIQUpdateDataStatus(String ipAddress) throws ServiceLayerException {
		PIQBulkSyncStatusDTO dto = new PIQBulkSyncStatusDTO();
		
		if (piqJobExecution == null) {
			Object[] ret = getLastPIQUpdateTime(ipAddress);
			dto.setLastUpdateTime( (Date)ret[0] );
			dto.setLastUpdateAction( (String)ret[1] );
			dto.setJobRunning( false );
			return dto;
		}
		
		dto.setJobRunning( piqJobExecution.isRunning() );
		
		if (!dto.isJobRunning()) {
			Object[] ret = getLastPIQUpdateTime(ipAddress);
			dto.setLastUpdateTime( (Date)ret[0] );
			dto.setLastUpdateAction( (String)ret[1] );
		}
		
		dto.setTotalLocations( piqJobListener.getCategoryTotalCount("location") );
		dto.setCurrentLocations( getLocationCurrentCount() );
			
		dto.setTotalCabinets( piqJobListener.getCategoryTotalCount("cabinet") );
		dto.setCurrentCabinets( getCabinetCurrentCount() );
		
		dto.setTotalDevices( piqJobListener.getCategoryTotalCount("device") ); 
		dto.setCurrentDevices( getDeviceCurrentCount() );
		
		dto.setTotalRPDUs( piqJobListener.getCategoryTotalCount("rpdu") );
		dto.setCurrentRPDUs( getRPDUCurrentCount() );
		
		dto.setTotalPowerConns( piqJobListener.getCategoryTotalCount("connection") );
		dto.setCurrentPowerConns( getPowerConnCurrentCount() );
		
		dto.setSkippedCount( getSkippedCount(piqJobExecution) );
		
		if (!piqJobExecution.isRunning()) {
			ExitStatus status = piqJobExecution.getExitStatus();
			if (status.equals(ExitStatus.FAILED)) {
				dto.setJobFailed( true );
			}
		}
		
		getTotalCount(dto);
		
		if (log.isDebugEnabled()) {
			log.info("PIQ Update Status : " + dto);
		}
		
		lastStatus = dto;
		
		return dto;
	}

	@Override
	public synchronized PIQBulkSyncStatusDTO stopPIQDataUpdate(String ipAddress) throws ServiceLayerException {
		PIQBulkSyncStatusDTO dto = getPIQUpdateDataStatus(ipAddress);
		if (piqJobExecution != null) {
			piqJobExecution.stop();
		}
		
		dto.setJobRunning( false );
		dto.setJobStopped( true );
		
		return dto;
	}
	
	//
	// Job Listener callbacks
	//
	
	@BeforeJob
	public void beforeJob(JobExecution execution) {
		Event ev = logPIQStartUpdateEvent( execution );
		
		if (ev != null) {
			// Add totalCount and the starting event ID to the job context
			ExecutionContext ctx = execution.getExecutionContext();
			ctx.put("totalCount", piqJobListener.getTotalCount());
			if (ev != null) {
				ctx.put("startEventId", ev.getId());
			}
		}
	}
	
	@AfterJob
	public void afterJob(JobExecution execution) {
		logPIQEndUpdateEvent( execution );
	}
	
	//
	// Private methods
	//

	private Event logPIQStartUpdateEvent(JobExecution execution) {
		Timestamp startedAt = new Timestamp(execution.getStartTime().getTime());
		Event startEv = null;
		
		// Create a event in the system log indicating we've started the PIQ update.
		try {
			startEv = eventHome.createEvent(startedAt, EventType.PIQ_UPDATE, EventSeverity.INFORMATIONAL, (new StringBuilder(PIQRestClient.EVENT_SOURCE).append(" (").append(piqHost).append(")")).toString());
			startEv.setSummary((new StringBuilder(PIQRestClient.EVENT_SOURCE).append(" (").append(piqHost).append(")").append(" Update started")).toString());
			eventHome.saveEvent( startEv );
		}
		catch (DataAccessException e) {
			log.error("", e);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Setting total count: " + piqJobListener.getTotalCount());
		}
		
		return startEv;
	}
	
	/**
	 * Creates a new system log event containing all details about the PIQ Update job that completed.
	 * @param execution
	 */
	private void logPIQEndUpdateEvent(JobExecution execution) {
		final ExitStatus exitStatus = execution.getExitStatus();
		
		// Create a event in the system log indicating that PIQ update has finished
		Event ev = null;
		try {
			ev = buildPIQUpdateEvent( execution );
			
			if (ev != null) {
				addJobEventParams( ev, exitStatus.getExitCode(), exitStatus.getExitDescription() );
				eventHome.saveEvent( ev );
				clearJobStartEvent(ev, (Long)execution.getExecutionContext().get("startEventId"));
			}
		}
		catch (ServiceLayerException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Build a new PIQ update event with an appropriate summary based on the job exit code.
	 * @param exitCode the exit code of the job
	 * @param timeMins time to complete the job in minutes
	 * @return an event
	 */
	private Event buildPIQUpdateEvent(JobExecution execution) throws ServiceLayerException {
		Event ev = null;
		final String exitCode = execution.getExitStatus().getExitCode();
		final Collection<Throwable> jobFailures = execution.getFailureExceptions();
		
		// Get total records skipped
		final int skippedCount = getSkippedCount( execution );
		
		// Get elapsed time
		final long time = execution.getEndTime().getTime() - execution.getStartTime().getTime();
		long timeMins = ((time / 1000) / 60);
		if (timeMins <= 0) timeMins = 1;
		
		//
		// Set the event summary
		//
		String summary = null;
		String eventSource = (new StringBuilder(PIQRestClient.EVENT_SOURCE).append(" (").append(piqHost).append(")")).toString();
		if (exitCode.equals( ExitStatus.COMPLETED.getExitCode() )) {
			ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.INFORMATIONAL, eventSource);
			summary = eventHome.getMessageSource().getMessage("piqUpdate.success", new Object[]{ timeMins, skippedCount, piqHost }, null);
		}
		else if (exitCode.equals( ExitStatus.FAILED.getExitCode() )) {
			ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.CRITICAL, eventSource);
			PIQBulkSyncStatusDTO status = lastStatus;
			if (status != null) {
				status = getPIQUpdateDataStatus(piqHost);
			}
			summary = eventHome .getMessageSource().getMessage("piqUpdate.failure", 
					new Object[] { 
						timeMins,
						status.getTotalProcessed(),
						status.getTotalCount(),
						status.getCurrentLocations(),
						status.getTotalLocations(),
						status.getCurrentCabinets(),
						status.getTotalCabinets(),
						status.getCurrentDevices(),
						status.getTotalDevices(),
						status.getCurrentRPDUs(),
						status.getTotalRPDUs(),
						status.getCurrentPowerConns(),
						status.getTotalPowerConns(),
						piqHost
					},
					null
			);
			
			// Log any specific failures on the job in the event details
			if (jobFailures != null) {
				for (Throwable t : jobFailures) {
					ev.addParam("Error", t.getLocalizedMessage());
				}
			}
		}
		else if (exitCode.equals( ExitStatus.STOPPED.getExitCode() )) {
			ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.INFORMATIONAL, eventSource);
			PIQBulkSyncStatusDTO status = lastStatus;
			if (status != null) {
				status = getPIQUpdateDataStatus(piqHost);
			}
			summary = eventHome.getMessageSource().getMessage("piqUpdate.stopped",
					new Object[] { 
					timeMins,
					status.getTotalProcessed(),
					status.getTotalCount(),
					status.getCurrentLocations(),
					status.getTotalLocations(),
					status.getCurrentCabinets(),
					status.getTotalCabinets(),
					status.getCurrentDevices(),
					status.getTotalDevices(),
					status.getCurrentRPDUs(),
					status.getTotalRPDUs(),
					status.getCurrentPowerConns(),
					status.getTotalPowerConns(),
					piqHost
				},
				null
			);
		}
		
		ev.setSummary( summary );
		return ev;
	}
	
	/**
	 * Add job information as event parameters.
	 * @param ev
	 * @param exitCode
	 * @param exitDescription
	 */
	private void addJobEventParams(Event ev, String exitCode, String exitDescription) {
		// Add counts
		ev.addParam("Locations", getLocationCurrentCount() + " of " + piqJobListener.getCategoryTotalCount("location"));
		ev.addParam("Cabinets", getCabinetCurrentCount() + " of " + piqJobListener.getCategoryTotalCount("cabinet"));
		ev.addParam("Devices", getDeviceCurrentCount() + " of " + piqJobListener.getCategoryTotalCount("device"));
		ev.addParam("Rack PDUs", getRPDUCurrentCount() + " of " + piqJobListener.getCategoryTotalCount("rpdu"));
		ev.addParam("Power Connections", getPowerConnCurrentCount() + " of " + piqJobListener.getCategoryTotalCount("connection") );
		
		// Add any error details
		if (StringUtils.hasText(exitCode)) {
			ev.addParam("Exit Code", exitCode);
		}
		if (StringUtils.hasText(exitDescription)) {
			ev.addParam("Exit Description", exitDescription);
		}
	}
	
	/**
	 * Clear the job start event.
	 * @param ev
	 * @param startEventId
	 * @throws DataAccessException
	 */
	private void clearJobStartEvent(Event ev, Long startEventId) throws DataAccessException {
		if (startEventId != null) {
			Event startEv = eventHome.getEventDetail( startEventId );
			if (startEv != null) {
				startEv.clear( ev );
				eventHome.saveEvent( startEv );
			}
		}
	}
	
	/**
	 * Get the total count.
	 * @param dto
	 */
	private void getTotalCount(PIQBulkSyncStatusDTO dto) {
		// We need to wait until the jobs start to get the total count from the job listener
		final int maxTries = 10;
		int tries = 0;
		
		Integer totalCount = null;
		do {
			totalCount = piqJobListener.getTotalCount();
			if (totalCount == null) {
				log.debug("Waiting for total count");
			}
			tries++;
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				// Ignore
			}
		} while (totalCount == null && tries < maxTries);
		
		if (totalCount == null) {
			totalCount = dto.getTotalLocations() + dto.getCurrentCabinets() +
				dto.getTotalDevices() + dto.getTotalRPDUs();
			log.warn("Failed to get total count from job... derived total count =  " + totalCount);
		}
		
		dto.setTotalCount( totalCount );
	}

	/**
	 * Get total number of items skipped so far.
	 * @param execution the job.
	 * @return number of items skipped
	 */
	private int getSkippedCount(JobExecution execution) {
		int skippedCount = 0;
		
		for (StepExecution step : execution.getStepExecutions()) {
			skippedCount += step.getWriteSkipCount();
		}
		
		return skippedCount;
	}
	
	private int getLocationCurrentCount() {
		Integer count = piqLocSyncListener.getCurrentCount();
		return count != null ? count : 0;
	}
	
	private int getCabinetCurrentCount() {
		Integer count = piqCabSyncListener.getCurrentCount();
		return count != null ? count : 0;
	}
	
	private int getDeviceCurrentCount() {
		Integer count = piqDevSyncListener.getCurrentCount();
		return count != null ? count : 0;
	}
	
	private int getRPDUCurrentCount() {
		Integer count = piqRPDUSyncListener.getCurrentCount();
		return count != null ? count : 0;
	}
	
	private int getPowerConnCurrentCount() {
		Integer count = piqPowerConnSyncListener.getCurrentCount();
		return count != null ? count : 0;
	}
	
	private void resetCurrentCounts() {
		piqLocSyncListener.setCurrentCount(0);
		piqCabSyncListener.setCurrentCount(0);
		piqDevSyncListener.setCurrentCount(0);
		piqRPDUSyncListener.setCurrentCount(0);
		piqPowerConnSyncListener.setCurrentCount(0);
	}
	
	private Object[] getLastPIQUpdateTime(String ipAddress) {
		Object[] ret = new Object[2];
		try {
			Event event = eventHome.getMostRecentEvent(EventType.PIQ_UPDATE, null, null, ipAddress);
			if (event != null) {
				ret[0] = event.getCreatedAt();
				if (event.getSeverity().getLkpValueCode() == SystemLookup.EventSeverity.CRITICAL) {
					ret[1] = "failed";
				}
				else {
					String summary = event.getSummary();
					String stopped = eventHome.getMessageSource().getMessage("piqUpdate.stopped.queryKey", null, null);
					if (summary != null && summary.contains(stopped)) {
						ret[1] = "stopped";
					}
					else {
						ret[1] = "completed";
					}
				}
			}
		}
		catch (DataAccessException e) {
			log.error("", e);
		}
		
		return ret;
	}

	public String getPiqHost() {
		return piqHost;
	}

	public void setPiqHost(String piqHost) {
		this.piqHost = piqHost;
	}
}
