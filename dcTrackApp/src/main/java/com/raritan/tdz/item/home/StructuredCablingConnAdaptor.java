package com.raritan.tdz.item.home;

import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Handles conversion of structured cabling connection DTOs to structured cabling connection Domain objects and vice versa.
 * @author KC
 */
public class StructuredCablingConnAdaptor {
	
	
	
	private static StructureCableDTO convertStructuredCableDTOToDomain() throws DataAccessException {		

		return null;
	}

	/**
	 * Converts a structured cabling connection DTO to a structured cabling connection domain object.	 
	 */
	public static StructureCableDTO adaptStructuredCablingDTOToDomain() throws DataAccessException {				
		StructureCableDTO structuredCabling = new StructureCableDTO();
		convertStructuredCableDTOToDomain();

		return structuredCabling;
	}						
}
