/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.PowerConnection;

/**
 * This validates if a given node in the circuit has enough power
 * for the given load/amps. Uses Chain-Of-Command Design pattern (slightly modified version of it)
 * @author prasanna
 *
 */
public interface EnoughPowerValidator {
	/**
	 * Checks to see if there is enough power, if it does not then it will provide appropriate errors in the Errors object
	 * <p>This will take in the powerSupplyPortId and the PowerConnection to which the power is calculated.</p>
	 * @param psPortId
	 * @param psPortToExclude TODO
	 * @param powerConn
	 * @param includeFirstNodeSum This indicates that we must include source port info in the calculation in addition to destination Port
	 * @param firstNodeSum TODO
	 * @param errors
	 * @param nodeInfo TODO
	 * @return TODO
	 */
	public EnoughPowerResult checkEnoughPower(Long psPortId, Long psPortToExclude, PowerConnection powerConn, boolean includeFirstNodeSum, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo);
	
	/**
	 * Checks to see if there is enough power, if it does not then it will provide appropriate errors in the Errors object
	 * <p>This will take in the power values and the circuitNodeId to which the power is calculated.</p>
	 * @param ampsBeingConnected
	 * @param wattsBeingConnected
	 * @param powerFactor
	 * @param srcNodeId TODO
	 * @param destNodeId
	 * @param psPortIdToExclude 
	 * @param psPortItemName 
	 * @param positionInCircuit
	 * @param firstNodeSum TODO
	 * @param errors
	 * @param nodeInfo TODO
	 * @return TODO
	 */
	public EnoughPowerResult checkEnoughPower(double ampsBeingConnected, 
								 long wattsBeingConnected, 
								 double powerFactor, 
								 Long srcNodeId, 
								 Long destNodeId, 
								 Long psPortIdToExclude, 
								 String psPortItemName, 
								 int positionInCircuit, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo);
}
