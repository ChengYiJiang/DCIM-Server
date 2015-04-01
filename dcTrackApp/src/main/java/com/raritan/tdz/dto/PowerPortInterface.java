package com.raritan.tdz.dto;

public interface PowerPortInterface extends PortInterface {

	public abstract String getPhaseLegsLksDesc();
	public abstract void setPhaseLegsLksDesc(String phaseLegsLksDesc);
	
	public abstract Long getPhaseLegsLksValueCode();
	public abstract void setPhaseLegsLksValueCode(Long phaseLegsLksValueCode);

	public abstract String getPhaseLksDesc();
	public abstract void setPhaseLksDesc(String phaseLksDesc);
	
	public abstract Long getPhaseLksValueCode();
	public abstract void setPhaseLksValueCode(Long phaseLksValueCode);

	public abstract String getPolesLksDesc();
	public abstract void setPolesLksDesc(String polesLksDesc);
	
	public abstract Long getPolesLksValueCode();
	public abstract void setPolesLksValueCode(Long polesLksValueCode);

	public abstract String getVoltsLksDesc();
	public abstract void setVoltsLksDesc(String voltsLksDesc);
	
	public abstract Long getVoltsLksValueCode();
	public abstract void setVoltsLksValueCode(Long voltsLksValueCode);

	public abstract String getFuseLkuDesc();
	public abstract void setFuseLkuDesc(String fuseLkuDesc);

	public abstract double getAmpsActual();
	public abstract void setAmpsActual(double ampsActual);

	public abstract double getAmpsActualA();
	public abstract void setAmpsActualA(double ampsActualA);

	public abstract double getAmpsActualB();
	public abstract void setAmpsActualB(double ampsActualB);

	public abstract double getAmpsActualC();
	public abstract void setAmpsActualC(double ampsActualC);

	public abstract double getAmpsBudget();
	public abstract void setAmpsBudget(double ampsBudget);

	public abstract double getAmpsNameplate();
	public abstract void setAmpsNameplate(double ampsNameplate);

	public abstract double getPowerFactor();
	public abstract void setPowerFactor(double powerFactor);

	public abstract double getPowerFactorActual();
	public abstract void setPowerFactorActual(double powerFactorActual);

	public abstract double getWattsActual();
	public abstract void setWattsActual(double wattsActual);

	public abstract long getWattsBudget();
	public abstract void setWattsBudget(long wattsBudget);

	public abstract long getWattsNameplate();
	public abstract void setWattsNameplate(long wattsNameplate);

	public abstract double getAmpsMax();
	public abstract void setAmpsMax(double ampsMax);

	public abstract double getAmpsRated();
	public abstract void setAmpsRated(double ampsRated);

	public abstract double getBreakerAmpsMax();
	public abstract void setBreakerAmpsMax(double breakerAmpsMax);

	public abstract double getBreakerAmpsRated();
	public abstract void setBreakerAmpsRated(double breakerAmpsRated);

	public abstract String getBreakerName();
	public abstract void setBreakerName(String breakerName);
	
	public abstract Long getFuseLkuId();
	public abstract void setFuseLkuId(Long fuseLkuId);
	
	public abstract Long getInputCordPortId();
	public abstract void setInputCordPortId(Long inputCordId);
	
	public abstract PowerPortInterface getBreakerPort();
	public abstract void setBreakerPort(PowerPortInterface breakerPort);
	
	public abstract Long getBuswayItemId();
	public abstract void setBuswayItemId(Long buswayItemId);
	
	public abstract String getPolePhase();
	public abstract void setPolePhase(String polePhase);
	
	public abstract Long getPiqId();
	public abstract void setPiqId(Long piqId);
}
