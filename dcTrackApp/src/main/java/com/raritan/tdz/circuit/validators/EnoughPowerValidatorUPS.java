/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.circuit.util.PowerCalc;
import com.raritan.tdz.circuit.util.Power3Phase;

/**
 * @author prasanna
 *
 */
public class EnoughPowerValidatorUPS extends AbstractEnoughPowerValidator {
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	PowerCalc powerCalc;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidator#checkEnoughPower(java.lang.Long, java.lang.Long, int, org.springframework.validation.Errors)
	 */
	@Override
	public EnoughPowerResult checkEnoughPower(Long psPortId, Long psPortToExclude,
			PowerConnection powerConn, boolean includeFirstNodeSum, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo) {
		return (checkEnoughPowerUsingPort(psPortId, psPortToExclude, powerConn, errors, includeFirstNodeSum, firstNodeSum, nodeInfo));
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidator#checkEnoughPower(java.lang.Long, java.lang.Long, java.lang.Double, java.lang.Long, int, org.springframework.validation.Errors)
	 */
	@Override
	public EnoughPowerResult checkEnoughPower(double ampsBeingConnected,
			long wattsBeingConnected, double powerFactor, Long srcNodeId,
			Long destNodeId, Long psPortIdToExclude, String psPortItemName, int positionInCircuit, Object firstNodeSum, Errors errors, PowerWattUsedSummary nodeInfo) {
		validateEnoughPowerParamsPort(powerFactor, destNodeId, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, errors);
		
		PowerPort powerPort = powerPortDAO.read(destNodeId);
		
		Power3Phase objElec =  powerCalc.get3PhaseNodeSum(powerPort, ampsBeingConnected, nodeInfo, psPortIdToExclude, null);
		
		//calculate against the source if user asked to include source port into the calculation
		Power3Phase resultNodeSum = calculatePhaseNodeSumForSourcePort(srcNodeId, destNodeId, (Power3Phase)firstNodeSum, objElec, nodeInfo);
		
		double presentLoadkW = 0.0;
		double presentLoadkVA = 0.0;

		if (objElec != null){
			presentLoadkW = (objElec.wRatedA + objElec.wRatedB + objElec.wRatedC) / 1000.0;
			presentLoadkVA = (objElec.vaRatedA + objElec.vaRatedB + objElec.vaRatedC) / 1000.0;
		}

		PowerBankInfo bankInfo =  powerCircuitDAO.getPowerBankInfo(powerPort.getItem().getItemId());
		
		validateBankInfo(powerPort, errors, bankInfo);
		
		//If we have displayed watts error, the vaError will be the same except the units.
		boolean wattsErrorDisplayed = false;
		
		if (!errors.hasErrors() && bankInfo != null){
			double maxkVA=0, maxkW=0;

			if(bankInfo.getUnits() != null && bankInfo.getRedundancy() != null && bankInfo.getRating_kw() != null){
				if(bankInfo.getRedundancy().equals("N")){
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits();
				}
				else{
					int redun = Integer.valueOf(bankInfo.getRedundancy().substring(2));
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits() - bankInfo.getUnits() * redun;
				}
				
				if((presentLoadkW + (wattsBeingConnected / 1000.0)) > maxkW){
					Item upsBankItem = powerPort.getItem();
					wattsBeingConnected = (long) getWattsBeingConnectedForDisplay(wattsBeingConnected, srcNodeId, resultNodeSum);
					String site = upsBankItem.getDataCenterLocation() != null ? upsBankItem.getDataCenterLocation().getCode():"<UKNOWN SITE>";
					String itemName = powerPort.getItem() != null && powerPort.getItem().getItemName() != null ? powerPort.getItem().getItemName() : "<Unknown> Item";
					String portName = "";
					setError(errors, wattsBeingConnected/1000.0, presentLoadkW + (wattsBeingConnected/1000.0), maxkW, "UPS Bank", itemName, portName, "KW", presentLoadkW);
					wattsErrorDisplayed = true;
				}
			}

			if(bankInfo.getUnits() != null && bankInfo.getRedundancy() != null && bankInfo.getRating_kva() != null){
				if(bankInfo.getRedundancy().equals("N")){
					maxkVA = bankInfo.getRating_kva() * bankInfo.getUnits();
				}
				else{
					int redun = Integer.valueOf(bankInfo.getRedundancy().substring(2));
					maxkVA = bankInfo.getRating_kva() * bankInfo.getUnits() - bankInfo.getRating_kva() * redun;
				}
				
				double vaBeingConnected = wattsBeingConnected / powerFactor;
				if((presentLoadkVA + (vaBeingConnected / 1000.0)) > maxkVA){
					Item upsBankItem = powerPort.getItem();
					String site = upsBankItem.getDataCenterLocation() != null ? upsBankItem.getDataCenterLocation().getCode():"<UKNOWN SITE>";
					vaBeingConnected = getVaBeingConnectedForDisplay(vaBeingConnected, srcNodeId, resultNodeSum);
					
					if (!wattsErrorDisplayed){
						String itemName = powerPort.getItem() != null && powerPort.getItem().getItemName() != null ? powerPort.getItem().getItemName() : "<Unknown> Item";
						String portName = "";
						setError(errors, vaBeingConnected/1000.0, presentLoadkVA + (vaBeingConnected/1000.0), maxkVA, "UPS Bank", itemName, portName, "kVA", presentLoadkVA);
					}
				}
			}
		}
			
		
		if (errors.hasErrors()){
			return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NODE_ERROR);
		}
		
		return new EnoughPowerResult(EnoughPowerResult.PowerChainResultHandler.PROCESS_NEXT_POWER_NODE,resultNodeSum);
		
	}

