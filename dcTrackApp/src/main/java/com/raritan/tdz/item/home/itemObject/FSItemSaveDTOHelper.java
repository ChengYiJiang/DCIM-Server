/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * @author prasanna
 *
 */
public class FSItemSaveDTOHelper implements ItemSaveDTOHelper {
	@Autowired
	private ModelDAO modelDao;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDao;
	
	@Autowired
	private ItemDAO itemDao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	protected ItemStateContext itemStateContext;
	
	@Autowired
	protected SystemLookupFinderDAO systemLookupFinderDAO;
	
	private RulesProcessor rulesProcessor;
	
	private ItemDomainAdaptor itemDomainAdaptor;
	
	private RemoteRef remoteRef;
	
	private boolean modelCreated = false;
	
	private Map<String,Long> domainToItemIdMap = new HashMap<String, Long>();
	
	private Map<String,Map<String,UiComponentDTO>> saveResultMap = new HashMap<String, Map<String,UiComponentDTO>>();
	
	
	private Logger log = Logger.getLogger(getClass());
	
	public FSItemSaveDTOHelper(RulesProcessor rulesProcessor, RemoteRef remoteRef, ItemDomainAdaptor itemDomainAdaptor) {
		this.rulesProcessor = rulesProcessor;
		this.remoteRef = remoteRef;
		this.itemDomainAdaptor = itemDomainAdaptor;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveDTOHelper#splitValueIdDTO(java.util.List)
	 */
	@Override
	public Map<String, List<ValueIdDTO>> splitValueIdDTO(
			Long itemId, List<ValueIdDTO> valueIdDTOList, UserInfo sessionUser) throws Throwable {
		Map<String,List<ValueIdDTO>> splitDTOMap = new HashMap<String, List<ValueIdDTO>>();
		
		//Grab the DTOList for Cabinet
		splitDTOMap.put(CabinetItem.class.getName(), getCabinetDTOlist(valueIdDTOList, (ModelDetails)preInit(itemId, valueIdDTOList), itemId, sessionUser));
		
		//Grab the DTOList for Device/Network
		splitDTOMap.put(ItItem.class.getName(), getItemDTOlist(valueIdDTOList));
		
		return splitDTOMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveDTOHelper#mergeResults(java.util.Map)
	 */
	@Override
	public Map<String, UiComponentDTO> mergeResults() {
		Map<String, UiComponentDTO> retval = new HashMap<String, UiComponentDTO>();
		
		Map<String,UiComponentDTO> cabinetItemMap = saveResultMap.get(CabinetItem.class.getName());
		Map<String,UiComponentDTO> itemMap = saveResultMap.get(ItItem.class.getName());
		
		if (itemMap != null){
			if (cabinetItemMap != null){
				retval.putAll(cabinetItemMap);
				itemMap.remove("cmbLocation");
				itemMap.remove("cmbRowLabel");
				itemMap.remove("cmbRowPosition");
				itemMap.remove("tiLocationRef");
				itemMap.remove("radioFrontFaces");
				// itemMap.remove("cmbStatus");
			}
			
			retval.putAll(itemMap);
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveDTOHelper#preInit(java.util.List)
	 */
	@Override
	public Object preInit(Long itemId, List<ValueIdDTO> valueIdDTOList) throws Throwable {
		ModelDetails cabinetModel = null;
		ModelDetails itemModel = getModelDetails(itemId);
		if (itemModel != null) cabinetModel = getOrCreateCabinetModel(itemModel);
		return cabinetModel;
	}
	

	@Override
	public Long getItemId(String domainEntityClass) {
		return domainToItemIdMap.get(domainEntityClass);
	}
	
	@Override
	public void setSaveItemResult(String domainEntityClass,
			Map<String, UiComponentDTO> resultMap) {
		saveResultMap.put(domainEntityClass, resultMap);
	}

	@Override
	public void preSave(String domainEntityClass,
			List<ValueIdDTO> valueIdDTOList) throws Throwable {
		if (domainEntityClass.equals(ItItem.class.getName())){
			Map<String, UiComponentDTO> resultMap = saveResultMap.get(CabinetItem.class.getName());
			if (resultMap != null){
				//Get the cabinet Id
				Object cabinetId = resultMap.get("tiName").getUiValueIdField().getValueId();
				ValueIdDTO dto = new ValueIdDTO();
				dto.setLabel("cmbCabinet");
				dto.setData(cabinetId);
				valueIdDTOList.add(dto);
			}
		}
		
	}
	
	@Override
	public void setItemId(Long itemId) {
		Item existingItem = null;
		if (itemId != null && itemId > 0){
			existingItem = (Item) itemDao.loadItem(itemId);
			if (existingItem != null && existingItem.getParentItem() != null){
				domainToItemIdMap.put(CabinetItem.class.getName(),existingItem.getParentItem().getItemId());
				domainToItemIdMap.put(ItItem.class.getName(), itemId);
			} else {
				domainToItemIdMap.put(ItItem.class.getName(), itemId);
			}
		}
	}

	
	//Getters and Setters
	
	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public ItemDomainAdaptor getItemDomainAdaptor() {
		return itemDomainAdaptor;
	}

	public void setItemDomainAdaptor(ItemDomainAdaptor itemDomainAdaptor) {
		this.itemDomainAdaptor = itemDomainAdaptor;
	}

	
	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}

	//Private methods go here
	private ModelDetails getModelDetails(Long itemId){
		ModelDetails modelDetails = null;

		Long modelId =  null;
		Object cmbModel = ValueIdDTOHolder.getCurrent().getValue("cmbModel");
		if (cmbModel != null){
			modelId = cmbModel instanceof Integer?((Integer)cmbModel).longValue()
				: (Long)cmbModel;
			modelDetails = modelDao.getModelById(modelId);
		} else if (itemId != null && itemId > 0) {
			Item item = itemDao.getItem(itemId);
			if (item != null && item.getModel() != null) {
				modelId = item.getModel().getModelDetailId();
				modelDetails = modelDao.getModelById(modelId);
			}
		}
		return modelDetails;
	}
	

	private ModelDetails getOrCreateCabinetModel(ModelDetails itemModel)
			throws ClassNotFoundException {
		modelCreated = false;
		StringBuffer cabModelName = new StringBuffer();
		cabModelName.append(itemModel.getRuHeight());
		cabModelName.append("RU-CABINET-");
		cabModelName.append(itemModel.getModelMfrDetails().getMfrName());
		cabModelName.append("-");
		cabModelName.append(itemModel.getDimW());
		cabModelName.append("x");
		cabModelName.append(itemModel.getDimD());

		ModelDetails cabinetModel = getCabinetModelByName(cabModelName.toString());
		if (cabinetModel == null) {
			cabinetModel = (ModelDetails) itemModel.clone();
			cabinetModel.setModelDetailId(null);
			cabinetModel.setModelName(cabModelName.toString());
			cabinetModel.setWeight(0);
			cabinetModel.setFormFactor("4-Post Enclosure");
			cabinetModel.setClassLookup(systemLookupFinderDao.findByLkpValueCode(
					SystemLookup.Class.CABINET).get(0));
			modelDao.create(cabinetModel);
			modelCreated = true;
			log.debug("created cabinet: " + cabinetModel.getModelName()
					+ " id:" + cabinetModel.getModelDetailId()
					+ " for FreeStanding device");
		}
		return cabinetModel;
	}

	private ModelDetails getCabinetModelByName(String modelName)
			throws ClassNotFoundException {
		ModelDetails cabinetModelDetails = null;
		ModelDetails modelDetails = modelDao.getModelByName(modelName);
		Long modelId = modelDetails != null ? modelDetails.getModelDetailId() : -1L;
		if (modelId != -1L) {
			// FIXME: remove hard coded values
			String label = "cmbModel";
			String remoteReference = rulesProcessor.getRemoteRef(label);
			String remoteType = remoteRef.getRemoteType(remoteReference);

			String remoteAlias = remoteRef.getRemoteAlias(remoteReference,
					RemoteRefConstantProperty.FOR_ID);
			String trace = itemDomainAdaptor.getFieldTrace(remoteType,
					remoteAlias);
			trace = trace.substring(0, trace.lastIndexOf("/"));
			cabinetModelDetails = (ModelDetails) itemDomainAdaptor.loadData(
					remoteType, remoteAlias, modelId);
		}
		return cabinetModelDetails;
	}
	
	
	/*
	 * dtoList contains combined info for cabinet and item
	 * Extract info for cabinet in a new list. Must change model
	 * since cabinet has different one. Then return new list
	 */
	private List<ValueIdDTO> getCabinetDTOlist(List<ValueIdDTO> dtoList,
			ModelDetails model, Long itemId, UserInfo sessionUser) {
		List<ValueIdDTO> cabinetDTOList = new LinkedList<ValueIdDTO>();
		Map<String, Boolean> dtoProcessed = new HashMap<String, Boolean>();
		String[] configUiIdList = {"tiLoadCapacity", "tiRailWidth", "tiFrontRailOffset", "tiRearRailOffset", "tiFrontDoorPerforation", 
				"tiRearDoorPerforation", "tiFrontClearance", "tiRearClearance", "tiLeftClearance", "tiRightClearance"};
		
		Item existingItem = null;
		if (itemId != null && itemId > 0){
			existingItem = (Item) itemDao.loadItem(itemId);
			if (existingItem != null && existingItem.getParentItem() != null){
				domainToItemIdMap.put(CabinetItem.class.getName(),existingItem.getParentItem().getItemId());
				domainToItemIdMap.put(ItItem.class.getName(), itemId);
			}
		}
		
		ValueIdDTO cabinetSubclass = null;
		
		for (ValueIdDTO dto : dtoList) {
			if (dto.getLabel() == null)
				continue;

			ValueIdDTO cabinetDTO = new ValueIdDTO();
			if (itemId != null && itemId <= 0 && dto.getLabel().equals("tiName")) { //Make sure that we do not update cabinet name while updating the device (CR 46549)
				StringBuffer name = new StringBuffer();
				name.append(dto.getData());
				name.append("-CABINET");
				cabinetDTO.setData(name.toString());
			} else if (itemId != null && itemId > 0 && dto.getLabel().equals("tiName") && existingItem != null && existingItem.getParentItem() == null){
				StringBuffer name = new StringBuffer();
				name.append(dto.getData());
				name.append("-CABINET");
				cabinetDTO.setData(name.toString());
			}else if (dto.getLabel().equals("cmbModel")) {
				if (model != null) cabinetDTO.setData(model.getModelDetailId());
			} else if (dto.getLabel().equals("cmbLocation") //Copy only placement information (CR 46550)
						|| dto.getLabel().equals("cmbRowLabel")
						|| dto.getLabel().equals("cmbRowPosition")
						|| dto.getLabel().equals("radioFrontFaces")
						|| dto.getLabel().equals("tiLocationRef")){
				cabinetDTO.setData(dto.getData());
			} else if (dto.getLabel().equals("cmbStatus")) {
				copyItemStatus(itemId, dto, cabinetDTO, sessionUser);
			} else if (dto.getLabel().equals("tiSubClass")){
				cabinetSubclass = dto;
			}
			else{
				//cabinetDTO.setData(dto.getData());
				continue;
			}
			cabinetDTO.setLabel(dto.getLabel());
			cabinetDTOList.add(cabinetDTO);
			dtoProcessed.put(dto.getLabel(), true);
		}
		
		if (itemId != null && itemId <= 0){
			//Set the notes for the cabinet that this is automatically created.
			setCabinetDescription(cabinetDTOList);
			
			setCabinetSubClass(cabinetDTOList,cabinetSubclass);
		}
		for (int i = 0; i < configUiIdList.length; i++) {
			if (!dtoProcessed.containsKey(configUiIdList[i])) {
				ValueIdDTO cabinetDTO = new ValueIdDTO();
				cabinetDTO.setLabel(configUiIdList[i]);
				cabinetDTOList.add(cabinetDTO);
			}
		}
		
		return cabinetDTOList;
	}

	private void copyItemStatus(Long itemId, ValueIdDTO dto,
			ValueIdDTO cabinetDTO, UserInfo sessionUser) {
		if (itemId != null && itemId > 0){
			Item currentItem = itemDao.read(itemId);
			Item currentParentItem = currentItem.getParentItem();
			Boolean permitted = isStatusCopyPermitted(
					currentParentItem, dto, sessionUser);
			if (permitted) 
				cabinetDTO.setData(dto.getData());
			else
				cabinetDTO.setData(currentParentItem.getStatusLookup().getLkpValueCode());
		} else {
			cabinetDTO.setData(dto.getData());
		}
	}

	private Boolean isStatusCopyPermitted(Item currentParentItem,
			ValueIdDTO dto, UserInfo sessionUser) {
		Item item = (Item) currentParentItem.clone();
		item.setItemId(-1);
		
		//The following check is due to the fact that Flex client sends data as Integer and REST-API call sends it as Long
		long statusLkpValueCode = dto.getData() != null && dto.getData() instanceof Integer ? ((Integer)dto.getData()).longValue() :
			((Long)dto.getData()).longValue();
		
		item.setStatusLookup(systemLookupFinderDAO.findByLkpValueCode(statusLkpValueCode).get(0));
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, item.getClass().getName());
		Boolean permitted = itemStateContext.isTransitionPermittedForUser(item, sessionUser, errors);
		return permitted;
	}
	
	private void setCabinetSubClass(List<ValueIdDTO> cabinetDTOList,
			ValueIdDTO cabinetSubClass) {
		if (cabinetSubClass != null){
			cabinetSubClass.setData("Container");
		} else {
			cabinetSubClass = new ValueIdDTO();
			cabinetSubClass.setLabel("tiSubClass");
			cabinetSubClass.setData("Container");
			
			cabinetDTOList.add(cabinetSubClass);
		}
		
	}

	private void setCabinetDescription(List<ValueIdDTO> cabDTOList) {
		String description = messageSource.getMessage("FreeStandingItem.Cabinet.Notes", null, Locale.getDefault());
		
		ValueIdDTO notesValueId = new ValueIdDTO();
		
		notesValueId.setLabel("tiNotes");
		notesValueId.setData(description);
		
		cabDTOList.add(notesValueId);
	}
	
	/*
	 * dtoList contains combined info for cabinet and item
	 * Extract info for item in a new list and return it
	 */
	private List<ValueIdDTO> getItemDTOlist(List<ValueIdDTO> dtoList) {
		Boolean foundUPosition = false;
		List<ValueIdDTO> itemDTOList = new LinkedList<ValueIdDTO>();
		for (ValueIdDTO dto : dtoList) {
			if (dto.getLabel() == null)
				continue;
			ValueIdDTO itemDTO = new ValueIdDTO();
			if (dto.getLabel().equals("cmbUPosition")) {
				// make sure it is position 1
				itemDTO.setData("1");
				foundUPosition = true;
			} else if (dto.getLabel().equals("cmbRowLabel")
					|| (dto.getLabel().equals("cmbRowPosition")) 
					|| (dto.getLabel().equals("tiLocationRef"))
					|| (dto.getLabel().equals("radioFrontFaces"))) {
				// rowLabel and positionInRow do not exist for Item object, only
				// for Cabinet
				continue;
			} else{
				itemDTO.setData(dto.getData());
			}
			itemDTO.setLabel(dto.getLabel());
			itemDTOList.add(itemDTO);
		}
		
		if (!foundUPosition) {
			// if client did not send info about U-position; we know it has to be 1, so add it
			ValueIdDTO itemDTO = new ValueIdDTO();
			itemDTO.setData("1");
			itemDTO.setLabel("cmbUPosition");
			itemDTOList.add(itemDTO);
		}
		return itemDTOList;
	}







}
