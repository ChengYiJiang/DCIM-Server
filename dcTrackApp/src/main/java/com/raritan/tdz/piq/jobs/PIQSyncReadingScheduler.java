/**
 * 
 */
package com.raritan.tdz.piq.jobs;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.raritan.tdz.piq.home.PIQSyncReading;

/**
 * @author basker
 *
 */
public class PIQSyncReadingScheduler {
	/**
	 * List of objects with piqId for which you need to sync readings
	 */
	List<PIQSyncReading> syncReadingList; 

	/**
	 * Default constructor 
	 */
	public PIQSyncReadingScheduler() {
 
	}

	public List<PIQSyncReading> getSyncReadingList() {
		return syncReadingList;
	}

	public void setSyncReadingList(List<PIQSyncReading> syncReadingList) {
		this.syncReadingList = syncReadingList;
	}

	/**
	 * Scheculed job which wakes up every minute to sync the 
	 * latest reading.
	 */
	/* uncomment the line below to schedule sync reading */
	//@Scheduled(fixedDelay=60*1000) /* every minute */
	void SyncReading() {

		for (PIQSyncReading piqSyncReading: syncReadingList) {
			piqSyncReading.SyncReading();
		}
	}
}
