package com.raritan.tdz.piq.dto;

import java.util.Calendar;
import java.util.Date;

/**
 * Returns the status of the current Power IQ synchronization job to the client.
 * @author Andrew Cohen
 */
public class PIQBulkSyncStatusDTO {
	
	private boolean isJobRunning = false;
	private boolean isJobStopped = false;
	
	private Integer totalProcessed = null;
	private int totalCount;
	private int totalCabinets;
	private int currentCabinets;
	private int totalLocations;
	private int currentLocations;
	private int totalDevices;
	private int currentDevices;
	private int totalRPDUs;
	private int currentRPDUs;
	private int totalPowerConns;
	private int currentPowerConns;
	private int skippedCount;
	private boolean jobFailed = false;
	private Date lastUpdateTime;
	private String lastUpdateAction;
	private String piqHost;
	
	public boolean isJobStopped() {
		return isJobStopped;
	}

	public void setJobStopped(boolean isJobStopped) {
		this.isJobStopped = isJobStopped;
	}

	public boolean isJobRunning() {
		return isJobRunning;
	}

	public void setJobRunning(boolean isJobRunning) {
		this.isJobRunning = isJobRunning;
	}

	public int getTotalCabinets() {
		return totalCabinets;
	}

	public void setTotalCabinets(int totalCabinets) {
		this.totalCabinets = totalCabinets;
	}

	public int getCurrentCabinets() {
		return currentCabinets;
	}

	public void setCurrentCabinets(int currentCabinets) {
		this.currentCabinets = currentCabinets;
	}

	public int getTotalLocations() {
		return totalLocations;
	}

	public void setTotalLocations(int totalLocations) {
		this.totalLocations = totalLocations;
	}

	public int getCurrentLocations() {
		return currentLocations;
	}

	public void setCurrentLocations(int currentLocations) {
		this.currentLocations = currentLocations;
	}

	public int getTotalDevices() {
		return totalDevices;
	}

	public void setTotalDevices(int totalDevices) {
		this.totalDevices = totalDevices;
	}

	public int getCurrentDevices() {
		return currentDevices;
	}

	public void setCurrentDevices(int currentDevices) {
		this.currentDevices = currentDevices;
	}

	public int getTotalRPDUs() {
		return totalRPDUs;
	}

	public void setTotalRPDUs(int totalRPDUs) {
		this.totalRPDUs = totalRPDUs;
	}

	public int getCurrentRPDUs() {
		return currentRPDUs;
	}

	public void setCurrentRPDUs(int currentRPDUs) {
		this.currentRPDUs = currentRPDUs;
	}

	public boolean isJobFailed() {
		return jobFailed;
	}

	public void setJobFailed(boolean jobFailed) {
		this.jobFailed = jobFailed;
	}
	
	public int getTotalPowerConns() {
		return totalPowerConns;
	}

	public void setTotalPowerConns(int totalPowerConns) {
		this.totalPowerConns = totalPowerConns;
	}

	public int getCurrentPowerConns() {
		return currentPowerConns;
	}

	public void setCurrentPowerConns(int currentPowerConns) {
		this.currentPowerConns = currentPowerConns;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public int getSkippedCount() {
		return skippedCount;
	}

	public void setSkippedCount(int totalSkipped) {
		this.skippedCount = totalSkipped;
	}
	
	public int getTotalProcessed() {
		if (totalProcessed == null) {
			totalProcessed = getCurrentDevices() + getCurrentRPDUs() + getCurrentCabinets() +
				getCurrentLocations() + getCurrentPowerConns();
		}
		return totalProcessed;
	}
	
	public void setTotalProcessed(int totalProcessed) {
		this.totalProcessed = totalProcessed;
	}
	
	public Date getLastUpdateTime() {
		//return Calendar.getInstance().getTime();
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getLastUpdateAction() {
		return lastUpdateAction;
	}

	public void setLastUpdateAction(String lastUpdateAction) {
		this.lastUpdateAction = lastUpdateAction;
	}

	public String getPiqHost(){
		return piqHost;
	}
	
	public void setPiqHost(String piqHost){
		this.piqHost = piqHost;
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("\n, piqHost: ").append(piqHost).append("\n");
		b.append("\n PIQ Update Status:\n{ locations: ").append( currentLocations).append("/").append( totalLocations );
		b.append("\n, cabinets: ").append( currentCabinets).append("/").append( totalCabinets );
		b.append("\n, devices: ").append( currentDevices ).append("/").append( totalDevices );
		b.append("\n, RPDUs: ").append( currentRPDUs ).append("/").append( totalRPDUs );
		b.append("\n, Power Connections: ").append( currentPowerConns ).append("/").append( totalPowerConns );		
		b.append("\n, Total Skipped Records: ").append( skippedCount );
		b.append(" }\n");
		return b.toString();
	}
}
