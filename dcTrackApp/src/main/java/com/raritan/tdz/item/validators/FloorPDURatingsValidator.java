package com.raritan.tdz.item.validators;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.GlobalUtils;

public class FloorPDURatingsValidator implements Validator {

	@Autowired
	PowerPortDAO powerPortDAO;
	
	@Autowired
	private ItemDAO itemDAO;

	@Autowired
	SystemLookupFinderDAO systemLookupDAO;
	
	final static long maxRatingVal = 99999L;
	@Override
	public boolean supports(Class<?> clazz) {
		return MeItem.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object item = targetMap.get(errors.getObjectName());
		
		validateUPSBankVolts( item, errors );
		
		// validateFPDUToUPSBankRatings( item, errors );
		
		validateFPDOutputWiring( item, errors );
		
		// validateFPDUToFPDURatings(item, errors );
		
		validateFPDUToOutputWiring( item, errors );
		
		validateKvaVsCalculatedWatt( item, errors );
		
		validateVoltageChange( item, errors );
		
	}
	
	private void validateVoltageChange(Object item, Errors errors) {

		MeItem fpdu = (MeItem) item;

		Assert.isTrue(fpdu != null);
		
		// voltage validation is performed only if the fpdu have panels 
		if (itemDAO.getNumOfChildren(fpdu) <= 0) return;
		
		MeItem upsBank = fpdu.getUpsBankItem();
		Long breakerPortId = fpdu.getBreakerPortId();
		
		if( upsBank != null && breakerPortId == null) {
			if (upsBank.getRatingV() < fpdu.getLineVolts()) {
				Object[] errorArgs = { "UPS Bank" };
				errors.rejectValue("PowerChain", "PowerChain.voltageLessThanExistingVolt", errorArgs, "The voltage rating of the selected UPS must be at an equal or greater value than the current Input Voltage (Vac) setting.");
			}
		}
		
		else if ( breakerPortId != null ) {
			
			PowerPort port = null;
			try {
				
				port = powerPortDAO.loadPort(breakerPortId);
				if ((new Double(port.getVoltsLookup().getLkpValue())).doubleValue() < fpdu.getLineVolts()) {
					Object[] errorArgs = { "breaker" };
					errors.rejectValue("PowerChain", "PowerChain.voltageLessThanExistingVolt", errorArgs, "The voltage rating of the selected breaker must be at an equal or greater value than the current Input Voltage (Vac) setting.");
				}
				
			} catch (DataAccessException e) {
				
				errors.rejectValue("PowerChain", "PowerChain.circuitBreakerPortAbsent", null, "Cannot load the breaker port to validate the volts.");
			}
			
		}
		
		else {
			// skip voltage validation 
		}

	}

	private void validateUPSBankVolts(Object item, Errors errors) {
		MeItem fpdu = (MeItem)item;

		Assert.isTrue(fpdu != null);
		MeItem upsBank = fpdu.getUpsBankItem();

		if( upsBank == null ) return;
		
		List<LksData> voltsLksList = systemLookupDAO.findByLkpTypeNameAndLkpValue("VOLTS", 
				new Long(upsBank.getRatingV()).toString());
		if( voltsLksList == null || voltsLksList.size() == 0 ){
			errors.rejectValue("PowerChain", "PowerChain.invalidVolts", null, "Selected UPS Bank has invalid Volts.");
		}

	}

	private void validateFPDOutputWiring(Object itemObj, Errors errors) {
		MeItem fpdu = (MeItem)itemObj;
		
		if( fpdu.getPhaseLookup() == null ) return;
		Assert.isTrue( fpdu.getPhaseLookup().getLkpValueCode() != null);
		
		if( fpdu.getPhaseLookup().getLkpValueCode().longValue() != SystemLookup.PhaseIdClass.THREE_DELTA && 
				fpdu.getPhaseLookup().getLkpValueCode().longValue() != SystemLookup.PhaseIdClass.THREE_WYE ){
			errors.rejectValue("PowerChain", "PowerChain.invalidOutputWiring", null, "Invalid Output Witing.");
		}

	}

	@SuppressWarnings("unused")
	private void validateFPDUToUPSBankRatings( Object itemObj, Errors errors ){

		MeItem fpdu = (MeItem)itemObj;

		Assert.isTrue(fpdu != null);
		MeItem upsBank = fpdu.getUpsBankItem();

		if( upsBank == null ) return;

		boolean validAmps = false;
		
		if( upsBank.getRatingKva() <= maxRatingVal && upsBank.getRatingAmps() <= maxRatingVal ){
			List<UPSBankDTO> validUPSBanks = itemDAO.getAllUPSBanksMatchingRating(fpdu.getRatingAmps());
			for( UPSBankDTO dto : validUPSBanks ){
				if( dto.getUpsBankId() == upsBank.getItemId() ){
					validAmps = true; 
					break;
				}
			}
		}else{
			Object[] errorArgs = { maxRatingVal };
			errors.rejectValue("PowerChain", "PowerChain.floorPduMaxRating", errorArgs, "Rating is exceeding max value");			
		}

		if( !validAmps ){
			Object[] errorArgs = { fpdu.getItemName(), upsBank.getItemName()};
			errors.rejectValue("PowerChain", "PowerChain.floorPduRatingMoreThanUpsBankRating", errorArgs, "Cannot create connection between Floor PDU and UPS Bank because Floor PDU rating is higher than UPS Bank rating.");
		}
	}

