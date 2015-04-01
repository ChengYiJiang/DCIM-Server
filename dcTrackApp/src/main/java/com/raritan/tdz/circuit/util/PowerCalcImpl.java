package com.raritan.tdz.circuit.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * 
 * @author Santo Rosario
 *
 */
public class PowerCalcImpl implements PowerCalc {
	@Autowired(required=true)
	protected PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	protected ItemDAO itemDAO;
	
	@Override
	public Power3Phase get3PhaseNodeSum(PowerPort port){
		return get3PhaseNodeSum(port, 0, null, null, null, false);
	}


	@Override
	public Power3Phase get3PhaseNodeSumMeasured(PowerPort port){
		return get3PhaseNodeSum(port, 0, null, null, null, true);
	}
	
	@Override
	public Power3Phase get3PhaseNodeSum(PowerPort port, double ampsToBeAdded){
		return get3PhaseNodeSum(port, ampsToBeAdded, null, null, null, false);
	}	
	
	/**
	 * This is the method that calculates the node sum. 
	 * @param port
	 * @param ampsToBeAdded
	 * @param nodeInfo
	 * @param portIdToExclude
	 * @param fuseLkuId
	 * @return
	 */
	@Override
	public Power3Phase get3PhaseNodeSum(PowerPort port, double ampsToBeAdded, PowerWattUsedSummary nodeInfo, Long portIdToExclude, Long fuseLkuId){
		return get3PhaseNodeSum(port, ampsToBeAdded, nodeInfo, portIdToExclude, fuseLkuId, false);
	}
	
	
	private Power3Phase get3PhaseNodeSum(PowerPort port, double ampsToBeAdded, PowerWattUsedSummary nodeInfo, Long portIdToExclude, Long fuseLkuId, boolean measured){
		PhaseCurrentParams  currentParam = new PhaseCurrentParams();
		Power3Phase phaseNodeSum = new Power3Phase();
		Long nodePhase = getNodePhase(port);
		
		//If the port is any type of breaker then we need to set the isBreaker to be true
		//If the nodePhase is delta then there is a transformer associated.
		boolean isBreaker = false;
		boolean isDeltaWyeTransformer = false;
		
		if (port.isBreaker()){
			isBreaker = true;
			if (nodePhase == SystemLookup.PhaseIdClass.THREE_DELTA) isDeltaWyeTransformer = true;
		}
				
		double nodeLineVolts = getVolts(port);    		
		double pbLineVolts = 0;
		double pbPhaseVolts = 0;
		
		nodePhase = nodePhase == null ? 0 : nodePhase;
		phaseNodeSum.volts = nodeLineVolts;
		
		Long nodePortIdToExclude = (nodeInfo != null && nodeInfo.getNodePortIdToExclude() != null ) ? nodeInfo.getNodePortIdToExclude() : -1L;
	    List<PowerWattUsedSummary> recList = this.powerCircuitDAO.getPowerWattUsedSummary(port.getPortId(), portIdToExclude, fuseLkuId, nodePortIdToExclude, measured);

	    for(PowerWattUsedSummary rec:recList){
	        if(pbLineVolts == 0){pbLineVolts = rec.getPbVolts();}
	        
	        if(pbPhaseVolts == 0){pbPhaseVolts = rec.getPbPhaseVolts();}
	        
	        if(pbLineVolts != rec.getPbVolts() || pbPhaseVolts != rec.getPbPhaseVolts()){
	        	computePhaseCurrents(phaseNodeSum, currentParam, pbLineVolts, pbPhaseVolts, nodeLineVolts, nodePhase, isBreaker, isDeltaWyeTransformer);
	        	//phaseNodeSum.reset();	        	
	        	pbLineVolts = rec.getPbVolts();
	        	pbPhaseVolts = rec.getPbPhaseVolts();
	        	currentParam = new PhaseCurrentParams();  
	        }
	        
	        addRecordToNodeSum(currentParam, phaseNodeSum, rec);	        
	    }
	
	    //Compute current load
	    computePhaseCurrents(phaseNodeSum, currentParam, pbLineVolts, pbPhaseVolts, nodeLineVolts, nodePhase, isBreaker, isDeltaWyeTransformer);
	    
	    phaseNodeSum.presentAmpsLoad =  max(phaseNodeSum.currentRatedA, phaseNodeSum.currentRatedB, phaseNodeSum.currentRatedC);
	    
	    if(ampsToBeAdded > 0){
	    	Integer phaseLeg = null;
	    	
		    if(nodeInfo != null){
		    	phaseLeg = Integer.parseInt(nodeInfo.getLegs());
		    	pbLineVolts = nodeInfo.getPbVolts();
		    	pbPhaseVolts = nodeInfo.getPbPhaseVolts();
		    }
		    currentParam = new PhaseCurrentParams();
		    		    
		    //Compute future/new load
		    //Add new load to current load
		    addAmpsToLeg(currentParam, ampsToBeAdded, (phaseLeg == null ? 0 : phaseLeg));
		    
	        computePhaseCurrents(phaseNodeSum, currentParam, pbLineVolts, pbPhaseVolts, nodeLineVolts, nodePhase, isBreaker, isDeltaWyeTransformer);
	    }
	    
		return phaseNodeSum;		
	}


