/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.util.Power3Phase;
import com.raritan.tdz.circuit.util.PowerCalc;

/**
 * @author prasanna
 *
 */
public class EnoughPowerValidatorBranchCircuitBreaker extends
		AbstractEnoughPowerValidator {
	@Autowired(required=true)
	PowerCalc powerCalc;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidator#checkEnoughPower(java.lang.Long, java.lang.Long, com.raritan.tdz.domain.PowerConnection, org.springframework.validation.Errors)
	 */
	@Override
	public EnoughPowerResult checkEnoughPower(Long psPortId,
			Long psPortToExclude, PowerConnection powerConn, boolean includeFirstNodeSum, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo) {
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
		validateEnoughPowerParamsPort(powerFactor, destNodeId, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, errors);
		
		PowerPort port = powerPortDAO.read(destNodeId);
		
		//Get outlet phase given the breaker as destination port.
		//Integer outletPhase = getOutletPhaseLeg(port,errors);
		
		Power3Phase objElecDst = powerCalc.get3PhaseNodeSum(port,ampsBeingConnected, nodeInfo,psPortIdToExclude, null);
		
		double nodeULRating =  getNodeULRating(port);
		
		double ampsOnNode = ampsBeingConnected;
		
		//calculate against the source if user asked to include source port into the calculation
		Power3Phase resultNodeSum = calculatePhaseNodeSumForSourcePort(srcNodeId, destNodeId, (Power3Phase)firstNodeSum, objElecDst, nodeInfo);

		if (objElecDst != null){
			ampsOnNode = max(objElecDst.currentRatedA, objElecDst.currentRatedB, objElecDst.currentRatedC);
		}
		
		//This will return the amps being connected only for the case of item save and power chain calculation.
		//If not it will return whatever came into this method as a parameter.
		ampsBeingConnected = getAmpsBeingConnectedForDisplay(ampsBeingConnected, srcNodeId, resultNodeSum);

		if((ampsOnNode) > nodeULRating){
			String itemName = port.getItem() != null && port.getItem().getItemName() != null ? port.getItem().getItemName() : "<Unknown> Item";
			String portName = "";
			setError(errors, ampsBeingConnected, ampsOnNode, nodeULRating, "Branch Circuit Breaker", itemName, portName, "Amps", objElecDst == null ? 0 : objElecDst.presentAmpsLoad);
		}
	
		if (errors.hasErrors()){
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		}
		
		return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE,resultNodeSum);
	}




	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.AbstractEnoughPowerValidator#getTransformerPhaseValueCode(java.lang.Long)
	 */
	@Override
	protected Long getTransformerPhaseValueCode(PowerPort port) {
		if (port != null && port.getPhaseLookup() != null){
			return port.getPhaseLookup().getLkpValueCode();
		}
		
		return null;
	}
	
	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}
	
	private Integer getOutletPhaseLeg(PowerPort outletPort, Errors errors){
		Integer outletPhase = null;
		if (outletPort.isOutlet()){
			LksData lskPhaseLeg = outletPort.getPhaseLegsLookup();		
			outletPhase = Integer.parseInt(lskPhaseLeg.getAttribute());
		}
		return outletPhase;
	}
	
	private double getNodeULRating(PowerPort powerPort) {
		if (powerPort == null || powerPort.getItem() == null) return 0.0;
		
		Item item = powerPort.getItem();
		
		//Get the location of the PDU
		//Based on the location, we multiply the rating amps with 0.8
		String country = 
				item.getDataCenterLocation() != null && item.getDataCenterLocation().getDcLocaleDetails() != null
						? item.getDataCenterLocation().getDcLocaleDetails().getCountry():"";
		return country.equalsIgnoreCase("United States") ? powerPort.getAmpsNameplate() * 0.8 : powerPort.getAmpsNameplate();
	}


}