	@SuppressWarnings("unused")
	private void validateFPDUToFPDURatings( Object itemObj, Errors errors ) {
		MeItem fpdu = (MeItem)itemObj;
		Assert.isTrue(fpdu != null);
		
		Long breakerPortId = fpdu.getBreakerPortId();
		if (breakerPortId != null && breakerPortId > 0) {
			try {
				PowerPort port = powerPortDAO.loadPort(breakerPortId);
				if (fpdu.getRatingAmps() > port.getAmpsNameplate()) {
					Object[] errorArgs = { fpdu.getItemName(), port.getPortName()};
					errors.rejectValue("PowerChain", 
							"PowerChain.floorPduRatingMoreThanBranchCircuitBreakerRating", 
							errorArgs, 
							"Cannot connect FPDU to Branch circuit breaker port because FPDU rating is greater than Branch circuit breaker");
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void validatePhaseWiring( String dstConnName, LksData phaseLookup, MeItem fpdu, Errors errors ) {
		
		if ( null == phaseLookup || null == phaseLookup.getLkpValueCode() ) return;
		
		if ( phaseLookup.getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_WYE ) {
			
			Long numOfChildrenWithDeltaPhase = itemDAO.getNumOfChildWithDeltaPhase(fpdu.getItemId());
			
			if (numOfChildrenWithDeltaPhase > 0) {
				Object[] errorArgs = { dstConnName };
				errors.rejectValue("PowerChain", 
						"PowerChain.cannotConnectWyeToDelta", 
						errorArgs, 
						"Cannot connect 3 wire output panels to 4-wire input.");
			}
			
			/*Hibernate.initialize(fpdu.getChildItems());
			Set<Item> panels = fpdu.getChildItems();
			for (Item childItem: panels) {
				if (childItem instanceof MeItem) {
					MeItem panel = (MeItem) childItem;
					if (panel.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_DELTA ) {
						Object[] errorArgs = { dstConnName };
						errors.rejectValue("PowerChain", 
								"PowerChain.cannotConnectWyeToDelta", 
								errorArgs, 
								"Cannot connect 3 wire output panels to 4-wire input.");
						return;
					}
				}
			}*/
			
		}
		
	}
	
	private void validateFPDUToOutputWiring( Object itemObj, Errors errors ) {
		MeItem fpdu = (MeItem)itemObj;
		Assert.isTrue(fpdu != null);
		
		Long breakerPortId = fpdu.getBreakerPortId();
		if (breakerPortId != null && breakerPortId > 0) {
			try {
				PowerPort port = powerPortDAO.loadPort(breakerPortId);

				validatePhaseWiring(port.getPortName(), port.getPhaseLookup(), fpdu, errors);
				
			}
			catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
			MeItem upsBank = fpdu.getUpsBankItem();

			if( upsBank == null ) return;
			
			validatePhaseWiring(upsBank.getItemName(), upsBank.getPhaseLookup(), fpdu, errors);

		}
		
	}

	private void validateKvaVsCalculatedWatt( Object itemObj, Errors errors ) {
		
		MeItem fpdu = (MeItem)itemObj;
		Assert.isTrue(fpdu != null);
		Long ampsVal = fpdu.getRatingAmps();
		Double kvaVal = Double.valueOf(fpdu.getRatingKva());

		if (fpdu.getItemId() > 0) {
			SavedItemData savedData = SavedItemData.getCurrentItem();
			MeItem origFpduItem = (MeItem) savedData.getSavedItem();
			// Do not validate if the rated amps and kva is not changed
			if (fpdu.getRatingAmps() == origFpduItem.getRatingAmps() && fpdu.getRatingKva() == origFpduItem.getRatingKva()) {
				return;
			}
		}
		
		if (fpdu.getSkipValidation() != null && fpdu.getSkipValidation()) {
			return;
		}
		
		Long voltVal = null;
		Double calculatedWatt = 0.0;
		
		Long breakerPortId = fpdu.getBreakerPortId();
		if (breakerPortId != null && breakerPortId > 0) {
			
			LksData voltLks = (LksData) powerPortDAO.loadPortField(breakerPortId, "voltsLookup");
			String volt = voltLks.getLkpValue();
			
			if (GlobalUtils.isNumeric(volt)) {
				voltVal = Long.valueOf(volt);
				calculatedWatt = ampsVal * voltVal * Math.sqrt(3) * 0.8;
			}

			validateDiffKvaVsCalculatedWatt(calculatedWatt, kvaVal, errors);
				
		}
		else {
			
			MeItem upsBank = fpdu.getUpsBankItem();
			
			if( upsBank == null ) return;
			
			voltVal = upsBank.getRatingV();
			
			calculatedWatt = ampsVal * voltVal * Math.sqrt(3) * 0.8;
			
			validateDiffKvaVsCalculatedWatt(calculatedWatt, kvaVal, errors);

		}
		
	}
	
	private void validateDiffKvaVsCalculatedWatt(Double calculatedWatt, Double kvaVal, Errors errors) {
		// ItemValidator.floorPduKvaVsCalculatedWatt
		kvaVal *= 1000.0;
		Double lowerNum = kvaVal;
		Double highNum = calculatedWatt;
		if (calculatedWatt < kvaVal) {
			lowerNum = calculatedWatt;
			highNum = kvaVal;
		}
		double percent = 100.0 * ((highNum - lowerNum) / highNum);
		if (percent > 15.0) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", 
					"ItemValidator.floorPduKvaVsCalculatedWatt", 
					errorArgs, 
					"The Floor PDU Power Rating (kVA) entered and its kVA capacity based on Floor PDU Main Breaker(A)rating are considerably different. The Floor PDU capacity is determined from the Main Breaker(A)rating specified. Do you want to continue?");
		}
		
	}

}