	private void computePhaseCurrents(Power3Phase phaseNodeSum, PhaseCurrentParams currentParam, double pbLineVolts, double pbPhaseVolts, double nodeLineVolts, long nodePhase, boolean isBreaker, boolean isDeltaWyeTransformer ){
        double nodePhaseVolts = 0;
        
        phaseNodeSum.windingsRatio = 1.0;
        
        if (isBreaker && pbLineVolts > 0 && pbPhaseVolts > 0){
        	 if(nodePhase == SystemLookup.PhaseIdClass.THREE_WYE){
                 nodePhaseVolts = getCorrectVolts(nodeLineVolts / Math.sqrt(3.0));
                 if(nodePhaseVolts == 0) nodePhaseVolts = nodeLineVolts;
             }
             else if(nodePhase == SystemLookup.PhaseIdClass.THREE_DELTA){
                 nodePhaseVolts = nodeLineVolts;
             }
             else if(nodePhase == SystemLookup.PhaseIdClass.SINGLE_3WIRE){
                 nodePhaseVolts = getCorrectVolts(nodeLineVolts / 2.0);
                 if(nodePhaseVolts == 0) nodePhaseVolts = nodeLineVolts;
             }
             else{ //Single-phase (2-Wire)
                 nodePhaseVolts = nodeLineVolts;
     		}
             
             //If there is a voltage drop, then use the panel's phase voltage to calculate
             //the voltage ratio i.e. the windings ratio of the transformer
             //TODO: Confirm the condition below. 
             if(pbLineVolts <= nodeLineVolts){
             	phaseNodeSum.windingsRatio = pbPhaseVolts / nodePhaseVolts;
             }
        }
       
	    if(isDeltaWyeTransformer){  //Requested node is on the Delta side of a delta/wye transformer
	        //This solves for the line currents on the delta side of a delta/wye transformer
	    	phaseNodeSum.currentMaxA += complex(currentParam.ABm, currentParam.CAm, currentParam.Am, currentParam.CAm, currentParam.BCm, currentParam.Cm) * phaseNodeSum.windingsRatio;	    	
	    	phaseNodeSum.currentMaxB += complex(currentParam.BCm, currentParam.ABm, currentParam.Bm, currentParam.ABm, currentParam.CAm, currentParam.Am) * phaseNodeSum.windingsRatio;
	    	phaseNodeSum.currentMaxC += complex(currentParam.CAm, currentParam.BCm, currentParam.Cm, currentParam.BCm, currentParam.ABm, currentParam.Bm) * phaseNodeSum.windingsRatio;
	    	phaseNodeSum.currentRatedA += complex(currentParam.ABr, currentParam.CAr, currentParam.Ar,  currentParam.CAr,  currentParam.BCr,  currentParam.Cr) * phaseNodeSum.windingsRatio;	    	
	        phaseNodeSum.currentRatedB += complex(currentParam.BCr, currentParam.ABr, currentParam.Br, currentParam.ABr, currentParam.CAr, currentParam.Ar) * phaseNodeSum.windingsRatio;
	        phaseNodeSum.currentRatedC += complex(currentParam.CAr, currentParam.BCr, currentParam.Cr, currentParam.BCr, currentParam.ABr, currentParam.Br) * phaseNodeSum.windingsRatio;
	    }
	    else {
	        //Requested node is on the Wye side, also includes single-phase 3-wire/2-wire
	    	//This solves for the line currents of an unbalanced 3-phase circuit.
	        phaseNodeSum.currentMaxA += (currentParam.Am.value + Math.sqrt(currentParam.ABm.value * currentParam.ABm.value + currentParam.CAm.value * currentParam.CAm.value + currentParam.ABm.value * currentParam.CAm.value)) * phaseNodeSum.windingsRatio;
	        phaseNodeSum.currentMaxB += (currentParam.Bm.value + Math.sqrt(currentParam.BCm.value * currentParam.BCm.value + currentParam.ABm.value * currentParam.ABm.value + currentParam.BCm.value * currentParam.ABm.value)) * phaseNodeSum.windingsRatio;
	        phaseNodeSum.currentMaxC += (currentParam.Cm.value + Math.sqrt(currentParam.CAm.value * currentParam.CAm.value + currentParam.BCm.value * currentParam.BCm.value + currentParam.CAm.value * currentParam.BCm.value)) * phaseNodeSum.windingsRatio;	       
	        phaseNodeSum.currentRatedA += (currentParam.Ar.value + Math.sqrt(currentParam.ABr.value * currentParam.ABr.value + currentParam.CAr.value * currentParam.CAr.value + currentParam.ABr.value * currentParam.CAr.value)) * phaseNodeSum.windingsRatio;
	        phaseNodeSum.currentRatedB += (currentParam.Br.value + Math.sqrt(currentParam.BCr.value * currentParam.BCr.value + currentParam.ABr.value * currentParam.ABr.value + currentParam.BCr.value * currentParam.ABr.value)) * phaseNodeSum.windingsRatio;
	        phaseNodeSum.currentRatedC += (currentParam.Cr.value + Math.sqrt(currentParam.CAr.value * currentParam.CAr.value + currentParam.BCr.value * currentParam.BCr.value + currentParam.CAr.value * currentParam.BCr.value)) * phaseNodeSum.windingsRatio;
	    }
	}
	
