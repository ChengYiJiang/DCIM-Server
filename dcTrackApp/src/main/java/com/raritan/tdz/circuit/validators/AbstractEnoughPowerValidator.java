/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.util.PowerCalc;
import com.raritan.tdz.circuit.util.Power3Phase;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * This abstract class will implement some of the common methods used by the 
 * power validators. This will specifically implement the three-phase node sum
 * @author prasanna
 *
 */
public abstract class AbstractEnoughPowerValidator implements
		EnoughPowerValidator {
	
	@Autowired(required=true)
	protected PowerPortDAO powerPortDAO;
	
	@Autowired(required=true)
	protected PowerConnDAO powerConnDAO;
	
	@Autowired(required=true)
	protected ItemDAO itemDAO; 
	
	protected Logger log = Logger.getLogger(getClass());
	
	@Autowired(required=true)
	protected PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	protected SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	PowerCalc powerCalc;
	
	protected StringBuilder powerUsageQryStrBuilder = new StringBuilder();
	protected List<Object> qryParams = new ArrayList<Object>();
	
	protected boolean includeSource = false;
	
	/* NOT USE - SANTO
	protected EnoughPowerResult checkEnoughPowerUsingItem(Long psPortId,
			Long psPortToExclude, PowerConnection powerConn, Errors errors, Object firstNodeSum) {
		if (powerConn == null || powerConn.getDestPowerPort() == null
				|| powerConn.getDestPowerPort().getPortId() == null){
			throw new IllegalArgumentException("Power connection and portId must be provided");
		}
		
		if (errors == null){
			throw new IllegalArgumentException("You must provide errors object");
		}
		
		PowerPort psPort = psPortId != null ? powerPortDAO.read(psPortId) : null;
		
		long wattsBeingConnected = 0;
		double ampsBeingConnected = 0.0;
		double pf = 1.0;
		String itemName = null;
		

		PowerPort dstPort = powerConn.getDestPowerPort();
		dstPort = powerPortDAO.read(dstPort.getPortId());
		
		Item circuitNode = dstPort != null ? dstPort.getItem() : null;
		Long circuitNodeId = circuitNode != null ? circuitNode.getItemId() : null;
		
		if (psPort != null && powerConn.getSourcePort() != null){
			if (psPort.getPortSubClassLookup() == null || !psPort.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.POWER_SUPPLY)){
				throw new IllegalArgumentException("The port id provided is not power supply port");
			}
			
			itemName = psPort.getItem() != null ? psPort.getItem().getItemName() : null;
			//Get the amps, watts, pf from the psPort
			wattsBeingConnected = psPort.getWattsBudget();
			ampsBeingConnected = psPort.getAmpsBudget();
			pf = psPort.getPowerFactor();
		}
		
		return (checkEnoughPower(ampsBeingConnected,wattsBeingConnected,pf,null,circuitNodeId,psPortToExclude,itemName, -1, firstNodeSum, errors, null));
	}
	*/
	protected EnoughPowerResult checkEnoughPowerUsingPort(Long psPortId,
			Long psPortToExclude, PowerConnection powerConn, Errors errors, boolean includeSource, Object firstNodeSum, PowerWattUsedSummary nodeInfo) {
		if (powerConn == null || powerConn.getDestPowerPort() == null 
				|| powerConn.getDestPowerPort().getPortId() == null){
			throw new IllegalArgumentException("Power connection and portId must be provided");
		}
		
		if (errors == null){
			throw new IllegalArgumentException("You must provide errors object");
		}
		
		this.includeSource = includeSource;
		
		PowerPort psPort = psPortId != null ? powerPortDAO.read(psPortId):null;
		
		long wattsBeingConnected = 0;
		double ampsBeingConnected = 0.0;
		double pf = 1.0;
		PowerPort srcPort = includeSource ? powerConn.getSourcePowerPort():null;
		Long srcPortId = srcPort != null ? srcPort.getPortId():null;
		
		PowerPort dstPort = powerConn.getDestPowerPort();
		dstPort = powerPortDAO.read(dstPort.getPortId());
		Long dstPortId = dstPort != null ? dstPort.getPortId():null;
		
		String itemName = null;
		
		if (psPort != null && powerConn.getSourcePort() != null){
			if (psPort.getPortSubClassLookup() == null || !psPort.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.POWER_SUPPLY)){
				throw new IllegalArgumentException("The port id provided is not power supply port");
			}
			
			itemName = psPort.getItem() != null ? psPort.getItem().getItemName() : null;
			
			//Get the amps, watts, pf from the psPort
			wattsBeingConnected = psPort.getWattsBudget();
			ampsBeingConnected = psPort.getAmpsBudget();
			pf = psPort.getPowerFactor();
		}
		
		return (checkEnoughPower(ampsBeingConnected,wattsBeingConnected,pf,srcPortId,dstPortId,psPortToExclude,itemName, -1, firstNodeSum, errors, nodeInfo));
	}


	protected void validateEnoughPowerParamsItem(
			double powerFactor, Long circuitNodeId, Long classLookupValueCode, Errors errors) {
		if (circuitNodeId == null){
			throw new IllegalArgumentException("NodeId must be provided");
		}
		
		Item item = itemDAO.read(circuitNodeId);
		if (item == null || item.getClassLookup() == null || !item.getClassLookup().getLkpValueCode().equals(classLookupValueCode)){
			LksData validClassLookup = systemLookupFinderDAO.findByLkpValueCode(classLookupValueCode).get(0);
			throw new IllegalArgumentException("The NodeId must be of a " + validClassLookup.getLkpValue());
		}
		
		if (powerFactor <= 0 || powerFactor > 1){
			Object[] errorArgs = { powerFactor };
			errors.rejectValue("powerFactor", "PortValidator.invalidPowerFactor", errorArgs, "The Power Factor must be greater than 0 and less than or equal to 1: " + powerFactor);
			
			// throw new IllegalArgumentException("The Power Factor must be greater than 0 and less than or equal to 1: " + powerFactor);
		}
	}
	
	//TODO: Change this to validateArgs
	protected void validateEnoughPowerParamsPort(
			double powerFactor, Long circuitNodeId, Long portSubClassLookup, Errors errors) {
		if (circuitNodeId == null){
			throw new IllegalArgumentException("NodeId must be provided");
		}
		
		PowerPort powerPort = powerPortDAO.read(circuitNodeId);
		if (powerPort == null || powerPort.getPortSubClassLookup() == null || !powerPort.getPortSubClassLookup().getLkpValueCode().equals(portSubClassLookup)){
			LksData validPortSubClassLookup = systemLookupFinderDAO.findByLkpValueCode(portSubClassLookup).get(0);
			throw new IllegalArgumentException("The NodeId must be of a " + validPortSubClassLookup.getLkpValue());
		}
		
		if (powerFactor <= 0 || powerFactor > 1){ 
			
			Object[] errorArgs = { powerFactor };
			errors.rejectValue("powerFactor", "PortValidator.invalidPowerFactor", errorArgs, "The Power Factor must be greater than 0 and less than or equal to 1: " + powerFactor);
			
			// throw new IllegalArgumentException("The Power Factor must be greater than 0 and less than or equal to 1: " + powerFactor);
		}
	}
	
	/**
	 * This will calculate the node sum if the user has asked to include the source port into
	 * the calculation as well.
	 * @param srcNodeId
	 * @param dstNodeId TODO
	 * @param firstNodeSum TODO
	 * @param objElecDst
	 * @param nodeInfo TODO
	 * @return TODO
	 */
	protected Power3Phase calculatePhaseNodeSumForSourcePort(Long srcNodeId,
			Long dstNodeId, Power3Phase firstNodeSum, Power3Phase objElecDst, PowerWattUsedSummary nodeInfo) {
		Power3Phase result = firstNodeSum;
		
		if (srcNodeId != null && srcNodeId > 0){
			PowerPort srcPort = powerPortDAO.read(srcNodeId);
//			if (firstNodeSum == null) { 
//				//When previous transaction fails and user changes back to the original chain
//				//we dont want to double add it. So, we just return from here.
//				try {
//					
//					PowerPort dstPort = powerConnDAO.getDestinationPort(srcNodeId);
//					if (dstPort.getPortId().equals(dstNodeId)){
//						return result;
//					}
//				} catch (DataAccessException e) {
//					if(log.isDebugEnabled())
//						e.printStackTrace();
//				}
//			}
			Power3Phase objElecSrc = firstNodeSum != null ? firstNodeSum : powerCalc.get3PhaseNodeSum(srcPort,0, nodeInfo, null, null);
			result = objElecSrc;
			
			if (objElecSrc != null && objElecDst != null){
				objElecDst.currentRatedA += objElecSrc.currentRatedA;
				objElecDst.currentRatedB += objElecSrc.currentRatedB;
				objElecDst.currentRatedC += objElecSrc.currentRatedC;
				objElecDst.wRatedA += objElecSrc.wRatedA;
				objElecDst.wRatedB += objElecSrc.wRatedB;
				objElecDst.wRatedC += objElecSrc.wRatedC;
				objElecDst.vaRatedA += objElecSrc.vaRatedA;
				objElecDst.vaRatedB += objElecSrc.vaRatedB;
				objElecDst.vaRatedC += objElecSrc.vaRatedC;
			}
		}
		
		return result;
	}
	
	protected double getAmpsBeingConnectedForDisplay(double ampsBeingConnected,
			Long srcNodeId, Power3Phase resultNodeSum) {
		if (includeSource && resultNodeSum != null && srcNodeId != null && srcNodeId > 0){
			ampsBeingConnected = max(resultNodeSum.currentRatedA,resultNodeSum.currentRatedB, resultNodeSum.currentRatedC);
		}
		return ampsBeingConnected;
	}
	
	protected double getWattsBeingConnectedForDisplay(double wattsBeingConnected,
			Long srcNodeId, Power3Phase resultNodeSum) {
		if (includeSource && resultNodeSum != null && srcNodeId != null && srcNodeId > 0){
			wattsBeingConnected = resultNodeSum.wRatedA + resultNodeSum.wRatedB + resultNodeSum.wRatedC;
		}
		return wattsBeingConnected;
	}
	
	protected double getVaBeingConnectedForDisplay(double vaBeingConnected,
			Long srcNodeId, Power3Phase resultNodeSum) {
		if (includeSource && resultNodeSum != null && srcNodeId != null && srcNodeId > 0){
			vaBeingConnected = resultNodeSum.vaRatedA + resultNodeSum.vaRatedB + resultNodeSum.vaRatedC;
		}
		return vaBeingConnected;
	}

	/**
	 * Get the transformer phase value code for 3-phase calculation
	 * @param powerport
	 * @return
	 */
	protected abstract Long getTransformerPhaseValueCode(PowerPort port);

	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}

	protected double getCurrentAmpsAtNode(PowerPort powerPort) {
		double ampsOnNode = 0.0;
		Power3Phase objElec =  powerCalc.get3PhaseNodeSum(powerPort, 0, null, null, null);
		
		if (objElec != null){
			ampsOnNode = max(objElec.currentRatedA, objElec.currentRatedB, objElec.currentRatedC);
		}
		
		return ampsOnNode;
	}
	
	protected void setError(Errors errors, double ampsOrWattsBeingConnected,
			double ampsOrWattsOnNode, double rating, String portType, String itemName, String portName, String units, double ampsOrWattsPresentLoad) {
		Object[] errorArgs = new Object[] {
			portType,
			portName, 
			itemName,
			//ampsOrWattsOnNode - ampsOrWattsBeingConnected,
			ampsOrWattsPresentLoad,
			ampsOrWattsBeingConnected,
			ampsOrWattsOnNode,
			rating,
			ampsOrWattsOnNode - rating,
			units
		};
		
		errors.reject("powerProc.insufficientPower",errorArgs,"Insufficient Power");
	}
}