	private void validateBankInfo(PowerPort powerPort, Errors errors,
			PowerBankInfo bankInfo) {
		
		if (bankInfo == null){
			
			Item upsBankItem = powerPort.getItem();
			Object[] errorsArgs = { upsBankItem.getItemName() };
			errors.reject("powerProc.upsBankInfoNotAvailable", errorsArgs, "UPS Bank info not available");
			return;
		}
		
		if (bankInfo.getUnits() == null){
			Item upsBankItem = (Item) powerPort.getItem();
			Object[] errorsArgs = { upsBankItem.getItemName() };
			errors.reject("powerProc.upsBankUnitsNotAvailable", errorsArgs, "UPS Bank Units info not available");
		}
		
		if (bankInfo.getRedundancy() == null){
			Item upsBankItem = powerPort.getItem();
			Object[] errorsArgs = { upsBankItem.getItemName() };
			errors.reject("powerProc.upsRedundancyNotAvailable", errorsArgs, "UPS Bank Redundancy info not available");
		} else if (!bankInfo.getRedundancy().matches("N|N\\+[1-9]+")){
			Item upsBankItem = powerPort.getItem();
			Object[] errorsArgs = { upsBankItem.getItemName(), bankInfo.getRedundancy() };
			errors.reject("powerProc.upsRedundancyInvalid", errorsArgs, "UPS Bank Redundancy info is invalid");
		}
		
		
		if (bankInfo.getRating_kw() == null && bankInfo.getRating_kva() == null){
			Item upsBankItem = powerPort.getItem();
			Object[] errorsArgs = { upsBankItem.getItemName() };
			errors.reject("powerProc.upsRatingNotAvailable", errorsArgs, "UPS Bank Rating info not available");
		}
	}

/* same as value in power port volts_lks_id
	@Override
	protected double getVolts(Long nodeId) {
		MeItem item = (MeItem) itemDAO.initializeAndUnproxy(itemDAO.getItem(nodeId));
		
		if (item != null){
			return item.getRatingV();
		}
		return 0;
	}
	*/
	@Override
	protected Long getTransformerPhaseValueCode(PowerPort port) {
		if (port != null && port.getPhaseLookup() != null){
			return port.getPhaseLookup().getLkpValueCode();
		}
		
		return null;
	}
}