	private void addAmpsToLeg(PhaseCurrentParams currentParam, double ampsToBeAdded,  int phaseLeg){
	    //Add new load to current load
        switch(phaseLeg){
        case SystemLookup.PhaseLegClass.L1_A:	
        	currentParam.Ar.value += ampsToBeAdded;
        	break;
        case SystemLookup.PhaseLegClass.L2_B:
        	currentParam.Br.value += ampsToBeAdded;
            break;
        case SystemLookup.PhaseLegClass.L3_C:
        	currentParam.Cr.value += ampsToBeAdded;
        	break;
        case SystemLookup.PhaseLegClass.L12_AB:
        	currentParam.ABr.value  += ampsToBeAdded;
        	break;
        case SystemLookup.PhaseLegClass.L23_BC:
        	currentParam.BCr.value += ampsToBeAdded;
        	break;
        case SystemLookup.PhaseLegClass.L31_CA:
        	currentParam.CAr.value += ampsToBeAdded;
        	break;
        case SystemLookup.PhaseLegClass.L123_ABC: 
        	currentParam.Ar.value += ampsToBeAdded;
        	currentParam.Br.value += ampsToBeAdded;
        	currentParam.Cr.value += ampsToBeAdded;
        	break;
        }	   
	}
	
	private void addRecordToNodeSum(PhaseCurrentParams currentParam, Power3Phase phaseNodeSum, PowerWattUsedSummary rec){
		if(rec.isMeasured()){
			addRecordToNodeSumMeasured(currentParam, phaseNodeSum, rec);
			return;
		}
		
		int legs = Integer.valueOf(rec.getLegs() == null ? "0": rec.getLegs());	  
        double currentRated = rec.getCurrentRated();
        double currentMax = rec.getCurrentMax();
        double vaRated = rec.getVaRated();
        double vaMax = rec.getVaMax();
        double wMax = rec.getWattMax();
        double wRated = rec.getWattRated();
        
        switch(legs){
        case SystemLookup.PhaseLegClass.L1_A:	
        	currentParam.Ar.value += currentRated;
        	currentParam.Am.value += currentMax;
            
            phaseNodeSum.vaMaxA += vaMax;    //VAMaxA = VAMaxA + !VAMax
            phaseNodeSum.vaRatedA += vaRated;//VARatedA = VARatedA + !VARated
            phaseNodeSum.wMaxA += wMax;      //WMaxA = WMaxA + !WMax
            phaseNodeSum.wRatedA += wRated;  //WRatedA = WRatedA + !WRated
        	break;
        case SystemLookup.PhaseLegClass.L2_B:
        	currentParam.Br.value += currentRated;  //Br = Br + !CurrentRated
        	currentParam.Bm.value += currentMax;
            
            phaseNodeSum.vaMaxB += vaMax;    //VAMaxA = VAMaxA + !VAMax
            phaseNodeSum.vaRatedB += vaRated;//VARatedA = VARatedA + !VARated
            phaseNodeSum.wMaxB += wMax;      //WMaxA = WMaxA + !WMax
            phaseNodeSum.wRatedB += wRated;  //WRatedA = WRatedA + !WRated	        		        	        	
        	break;
        case SystemLookup.PhaseLegClass.L3_C:
        	currentParam.Cr.value += currentRated;  //Cr = Cr + !CurrentRated
        	currentParam.Cm.value += currentMax;
            
            phaseNodeSum.vaMaxC += vaMax;    
            phaseNodeSum.vaRatedC += vaRated;
            phaseNodeSum.wMaxC += wMax;      
            phaseNodeSum.wRatedC += wRated;  	        		        	        	
        	break;
        case SystemLookup.PhaseLegClass.L12_AB:
        	currentParam.ABr.value += currentRated;  //ABr = !CurrentRated
        	currentParam.ABm.value += currentMax;
            
            phaseNodeSum.vaMaxA += vaMax / 2;    
            phaseNodeSum.vaRatedA += vaRated / 2;
            phaseNodeSum.wMaxA += wMax / 2;      
            phaseNodeSum.wRatedA += wRated / 2;
            
            phaseNodeSum.vaMaxB += vaMax / 2;    
            phaseNodeSum.vaRatedB += vaRated / 2;
            phaseNodeSum.wMaxB += wMax / 2;      
            phaseNodeSum.wRatedB += wRated / 2;  	        		        	        	
            
        	break;
        case SystemLookup.PhaseLegClass.L23_BC:
        	currentParam.BCr.value += currentRated;  //BCr = !CurrentRated
        	currentParam.BCm.value += currentMax;

            phaseNodeSum.vaMaxB += vaMax / 2;    
            phaseNodeSum.vaRatedB += vaRated / 2;
            phaseNodeSum.wMaxB += wMax / 2;      
            phaseNodeSum.wRatedB += wRated / 2;  	        		        	        	
            
            phaseNodeSum.vaMaxC += vaMax / 2;    
            phaseNodeSum.vaRatedC += vaRated / 2;
            phaseNodeSum.wMaxC += wMax / 2;      
            phaseNodeSum.wRatedC += wRated / 2;
                            
        	break;
        case SystemLookup.PhaseLegClass.L31_CA:
        	currentParam.CAr.value += currentRated;  //CAr = !CurrentRated
        	currentParam.CAm.value += currentMax;

            phaseNodeSum.vaMaxA += vaMax / 2;    
            phaseNodeSum.vaRatedA += vaRated / 2;
            phaseNodeSum.wMaxA += wMax / 2;      
            phaseNodeSum.wRatedA += wRated / 2;  	        		        	        	
            
            phaseNodeSum.vaMaxC += vaMax / 2;    
            phaseNodeSum.vaRatedC += vaRated / 2;
            phaseNodeSum.wMaxC += wMax / 2;      
            phaseNodeSum.wRatedC += wRated / 2;
                                                   
        	break;
        case SystemLookup.PhaseLegClass.L123_ABC:                
        	currentParam.Ar.value += currentRated;
        	currentParam.Am.value += currentMax;
        	currentParam.Br.value += currentRated;
        	currentParam.Bm.value += currentMax;
        	currentParam.Cr.value += currentRated;
        	currentParam.Cm.value += currentMax;
        	
            phaseNodeSum.vaMaxA += vaMax / 3;    
            phaseNodeSum.vaRatedA += vaRated / 3;
            phaseNodeSum.wMaxA += wMax / 3;      
            phaseNodeSum.wRatedA += wRated / 3;
            
            phaseNodeSum.vaMaxB += vaMax / 3;    
            phaseNodeSum.vaRatedB += vaRated / 3;
            phaseNodeSum.wMaxB += wMax / 3;      
            phaseNodeSum.wRatedB += wRated / 3;  	        		        	        	
            
            phaseNodeSum.vaMaxC += vaMax / 3;    
            phaseNodeSum.vaRatedC += vaRated / 3;
            phaseNodeSum.wMaxC += wMax / 3;      
            phaseNodeSum.wRatedC += wRated / 3;
                            
        	break;
        }	        
	}

