package com.raritan.tdz.circuit.util;

public class Power3Phase{
    public double vaMaxA;
    public double vaMaxB;
    public double vaMaxC;
    public double vaRatedA;
    public double vaRatedB;
    public double vaRatedC;
    public double wMaxA;
    public double wMaxB;
    public double wMaxC;
    public double wRatedA;
    public double wRatedB;
    public double wRatedC;
    public double currentMaxA;
    public double currentMaxB;
    public double currentMaxC;
    public double currentMaxN;
    public double currentRatedA;
    public double currentRatedB;
    public double currentRatedC;
    public double currentRatedN;
    public double currentRating;
    public double powerRating;
    public double volts;
    public String phaseType;
    public String name;
    public double windingsRatio;
    public double presentAmpsLoad;
    public Long reccount = new Long(0);

	public void reset() {
		this.vaMaxA = 0;
		this.vaMaxB = 0;
		this.vaMaxC = 0;
		this.vaRatedA = 0;
		this.vaRatedB = 0;
		this.vaRatedC = 0;
		this.wMaxA = 0;
		this.wMaxB = 0;
		this.wMaxC = 0;
		this.wRatedA = 0;
		this.wRatedB = 0;
		this.wRatedC = 0;
	}  
}
