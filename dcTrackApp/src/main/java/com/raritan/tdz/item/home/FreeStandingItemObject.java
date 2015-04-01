/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.MessageSource;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.home.ModelHome;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;


import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;

import com.raritan.tdz.item.validators.ItemValidator;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.rits.cloning.Cloner;


public class FreeStandingItemObject extends ItemObjectBase {
	
	/*private RemoteRef remoteRef;*/
	private ModelHome modelHome;
	
	private ItemValidator cabinetValidator;
	
	
	private static Logger log = Logger.getLogger("FreeStandingItemObject");

	public ModelHome getModelHome() {
		return modelHome;
	}

	public void setModelHome(ModelHome modelHome) {
		this.modelHome = modelHome;
	}

/*	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}
*/

	private ItemObjectFactory itemObjectFactory;

	public ItemObjectFactory getItemObjectFactory() {
		return itemObjectFactory;
	}

	public void setItemObjectFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectFactory = itemObjectFactory;
	}

	ItemHome itemHome;
	private boolean modelCreated;

	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public FreeStandingItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ItItem.class);
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add(SystemLookup.SubClass.RACKABLE);
		codes.add(SystemLookup.SubClass.BLADE_CHASSIS);
		codes.add(SystemLookup.SubClass.BLADE);
		codes.add(SystemLookup.SubClass.BLADE_SERVER);
		codes.add(SystemLookup.SubClass.BUSWAY_OUTLET);
		codes.add(SystemLookup.SubClass.WHIP_OUTLET);
		codes.add(SystemLookup.SubClass.NETWORK_STACK);
		// TODO: Since we do not have a way to handle the class lookup values
		// TODO: in the base, we are returning the DATA_PANEL as the subclass
		// TODO: We need to fix this in future
		codes.add(SystemLookup.Class.DATA_PANEL);
		codes.add(SystemLookup.Class.RACK_PDU);

		codes.add(SystemLookup.Class.FLOOR_PDU);
		codes.add(SystemLookup.Class.PROBE);
		return Collections.unmodifiableSet(codes);
	}

	// TODO: This is for testing only, remove when not needed
	private void printUiComponentDTOMap(Map<String, UiComponentDTO> xyzMap) 
	{
		try {
			System.out.println("====== Map content: ");
			Set<String> keySet = xyzMap.keySet();
			for (String s1 : keySet) {
				UiComponentDTO componentDTO = xyzMap.get(s1);
				if (componentDTO != null) {
					UiValueIdField uiValueIdField = componentDTO
							.getUiValueIdField();
					if (uiValueIdField != null) {
						if (uiValueIdField.getValue() != null) {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId() + ", value="
									+ uiValueIdField.getValue().toString());
						} else {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId());
						}
					} else {
						System.out.println("key=" + s1 + "val=");
					}
				} else
					System.out.println("key=" + s1 + ", but val not exist");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	private void deleteCreatedCabinetModel(ModelDetails cabinetModel){
		if (modelCreated) {
			Session session = sessionFactory.getCurrentSession();
			//session.delete(cabinetModel);
			session.flush();
			
			this.modelHome.deleteModel(cabinetModel);
			
			session.flush();
			
			log.debug("Created cabinetModel:" + cabinetModel.getModelName() + " has been deleted.");
		}	
	}
	
	private void setContainerSubclass(Item cabinetDomain){
		
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LksData.class);
		criteria.setProjection(Projections.property("lksId"));
		criteria.add(Restrictions.eq("lkpValueCode", SystemLookup.SubClass.CONTAINER));
		List<Long> list = criteria.list();
        assert (list.size() == 1);
        
        Long lksId = list.get(0);
        LksData subclass = (LksData)session.load(LksData.class, lksId);
        cabinetDomain.setSubclassLookup(subclass);
		log.debug("cabinet's subclass: lks_id=" + cabinetDomain.getSubclassLookup().getLksId() + 
				", lkp_value_code=" + cabinetDomain.getSubclassLookup().getLkpValueCode() + 
				", lkp_value=" + cabinetDomain.getSubclassLookup().getLkpValue());		
	}
	
	private
	Map<String, UiComponentDTO> loadOrSaveCabinet(Long cabinetId, UserInfo sessionUser, List<ValueIdDTO> cabDTOList, ModelDetails cabinetModel) throws Throwable{
		Map<String, UiComponentDTO> cabinetItemMap = null;
		try{
			// Create Cabinet's ItemDomain object or load it from DB if already exists
			Item cabinetDomain = (Item) itemDomainAdaptor.createItem(cabinetId, cabDTOList);
			
			assert (cabinetDomain != null);
			if (cabinetDomain != null) {
				//We have to set up subclass to be Container for this cabinet
				//so that required fields validation can be skipped for Container subclass
				setContainerSubclass(cabinetDomain);
				// Get Cabinet specific object so that it will do cabinet validation
				CabinetItemObject itemBusinessObject = (CabinetItemObject) itemObjectFactory.getItemObject(cabinetDomain);

				// fill out ItemDomain, validate cabinet data and if good save it
				// WARNING: we have to use cloner to make a deep copy of the returned map
				// because otherwise it will be overwritten with item's map later
//				Cloner cloner = new Cloner();
//				cabinetItemMap = cloner.deepClone(itemBusinessObject.saveItem(cabinetId, cabDTOList, sessionUser));
				try {
					itemBusinessObject.captureItemData(cabinetDomain, itemPlacementHome, cabinetId);
					itemBusinessObject.setItemValidator(cabinetValidator);
					cabinetItemMap = itemBusinessObject.saveItem(cabinetId, cabDTOList, sessionUser);
				} finally {
					itemBusinessObject.clearCapturedItemData(cabinetId);
				}
				
			}
		} catch (BusinessValidationException e) {
			// cabinet and item won't be saved unless they passed all
			// validation. However, we have to cleanup created model
			deleteCreatedCabinetModel(cabinetModel);
			throw e;
		} catch (Throwable e) {
			deleteCreatedCabinetModel(cabinetModel);
			throw e;
		} 
		return cabinetItemMap;
	}

	private void setCabinetDescription(List<ValueIdDTO> cabDTOList) {
		String description = messageSource.getMessage("FreeStandingItem.Cabinet.Notes", null, Locale.getDefault());
		
		ValueIdDTO notesValueId = new ValueIdDTO();
		
		notesValueId.setLabel("tiNotes");
		notesValueId.setData(description);
		
		cabDTOList.add(notesValueId);
	}

	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo sessionUser)
			throws ClassNotFoundException, BusinessValidationException, Throwable 
	{
		Map<String, UiComponentDTO> retval = null;

		List<ValueIdDTO> itemDTOList = getItemDTOlist(dtoList);
		
		// Item object will pass all validation after this call
		item = (Item) getItemDomainObject(itemId, itemDTOList, sessionUser);

		ModelDetails itemModel = getModelDetails(dtoList);
		ModelDetails cabinetModel = getOrCreateCabinetModel(itemModel);

		Long cabinetId = -1L;
		if (itemId != null && itemId > 0 && item.getParentItem() != null) {
			// edit mode; obtain cabinetId
			cabinetId = item.getParentItem().getItemId();
			log.debug("FreeStanding item using cabinet:" + cabinetId);
		}

		// create cabinet's DTO list by modifying item's DTO list
		List<ValueIdDTO> cabDTOList = getCabinetDTOlist(dtoList, cabinetModel, itemId);		
		
		Map<String, UiComponentDTO> cabinetItemMap  = loadOrSaveCabinet(cabinetId, sessionUser, cabDTOList, cabinetModel);

		assert( cabinetItemMap != null );
		assert( cabinetItemMap.size() > 0);
		CabinetItem cabinetItem = getCabinetItem(cabinetItemMap);
		assert( cabinetItem != null);
		assert (cabinetItem.getItemId() > 0);

		log.info("cabinet with id: " + cabinetItem.getItemId()
					+ " (for FreeStanding item) has been created.");

		// link item and cabinet, then save item
		item.setParentItem(cabinetItem);
		
		//For cabinet elevation, rails used cannot be null.  CR#46383
		if(item.getMountedRailLookup() == null){
			item.setMountedRailLookup(SystemLookup.getLksData(this.sessionFactory.getCurrentSession(), SystemLookup.RailsUsed.BOTH));
		}
		
		// set default value for item's orientation
		item.setFacingLookup(SystemLookup.getLksData(this.sessionFactory.getCurrentSession(), SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT));
				
		// save item in DB
		Map<String, UiComponentDTO> itemMap = saveItem(item, ((sessionUser != null) ? sessionUser.getUnits(): "1"));
		
		//assert (cabinetItem.getItemId() > 0);
		
		if (item != null) {
			log.info("FreeStanding item with id: " + item.getItemId()
				+ " has been created.");
		}
		
		retval = mergeCabinetAndItemMap(cabinetItemMap, itemMap);

		return retval;
	}


	private Map<String, UiComponentDTO> mergeCabinetAndItemMap(
			Map<String, UiComponentDTO> cabinetItemMap,
			Map<String, UiComponentDTO> itemMap) {
		Map<String, UiComponentDTO> retval = new HashMap<String, UiComponentDTO>();
		retval.putAll(cabinetItemMap);
		// FIXME: Because of bug in getItemDetails which does not do deep copy
		// and we have to manually clone it, we have to manually remove
		// these 2 elements since they really do not exist for item
		itemMap.remove("cmbLocation");
		itemMap.remove("cmbRowLabel");
		itemMap.remove("cmbRowPosition");
		itemMap.remove("tiLocationRef");
		itemMap.remove("radioFrontFaces");
		itemMap.remove("cmbStatus");
		
		retval.putAll(itemMap);
		return retval;
	}

	private CabinetItem getCabinetItem(Map<String, UiComponentDTO> cabinetItemMap) {
		long cabinetId = -1;
		CabinetItem retval = null;
		if( cabinetItemMap != null && cabinetItemMap.size() > 0){
			UiComponentDTO componentDTO = cabinetItemMap.get("tiName");
			if (componentDTO != null) {
				UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
				if (uiValueIdField != null) cabinetId = (Long) uiValueIdField.getValueId();
			}
			if (cabinetId > 0) {
				Session session = sessionFactory.getCurrentSession();
				retval = (CabinetItem) session.load(CabinetItem.class, cabinetId);
			}
		}
		return retval;
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
			cabinetModel.setModelDetailId((long) -1);
			cabinetModel.setModelName(cabModelName.toString());
			cabinetModel.setWeight(0);
			cabinetModel.setFormFactor("4-Post Enclosure");
			Session session = sessionFactory.getCurrentSession();
			cabinetModel.setClassLookup(SystemLookup.getLksData(session,
					SystemLookup.Class.CABINET));
			cabinetModel = modelHome.createModel(cabinetModel);
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
		Long modelId = modelHome.getModelIdByName(modelName);
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
			ModelDetails model, Long itemId) {
		List<ValueIdDTO> cabinetDTOList = new LinkedList<ValueIdDTO>();
		Map<String, Boolean> dtoProcessed = new HashMap<String, Boolean>();
		String[] configUiIdList = {"tiLoadCapacity", "tiRailWidth", "tiFrontRailOffset", "tiRearRailOffset", "tiFrontDoorPerforation", 
				"tiRearDoorPerforation", "tiFrontClearance", "tiRearClearance", "tiLeftClearance", "tiRightClearance"};
		
		Item existingItem = null;
		if (itemId != null && itemId > 0){
			existingItem = (Item) loadItem(Item.class, itemId);
		}
		
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
				cabinetDTO.setData(model.getModelDetailId());
			} else if (dto.getLabel().equals("cmbLocation") //Copy only placement information (CR 46550)
						|| dto.getLabel().equals("cmbRowLabel")
						|| dto.getLabel().equals("cmbRowPosition")
						|| dto.getLabel().equals("radioFrontFaces")
						|| dto.getLabel().equals("tiLocationRef")
						|| dto.getLabel().equals("cmbStatus")){
				cabinetDTO.setData(dto.getData());
			} else if (itemId != null && itemId <= 0 && dto.getLabel().equals("_tiOrigin")) {
				// set origin for cabinet only when free standing item is a new item.  
				cabinetDTO.setData(dto.getData());
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

	private ModelDetails getModelDetails(List<ValueIdDTO> dtoList)
			throws ClassNotFoundException {
		ModelDetails modelDetails = null;

		for (ValueIdDTO dto : dtoList) {
			if (dto != null && dto.getLabel() != null
					&& !dto.getLabel().equals("cmbModel"))
				continue;
			else {
				modelDetails = itemDomainAdaptor.getModelDetails(dto);
				break;
			}
		}
		return modelDetails;
	}
	
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit) throws Throwable{
		Map<String, UiComponentDTO> itemMap = super.getItemDetails(itemId, unit);
		
		Item item = this.getItem();
		Item cabinet = item.getParentItem();
		//Cloned freestanding devices/network items may not have the cabinets setup.
		if (cabinet == null) {
			return itemMap;
		}
		else {
			Map<String, UiComponentDTO> cabinetItemMap = super.getItemDetails(cabinet.getItemId(), unit);
	
			return mergeCabinetAndItemMap(cabinetItemMap, itemMap);
		}
	}
	

	@Override
	public boolean deleteItem() throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		Session session = this.sessionFactory.getCurrentSession();
		//TODO: Need to handle Floor PDU with Panels
		
		//Delete Container for Free Standing Object
		Item cabinet = item.getParentItem();
		
		if(cabinet != null){
			ItemObject cab = this.itemObjectFactory.getItemObject(cabinet.getItemId());
			
			if(cab != null){
				cab.deleteItem();
			}
		}
		
		//Delete IT/ME records
		Query q = session.createSQLQuery("delete from dct_items_me where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " ME Item");
		}
		
		q = session.createSQLQuery("delete from dct_items_it where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " IT Item");
		}

		return super.deleteItem();
	}

	public ItemValidator getCabinetValidator() {
		return cabinetValidator;
	}

	public void setCabinetValidator(ItemValidator cabinetValidator) {
		this.cabinetValidator = cabinetValidator;
	}
		
}
