package com.raritan.tdz.views;
import com.raritan.tdz.dto.PortConnectorDTO;

public class PowerPortObject {
	private Long itemId;
	private Long portId;
	private boolean used;
	private int sortOrder;	
	private String portName;
	private String itemName;
	private String connectorName;
	private String phaseLegsLksDesc;
	private String phaseLksDesc;
	private String polesLksDesc;
	private String voltsLksDesc;
	private String cableGradeLkuDesc;
	private String colorLkuDesc;
	private String colorNumber;
	private String fuseLkuDesc;
	private boolean isRedundant;
	private double ampsActual;
	private double ampsActualA;
	private double ampsActualB;
	private double ampsActualC;
	private double ampsBudget;
	private double ampsNameplate;
	private double powerFactor;
	private double powerFactorActual;
	private double wattsActual;
	private int wattsBudget;
	private int wattsNameplate;
	private double ampsMax;
	private double ampsRated;
	private double breakerAmpsMax;
	private double breakerAmpsRated;
	private String breakerName;
	private Long portSubClassLksValueCode;
	private Long itemClassLksValueCode;
	private PortConnectorDTO connector;
	private int placementX;
	private int placementY;
	private Long faceValueCode;
	private long freeWatts;
	private long usedWatts;
	private Long voltsLksValueCode;
	private long totalWatts;
	private long breakerUsedWatts;
	private long breakerTotalWatts;
	
	public PowerPortObject()
	{
		freeWatts = 0;
		totalWatts = 0;
		usedWatts = 0;
	}
	
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public Long getPortId() {
		return portId;
	}
	public void setPortId(Long portId) {
		this.portId = portId;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getConnectorName() {
		return connectorName;
	}
	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}
	public String getPhaseLegsLksDesc() {
		return phaseLegsLksDesc;
	}
	public void setPhaseLegsLksDesc(String phaseLegsLksDesc) {
		this.phaseLegsLksDesc = phaseLegsLksDesc;
	}
	public String getPhaseLksDesc() {
		return phaseLksDesc;
	}
	public void setPhaseLksDesc(String phaseLksDesc) {
		this.phaseLksDesc = phaseLksDesc;
	}
	public String getPolesLksDesc() {
		return polesLksDesc;
	}
	public void setPolesLksDesc(String polesLksDesc) {
		this.polesLksDesc = polesLksDesc;
	}
	public String getVoltsLksDesc() {
		return voltsLksDesc;
	}
	public void setVoltsLksDesc(String voltsLksDesc) {
		this.voltsLksDesc = voltsLksDesc;
	}
	public String getCableGradeLkuDesc() {
		return cableGradeLkuDesc;
	}
	public void setCableGradeLkuDesc(String cableGradeLkuDesc) {
		this.cableGradeLkuDesc = cableGradeLkuDesc;
	}
	public String getColorLkuDesc() {
		return colorLkuDesc;
	}
	public void setColorLkuDesc(String colorLkuDesc) {
		this.colorLkuDesc = colorLkuDesc;
	}
	public String getColorNumber() {
		return colorNumber;
	}
	public void setColorNumber(String colorNumber) {
		this.colorNumber = colorNumber;
	}
	public String getFuseLkuDesc() {
		return fuseLkuDesc;
	}
	public void setFuseLkuDesc(String fuseLkuDesc) {
		this.fuseLkuDesc = fuseLkuDesc;
	}
	public boolean isRedundant() {
		return isRedundant;
	}
	public void setRedundant(boolean isRedundant) {
		this.isRedundant = isRedundant;
	}
	public double getAmpsActual() {
		return ampsActual;
	}
	public void setAmpsActual(double ampsActual) {
		this.ampsActual = ampsActual;
	}
	public double getAmpsActualA() {
		return ampsActualA;
	}
	public void setAmpsActualA(double ampsActualA) {
		this.ampsActualA = ampsActualA;
	}
	public double getAmpsActualB() {
		return ampsActualB;
	}
	public void setAmpsActualB(double ampsActualB) {
		this.ampsActualB = ampsActualB;
	}
	public double getAmpsActualC() {
		return ampsActualC;
	}
	public void setAmpsActualC(double ampsActualC) {
		this.ampsActualC = ampsActualC;
	}
	public double getAmpsBudget() {
		return ampsBudget;
	}
	public void setAmpsBudget(double ampsBudget) {
		this.ampsBudget = ampsBudget;
	}
	public double getAmpsNameplate() {
		return ampsNameplate;
	}
	public void setAmpsNameplate(double ampsNameplate) {
		this.ampsNameplate = ampsNameplate;
	}
	public double getPowerFactor() {
		return powerFactor;
	}
	public void setPowerFactor(double powerFactor) {
		this.powerFactor = powerFactor;
	}
	public double getPowerFactorActual() {
		return powerFactorActual;
	}
	public void setPowerFactorActual(double powerFactorActual) {
		this.powerFactorActual = powerFactorActual;
	}
	public double getWattsActual() {
		return wattsActual;
	}
	public void setWattsActual(double wattsActual) {
		this.wattsActual = wattsActual;
	}
	public int getWattsBudget() {
		return wattsBudget;
	}
	public void setWattsBudget(int wattsBudget) {
		this.wattsBudget = wattsBudget;
	}
	public int getWattsNameplate() {
		return wattsNameplate;
	}
	public void setWattsNameplate(int wattsNameplate) {
		this.wattsNameplate = wattsNameplate;
	}

