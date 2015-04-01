package com.raritan.tdz.component.inspector.dto;

/**
 * Data transfer object for current readings in power feed.
 */
public class CabinetMetricPowerFeedAmpereDto {

    private int rated;
    private int budgeted;
    private int measured;
    private long poleLeg; 
    
    public int getRated() {
        return rated;
    }

    public void setRated(int rated) {
        this.rated = rated;
    }

    public int getBudgeted() {
        return budgeted;
    }

    public void setBudgeted(int budgeted) {
        this.budgeted = budgeted;
    }

    public int getMeasured() {
        return measured;
    }

    public void setMeasured(int measured) {
        this.measured = measured;
    }

	public long getPoleLeg() {
		return poleLeg;
	}

	public void setPoleLeg(long poleLeg) {
		this.poleLeg = poleLeg;
	}

	@Override
	public String toString() {
		return "CabinetMetricPowerFeedAmpereDto [rated=" + rated
				+ ", budgeted=" + budgeted + ", measured=" + measured
				+ ", poleLeg=" + poleLeg + "]";
	}

}