	private void addRecordToNodeSumMeasured(PhaseCurrentParams currentParam, Power3Phase phaseNodeSum, PowerWattUsedSummary rec){
		int legs = Integer.valueOf(rec.getLegs() == null ? "0": rec.getLegs());	  
        double currentActual = rec.getCurrentMax();
        double currentMaxA = rec.getCurrentMaxA();
        double currentMaxB = rec.getCurrentMaxB();
        double currentMaxC = rec.getCurrentMaxC();
        
        switch(legs){
        case SystemLookup.PhaseLegClass.L1_A:	
        	currentParam.Am.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L2_B:
        	currentParam.Bm.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L3_C:
        	currentParam.Cm.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L12_AB:
        	currentParam.ABm.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L23_BC:
        	currentParam.BCm.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L31_CA:
        	currentParam.CAm.value += currentActual;
        	break;
        case SystemLookup.PhaseLegClass.L123_ABC:                
        	currentParam.Am.value += currentMaxA;
        	currentParam.Bm.value += currentMaxB;
        	currentParam.Cm.value += currentMaxC;
        	break;
        }	        
	}
	/**
	 * Get the volts for 3-phase calculation
	 * @param power port
	 * @return
	 */
	protected double getVolts(PowerPort port) {
		if (port != null && port.getVoltsLookup() != null && port.isPowerSupply() == false){
			Double volts = Double.parseDouble(port.getVoltsLookup().getLkpValue());
			
			return volts;
		}
		//volts for a power supply is the voltage from the outlet where it is connected.
		//the enough power function does not compute power for a power supply port
		return 0;
	}	

