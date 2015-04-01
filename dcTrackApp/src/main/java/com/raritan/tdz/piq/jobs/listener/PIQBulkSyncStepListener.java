/**
 * 
 */
package com.raritan.tdz.piq.jobs.listener;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * @author prasanna
 *
 */
public class PIQBulkSyncStepListener implements StepExecutionListener, ChunkListener{
	
	// Logger for all PIQ sync operations
	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	private Integer totalCount;
	private Integer currentCount;
	private Integer skipCount;
	
	protected SessionFactory sessionFactory;
	protected String queryString;
	protected String piqHost;
	
	StepExecution stepExecution;
	
	PIQBulkSyncStepListener(SessionFactory sessionFactory, String queryString, String piqHost){
		this.sessionFactory = sessionFactory;
		this.queryString = queryString;
		this.piqHost = piqHost;
	}
	
	/**
	 * @return the totalCount
	 */
	public final Integer getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount the totalCount to set
	 */
	public final void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return the currentCount
	 */
	public final Integer getCurrentCount() {
		return currentCount;
	}

	/**
	 * @param currentCount the currentCount to set
	 */
	public final void setCurrentCount(Integer currentCount) {
		this.currentCount = currentCount;
	}

	/**
	 * @return the stepExecution
	 */
	public final StepExecution getStepExecution() {
		return stepExecution;
	}

	/**
	 * @param stepExecution the stepExecution to set
	 */
	public final void setStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	@Override
	public void beforeStep(StepExecution stepExecution){
		currentCount = 0;
		
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			Query query = session.createQuery("select count(*) " + queryString);
			query.setParameter("piqHost", piqHost);
			totalCount = ((Long)query.uniqueResult()).intValue();
			session.close();
		}
		
		this.stepExecution = stepExecution;
	}
	
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution){
		log.debug("PIQLocationBulkSyncStepListner afterStep: CurrentCount = " + currentCount);
		//This is to just to make sure that we no longer have a ref to the original stepExecution and
		//garbage collection can destroy it.
		this.stepExecution = null;
		return null;
	}


	@Override
	public void afterChunk(ChunkContext arg0) {
		// TODO Auto-generated method stub
		if (stepExecution != null){
			
			currentCount = stepExecution.getReadCount();
			skipCount = stepExecution.getSkipCount();
			log.info(stepExecution.getStepName() + ": " + currentCount + " of " + totalCount + " skipped: " + skipCount);
		}

		
	}

	@Override
	public void afterChunkError(ChunkContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeChunk(ChunkContext arg0) {
		// TODO Auto-generated method stub
		
	}

}
