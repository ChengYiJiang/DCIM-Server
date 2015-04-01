package com.raritan.tdz.circuit.dto;

public class PowerWattUsedSummary {
	String legs;
    double currentRated;
    double currentMax;
    double vaRated;
    double vaMax;
    double wattMax;
    double wattRated;
    Double pbVolts;
    Double pbPhaseVolts;
    double currentMaxA;
    double currentMaxB;
    double currentMaxC;
    boolean isMeasured;
    Long nodePortIdToExclude;
    
    public PowerWattUsedSummary(){
    	
    }
    
	public String getLegs() {
		return legs;
	}
	public void setLegs(String legs) {
		this.legs = legs;
	}
	public double getCurrentRated() {
		return currentRated;
	}
	
	public void setCurrentRated(double currentRated) {
		this.currentRated = currentRated;
	}
	
	public double getCurrentMax() {
		return currentMax;
	}
	public void setCurrentMax(double currentMax) {
		this.currentMax = currentMax;
	}
	public double getVaRated() {
		return vaRated;
	}
	public void setVaRated(double vaRated) {
		this.vaRated = vaRated;
	}
	public double getVaMax() {
		return vaMax;
	}
	public void setVaMax(double vaMax) {
		this.vaMax = vaMax;
	}
	public double getWattMax() {
		return wattMax;
	}
	public void setWattMax(double wattMax) {
		this.wattMax = wattMax;
	}
	public double getWattRated() {
		return wattRated;
	}
	public void setWattRated(double wattRated) {
		this.wattRated = wattRated;
	}
	public double getPbVolts() {
		if(pbVolts == null){
			return 0.0;
		}
		return pbVolts.doubleValue();
	}
	public void setPbVolts(Double pbVolts) {
		if(pbVolts == null){
			this.pbVolts = 0.0;
		}
		else{
			this.pbVolts = pbVolts;
		}
	}
	public double getPbPhaseVolts() {
		if(pbPhaseVolts == null){
			return 0.0;
		}
		return pbPhaseVolts.doubleValue();
	}
	public void setPbPhaseVolts(Double pbPhaseVolts) {
		if(pbPhaseVolts == null){
			this.pbPhaseVolts = 0.0;
		}
		else{
			this.pbPhaseVolts = pbPhaseVolts;
		}
	}

	public double getCurrentMaxA() {
		return currentMaxA;
	}

	public void setCurrentMaxA(double currentMaxA) {
		this.currentMaxA = currentMaxA;
	}

	public double getCurrentMaxB() {
		return currentMaxB;
	}

	public void setCurrentMaxB(double currentMaxB) {
		this.currentMaxB = currentMaxB;
	}

	public double getCurrentMaxC() {
		return currentMaxC;
	}

	public void setCurrentMaxC(double currentMaxC) {
		this.currentMaxC = currentMaxC;
	}

	
	public boolean isMeasured() {
		return isMeasured;
	}

	public void setMeasured(boolean isMeasured) {
		this.isMeasured = isMeasured;
	}

	public Long getNodePortIdToExclude() {
		return nodePortIdToExclude;
	}

	public void setNodePortIdToExclude(Long nodePortIdToExclude) {
		this.nodePortIdToExclude = nodePortIdToExclude;
	}

	@Override
	public String toString() {
		return "PowerWattUsedSummary [legs=" + legs + ", currentRated="
				+ currentRated + ", currentMax=" + currentMax + ", vaRated="
				+ vaRated + ", vaMax=" + vaMax + ", wattMax=" + wattMax
				+ ", wattRated=" + wattRated + ", pbVolts=" + pbVolts
				+ ", pbPhaseVolts=" + pbPhaseVolts + ", currentMaxA="
				+ currentMaxA + ", currentMaxB=" + currentMaxB
				+ ", currentMaxC=" + currentMaxC + "]";
	}


	
}
