package com.raritan.tdz.dto;

import com.raritan.tdz.lookup.SystemLookup;

public class PowerPortDTO extends PortDTOBase implements PowerPortInterface {
	private String phaseLegsLksDesc;
	private Long phaseLegsLksValueCode;
	private String phaseLksDesc;
	private Long phaseLksValueCode;
	private String polesLksDesc;
	private Long polesLksValueCode;
	private String voltsLksDesc;
	private Long voltsLksValueCode;
	private String fuseLkuDesc;
	private Long fuseLkuId;
	private double ampsActual = -1.0;
	private double ampsActualA = -1.0;
	private double ampsActualB = -1.0;
	private double ampsActualC = -1.0;
	private double ampsBudget;
	private double ampsNameplate;
	private double powerFactor;
	private double powerFactorActual = 1.0;
	private double wattsActual = -1.0;
	private long wattsBudget;
	private long wattsNameplate;
	private double ampsMax;
	private double ampsRated;
	private double breakerAmpsMax;
	private double breakerAmpsRated;
	private String breakerName;
	private Long inputCordPortId;
	private PowerPortInterface breakerPort;
	private long freeWatts;
	private long usedWatts;
	private long totalWatts;  //this is not freeWatts + usedWatts
	private long breakerUsedWatts;
	private long breakerTotalWatts;
	private Long buswayItemId;
	private String polePhase;
	private Long piqId;
	private Long modelPowerPortId;
	private Long inputCordModelPowerPortId;
	private double ampsActualNextNode = -1.0;
	private String psRedundancy;
	
	public Long getInputCordModelPowerPortId() {
		return inputCordModelPowerPortId;
	}

	public void setInputCordModelPowerPortId(Long inputCordModelPowerPortId) {
		this.inputCordModelPowerPortId = inputCordModelPowerPortId;
	}

	public Long getPiqId() {
		return piqId;
	}

	public void setPiqId(Long piqId) {
		this.piqId = piqId;
	}

	public String getPolePhase() {
		return polePhase;
	}

	public void setPolePhase(String polePhase) {
		this.polePhase = polePhase;
	}

	public PowerPortDTO(){
		freeWatts = 0;
	}
	
	@Override
	public String getPhaseLegsLksDesc() {
		return phaseLegsLksDesc;
	}
	@Override
	public void setPhaseLegsLksDesc(String phaseLegsLksDesc) {
		this.phaseLegsLksDesc = phaseLegsLksDesc;
	}
	@Override
	public String getPhaseLksDesc() {
		return phaseLksDesc;
	}
	@Override
	public void setPhaseLksDesc(String phaseLksDesc) {
		this.phaseLksDesc = phaseLksDesc;
	}
	@Override
	public String getPolesLksDesc() {
		return polesLksDesc;
	}
	@Override
	public void setPolesLksDesc(String polesLksDesc) {
		this.polesLksDesc = polesLksDesc;
	}
	@Override
	public String getVoltsLksDesc() {
		return voltsLksDesc;
	}
	@Override
	public void setVoltsLksDesc(String voltsLksDesc) {
		this.voltsLksDesc = voltsLksDesc;
	}
	@Override
	public String getFuseLkuDesc() {
		return fuseLkuDesc;
	}
	@Override
	public void setFuseLkuDesc(String fuseLkuDesc) {
		this.fuseLkuDesc = fuseLkuDesc;
	}
	@Override
	public double getAmpsActual() {
		return ampsActual;
	}
	@Override
	public void setAmpsActual(double ampsActual) {
		this.ampsActual = ampsActual;
	}
	@Override
	public double getAmpsActualA() {
		return ampsActualA;
	}
	@Override
	public void setAmpsActualA(double ampsActualA) {
		this.ampsActualA = ampsActualA;
	}
	@Override
	public double getAmpsActualB() {
		return ampsActualB;
	}
	@Override
	public void setAmpsActualB(double ampsActualB) {
		this.ampsActualB = ampsActualB;
	}
	@Override
	public double getAmpsActualC() {
		return ampsActualC;
	}
	@Override
	public void setAmpsActualC(double ampsActualC) {
		this.ampsActualC = ampsActualC;
	}
	@Override
	public double getAmpsBudget() {
		return ampsBudget;
	}
	@Override
	public void setAmpsBudget(double ampsBudget) {
		this.ampsBudget = ampsBudget;
	}
	@Override
	public double getAmpsNameplate() {
		return ampsNameplate;
	}
	@Override
	public void setAmpsNameplate(double ampsNameplate) {
		this.ampsNameplate = ampsNameplate;
	}
	@Override
	public double getPowerFactor() {
		return powerFactor;
	}
	@Override
	public void setPowerFactor(double powerFactor) {
		this.powerFactor = powerFactor;
	}
	@Override
	public double getPowerFactorActual() {
		return powerFactorActual;
	}
	@Override
	public void setPowerFactorActual(double powerFactorActual) {
		this.powerFactorActual = powerFactorActual;
	}
	@Override
	public double getWattsActual() {
		return wattsActual;
	}
	@Override
	public void setWattsActual(double wattsActual) {
		this.wattsActual = wattsActual;
	}
	@Override
	public long getWattsBudget() {
		return wattsBudget;
	}
	@Override
	public void setWattsBudget(long wattsBudget) {
		this.wattsBudget = wattsBudget;
	}
	@Override
	public long getWattsNameplate() {
		return wattsNameplate;
	}
	@Override
	public void setWattsNameplate(long wattsNameplate) {
		this.wattsNameplate = wattsNameplate;
	}
	@Override
	public double getAmpsMax() {
		return ampsMax;
	}
	@Override
	public void setAmpsMax(double ampsMax) {
		this.ampsMax = ampsMax;
	}
	@Override
	public double getAmpsRated() {
		return ampsRated;
	}
	@Override
	public void setAmpsRated(double ampsRated) {
		this.ampsRated = ampsRated;
	}
	@Override
	public double getBreakerAmpsMax() {
		return breakerAmpsMax;
	}
	@Override
	public void setBreakerAmpsMax(double breakerAmpsMax) {
		this.breakerAmpsMax = breakerAmpsMax;
	}
	@Override
	public double getBreakerAmpsRated() {
		return breakerAmpsRated;
	}
	@Override
	public void setBreakerAmpsRated(double breakerAmpsRated) {
		this.breakerAmpsRated = breakerAmpsRated;
	}
	@Override
	public String getBreakerName() {
		return breakerName;
	}
	@Override
	public void setBreakerName(String breakerName) {
		this.breakerName = breakerName;
	}
	@Override
	public Long getPhaseLegsLksValueCode() {
		return phaseLegsLksValueCode;
	}
	@Override
	public void setPhaseLegsLksValueCode(Long phaseLegsLksValueCode) {
		this.phaseLegsLksValueCode = phaseLegsLksValueCode;
	}
	@Override
	public Long getPhaseLksValueCode() {
		return phaseLksValueCode;
	}
	@Override
	public void setPhaseLksValueCode(Long phaseLksValueCode) {
		this.phaseLksValueCode = phaseLksValueCode;
	}
	@Override
	public Long getPolesLksValueCode() {
		return polesLksValueCode;
	}
	@Override
	public void setPolesLksValueCode(Long polesLksValueCode) {
		this.polesLksValueCode = polesLksValueCode;
	}
	@Override
	public Long getVoltsLksValueCode() {
		return voltsLksValueCode;
	}
	@Override
	public void setVoltsLksValueCode(Long voltsLksValueCode) {
		this.voltsLksValueCode = voltsLksValueCode;
	}
	@Override
	public Long getFuseLkuId() {
		return fuseLkuId;
	}
	@Override
	public void setFuseLkuId(Long fuseLkuId) {
		this.fuseLkuId = fuseLkuId;
	}
	@Override
	public Long getInputCordPortId() {
		return inputCordPortId;
	}
	@Override
	public void setInputCordPortId(Long inputCordPortId) {
		this.inputCordPortId = inputCordPortId;
	}
	@Override
	public PowerPortInterface getBreakerPort() {
		return breakerPort;
	}
	@Override
	public void setBreakerPort(PowerPortInterface breakerPort) {
		this.breakerPort = breakerPort;
	}
	
