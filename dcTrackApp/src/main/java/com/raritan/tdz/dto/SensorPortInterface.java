package com.raritan.tdz.dto;

public interface SensorPortInterface extends PortInterface {
	//Type is determined with port subclass
	
	public abstract Boolean isInternal();
	public abstract void setIsInternal(Boolean internal);
	
	public abstract void setCabinetId(Long cbainetId);
	public abstract Long getCabinetId();
	
	public abstract void setCabinetName(String cabinetName);
	public abstract String getCabinetName();
	
	public abstract void setCabLocLksValueCode(Long locationLksValueCode);
	public abstract Long getCabLocLksValueCode();
	
	public abstract void setCabLocLksDesc(String locationLksValue);
	public abstract String getCabLocLksDesc();
	
	public abstract void setXyzLocation( String xyz);
	public abstract String getXyzLocation();
	
	public abstract void setReadingValue( double reading );
	public abstract double getReadingValue();
	
	public abstract void setReadingUnit( String readingUnits);
	public abstract String getReadingUnit();

}