	//--------- Private methods ---------------------
	private double complex(ComplexObject co1, ComplexObject co2, ComplexObject co3, ComplexObject co4, ComplexObject co5, ComplexObject co6){
	    double real = (co1.value * co1.real - co2.value * co2.real + co3.value * co3.real) - (co4.value * co4.real - co5.value * co5.real + co6.value * co6.real);
	    double imag = (co1.value * co1.imaginary - co2.value * co2.imaginary + co3.value * co3.imaginary) - (co4.value * co4.imaginary - co5.value * co5.imaginary + co6.value * co6.imaginary);

	        //Real = (V1 * R1 - V2 * R2 + V3 * R3) - (V4 * R4 - V5 * R5 + V6 * R6)
	        //Imag = (V1 * I1 - V2 * I2 + V3 * I3) - (V4 * I4 - V5 * I5 + V6 * I6)
	    return Math.sqrt((real * real) + (imag * imag));
	}
	

	protected double getCorrectVolts(Double volts){
		 //Add new load to current load
		int retValue = 0;
		
		//TODO: Add all voltages 240
        switch(volts.intValue()){
        case 120:
        	retValue = 120;
            break;
        case 208:
        	retValue = 208;
            break;
        case 219:        
        case 220:
        	retValue = 220;
            break;
        case 239:
        case 240:
        case 241:
        	retValue = 240;
            break;
        case 479:
        case 480:
        	retValue = 480;
            break;
        case 127:
        case 130:
        	retValue = 130;
            break;
        case 139:
        case 140:
        	retValue = 140;
            break;
        case 231:
        case 230:
        	retValue = 230;
            break;
        case 277:
        	retValue = 277;
            break;
        case 381:
        case 380:
        	retValue = 380;
            break;
        case 398:
        case 400:
        	retValue = 400;
            break;
        case 415:
        case 416:
        	retValue = 415;
            break;
        case 600:
        	retValue = 600;
            break;
        case 830:
        case 831:
        	retValue = 830;
            break;
        case 1039:
        case 1040:
        	retValue = 1040;
            break;
       }	
        
       return (double)retValue;
	}
	
