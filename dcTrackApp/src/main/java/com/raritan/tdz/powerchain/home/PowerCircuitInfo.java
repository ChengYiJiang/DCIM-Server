package com.raritan.tdz.powerchain.home;

public class PowerCircuitInfo {

	public PowerCircuitInfo() {
	}

	public PowerCircuitInfo(String circuitTrace, String sharedCircuitTrace,
			Long endConnectionId, Long circuitId) {

		this.circuitTrace = circuitTrace;
		this.sharedCircuitTrace = sharedCircuitTrace;
		this.endConnectionId = endConnectionId;
		this.circuitId = circuitId;
	}

	private String circuitTrace;
	
	private String sharedCircuitTrace;
	
	private Long endConnectionId;
	
	private Long circuitId;

	public String getCircuitTrace() {
		return circuitTrace;
	}

	public void setCircuitTrace(String circuitTrace) {
		this.circuitTrace = circuitTrace;
	}

	public String getSharedCircuitTrace() {
		return sharedCircuitTrace;
	}

	public void setSharedCircuitTrace(String sharedCircuitTrace) {
		this.sharedCircuitTrace = sharedCircuitTrace;
	}

	public Long getEndConnectionId() {
		return endConnectionId;
	}

	public void setEndConnectionId(Long endConnectionId) {
		this.endConnectionId = endConnectionId;
	}

	public Long getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
	}
	
	
}
