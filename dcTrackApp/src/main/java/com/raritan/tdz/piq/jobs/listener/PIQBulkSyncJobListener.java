/**
 * 
 */
package com.raritan.tdz.piq.jobs.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

import com.raritan.tdz.piq.home.PIQSystemEventLogger;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
public class PIQBulkSyncJobListener {
	// Logger for all PIQ sync operations
	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	private LNHome lnHome;
	private SessionFactory sessionFactory;
	private int totalCount = 0;
	
	private Map<String, String> totalCountQueries;
	private Map<String, Integer> totalCounts;
	
	private PIQSystemEventLogger piqSysEventLogger;
	
	private String piqHost;
	
	private boolean recomputeTotalCount = false;
	
	public PIQBulkSyncJobListener(LNHome lnHome, SessionFactory sessionFactory, String piqHost){
		this.lnHome = lnHome;
		this.sessionFactory = sessionFactory;
		this.totalCounts = new HashMap<String, Integer>();
		this.piqHost = piqHost;
	}
	
	public void setTotalCountQueries(Map<String, String> totalCountQueries) {
		this.totalCountQueries = totalCountQueries;
	}
	
	public void setPiqSysEventLogger(PIQSystemEventLogger piqSysEventLogger) {
		this.piqSysEventLogger = piqSysEventLogger;
	}

	@BeforeJob
	public void beforeJob(JobExecution jobExecution){
		totalCount = 0;
		recomputeTotalCount = false;
		piqSysEventLogger.enableDuplicateTracking();
		lnHome.setSuspend(true);
		
		for(String key : totalCountQueries.keySet()) {
			int count = getTotalCount( totalCountQueries.get(key) );
			totalCounts.put(key, count);
			totalCount += count;
		}
		
		log.debug("Total number of records to be processed: " + totalCount);
	}

	@AfterJob
	public void afterJob(JobExecution jobExecution){
		piqSysEventLogger.clearDuplicateTracking();
		lnHome.setSuspend(false);
	}
	
	/**
	 * @return the totalCount
	 */
	public final Integer getTotalCount() {
		if (recomputeTotalCount) {
			totalCount = recomputeTotalCount();
			recomputeTotalCount = false;
		}
		return totalCount;
	}
	
	/**
	 * Returns the total count for a given category.
	 * @param category the category (example" "location", "device", etc.)
	 * @return
	 */
	public final int getCategoryTotalCount(String category) {
		Integer count = totalCounts.get( category );
		return count != null ? count : 0;
	}
	
	final void setCategoryTotalCount(String category, int totalCount) {
		totalCounts.put(category, totalCount);
		recomputeTotalCount = true;
	}
	
	private int getTotalCount(String queryString){
		int totalCount = 0;
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			Query query = session.createQuery("select count(*) " + queryString);
			query.setParameter("piqHost", piqHost);
			totalCount = ((Long)query.uniqueResult()).intValue();
			session.close();
		}
		
		return totalCount;
	}
	
	private int recomputeTotalCount() {
		int totalCount = 0;
		for (String key : totalCounts.keySet()) {
			totalCount += totalCounts.get(key);
		}
		return totalCount;
	}
}
