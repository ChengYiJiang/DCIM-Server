package com.raritan.tdz.chassis.service;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dto.BladeDTO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;

public interface ChassisService {

	/**
	 * Get all slot positions available to place the blade in the chassis
	 * @param chassisId - chassis where the blade is to be placed
	 * @param bladeFormFactor - the blade form factor to be placed "Full" "Half" "Full-Double" "Quarter" "Half-Double"
	 * @param faceLksValueCode - the face lks value code for the chassis - FRONT = 20501 REAR = 20502
	 * @param bladeId - the blade being edited or -1 for the new item
	 * @return Map<Long, String> - map of slot# (key) and slot label  
	 * @throws ServiceLayerException 
	 */
	/*public Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId,
			String bladeFormFactorStr, long faceLksValueCode, long bladeId)
			throws ServiceLayerException;
	 */
	
	/**
	 * get the list of all blade items for a given chassis or 
	 * get the list of all blades from a chassis that the passed itemId is in
	 * @param itemId - chassis item id OR blade item id
	 * @return List<BladeDTO>
	 * 				BladeDTO will have following data
	 * 					- item id
	 * 					- item name
	 * 					- item class
	 * 					- position (slot #s)
	 * 					- facing
	 * 					- form factor
	 * 					- item status
	 * @throws ServiceLayerException
	 */
	public List<BladeDTO> getAllBladeItem(long itemId) throws ServiceLayerException;

	/**
	 * get all the chassis item in the cabinet's front or rear
	 * @param cabinetItemId - item id of the cabinet
	 * @param bladeTypeLkpValueCode - the blade type class lkp value code. Eg: 1200 - Device, 1300 - Network
	 * @return List of ValueIdDTO with the chassis item id and chassis name
	 */
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode);

	/**
	 * get all the chassis item in the cabinet's front or rear
	 * @param cabinetItemId - item id of the cabinet
	 * @param bladeModelId - blade model to be placed on this chassis
	 * @param bladeId - blade that is current being edited or -1 for new item
	 * @return List of ValueIdDTO with the chassis item id and chassis name
	 * @throws ServiceLayerException 
	 */
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeModelId, long bladeId) throws ServiceLayerException;
	
	public ChassisItemDTO getChassisInfo(long chassisItemId, long faceLksValueCode)  throws ServiceLayerException;
	
	/**
	 * Get a list of ChassisSlotsDTO for a given Chassis item.
	 * @param chassisId
	 * @param faceLksValueCode - chassis face, FRONT = 20501 and REAR = 20502
	 * @return List<ChassisSlotDTO>
	 * @throws ServiceLayerException
	 * 
	 * reports business validation error which must be checked by client. 
	 */
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId, Long faceLksValueCode) throws ServiceLayerException;

	/**
	 * get the list of all cabinet that can hold a given blade model (form factor, blade type - N/W, Device). 
	 * Also if trying to edit a blade id that is already been placed in a cabinet, then pass the blade id
	 * @param locationId -site id where the blade is to be placed
	 * @param bladeModelId - the model id of the blade that is to be placed
	 * @param bladeId - if trying to edit the placement for a given blade then pass the exception blade id 
	 * 					that should be included in the cabinet list
	 * @return list of cabinet id and the respective cabinet name
	 * @throws ServiceLayerException 
	 */
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(long locationId, long bladeModelId, long bladeId) throws ServiceLayerException;

	/**
	 * get the blade dto for a blade item
	 * @param bladeItemId
	 * @return BladeDTO
	 * @throws ServiceLayerException 
	 */
	public BladeDTO getBladeItemDetails(long bladeItemId) throws ServiceLayerException;

	/**
	 * get the first blade model for a given make. First here mean the model that have lowest model_id
	 * @param makeId
	 * @return first blade's BladeDTO for a given make
	 */
	public BladeDTO getFirstBladeModelForMake(long makeId) throws ServiceLayerException;

	/**
	 * informs if the rear of the chassis is defined
	 * @param chassisItemId
	 * @return boolean, true if the chassis rear is defined, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isChassisRearDefined(long chassisItemId)  throws ServiceLayerException;


	/**
	 * Get all slot positions available to place the blade in the chassis
	 * @param chassisId - chassis where the blade is to be placed
	 * @param bladeModelId - the blade model id to be placed
	 * @param faceLksValueCode - the face lks value code for the chassis - FRONT = 20501 REAR = 20502
	 * @param bladeId - the blade being edited or -1 for the new item
	 * @return Map<Long, String> - map of slot# (key) and slot label  
	 * @throws ServiceLayerException 
	 */
	Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId,
			long bladeModelId, long faceLksValueCode, long bladeId)
			throws ServiceLayerException;

	/**
	 * informs if a given blade model is allowed to be placed in the rear of the chassis 
	 * @param chassisItemId
	 * @param bladeModelId
	 * @param bladeItemId
	 * @return boolean, true if the blade is allowed in chassis rear, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId)
			throws ServiceLayerException;

	/**
	 * informs if a given blade model is allowed to be placed in the front of the chassis 
	 * @param chassisItemId
	 * @param bladeModelId
	 * @param bladeItemId
	 * @return boolean, true if the blade is allowed in chassis front, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId)
			throws ServiceLayerException;

	/**
	 * get the first blade model for a given make and class. First here mean the model that have lowest model_id 
	 * @param makeId
	 * @param classLkpValueCode
	 * @return BladeDTO
	 * @throws ServiceLayerException
	 */
	BladeDTO getFirstBladeModelForMake(long makeId, long classLkpValueCode)
			throws ServiceLayerException;

}
