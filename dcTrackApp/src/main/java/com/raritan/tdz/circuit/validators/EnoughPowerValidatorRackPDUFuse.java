/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
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
public class EnoughPowerValidatorRackPDUFuse extends AbstractEnoughPowerValidator {
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
		validateEnoughPowerParamsPort(powerFactor, destNodeId, SystemLookup.PortSubClass.RACK_PDU_OUTPUT, errors);
	
		
		
		PowerPort outputPort = powerPortDAO.read(destNodeId);
		
		//If there is no fuse, we do'nt need to validate
		if (outputPort.getFuseLookup() == null){
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE);
		}
		
		PowerPort inputPort = outputPort.getInputCordPort();
		if (inputPort == null){
			Object[] errorArgs = new Object[]{ outputPort.getPortName(), outputPort.getItem().getItemName() };
			errors.reject("powerProc.inputCordInvalid",errorArgs,"Input Cord not available");
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		} 
		
		
		Power3Phase objElec =  powerCalc.get3PhaseNodeSum(inputPort, ampsBeingConnected, nodeInfo, psPortIdToExclude, outputPort.getFuseLookup().getLkuId());
		
	    double ampsOnNode = ampsBeingConnected;
	    
	    double maxAmps = outputPort.getAmpsBudget();
	    
		//calculate against the source if user asked to include source port into the calculation
		Power3Phase resultNodeSum = calculatePhaseNodeSumForSourcePort(srcNodeId, destNodeId, (Power3Phase)firstNodeSum, objElec, nodeInfo);

	    if (objElec != null)
	     ampsOnNode = max(objElec.currentRatedA, objElec.currentRatedB, objElec.currentRatedC);

	   
	    if((ampsOnNode - maxAmps) > 0){
			String itemName = outputPort.getItem() != null &&  outputPort.getItem().getItemName() != null ? outputPort.getItem().getItemName() : "<Unknown> Item";
			String portName = ", " + (outputPort.getPortName() != null ? outputPort.getPortName() : "<Unknown> Port");
			setError(errors, ampsBeingConnected, ampsOnNode + ampsBeingConnected, maxAmps, "Rack PDU Fuse", itemName, portName, "Amps", objElec == null ? 0 : objElec.presentAmpsLoad);
	    		
	    	return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
	    	
	    }
		    
		
		return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE, resultNodeSum);
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.AbstractEnoughPowerValidator#getTransformerPhaseValueCode(java.lang.Long)
	 */
	@Override
	protected Long getTransformerPhaseValueCode(PowerPort port) {
		return null;
	}
	
	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}

}
