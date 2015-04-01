package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.SavedItemData;

/**
 * @author prasanna
 *
 */
public class CabinetPlacementValidator implements Validator {

	@Autowired
	private CabinetHome cabinetHome;
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(CabinetItem.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>) target;
		
		CabinetItem cabinet = (CabinetItem) targetMap.get(errors.getObjectName());
		validateCabinetPlacementInfo(cabinet, errors);
	}
	
	private boolean isSiteChanged (Item origCabItem, Item cabinet) {
		DataCenterLocationDetails origLocation =  origCabItem.getDataCenterLocation();
		DataCenterLocationDetails newLocation = cabinet.getDataCenterLocation();
		if (origLocation != null && newLocation != null) {
			return !(origLocation.getDataCenterLocationId().equals(newLocation.getDataCenterLocationId()));
		}
		return false;
	}
	
	private boolean isRowPositionAvailable(CabinetItem cabinet) {
		int rowPos = cabinet.getPositionInRow();
		List<Integer> positions = cabinetHome.getCabinetPositionInRows(
				cabinet.getDataCenterLocation().getDataCenterLocationId(),
				cabinet.getRowLabel());
		return (rowPos > 0 && !positions.contains(rowPos)); 
	}
	
	private synchronized void validateCabinetPlacementInfo(CabinetItem cabinet, Errors errors) {
		final String rowLabel = cabinet.getRowLabel();
		final Integer rowPos = cabinet.getPositionInRow();
		
		// No row label specified for cabinet, okay to place with just location
		// no requirement to set both row and position. they both can be null
		if (!StringUtils.hasText(rowLabel) && (rowPos == null || rowPos == 0 )) return;  

		// report error when rowLabel is provided and positionInRow is null
		if (StringUtils.hasText(rowLabel) && (rowPos == null || rowPos == 0 )) {
			Object[] errorArgs = { cabinet.getItemName(), rowLabel };
			errors.rejectValue("cmbCabinet", "ItemValidator.NoPositionInRow", errorArgs, "The PositionInRow is not valid");
			return;
		}

		// report error when rowLabel is not provided but positionInRow is provided
		if (!StringUtils.hasText(rowLabel) && rowPos != null && (rowPos != 0 )) {
			Object[] errorArgs = { cabinet.getItemName() };
			errors.rejectValue("cmbCabinet", "ItemValidator.InvalidRowLabel", errorArgs, "The rowLabel is not valid");
			return;
		}
		
		// report error when value goes out of range 
		if (rowPos != null && (rowPos < 0 || rowPos > 99)) {
			Object[] errorArgs = {rowPos, cabinet.getItemName(), rowLabel };
			errors.rejectValue("cmbCabinet", "ItemValidator.InvalidPositionInRow", errorArgs, "The PositionInRow is not valid");
			return;
		}
		
		// Get available positions
		Collection<Long> rowPositions = null;
		Boolean rowPositionAvailable = true;
		if (cabinet.getItemId() > 0) {
			SavedItemData savedData = SavedItemData.getCurrentItem();
			CabinetItem origCabItem = (CabinetItem)savedData.getSavedItem();
			
			if (isSiteChanged (origCabItem, cabinet)) {
				rowPositionAvailable = isRowPositionAvailable(cabinet);
			}
			else {
				if (isRowLabelChanged(origCabItem, cabinet)) {
					// If row label changed, get available row positions for the new row label
					rowPositions = getCabinetPositionsInRow(cabinet, rowLabel, null); //rowPos != null ? rowPos.longValue() : null);
				}
				else {
					if (!isRowPositionChanged(origCabItem, cabinet)) return;
					// If row label is saved, get the saved available positions
					rowPositions = savedData.getAvailablePositions();
				}
			}
		}
		else {
			rowPositions = getCabinetPositionsInRow(cabinet, rowLabel, null);
		}
		
		// Specified row number is not available
		if (rowPositionAvailable == false || (rowPos > 0 && rowPositions != null && rowPositions.contains( Long.valueOf(rowPos)) )) {
			errors.reject("ItemValidator.noAvailableRowPosition", null, "The Cabinet placement information is not valid");
		}
	}
	
	private boolean isRowLabelChanged(CabinetItem origItem, CabinetItem item) {
		final String origLabel = origItem.getRowLabel() != null ? origItem.getRowLabel() : "";
		final String curLabel = item.getRowLabel() != null ? item.getRowLabel() : "";
		return !origLabel.equals( curLabel );
	}
	
	private boolean isRowPositionChanged(CabinetItem origItem, CabinetItem item) {
		return (origItem.getPositionInRow() != item.getPositionInRow()); // No change
	}
	
	private List<Long> getCabinetPositionsInRow(CabinetItem cabinet, String rowLabel, Long exceptionIndex) {
		if(cabinet == null || cabinet.getDataCenterLocation() == null) return new ArrayList<Long>();
		
		List<Integer> tmp = cabinetHome.getCabinetPositionInRows(cabinet.getDataCenterLocation().getDataCenterLocationId(), rowLabel);
		List<Long> rowPositions = new ArrayList<Long>( tmp.size() );
		
		for (Integer i : tmp) {
			rowPositions.add( i.longValue() );
		}
		
		if (exceptionIndex != null && exceptionIndex > 0) {
			rowPositions.remove( exceptionIndex );
		}
		
		return rowPositions;
	}
}
