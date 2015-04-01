/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemDeleteHelper;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.ItemObject;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna, santo
 *
 */
@Transactional
public class ChangeModelDAOImpl implements ChangeModelDAO {

	SessionFactory sessionFactory;
	ItemDeleteHelper itemDeleteHelper;
	ItemObjectFactory itemObjectFactory;
	ItemHome itemHome;

	private ModelHome modelHome;
	
	private ItemObjectFactory itemObjectTemplateFactory;
	
	@Autowired
	private CabinetHome cabinetHome;
	
	public ItemObjectFactory getItemObjectTemplateFactory() {
		return itemObjectTemplateFactory;
	}

	public void setItemObjectTemplateFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectTemplateFactory = itemObjectFactory;
	}

	public ModelHome getModelHome() {
		return modelHome;
	}

	public void setModelHome(ModelHome modelHome) {
		this.modelHome = modelHome;
	}	
	
	private ResourceBundleMessageSource messageSource;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	ChangeModelDAOImpl(SessionFactory sessionFactory, ItemObjectFactory itemObjectFactory, 
			ItemDeleteHelper itemDeleteHelper, ItemHome itemHome){
		this.sessionFactory = sessionFactory;
		this.itemDeleteHelper = itemDeleteHelper;
		this.itemObjectFactory = itemObjectFactory;
		this.itemHome = itemHome;
	}


	public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#updateUPosition(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item, com.raritan.tdz.item.home.modelchange.ChangeModelDAO.UPositionEnum)
	 */
	@Override
	public void updateUPosition(Item itemInDB, Item itemToSave,
			UPositionEnum uPositionEnum) throws BusinessValidationException {
		log.debug("updateUPosition()");
		
		if(uPositionEnum == uPositionEnum.CLEAR_UPOSITION){
			itemToSave.setUPosition(-9);
			return;
		}
		
		ItemObject item = itemObjectFactory.getItemObject(itemToSave);
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, itemToSave.getClass().getName());
		
		try {
			if(item.isItemPlacementValid(itemToSave, errors) == false){
				itemToSave.setUPosition(-9);
			}
		} catch (DataAccessException e) {
			itemToSave.setUPosition(-9); //clear u position on error
		}
			
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#deletePowerPorts(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deletePowerPorts(Item itemToSave) throws BusinessValidationException {
		log.debug("deletePowerPorts()");
		
		//validatePortUsage(PowerPort.class, itemToSave.getItemId());
				
		// itemToSave.setPowerPorts(null);
		
		// itemDeleteHelper.deleteItemPowerPort(itemToSave.getItemId());

		if (null != itemToSave.getPowerPorts()) {
			itemToSave.getPowerPorts().clear();
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#changeDPSubclassToVirtual(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void changeDPSubclassToVirtual(Item itemToSave) throws BusinessValidationException {
		log.debug("changeDPSubclassToVirtual()");
		
		//validatePortUsage(DataPort.class, itemToSave.getItemId());
		
		Session session = this.sessionFactory.getCurrentSession();
		LksData subclass = SystemLookup.getLksData(session, SystemLookup.PortSubClass.VIRTUAL, "PORT_SUBCLASS");
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		if(itemToSave.getDataPorts() != null && itemToSave.getDataPorts().size() > 0){
			for(DataPort port:itemToSave.getDataPorts()){
				port.setPortSubClassLookup(subclass);
				port.setUpdateDate(currentDate);
				session.update(port);
				session.flush();
			}
		}
		else{
			for(Long portId:getItemPortIds(DataPort.class, itemToSave.getItemId())){
				this.updateDataPortSubClass(itemToSave, portId, subclass);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#changeDPSubclassToActive(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void changeDPSubclassToActive(Item itemToSave) throws BusinessValidationException {
		log.debug("changeDPSubclassToActive()");
		
		//validatePortUsage(DataPort.class, itemToSave.getItemId());
		
		Session session = this.sessionFactory.getCurrentSession();
		LksData subclass = SystemLookup.getLksData(session, SystemLookup.PortSubClass.ACTIVE, "PORT_SUBCLASS");
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		if(itemToSave.getDataPorts() != null && itemToSave.getDataPorts().size() > 0){
			for(DataPort port:itemToSave.getDataPorts()){
				port.setPortSubClassLookup(subclass);
				port.setUpdateDate(currentDate);
				session.update(port);
			}
			session.flush();
		}
		else{
			for(Long portId:getItemPortIds(DataPort.class, itemToSave.getItemId())){
				this.updateDataPortSubClass(itemToSave, portId, subclass);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#deleteDataStore(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteDataStore(Item itemToSave) throws BusinessValidationException {
		log.debug("deleteItemDataStore()");
		
		itemDeleteHelper.deleteItemDataStore(itemToSave.getItemId());		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#deleteDataStoreFromVMItem(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteDataStoreFromVMItem( Item itemToSave) throws BusinessValidationException {
		log.debug("deleteDataStoreFromVMItem()");
		
		ItItem vm = (ItItem) itemToSave;
		vm.setDataStore(null);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#deleteChassisRefFromBladeItems(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteChassisRefFromBladeItems(Item itemToSave) throws BusinessValidationException {
		log.debug("deleteChassisRefFromBladeItems()");
		
		if(itemToSave.getSkipValidation() == null || !itemToSave.getSkipValidation()){
			validateChassisAssociation(itemToSave);
		}

		// clear the chassis reference
		Session session = sessionFactory.getCurrentSession();
		
		// clear the slot position and grouping name
		Query q = session.createSQLQuery("update dct_items set slot_position=-9,grouping_name=null where item_id in (select item_id from dct_items_it where chassis_id = :itemId)");
		q.setLong("itemId", itemToSave.getItemId());
		q.executeUpdate();

		// clear the chassis
		q = session.createSQLQuery("update dct_items_it set chassis_id=null where chassis_id = :itemId");
		q.setLong("itemId", itemToSave.getItemId());
		q.executeUpdate();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#clearChassisId(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void clearChassisId(Item itemToSave) throws BusinessValidationException {
		log.debug("clearChassisId()");
				
		ItItem blade = (ItItem) itemToSave;
		blade.setBladeChassis(null);
	}

	@SuppressWarnings("unchecked")
	private <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	        throw new 
	           NullPointerException("Entity passed for initialization is null");
	    }

	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#createPhantomCabinet(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void createPhantomCabinet(Item itemToSave) throws BusinessValidationException {
		log.debug("createPhantomCabinet()");
		Session session = sessionFactory.getCurrentSession();
		
		ModelDetails itemModel = itemToSave.getModel();
		CabinetItem oldCabinet = null;
		if (null != itemToSave.getParentItem()) {
			if (itemToSave.getParentItem() instanceof CabinetItem) {
				oldCabinet = (CabinetItem) itemToSave.getParentItem();
			}
			else {
				Item itemUnProxy = initializeAndUnproxy(itemToSave.getParentItem());
				if (itemUnProxy instanceof CabinetItem) {
					oldCabinet = (CabinetItem) itemUnProxy;
				}
			}
		}
		// oldCabinet = (CabinetItem) initializeAndUnproxy(itemToSave.getParentItem());
		
		if(itemModel == null) return;
		
		ModelDetails cabinetModel = modelHome.createPhatomCabinetModel(itemModel);
			
		CabinetItem cabinet = new CabinetItem();
		cabinet.setItemName(itemToSave.getItemName() + "-CABINET");
		cabinet.setClassLookup(SystemLookup.getLksData(session, SystemLookup.Class.CABINET));
		cabinet.setSubclassLookup(SystemLookup.getLksData(session, SystemLookup.SubClass.CONTAINER));
		cabinet.setModel(cabinetModel);				
		cabinet.setStatusLookup(itemToSave.getStatusLookup());
		cabinet.setDataCenterLocation(itemToSave.getDataCenterLocation());	
		
		if(oldCabinet != null) {
			int rowPos = oldCabinet.getPositionInRow();
			String rowLabel = oldCabinet.getRowLabel();
			List<Integer> positions = cabinetHome.getCabinetPositionInRows(
						cabinet.getDataCenterLocation().getDataCenterLocationId(),
						rowLabel);
			// if row position is already occupied do not set rowLabel and rowPosition
			if (!positions.contains(rowPos)) {
				cabinet.setRowLabel(oldCabinet.getRowLabel());
				cabinet.setPositionInRow(oldCabinet.getPositionInRow());
			}
		}
		
		cabinet.setFacingLookup(itemToSave.getFacingLookup());
		cabinet.setLocationReference(itemToSave.getLocationReference());

		String description = messageSource.getMessage("FreeStandingItem.Cabinet.Notes", null, Locale.getDefault());
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		ItemServiceDetails detail = new ItemServiceDetails();
		detail.setDescription(description);
		detail.setSysCreationDate(currentDate);
		detail.setSysUpdateDate(currentDate);
		
		if(itemToSave.getItemServiceDetails() != null){
			detail.setSysCreatedBy(itemToSave.getItemServiceDetails().getSysCreatedBy());
			detail.setOriginLookup(itemToSave.getItemServiceDetails().getOriginLookup());
		}
		
		cabinet.setItemServiceDetails(detail);
				
		session.save(cabinet);
		session.flush();
		
		itemToSave.setParentItem(cabinet);
		itemToSave.setUPosition(1L);
		itemToSave.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.BOTH));
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#deletePhantomCabinet(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deletePhantomCabinet(Item itemInDB) throws BusinessValidationException {
		log.debug("deletePhantomCabinet()");
		
		if(itemInDB.getParentItem() != null){	
			// ItemObject cabinet = itemObjectFactory.getItemObject(itemInDB.getParentItem().getItemId());
			ItemObjectTemplate itemObject = null; 
			Long cabinetItemId = itemInDB.getParentItem().getItemId();
			itemObject = itemObjectTemplateFactory.getItemObjectFromItemId(cabinetItemId);
			
			try {
				// cabinet.deleteItem();
				itemObject.deleteItem(cabinetItemId, false, null);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#clearSiblingId(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void clearSiblingId(Item itemToSave) throws BusinessValidationException {
		log.debug("clearSiblingId()");
		
		ItItem blade = (ItItem) itemToSave;
		
		//remove blade item from stack
		try {
			itemHome.removeStackItem(blade.getItemId());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		blade.setCracNwGrpItem(null);
		blade.setGroupingName("");

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelDAO#clearDeviceConfigurationFields(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void clearDeviceConfigurationFields(Item itemToSave) throws BusinessValidationException {
		log.debug("clearDeviceConfigurationFields()");
		
		itemToSave.setGroupingNumber(null);
		itemToSave.setGroupingName(null);
		itemToSave.setNumPorts(0);
		
		ItItem item = (ItItem)itemToSave;
		item.setOsiLayerLookup(null);
		item.setOsLookup(null);
		item.setVmClusterLookup(null);
		item.setServices(null);
		item.setTotalProcesses(null);
		item.setTotalUsers(null);
		item.setRamGB(null);
		item.setCpuQuantity(null);
		item.setCpuType(null);
		item.setDomainLookup(null);
	}
	
	@Override
	public void clearSlotPosition(Item itemToSave ) throws BusinessValidationException{
		log.debug("clearSlotPosition()");
		
		itemToSave.setSlotPosition(-9);
	}
	
	@Override
	public void clearCabinetId(Item itemToSave) throws BusinessValidationException {
		log.debug("clearCabinetId()");
		
		itemToSave.setParentItem(null);
	}

	@Override
	public void clearVmClusterId(Item itemToSave) throws BusinessValidationException {
		log.debug("clearVmClusterId()");
		
		ItItem vm = (ItItem) itemToSave;
		vm.setVmClusterLookup(null);
	}


	@Override
	public void clearModelId(Item itemToSave) throws BusinessValidationException {
		log.debug("clearModelId()");
		
		itemToSave.setModel(null);
	}
	
	private List<Long> getItemPortIds(Class<?> portClass, long itemId){
		Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(portClass);
		
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portId"), "portId");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("item.itemId", itemId));

		List<Long> recList = new ArrayList<Long>(); 

		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((Long)row.get(0));
		}
		
		return recList;
	}
	
	private void updateDataPortSubClass(Item item, long portId, LksData subclass) throws BusinessValidationException{
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		Session session = sessionFactory.getCurrentSession();		
		
		/* This code only works doing unit test. It failed to comit data to DB */
		DataPort port = (DataPort)session.get(DataPort.class, portId);
		port.setPortSubClassLookup(subclass);		
		port.setUpdateDate(currentDate);		
		session.update(port);
		session.flush();
	}
		
	private void validatePortUsage(Class<?> portClass, long itemId) throws BusinessValidationException{
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(portClass);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portName"), "portName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("item.itemId", itemId));
		c.add(Restrictions.eq("used", true));
		
		StringBuffer portName = new StringBuffer(); 
		int count=0;
		
		for(Object rec:c.list()){
			List row = (List) rec;

			if (count == 0){
				portName.append(row.get(0));
			} else {
				portName.append(",\t").append(row.get(0));
			}
			count++;
		}
		
		if(count > 0){
			//Create a business validation exception out of the errors
			Object args[]  = {count, portName.toString() };
			String code = "ItemValidator.itemHasConnected";
			String msg = messageSource.getMessage(code, args, null);	
			BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
			be.addValidationError(msg);
			be.addValidationError(code, msg);
			throw be;
		}
	}
	
	private void validateChassisAssociation(Item item) throws BusinessValidationException{
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("bladeChassis", "chassis");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("chassis.itemId", item.getItemId()));
		
		StringBuffer tempName = new StringBuffer(); 
		int count=0;
		
		for(Object rec:c.list()){
			List row = (List) rec;

			tempName.append("\t").append(row.get(0)).append("\n");
			count++;
		}
		
		if(count > 0){
			//Create a business validation exception out of the errors
			Object args[]  = {tempName.toString() };
			String code = "ItemValidator.invalidDefinitionModelChassis";
			String msg = messageSource.getMessage(code, args, null);	
			BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
			be.setCallbackURL("itemService.saveItem");
			be.addValidationWarning(msg);
			be.addValidationWarning(code, msg);
			throw be;
		}
	}

	@Override
	public void validatePortUsage(Long itemId) throws BusinessValidationException {
		validatePortUsage(DataPort.class, itemId);
		
		validatePortUsage(PowerPort.class, itemId);		
	}

	@Override
	public void clearShelfPosition(Item itemToSave, Item itemDB)
			throws BusinessValidationException {
		itemToSave.setShelfPosition(null);
		// Update shelf position for other non-rackables in the same cabinet, u-position and side
		try {
			if (null != itemDB.getParentItem() && null != itemDB.getMountedRailLookup()) {
				itemHome.updateShelfPosition(itemDB.getParentItem().getItemId(), itemDB.getuPosition(), itemDB.getMountedRailLookup().getLkpValueCode(), null, itemToSave);
			}
		}
		catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateShelfPosition(Item itemToSave)
			throws BusinessValidationException {
		if (itemToSave.getuPosition() != -9) {
			// itemToSave.setShelfPosition(1);
		}
	}

}


