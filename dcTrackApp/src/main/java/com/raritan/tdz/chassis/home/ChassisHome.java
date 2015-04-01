package com.raritan.tdz.chassis.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.chassis.home.ChassisHomeImpl.SlotReAssignmentInfo;
import com.raritan.tdz.chassis.home.ChassisHomeImpl.SlotReAssignmentInfoFrontBack;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.domain.ModelChassisSlot;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;

@Transactional
public interface ChassisHome {

	//<-- Chassis API
	@Transactional
	public List<ModelChassis> getAllBladeChassis() throws DataAccessException;

	@Transactional
	public void updateChassisLayout(long chassisId) throws DataAccessException;

	@Transactional
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId)
			throws DataAccessException;

	@Transactional(readOnly = true)
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId,
			Long mountedRailLksValueCode) throws DataAccessException;

	@Transactional
	public void updateAllChassisLayout() throws DataAccessException;

	public ArrayList<ModelChassisSlot> getSlotNumbers(
			ModelChassis modelChassis, Item blade);

	@Transactional
	public ItItem getItItemDomainObject(Long chassisId);

	@Transactional
	public Collection<ItItem> getAllChassisInCabinet(long cabinetItemId,
			long bladeTypeLkpValueCode);
	
	@Transactional
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId,  
			long bladeModelId, long bladeId) throws DataAccessException;

	@Transactional(readOnly = true)
	public ChassisItemDTO getChassisInfo(long chassisItemId,
			long faceLksValueCode) throws ServiceLayerException;

	@Transactional(readOnly = true)
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId,
			Long faceLksValueCode) throws DataAccessException,
			BusinessValidationException;

	@Transactional
	public Collection<ItItem> getAllBladesForItem(long itemId)
			throws DataAccessException;
	
	@Transactional
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(
			long locationId, long bladeModelId, long bladeId)
					throws DataAccessException;

	@Transactional
	public ModelChassis getBladeChassis(Long model_id, long faceLksId)
			throws DataAccessException; 

	//public void updateBladeDefinition(long bladeModelId)
	//		throws DataAccessException;
	
	@Transactional
	public void updateModelDefinition(long modelId)
			throws DataAccessException;

	long getBladeFaceLookUpLks(ItItem blade);

	List<ModelDetails> getAllBladeModelForMake(long makeId);
	
	public List<ModelDetails> getAllBladeModelForMake(long makeId,
			long classLkpValueCode);

	boolean isChassisRearDefined(long chassisItemId)
			throws DataAccessException;

	void updateBladeFacing(long bladeItemId);

	int getAnchorSlot(ModelChassis modelChassis, ItItem blade);

	Map<Long, String> getSortedSlotNumber(ModelChassis modelChassis,
			ItItem blade);

	@Transactional
	Map<Long, String> getAvailableSlotPositions(Long chassisId,
			String bladeFormFactorStr, long bladeClassLksValueCode,
			long faceLksValueCode, long bladeId) throws DataAccessException;

	@Transactional
	Map<Long, String> getAvailableSlotPositions(Long chassisId,
			long bladeModelId, long faceLksValueCode, long bladeId)
			throws DataAccessException;

	public SlotReAssignmentInfo validateChassisModelChange(long itemId,
			long modelDetailId) throws DataAccessException;

	@Transactional
	public SlotReAssignmentInfoFrontBack updateSlotNumberForBladesInChassis(
			long chassisId) throws DataAccessException;

	@Transactional
	void updateChassisGroupName(long chassisId) throws DataAccessException;

	@Transactional
	void updateBladeGroupName(long bladeItemId, String groupName);

	void updateAllChassisGroupName() throws DataAccessException;

	boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId)
			throws DataAccessException;

	boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId)
			throws DataAccessException;

	@Transactional
	void updateCabinetAndLocationForBladesInChassis(long chassisItemId);

	public String getBladeSlotLabel(ItItem bladeItem) throws DataAccessException, BusinessValidationException;

	public Integer getChassisSlotNumber(Long chassisId, long chassisFace, String slotLabel)  throws DataAccessException, BusinessValidationException;

	public Map<Long, String> getSortedSlotNumber(Long bladeId) throws DataAccessException;

	/**
	 * get the slot label for a chassis for a given chassis face and slot number
	 * @param chassisId
	 * @param chassisFace
	 * @param slotNumber
	 * @return
	 * @throws DataAccessException
	 * @throws BusinessValidationException
	 */
	public String getChassisSlotLabel(Long chassisId, long chassisFace, Integer slotNumber) throws DataAccessException, BusinessValidationException;


}
