/**
 * 
 */
package com.raritan.tdz.circuit.validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.util.PowerCalc;
import com.raritan.tdz.circuit.util.Power3Phase;

/**
 * @author prasanna
 *
 */
public class EnoughPowerValidatorRackPDUInput extends AbstractEnoughPowerValidator {

	private Long receptacleId = null;
	@Autowired(required=true)
	PowerCalc powerCalc;

	
	public Long getReceptacleId() {
		return receptacleId;
	}



	public void setReceptacleId(Long receptacleId) {
		this.receptacleId = receptacleId;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidator#checkEnoughPower(java.lang.Long, java.lang.Long, com.raritan.tdz.domain.PowerConnection, org.springframework.validation.Errors)
	 */
	@Override
	public EnoughPowerResult checkEnoughPower(Long psPortId,
			Long psPortToExclude, PowerConnection powerConn, boolean includeFirstNodeSum, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo) {
		if (powerConn != null && powerConn.getSourcePowerPort() != null){
			receptacleId = powerConn.getSourcePowerPort().getPortId();
		}
		return checkEnoughPowerUsingPort(psPortId, psPortToExclude, powerConn, errors, includeFirstNodeSum, firstNodeSum, nodeInfo);
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidator#checkEnoughPower(double, long, double, java.lang.Long, java.lang.Long, java.lang.String, int, org.springframework.validation.Errors)
	 */
	@Override
	public EnoughPowerResult checkEnoughPower(double ampsBeingConnected,
			long wattsBeingConnected, double powerFactor, Long srcNodeId,
			Long destNodeId, Long psPortIdToExclude,
			String psPortItemName, int positionInCircuit, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo) {
		validateEnoughPowerParamsPort(powerFactor, destNodeId, SystemLookup.PortSubClass.INPUT_CORD, errors);
	
		if (receptacleId == null && srcNodeId != null ){
			receptacleId = srcNodeId;
		}
		
		PowerPort outletPort = powerPortDAO.read(receptacleId);
		//Integer outletLegs = getOutletPhaseLeg(outletPort);
		PowerPort inputPort = outletPort.getInputCordPort();
		
		if (ampsBeingConnected > outletPort.getAmpsNameplate()){
			Object[] errorArgs = new Object[] {
					outletPort.getAmpsNameplate(),
					ampsBeingConnected
					};
			errors.reject("powerProc.AmpsExceedsNamePlate",errorArgs,"Amps being connected exceeds Nameplate value of Rack PDU output port");
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		}
		
		Power3Phase resultNodeSum = null;
		if (inputPort != null && ampsBeingConnected != 0){
			double ampsOnNode = 0.0;
			double ampsOver = 0.0;
			double retAmps = -1.0;
			
			Power3Phase objElec = powerCalc.get3PhaseNodeSum(inputPort, ampsBeingConnected, nodeInfo, psPortIdToExclude, null);
			
			//calculate against the source if user asked to include source port into the calculation
			resultNodeSum = calculatePhaseNodeSumForSourcePort(srcNodeId, destNodeId, (Power3Phase)firstNodeSum, objElec, nodeInfo);
			
			ampsOnNode = ampsBeingConnected;
			
		    if (objElec != null){
		    	ampsOnNode = max(objElec.currentRatedA, objElec.currentRatedB, objElec.currentRatedC);
		    }
		    
	        ampsOver = ampsOnNode - inputPort.getAmpsNameplate();
	        retAmps = ampsOnNode;
		    
		    if(ampsOver > 0){
				String itemName = inputPort.getItem() != null && inputPort.getItem().getItemName() != null? inputPort.getItem().getItemName() : "<Unknown>";
				String portName = ", " + (inputPort.getPortName() != null ? inputPort.getPortName() : "<Unknown> Port") ;
				setError(errors, ampsBeingConnected, retAmps, inputPort.getAmpsNameplate(), "Rack PDU Input Cord", itemName, portName, "Amps", objElec == null ? 0 : objElec.presentAmpsLoad);
		    	
		    	return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		    }
		}
		
		
		return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE,resultNodeSum);
	}


	
	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.AbstractEnoughPowerValidator#getTransformerPhaseValueCode(java.lang.Long)
	 */
	@Override
	protected Long getTransformerPhaseValueCode(PowerPort port) {	
		return null;
	}
	
	private Integer getOutletPhaseLeg(PowerPort outletPort){
		Integer outletPhase = null;
		LksData lskPhaseLeg = outletPort.getPhaseLegsLookup();		
		if (lskPhaseLeg != null)
			outletPhase = Integer.parseInt(lskPhaseLeg.getAttribute());
		return outletPhase;
	}


}