	//Helper classes
	public class ComplexObject{
		public double value;  //rename to value
		public double real;  //rename to real
		public double imaginary;  //rename to imaginary
		
		public ComplexObject(double value, double real, double imaginary){
			this.value = value;
			this.real = real;
			this.imaginary = imaginary;
		}
	}

	public class PhaseCurrentParams{
	    public ComplexObject ABm = new ComplexObject(0, 0.866, -0.5);  //ABm, 0.866, -0.5
	    public ComplexObject CAm = new ComplexObject(0, -0.866, -0.5); //CAm, -0.866, -0.5
	    public ComplexObject Am = new ComplexObject(0, 1, 0);          //Am, 1, 0
	    public ComplexObject BCm = new ComplexObject(0, 0, 1);    	    //BCm, 0, 1
	    public ComplexObject Cm = new ComplexObject(0, -0.5, -0.866);  //Cm, -0.5, -0.866
	    public ComplexObject Bm = new ComplexObject(0, -0.5, 0.866);   //Bm, -0.5, 0.866	    
	    public ComplexObject ABr = new ComplexObject(0, 0.866, -0.5);   //ABr, 0.866, -0.5
	    public ComplexObject CAr = new ComplexObject(0, -0.866, -0.5);  //CAr, -0.866, -0.5
	    public ComplexObject Ar = new ComplexObject(0, 1, 0);           //Ar, 1, 0
	    public ComplexObject BCr = new ComplexObject(0, 0, 1);    	     //BCr, 0, 1
	    public ComplexObject Cr = new ComplexObject(0, -0.5, -0.866);   //Cr, -0.5, -0.866
	    public ComplexObject Br = new ComplexObject(0, -0.5, 0.866);   //Br, -0.5, 0.866		
	}

	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}	
	
	/**
	 * Get the node phase for a power port. If port is branch circuit breaker, the function return the phase of its panel
	 * @param power port
	 * @return
	 */
	private Long getNodePhase(PowerPort port) {
		Long phase = null;
		
		if (port != null){
			phase = port.getPhaseLookup().getLkpValueCode();
			
			if(port.isBranchCircuitBreaker()){ //get the phase from the panel
				MeItem panel = (MeItem)itemDAO.loadItem(port.getItem().getItemId());
				
				phase = panel.getPhaseLookup().getLkpValueCode();
			}
		}
		
		return phase;
	}		
}
