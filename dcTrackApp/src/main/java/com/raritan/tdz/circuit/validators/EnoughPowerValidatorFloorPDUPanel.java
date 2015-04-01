/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
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
public class EnoughPowerValidatorFloorPDUPanel extends
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
		
		final List<Long> validSubClassList = Collections.unmodifiableList(new ArrayList<Long>() {{
			add(SystemLookup.SubClass.LOCAL);
			add(SystemLookup.SubClass.REMOTE);
			add(SystemLookup.SubClass.BUSWAY);
		}});
		
		validateEnoughPowerParamsPort(powerFactor, destNodeId, SystemLookup.PortSubClass.PANEL_BREAKER, errors);
		
		PowerPort powerPort = powerPortDAO.read(destNodeId);
		if (powerPort.getItem() == null){
			throw new IllegalArgumentException("The Item for this PDU Panel is null");
		}
		
		Power3Phase objElec =  powerCalc.get3PhaseNodeSum(powerPort, ampsBeingConnected, nodeInfo, psPortIdToExclude, null);
		
		double ampsOnNode = ampsBeingConnected;
		
		//calculate against the source if user asked to include source port into the calculation
		Power3Phase resultNodeSum = calculatePhaseNodeSumForSourcePort(srcNodeId, destNodeId, (Power3Phase) firstNodeSum, objElec, nodeInfo);

		if (objElec != null)
			ampsOnNode = max(objElec.currentRatedA, objElec.currentRatedB, objElec.currentRatedC);
		
		double panelULRating = getPanelULRating(powerPort);
		
		//This will return the amps being connected only for the case of item save and power chain calculation.
		//If not it will return whatever came into this method as a parameter.
		ampsBeingConnected = getAmpsBeingConnectedForDisplay(ampsBeingConnected, srcNodeId, resultNodeSum);


		if(ampsOnNode > panelULRating){
			Item pbItem = powerPort.getItem();
			String itemName = "";
			if (pbItem != null){

				Item parent = pbItem.getParentItem();
				itemName = parent != null ? parent.getItemName() != null ? parent.getItemName() : "<Unknown> Item" + "/":"";
				itemName += pbItem.getItemName() != null ? pbItem.getItemName() : "<Unknown> Item";
			}

			String portName = "";
			setError(errors, ampsBeingConnected, ampsOnNode, panelULRating, "Panelboard Main Breaker", itemName, portName, "Amps", objElec == null ? 0 : objElec.presentAmpsLoad);
			
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		}
		
		return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE,resultNodeSum);
	}

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
	
	private double getPanelULRating(PowerPort powerPort) {
		MeItem meItem = (MeItem)itemDAO.initializeAndUnproxy(powerPort.getItem());
		
		if (meItem == null) return 0.0;
		
		//Get the location of the PDU
		//Based on the location, we multiply the rating amps with 0.8
		String country = 
				meItem.getDataCenterLocation() != null && meItem.getDataCenterLocation().getDcLocaleDetails() != null
						? meItem.getDataCenterLocation().getDcLocaleDetails().getCountry():"";
		return country.equalsIgnoreCase("United States") ? meItem.getRatingAmps() * 0.8 : meItem.getRatingAmps();
	}


}
