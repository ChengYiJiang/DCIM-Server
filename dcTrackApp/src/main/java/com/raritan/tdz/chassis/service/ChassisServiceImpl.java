package com.raritan.tdz.chassis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dto.BladeDTO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.lookup.SystemLookup;

public class ChassisServiceImpl implements ChassisService {
	
	private ChassisHome chassisHome;

	//<-- constructor
	public ChassisServiceImpl(ChassisHome chassisHome) {
		this.chassisHome = chassisHome;
	}
	
	//<-- service api's
/*	@Override
	public Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId, String bladeFormFactorStr, long faceLksValueCode, long bladeId)
		 throws ServiceLayerException
	{
		return this.chassisHome.getAvailableSlotPositions(chassisId, bladeFormFactorStr, faceLksValueCode, bladeId);
	}
*/

	@Override
	public Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId, long bladeModelId, long faceLksValueCode, long bladeId)
		 throws ServiceLayerException
	{
		return this.chassisHome.getAvailableSlotPositions(chassisId, bladeModelId, faceLksValueCode, bladeId);
	}
	
	@Override
	public BladeDTO getBladeItemDetails(long bladeItemId) throws ServiceLayerException {
		ItItem blade = chassisHome.getItItemDomainObject(bladeItemId);
		if (null == blade.getBladeChassis() || 
				(blade.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE && 
				blade.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE_SERVER) ) {
			return null;
		}

		BladeDTO dto = new BladeDTO();
		long facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
		if (null != blade.getFacingLookup()) {
			facingLkpValueCode = blade.getFacingLookup().getLkpValueCode();
		}
		else {
			long bladeRailsUsedLkpValueCode = -1;
			if (null != blade.getMountedRailLookup()) {
				bladeRailsUsedLkpValueCode = blade.getMountedRailLookup().getLkpValueCode();
			}
			/* The facing information for the blade is not updated in the database. 
			 * If updated use the  blade.getFacingLookup().getLkpValueCode() */
			if (SystemLookup.RailsUsed.FRONT == bladeRailsUsedLkpValueCode || 
				SystemLookup.RailsUsed.BOTH == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
			}
			else if (SystemLookup.RailsUsed.REAR == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.REAR;
			}
		}
		/* TODO:: Use dto.setFaceLksId(blade.getFacingLookup().getLksId());*/
		dto.setItemId(blade.getItemId());
		dto.setItemName(blade.getItemName());
		dto.setFaceLkpValueCode(facingLkpValueCode);
		dto.setModelId(blade.getModel().getModelDetailId());
		dto.setModelName(blade.getModel().getModelName());
		dto.setFormFactor(blade.getModel().getFormFactor());
		dto.setDimD(blade.getModel().getDimD());
		dto.setDimH(blade.getModel().getDimH());
		dto.setDimW(blade.getModel().getDimW());
		dto.setWeight(blade.getModel().getWeight());
		dto.setSerialNumber(blade.getItemServiceDetails().getSerialNumber());
		dto.setAssetNumber(blade.getItemServiceDetails().getAssetNumber());
		dto.setRaritanAssetTag(blade.getRaritanAssetTag());
		dto.setRuHeight(blade.getModel().getRuHeight());
		dto.setMounting(blade.getModel().getMounting());
		dto.setItemClass(blade.getClassLookup().getLkpValueCode());
		dto.setItemClassName(blade.getClassLookup().getLkpValue());
		dto.setItemSubClass(blade.getSubclassLookup().getLkpValueCode());
		dto.setItemSubClassName(blade.getSubclassLookup().getLkpValue());
		dto.setItemStatus(blade.getStatusLookup().getLkpValueCode());
		dto.setItemStatusStr(blade.getStatusLookup().getLkpValue());
		dto.setMakeId(blade.getModel().getModelMfrDetails().getModelMfrDetailId());
		dto.setMakeName(blade.getModel().getModelMfrDetails().getMfrName());
		dto.setChassisId(blade.getBladeChassis().getItemId());
		dto.setChassisName(blade.getBladeChassis().getItemName());
		
		ItItem itItem = chassisHome.getItItemDomainObject(blade.getBladeChassis().getItemId());
		ModelChassis modelChassis = null;
		if (null != itItem) {
			modelChassis = chassisHome.getBladeChassis(itItem.getModel().getModelDetailId(), facingLkpValueCode);
		}
		
		Map<Long, String> sortedSlotNumbers = chassisHome.getSortedSlotNumber(modelChassis, blade);
		int anchorSlot = chassisHome.getAnchorSlot(modelChassis, blade);
		if (anchorSlot > 0) {
			dto.setAnchorSlot(anchorSlot);
		}
		dto.setSlotNumbers(sortedSlotNumbers);
		return dto;
	}
	
	@Override
	public List<BladeDTO> getAllBladeItem(long itemId)
			throws ServiceLayerException {
		Collection<ItItem> blades = this.chassisHome.getAllBladesForItem(itemId);
		List<BladeDTO> dtos = new ArrayList<BladeDTO>();
		for (ItItem blade: blades) {
			BladeDTO dto = getBladeItemDetails(blade.getItemId());
			if (null != dto) {
				dtos.add(dto);
			}
		}
		return dtos;
	}
	
	@Override
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode) {
		Collection<ItItem> chassisItem = chassisHome.getAllChassisInCabinet(cabinetItemId, bladeTypeLkpValueCode);
		List<ValueIdDTO> dtos = new ArrayList<ValueIdDTO>();
		// Sort the cabinet list
		for (ItItem chassis: chassisItem) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setData(chassis.getItemId());
			dto.setLabel(chassis.getItemName());
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeModelId, long bladeId) throws ServiceLayerException {
		List<ValueIdDTO> chassisItem = chassisHome.getAllChassisInCabinet(cabinetItemId, bladeModelId, bladeId);
		return chassisItem;
	}
	
	@Override
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(long locationId, long bladeModelId, long bladeId) throws ServiceLayerException {
		return chassisHome.getAllAvailableCabinetForBladeModel(locationId, bladeModelId, bladeId);
	}

	@Override
	public ChassisItemDTO getChassisInfo(long chassisItemId, long faceLksValueCode)  throws ServiceLayerException {
		return chassisHome.getChassisInfo(chassisItemId, faceLksValueCode);
	}
	
	@Override
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId, Long faceLksValueCode) throws ServiceLayerException {
		return chassisHome.getChassisSlotDetails(chassisId, faceLksValueCode);
	}
	
	private BladeDTO getFirstBladeModelForMake(List<ModelDetails> bladeModelListForMake) {
		BladeDTO dto = null;
		if (null != bladeModelListForMake && bladeModelListForMake.size() > 0) {
			ModelDetails model =  bladeModelListForMake.get(0);
			dto = new BladeDTO();
			dto.setItemId(-1);
			dto.setItemName(null);
			dto.setFaceLkpValueCode(-1);
			dto.setModelId(model.getModelDetailId());
			dto.setModelName(model.getModelName());
			dto.setFormFactor(model.getFormFactor());
			dto.setDimD(model.getDimD());
			dto.setDimH(model.getDimH());
			dto.setDimW(model.getDimW());
			dto.setWeight(model.getWeight());
			dto.setSerialNumber(null);
			dto.setAssetNumber(null);
			dto.setRaritanAssetTag(null);
			dto.setRuHeight(model.getRuHeight());
			dto.setMounting(model.getMounting());
			dto.setItemClass(model.getClassLookup().getLkpValueCode());
			dto.setItemClassName(model.getClassLookup().getLkpValue());
			long subClass = SystemLookup.SubClass.BLADE_SERVER;
			String subClassName = SystemLookup.SubClass.BLADE_SERVER_DESC;
			if (model.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK) {
				subClass = SystemLookup.SubClass.BLADE;
				subClassName = SystemLookup.SubClass.BLADE_DESC;
			}
			dto.setItemSubClass(subClass);
			dto.setItemSubClassName(subClassName);
			dto.setItemStatus(SystemLookup.ItemStatus.PLANNED);
			dto.setItemStatusStr(SystemLookup.ItemStatus.NEW_DESC);
			dto.setMakeId(model.getModelMfrDetails().getModelMfrDetailId());
			dto.setMakeName(model.getModelMfrDetails().getMfrName());
			dto.setAnchorSlot(-1);
			Map<Long, String> slotNumbers = new HashMap<Long, String>();
			dto.setSlotNumbers(slotNumbers);
		}
		return dto;
	}
	
	@Override
	public BladeDTO getFirstBladeModelForMake(long makeId)  throws ServiceLayerException {
		List<ModelDetails> bladeModelListForMake = chassisHome.getAllBladeModelForMake(makeId);
		return getFirstBladeModelForMake(bladeModelListForMake);
	}

	@Override
	public BladeDTO getFirstBladeModelForMake(long makeId, long classLkpValueCode)  throws ServiceLayerException {
		List<ModelDetails> bladeModelListForMake = chassisHome.getAllBladeModelForMake(makeId, classLkpValueCode);
		return getFirstBladeModelForMake(bladeModelListForMake);
	}
	
	@Override
	public boolean isChassisRearDefined(long chassisItemId) throws ServiceLayerException {
		return chassisHome.isChassisRearDefined(chassisItemId);
	}

	@Override
	public boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId) throws ServiceLayerException {
		return chassisHome.isBladeAllowedinChassisRear(chassisItemId, bladeModelId, bladeItemId);
	}

	@Override
	public boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId) throws ServiceLayerException {
		return chassisHome.isBladeAllowedinChassisFront(chassisItemId, bladeModelId, bladeItemId);
	}

}
