package com.raritan.tdz.dto;

public class SensorPortDTO extends PortDTOBase implements SensorPortInterface {
	Boolean isInternal;
	Long cabinetId;
	String cabinetName;
	Long cabLocLksValueCode; // location in cabinet (lkpValueCode)
	String cabLocLksDesc; // location in cabinet (lkpValue)
	String xyzLocation;
	double readingValue;
	String readingUnit;
	String statusValue;

	@Override
	public Boolean isInternal() {
		return isInternal;
	}

	@Override
	public void setIsInternal(Boolean internal) {
		this.isInternal = internal;
	}
	
	@Override
	public Long getCabinetId() {
		return cabinetId;
	}
	
	@Override
	public void setCabinetId(Long cabinetId) {
		this.cabinetId = cabinetId;
	}

	@Override
	public String getCabinetName() {
		return cabinetName;
	}

	@Override
	public void setCabinetName(String cabinetName) {
		this.cabinetName = cabinetName;
	}

	@Override
	public Long getCabLocLksValueCode() {
		return cabLocLksValueCode;
	}
	
	@Override
	public void setCabLocLksValueCode(Long cabinetLocLksValueCode) {
		this.cabLocLksValueCode = cabinetLocLksValueCode;
	}
	
	@Override
	public String getCabLocLksDesc() {
		return cabLocLksDesc;
	}
	
	@Override
	public void setCabLocLksDesc(String cabLocLksValue) {
		this.cabLocLksDesc = cabLocLksValue;
	}

	@Override
	public String getXyzLocation() {
		return xyzLocation;
	}
	@Override
	public void setXyzLocation(String xyz) {
		this.xyzLocation = xyz;
	}

	@Override
	public double getReadingValue() {
		return readingValue;
	}
	
	@Override
	public void setReadingValue(double reading) {
		this.readingValue = reading;
	}

	@Override
	public String getReadingUnit() {
		return readingUnit;
	}

	@Override
	public void setReadingUnit(String readingUnit) {
		this.readingUnit= readingUnit;  
	}

	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

}