	public boolean isRackPduOutPut(){
		
		if(getPortSubClassLksValueCode() != null && getPortSubClassLksValueCode() == SystemLookup.PortSubClass.RACK_PDU_OUTPUT){
			return true;
		}
		return false;
	}
	
	public boolean isInputCord(){
		if(getPortSubClassLksValueCode() != null && getPortSubClassLksValueCode() == SystemLookup.PortSubClass.INPUT_CORD){
			return true;
		}
		return false;
	}

	public boolean isOutLet(){
		if(getPortSubClassLksValueCode() != null){
			long portSubClass = getPortSubClassLksValueCode();
			
			if(portSubClass == SystemLookup.PortSubClass.WHIP_OUTLET ||
			   portSubClass == SystemLookup.PortSubClass.BUSWAY_OUTLET){
				return true;
			}
		}
		return false;
	}

	public boolean isBreaker(){	
		if(getPortSubClassLksValueCode() != null){
			long valueCode = getPortSubClassLksValueCode().longValue();
		
			if(valueCode == SystemLookup.PortSubClass.PANEL_BREAKER || 
					valueCode == SystemLookup.PortSubClass.PDU_INPUT_BREAKER ||
					valueCode == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER || 
					valueCode == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER ){
				return true;
			}
		}
		
		return false;
	}

	public boolean isPowerSupply(){
		if(getPortSubClassLksValueCode() != null && getPortSubClassLksValueCode() == SystemLookup.PortSubClass.POWER_SUPPLY){
			return true;
		}
		return false;
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

	public Long getBuswayItemId() {
		return buswayItemId;
	}

	public void setBuswayItemId(Long buswayItemId) {
		this.buswayItemId = buswayItemId;
	}

	public Long getModelPowerPortId() {
		return modelPowerPortId;
	}

	public void setModelPowerPortId(Long modelPowerPortId) {
		this.modelPowerPortId = modelPowerPortId;
	}

	public double getAmpsActualNextNode() {
		return ampsActualNextNode;
	}

	public void setAmpsActualNextNode(double ampsActualNextNode) {
		this.ampsActualNextNode = ampsActualNextNode;
	}

	/**
	 * @return the psRedundancy
	 */
	public String getPsRedundancy() {
		return psRedundancy;
	}

	/**
	 * @param psRedundancy the psRedundancy to set
	 */
	public void setPsRedundancy(String psRedundancy) {
		this.psRedundancy = psRedundancy;
	}
	
}