	public double getAmpsMax() {
		return ampsMax;
	}

	public void setAmpsMax(double ampsMax) {
		this.ampsMax = ampsMax;
	}

	public double getAmpsRated() {
		return ampsRated;
	}

	public void setAmpsRated(double ampsRated) {
		this.ampsRated = ampsRated;
	}

	public double getBreakerAmpsMax() {
		return breakerAmpsMax;
	}

	public void setBreakerAmpsMax(double breakerAmpsMax) {
		this.breakerAmpsMax = breakerAmpsMax;
	}

	public double getBreakerAmpsRated() {
		return breakerAmpsRated;
	}

	public void setBreakerAmpsRated(double breakerAmpsRated) {
		this.breakerAmpsRated = breakerAmpsRated;
	}

	public String getBreakerName() {
		return breakerName;
	}

	public void setBreakerName(String breakerName) {
		this.breakerName = breakerName;
	}	
	
	public Long getPortSubClassLksValueCode() {
		return portSubClassLksValueCode;
	}
	public void setPortSubClassLksValueCode(Long portSubClassLksValueCode) {
		this.portSubClassLksValueCode = portSubClassLksValueCode;
	}

	public Long getItemClassLksValueCode() {
		return itemClassLksValueCode;
	}

	public void setItemClassLksValueCode(Long itemClassLksValueCode) {
		this.itemClassLksValueCode = itemClassLksValueCode;
	}

	public PortConnectorDTO getConnector() {
		return connector;
	}
	public void setConnector(PortConnectorDTO connector) {
		this.connector = connector;
	}	
	
	public int getPlacementX() {
		return placementX;
	}
	public void setPlacementX(int placementX) {
		this.placementX = placementX;
	}
	public int getPlacementY() {
		return placementY;
	}
	public void setPlacementY(int placementY) {
		this.placementY = placementY;
	}
	public Long getFaceValueCode() {
		return faceValueCode;
	}
	public void setFaceValueCode(Long faceValueCode) {
		this.faceValueCode = faceValueCode;
	}

	public long getFreeWatts() {
		return freeWatts;
	}

	public void setFreeWatts(long freeWatts) {
		this.freeWatts = freeWatts;
	}

	public long getUsedWatts() {
		return usedWatts;
	}

	public void setUsedWatts(long usedWatts) {
		this.usedWatts = usedWatts;
	}

	public Long getVoltsLksValueCode() {
		return voltsLksValueCode;
	}

	public void setVoltsLksValueCode(Long voltsLksValueCode) {
		this.voltsLksValueCode = voltsLksValueCode;
	}

	public long getTotalWatts() {
		return totalWatts;
	}

	public void setTotalWatts(long totalWatts) {
		this.totalWatts = totalWatts;
	}

	public long getBreakerUsedWatts() {
		return breakerUsedWatts;
	}

	public void setBreakerUsedWatts(long breakerUsedWatts) {
		this.breakerUsedWatts = breakerUsedWatts;
	}

	public long getBreakerTotalWatts() {
		return breakerTotalWatts;
	}

	public void setBreakerTotalWatts(long breakerTotalWatts) {
		this.breakerTotalWatts = breakerTotalWatts;
	}
	
}
