package com.raritan.tdz.cabinet.home;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dto.ValueIdDTO;

/**
 * Home interface for Cabinet specific operations.
 * 
 * @author Andrew Cohen
 */
@Transactional
public interface CabinetHome {
	
	/**
	 * Returns a list of all cabinets in the specified site/location.
	 * @param locationId
	 * @return List of cabinets
	 */
	public List<ValueIdDTO> getAllCabinets(Long locationId);
	
	/**
	 * Gets all existing Row Labels for the selected site/location.
	 * @param locationId the site/location ID
	 * @return List of row labels
	 */
	public List<String> getCabinetRowLabels(Long locationId);
	
	/**
	 * Gets all USED row positions for the specified row label and site/location.
	 * @param locationId the site/location ID
	 * @param rowLabel the row label
	 * @return A list of row positions
	 */
	public List<Integer> getCabinetPositionInRows(Long locationId, String rowLabel);
}
