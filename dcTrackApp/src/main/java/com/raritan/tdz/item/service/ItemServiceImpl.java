package com.raritan.tdz.item.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.chassis.service.ChassisService;
import com.raritan.tdz.circuit.service.CircuitProc;
import com.raritan.tdz.domain.CircuitItemViewData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.DataPortInterface;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.PowerPortInterface;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.UniqueValidatorDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.ip.dao.IPAddressDAO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.BladeDTO;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTOImpl;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.StackItemDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.item.home.UPSBank;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.page.home.PaginatedHome;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.service.UtilService;
import com.raritan.tdz.session.DCTrackSessionManager;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.session.UserSessionManager;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.validators.ValidatorFactory;
import com.raritan.tdz.views.ItemObject;

import flex.messaging.FlexContext;

/**
 * @author Andrew Cohen
 */
public class ItemServiceImpl extends com.raritan.tdz.service.ItemServiceImpl implements ItemService {
	private ItemHome itemHome;
	private PortHome portHome;
	private UtilHome utilHome;
	private CabinetHome cabinetHome;
	private ChassisService chassisService;
	private PaginatedHome paginatedHome;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired(required=true)
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;
	
	@Autowired(required=true)
	private IPAddressDAO ipAddressDAO;
	
	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	public ItemServiceImpl(ItemHome itemHome, UtilService utilService,
			ValidatorFactory validatorFactory) {
		super(itemHome, utilService, validatorFactory);
		this.itemHome = itemHome;
	}
	
	public void setPortHome(PortHome portHome) {
		this.portHome = portHome;
	}

	public void setUtilHome(UtilHome utilHome) {
		this.utilHome = utilHome;
	}
	
	public CabinetHome getCabinetHome() {
		return cabinetHome;
	}

	public void setCabinetHome(CabinetHome cabinetHome) {
		this.cabinetHome = cabinetHome;
	}
	
	public PaginatedHome getPaginatedHome() {
		return paginatedHome;
	}
	
	public void setPaginatedHome(PaginatedHome paginatedHome) {
		this.paginatedHome=paginatedHome;
	}

	public ChassisService getChassisService() {
		return chassisService;
	}

	public void setChassisService(ChassisService chassisService) {
		this.chassisService = chassisService;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<Long> addItemPorts(List<PortInterface> ports) throws ServiceLayerException {
		List<Long> ids = new LinkedList<Long>();
		if (ports == null) return ids;
		
		for (PortInterface port : ports) {
			if (port instanceof DataPortInterface) {
				DataPortInterface dataPort = (DataPortInterface)port;
				ids.add( portHome.saveDataPort( PortsAdaptor.adaptDataPortDTOToDomain(dataPort, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO) ) );
			}
			else if (port instanceof PowerPortInterface) {
				PowerPortInterface powerPort = (PowerPortInterface)port;
				ids.add( portHome.savePowerPort( PortsAdaptor.adaptPowerPortDTOToDomain(powerPort, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO) ) );
			}
		}
		
		return ids;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public long deleteItemPorts(List<PortInterface> ports) throws ServiceLayerException {
		long count = 0;
		
		if (ports != null) {
			for (PortInterface port : ports) {
				try {
					if (port instanceof DataPortInterface) {
						portHome.deleteDataPortById( port.getPortId() );
					}
					else if (port instanceof PowerPortInterface){
						portHome.deletePowerPortById( port.getPortId() );
					}
					// TODO: Sensor Port?
					count++;
				}
				catch(DataAccessException e) {
					// Ignore
				}
			}
		}
		
		return count;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<PortInterface> viewPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException{
		List<PortInterface> portList = new ArrayList<PortInterface>();

		for(DataPortInterface rec : viewDataPortListForItem(itemId, freePortOnly)){
			portList.add(rec);
		}
		for(PowerPortInterface rec : viewPowerPortListForItem(itemId, freePortOnly)){
			portList.add(rec);
		}

		return portList;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<DataPortInterface> viewDataPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException {

		List<DataPortInterface> recList = new ArrayList<DataPortInterface>();

		Map<Integer, String> netinfo = this.itemHome.getDataPortNetInfoAsMap(itemId);

		List<DataPort> ports = this.itemHome.viewDataPortsForItem(itemId, freePortOnly, null);
		
		List<Long> movePortIds = new ArrayList<Long>();
		if (null != ports) {
			for (DataPort dp: ports) {
				movePortIds.add(dp.getPortId());
			}
		}
		Map<Long, LksData> portsAction = dataPortMoveDAO.getMovePortAction(movePortIds);
		
		for(DataPort o: this.itemHome.viewDataPortsForItem(itemId, freePortOnly, null)) {
			DataPortDTO rec = PortsAdaptor.adaptDataPortDomainToDTO( o, portsAction );

			if(netinfo.get(rec.getPortId().intValue()) != null){
				String t[]= netinfo.get(rec.getPortId().intValue()).split("\\|");
				rec.setIpAddress(t[0]);
				rec.setSubnet(t[1]);
			}

			recList.add(rec);
		}

		return recList;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<PowerPortInterface> viewPowerPortListForItem(Long itemId, boolean freePortOnly)	throws ServiceLayerException {

		List<PowerPortInterface> recList = new ArrayList<PowerPortInterface>();

		List<PowerPort> ports = this.itemHome.viewPowerPortsForItem(itemId, freePortOnly, null);
		List<Long> movePortIds = new ArrayList<Long>();
		if (null != ports) {
			for (PowerPort dp: ports) {
				movePortIds.add(dp.getPortId());
			}
		}
		Map<Long, LksData> portsAction = dataPortMoveDAO.getMovePortAction(movePortIds);
		
		for(PowerPort o: this.itemHome.viewPowerPortsForItem(itemId, freePortOnly, null)) {
			PowerPortDTO rec = PortsAdaptor.adaptPowerPortDomainToDTO( o, portsAction );
			recList.add(rec);
		}

		return recList;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<ItemObject> viewItemsForLocation(Long locationId, Long portClassValueCode) throws ServiceLayerException {
		List<ItemObject> recList = new ArrayList<ItemObject>();
		//HashMap<Long, Long> dataCount = null;
		
		//if(portClassValueCode == null || portClassValueCode == SystemLookup.PortClass.DATA){
		//	dataCount = this.itemHome.getItemPortCount(SystemLookup.PortClass.DATA, false);
		//}

		boolean addRecord = false;

		for(Object o : this.itemHome.viewItemsForLocation(locationId, true, null, portClassValueCode))
		{
			addRecord = true;

			CircuitItemViewData item = (CircuitItemViewData)o;
			ItemObject rec = CircuitProc.newItemObject(item);
			
			//if(dataCount != null){
			//	rec.setDataPortCount((Long)dataCount.get(item.getItemId()));
			//}
			
			if(item.getClassLkpValueCode() == SystemLookup.Class.UPS_BANK) {
				UPSBank upsBank = (UPSBank)itemHome.getFactory().getItemObject( item.getItemId() );
				rec.setUnitsInUpsBank( upsBank.getLinkedUPSCount() );
			}
						
			if(rec.getClassLksCode() != SystemLookup.Class.FLOOR_PDU &&
			   rec.getClassLksCode() != SystemLookup.Class.CRAC &&
			   rec.getClassLksCode() != SystemLookup.Class.UPS ){
				if(rec.getUPosition() <= -9 && rec.getSlotPosition() <= -9){
					addRecord = false;
				}
			}
			
			if(rec.getSubClassLksCode() != null && rec.getSubClassLksCode() == SystemLookup.SubClass.VIRTUAL_MACHINE){
				addRecord = true;
			}
			
			//last check, add any check before this
			if(addRecord == false){
				continue;
			}

			recList.add(rec);
		}

		return recList;
	}
	
	/**
	*Added by Randy Chen
	*/
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Collection<Long> getAvailUPositionsForItem(long parenItemId, int itemRUHeight, int itemUPosition, int blockSize, String itemMounting, long railsCode) throws ServiceLayerException{
	
		 Collection<Long> positions = itemHome.getAvailUPositionsForItem(parenItemId, itemRUHeight, itemUPosition, blockSize, railsCode);

		return positions;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Collection<ValueIdDTO> getAvailUPositionsForNRItem(long cabinetId, long modelId, long railsCode, long editItemId) throws ServiceLayerException{
		
		List<ValueIdDTO> dtos = new ArrayList<ValueIdDTO>();
		Collection<Long> uPositions =  itemHome.getNonRackableUPosition(cabinetId, modelId, railsCode, editItemId); // bunty
		
		//Add the above UPosition here
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel("Above");
		dto.setData(new Long(SystemLookup.SpecialUPositions.ABOVE));
		dtos.add(dto);
		
		//Add all the others in between.
		for (Long uPosition: uPositions){
			dto = new ValueIdDTO();
			dto.setLabel(uPosition.toString());
			dto.setData( uPosition );
			dtos.add(dto);
		}
		
		//Add the below UPosition here
		dto = new ValueIdDTO();
		dto.setLabel("Below");
		dto.setData(new Long(SystemLookup.SpecialUPositions.BELOW));
		dtos.add(dto);
		
		return dtos;
	}
	
	@Override
	public List<ValueIdDTO> getAvailableZeroUPositionsForItem(Long cabinetId, 
			long depth_lkp_value, long side_lkp_value, int itemRUHeight, 
			Long exceptionItemId) throws ServiceLayerException {
		
		Collection<Long> uPositions = this.itemHome.getAvailableZeroUPositions(cabinetId, depth_lkp_value, side_lkp_value, itemRUHeight, exceptionItemId); // bunty
		
		List<ValueIdDTO> values = new ArrayList<ValueIdDTO>( uPositions.size() );
		for (Long uPos: uPositions) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel( uPos.toString() );
			dto.setData( uPos );
			values.add( dto );
		}
		
		return values;
	}
	
	@Override
	public List<SystemLookupDTO> getAllItemClassLookup() {
		return itemHome.getAllItemClassLookup();
	}

	@Override
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTOImpl criteriaDTO) throws ServiceLayerException{
		return itemHome.search(criteriaDTO);
	}

	@Override
	public List<SystemLookupDTO> getAllStatusLookup() {
		List<SystemLookupDTO> statusLookups = new ArrayList<SystemLookupDTO>();
		
		//We will add the "Item Status: All" value in addition to what is there in the database
		//That way client has to just display and use the lkp value defined for the filter.
//		SystemLookupDTO statusLookupAll = new SystemLookupDTO();
//		statusLookupAll.setLabel(SystemLookup.SpecialStatus.ALL_VALUE);
//		statusLookupAll.setData(SystemLookup.SpecialStatus.ALL);
//		statusLookups.add(statusLookupAll);
		
		for (SystemLookupDTO systemDTO: itemHome.getSystemLookup(SystemLookup.LkpType.ITEM_STATUS)){
			SystemLookupDTO statusLookup = new SystemLookupDTO();
			statusLookup.setLabel("Item Status: " + systemDTO.getLabel());
			statusLookup.setData(systemDTO.getData());
			statusLookups.add(statusLookup);
		}
		
		return statusLookups;
	}

	@Override
	public Long getTotalItemCountForClass(Long classLkpValueCode) {
		return itemHome.getTotalItemCountForClass(classLkpValueCode);
	}

	@Override
	public UserInfo getUserInfoBySessionId(String userSessionId) throws ServiceLayerException {
		UserInfo userInfo = super.getUserInfoBySessionId(userSessionId);
		UserInfo sessionUser = FlexUserSessionContext.getUser();
		
		if (userInfo != null && sessionUser != null && !userInfo.getSessionId().equals( sessionUser.getSessionId() )) {
			/*
			 *  For CR 42330 - This code handles the following scenario:
			 *  1) User logs in as UserA.
			 *  2) User logs off by clicking the logout button.
			 *  3) User then logs in as UserB.
			 *  
			 *  Step #3 will bypass BlazeDS authentication because the browser session was already
			 *  authenticated in step #1. So this ensures that we update the existing BlazeDS session
			 *  with the new UserInfo that we logged in with.
			 */
			FlexContext.getFlexSession().setAttribute(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY, userInfo);
			
			Logger log = Logger.getLogger( UserSessionManager.class );
			if (log.isInfoEnabled()) {
				log.info("Session key changed: Updated User info in BlazeDS session from user '" +
					sessionUser.getUserName() +"' to user '" + userInfo.getUserName() + "'");
			}
		}
		return userInfo;
	}

	@Override
	public String getItem(Long itemId, String xPath) throws Throwable {
		return itemHome.getItem(itemId, xPath, FlexUserSessionContext.getUser());
	}

	@Override
	public Map<String, UiComponentDTO> getItem(Object[] itemId) throws Throwable {
		Map<String, UiComponentDTO> dtos = itemHome.getItem((Long)(((Integer)itemId[0]).longValue()), FlexUserSessionContext.getUser());
		return dtos;
	}
	
	@Override
	public Boolean isUnique( UniqueValidatorDTO uniqueValidatorDTO) throws Throwable {
		return itemHome.isUnique(uniqueValidatorDTO.getUiId(), uniqueValidatorDTO.getValue(), uniqueValidatorDTO.getSiteCode(), uniqueValidatorDTO.getParentId(), uniqueValidatorDTO.getIgnoreProperty(), uniqueValidatorDTO.getIgnorePropertyValue());
	}

	@Override
	public List<SystemLookupDTO> getSystemLookup(String lkpTypeName) {
		return itemHome.getSystemLookup(lkpTypeName);
	}

	@Override
	public List<ValueIdDTO> getAllCabinets(Long locationId) {
		// TODO Auto-generated method stub
		return cabinetHome.getAllCabinets(locationId); // bunty
	}

	@Override
	public List<ValueIdDTO> getAllLocations() {
		
		return itemHome.getAllLocations();
	}


	@Override
	public List<ValueIdDTO> getAvailableUPositions(Long cabinetId,
			int ruHeight, long itemId, long railsCode, Long reservationId) throws DataAccessException {
	
		List<ValueIdDTO> dtos = new ArrayList<ValueIdDTO>();
		Collection<Long> uPositions =  itemHome.getAvailableUPositions(cabinetId, ruHeight, itemId, railsCode, reservationId);
		
		//Add the above UPosition here
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel("Above");
		dto.setData(new Long(SystemLookup.SpecialUPositions.ABOVE));
		dtos.add(dto);
		
		//Add all the others in between.
		for (Long uPosition: uPositions){
			dto = new ValueIdDTO();
			dto.setLabel(uPosition.toString());
			dto.setData( uPosition );
			dtos.add(dto);
		}
		
		//Add the below UPosition here
		dto = new ValueIdDTO();
		dto.setLabel("Below");
		dto.setData(new Long(SystemLookup.SpecialUPositions.BELOW));
		dtos.add(dto);
		
		return dtos;
	}

    @Override
    public List<ValueIdDTO> getCabinetRowLabels(Long locationId) {        
        List<String> tempList = this.cabinetHome.getCabinetRowLabels(locationId);
        
        return convertStringListToValueIdDTOList(tempList);        
    }

    @Override
    public List<ValueIdDTO> getCabinetPositionInRows(Long locationId, String rowLabel) {
          return getCabinetPositionInRows(locationId, rowLabel, null);
    }
    
    public List<ValueIdDTO> getCabinetPositionInRows(Long locationId, String rowLabel, Integer availPos) {
    	 List<Integer> usedList =  this.cabinetHome.getCabinetPositionInRows(locationId, rowLabel);

    	 if (availPos != null) {
    		 usedList.remove( availPos );
    	 }
    	 
         List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
         for (Integer i = 1; i < 100; i++) {
        	 if (!usedList.contains(i)) {
        		 ValueIdDTO rec = new ValueIdDTO();
                 rec.setData(i);
                 rec.setLabel(String.valueOf(i));
                 recList.add(rec);
        	 }
         }

         return recList;     
    }

	@Override
	public List<SystemLookupDTO> getStatusLookupForCurrentState(Long itemId, Long modelId)  throws DataAccessException, ClassNotFoundException {
		return itemHome.getStatusLookupForCurrentState(itemId, modelId, FlexUserSessionContext.getUser());
	}
	
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId)
			throws Throwable {
		return itemHome.getItemDetails(itemId, FlexUserSessionContext.getUser());
	}
	
	@Override
	public Boolean getIsTagVerified(Long itemId) {
		return itemHome.isItemTagVerified(itemId);
	}

	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId,
			List<ValueIdDTO> dtoList) throws ClassNotFoundException,
			BusinessValidationException, Throwable {
		// set origin field for this new item.
		if (itemId <=0) {
			dtoList.add(new ValueIdDTO( "_tiOrigin", SystemLookup.ItemOrigen.CLIENT));
		}
		return itemHome.saveItem(itemId, dtoList, FlexUserSessionContext.getUser());
	}

	@Override
	public List<ValueIdDTO> getProjectNumbers(Long locationId) {
        List<String> tempList = this.itemHome.getProjectNumbers(locationId);
        
        return convertStringListToValueIdDTOList(tempList);       
	}

	@Override
	public List<ValueIdDTO> getContractNumbers(Long locationId) {
        List<String> tempList = this.itemHome.getContractNumbers(locationId);
                        
        return convertStringListToValueIdDTOList(tempList);
	}
	
	public List<ValueIdDTO> convertStringListToValueIdDTOList(List<String> tempList){
        List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
        
        for(String s:tempList){
            ValueIdDTO rec = new ValueIdDTO();
            rec.setData(s);
            rec.setLabel(s);
            recList.add(rec);
        }
                
        return recList;		
	}

	public static enum ResultIdx {
		ID, NAME, MODEL_NAME, CABINET_NAME, U_POSITION, STATUS, SIBLING_ITEM_ID
	}
	
	private List<StackItemDTO> getStackItemDto(List<List<?>> objs) {
		List<StackItemDTO> stackItemDtoList = new ArrayList<StackItemDTO>();
		for (List<?> obj: objs ) {
			StackItemDTO stackItemDto = new StackItemDTO();
			stackItemDto.setId((Long)obj.get(ResultIdx.ID.ordinal()));
			stackItemDto.setName((String)obj.get(ResultIdx.NAME.ordinal()));
			stackItemDto.setModelName((String)obj.get(ResultIdx.MODEL_NAME.ordinal()));
			stackItemDto.setCabinetName((String)obj.get(ResultIdx.CABINET_NAME.ordinal()));
			stackItemDto.setUPosition((Long)obj.get(ResultIdx.U_POSITION.ordinal()));
			stackItemDto.setStatus((Long)obj.get(ResultIdx.STATUS.ordinal()));
			stackItemDto.setSiblingId((Long)obj.get(ResultIdx.SIBLING_ITEM_ID.ordinal()));
			stackItemDtoList.add(stackItemDto);
		}
		return stackItemDtoList;
	}

	@Override
	public List<StackItemDTO> getAllStackItems(Long itemId) throws ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.getAllStackItems( itemId );
		return getStackItemDto(objs);
	}

	@Override
	public List<StackItemDTO> getAddableStackItems() throws ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.getAddableStackItems();
		return getStackItemDto(objs);
	}

	@Override
	public List<StackItemDTO> addStackItem(long newItemId, long siblingItemId) throws ClassNotFoundException, ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.addStackItem(newItemId, siblingItemId);
		return getStackItemDto(objs);		
	}

	@Override
	public List<StackItemDTO> removeStackItem(long itemId, String newName) throws  ClassNotFoundException, ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.removeStackItem(itemId, newName);
		return getStackItemDto(objs);
	}

	@Override
	public List<StackItemDTO> setStackName(Long itemId, String stackName) throws ClassNotFoundException, ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.setStackName(itemId, stackName);
		return getStackItemDto(objs);
	}

	@Override
	public List<StackItemDTO> setStackNumber(Long itemId, Long stackNumber) throws  ClassNotFoundException, ServiceLayerException {
		List<List<?>> objs = (List<List<?>>)itemHome.setStackNumber(itemId, stackNumber);
		return getStackItemDto(objs);
	}
	
	//FIXME: client must point to chassisService directly and not via itemService API's below

	@Override
	public Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId, long bladeModelId, long faceLksValueCode, long bladeId) 
		 throws ServiceLayerException 
	{
		return this.chassisService.getAvailableChassisSlotPositionsForBlade(chassisId, bladeModelId, faceLksValueCode, bladeId);
	}

	@Override
	public List<BladeDTO> getAllBladeItem(long itemId) throws ServiceLayerException {
		return this.chassisService.getAllBladeItem(itemId);
	}

	@Override
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeModelId, long bladeId) throws ServiceLayerException {
		return this.chassisService.getAllChassisInCabinet(cabinetItemId, bladeModelId, bladeId);
	}

	@Override
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode) {
		return this.chassisService.getAllChassisInCabinet(cabinetItemId, bladeTypeLkpValueCode);
	}

	@Override
	public ChassisItemDTO getChassisInfo(long chassisItemId, long faceLksValueCode)  throws ServiceLayerException {
		return this.chassisService.getChassisInfo(chassisItemId, faceLksValueCode);
	}
	
	@Override
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId, Long faceLksValueCode) throws ServiceLayerException {
		return this.chassisService.getChassisSlotDetails(chassisId, faceLksValueCode);
	}

	@Override
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(long locationId, long bladeModelId, long bladeId) throws ServiceLayerException {
		return this.chassisService.getAllAvailableCabinetForBladeModel(locationId, bladeModelId, bladeId);
	}
	
	@Override
	public BladeDTO getFirstBladeModelForMake(long makeId) throws ServiceLayerException {
		return this.chassisService.getFirstBladeModelForMake(makeId);
	}

	@Override
	public BladeDTO getFirstBladeModelForMakeAndClass(long makeId, long classLkpValueCode) throws ServiceLayerException {
		return this.chassisService.getFirstBladeModelForMake(makeId, classLkpValueCode);
	}

	@Override
	public Long getItemClass(long itemId)  throws ServiceLayerException {
		return this.itemHome.getItemClass(itemId);
	}
	
	@Override
	public List<ValueIdDTO> getAllAvailableCabinetForZeroUModel(long locationId, long zeroUModelId, long depth_lkp_value, long side_lkp_value, long zeroUId) throws ServiceLayerException {
		return this.itemHome.getAllAvailableCabinetForZeroUModel(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
	}

	@Override
	public List<Long> deleteItemsExt(List<Integer> itemIdList, Boolean deleteAssociatedItems) throws ClassNotFoundException,	BusinessValidationException, Throwable {

		List<Long> itemIds = new ArrayList<Long>();
		for (Integer itemId: itemIdList) {
			itemIds.add(itemId.longValue());
		}
		return itemHome.deleteItemsConfirmed(itemIds, deleteAssociatedItems, true, FlexUserSessionContext.getUser());
		
	}
	
	@Override
	public List<Long> deleteItems(List<Integer> itemIdList) throws ClassNotFoundException,	BusinessValidationException, Throwable  {
		
		List<Long> itemIds = new ArrayList<Long>();
		for (Integer itemId: itemIdList) {
			itemIds.add(itemId.longValue());
		}
		return itemHome.deleteItems(itemIds, FlexUserSessionContext.getUser());
		
	}
	
	@Override
	public Collection<ValueIdDTO> getAvailableShelfPosition(Long cabinetId, int uPosition, long railsUsed, long editItemId) {
		List<ValueIdDTO> dtos = new ArrayList<ValueIdDTO>();
		Collection<Integer> shelfPositions = itemHome.getAvailableShelfPosition(cabinetId, uPosition, railsUsed, editItemId);
		for (Integer shelfPositon: shelfPositions){
			ValueIdDTO dto = new ValueIdDTO();
			dto.setData(shelfPositon);
			dto.setLabel(shelfPositon.toString());
			
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	@Override
	public boolean isChassisRearDefined(long chassisItemId) throws ServiceLayerException {
		return chassisService.isChassisRearDefined(chassisItemId);
	}
	
	@Override
	public boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId) throws ServiceLayerException {
		return chassisService.isBladeAllowedinChassisRear(chassisItemId, bladeModelId, bladeItemId);
	}

	@Override
	public boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId) throws ServiceLayerException {
		return chassisService.isBladeAllowedinChassisFront(chassisItemId, bladeModelId, bladeItemId);
	}
	
	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		return paginatedHome.getItemActionMenuStatus(itemIdList);
	}
	
	//In the case of itemRequests, the businessValidation exception is litterally generated when there are validation exceptions before 
	//committing any data into the database. Therefore, it is okay to have noRollbackFor over this as this will not harm the transaction
	//in anyway.
	//NOTE: We must ensure that businessvalidation exception are truly what it says it is. We should not be throwing this when
	//      we would like to rollback. In case when we need to rollback, we should be throwing DataAccessException as this is more appropriate
	//      for the purpose.
	// @Transactional(noRollbackFor={BusinessValidationException.class})
	@Override
	public List<RequestDTO> itemRequest( List<Long> itemIds, String typeOfRequest ) throws BusinessValidationException, DataAccessException {
		Map<String, Object> requestMap = itemHome.itemRequest(itemIds, typeOfRequest, false);

		return postProcessRequest(requestMap);
	}
	
	@Override
	public List<RequestDTO> itemRequestConfirmed( List<Long> itemIds, String typeOfRequest ) throws BusinessValidationException, DataAccessException {
		Map<String, Object> requestMap = itemHome.itemRequest(itemIds, typeOfRequest, true);

		return postProcessRequest(requestMap);
		
	}
	
	private List<RequestDTO> postProcessRequest(Map<String, Object> requestMap) throws BusinessValidationException, DataAccessException {
		@SuppressWarnings("unchecked")
		List<RequestDTO> requestDTOs = (List<RequestDTO>) requestMap.get(List.class.getName());
		BusinessValidationException bvex = (BusinessValidationException) requestMap.get(BusinessValidationException.class.getName());
		
		requestDAO.getSession().flush();
		
		itemHome.processRequest(requestDTOs, bvex);
		
		return requestDTOs;
	}
	
	@Override
	public Boolean isItemRequestStageAllowEdit(Long itemId) throws ServiceLayerException {
		return itemHome.doesItemStageAllowModification(itemId);
	}

	@Override
	public List<CloneItemDTO> cloneItems(List<CloneItemDTO> itemList) throws BusinessValidationException, Throwable {
		List<CloneItemDTO> recList = new ArrayList<CloneItemDTO>();
		UserInfo currentUser = FlexUserSessionContext.getUser();		
		BusinessValidationException be = null;
		Timestamp currentDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		itemList = this.itemHome.cloneItemRemoveDup(itemList);
		
		for(CloneItemDTO rec:itemList){
			try{
				if(rec.getCreationDate() == null){
					rec.setCreationDate(currentDate);
				}
				CloneItemDTO x= this.itemHome.cloneItem(rec, currentUser); 

				recList.add(x);
			}
			catch(BusinessValidationException ex){
				if(be == null){
					be = new BusinessValidationException(ex.getExceptionContext());
					be.setValidationErrors( ex.getValidationErrors());
					be.setValidationErrors(ex.getValidationErrorsMap());
				}
				else{
					for(String s:ex.getValidationErrors()){
						be.addValidationError(s);
					}
					for(Map.Entry<String, String> s : ex.getValidationErrorsMap().entrySet()){
						be.addValidationError(s.getKey(), s.getValue());
					}
				}					
			}
		}
		
		if(be != null ) throw be;
		
		return recList;
	}
	
	@Override
	public List<String> syncPduReadings(Long[] idsArray) throws BusinessValidationException, DataAccessException, RemoteDataAccessException, Exception {
		List<Long> ids = new ArrayList<Long>();
		for (Long id: idsArray) {
			ids.add(id);
		}
		return itemHome.syncPduReadings(ids);
	}

	@Override
	public List<ValueIdDTO> getAvailableCabinetsForSensor(String siteCode, Long sensorTypeLksValueCode, Long includeCabinetId) throws ServiceLayerException {
		return itemHome.getAvailableCabinetsForSensor(siteCode, sensorTypeLksValueCode, includeCabinetId);
	}

	@Override
	public List<UPSBankDTO> getAllUpsBanks(Long minAmpsRating) throws ServiceLayerException {
		return itemHome.getAllUpsBanks(minAmpsRating);
	}

	@Override
	public List<BreakerDTO> getAllBreakers(Long ampsRating, Boolean[] isUsed, Long[] breakerLkpValueCodes,  Long[] phases, Long breakerPortId, Long fpduItemId) { 
		return itemHome.getAllBreakers(ampsRating, isUsed, breakerLkpValueCodes, phases, breakerPortId, fpduItemId); 
	}

	@Override
	public Boolean isMoveRequestAllowed(List<Integer> itemId) throws BusinessValidationException, DataAccessException {
		return itemHome.isMoveRequestAllowed(itemId, FlexUserSessionContext.getUser());
	}

    @Override
    public List<Map<String, Object>> getCabinetItemList(Long cabinetId) throws Throwable {
        return itemHome.getCabinetItemList(cabinetId);
    }

    @Override
    public Long savePassiveItem(Long itemId, List<ValueIdDTO> dtoList) throws Throwable {
        return itemHome.savePassiveItem(itemId, dtoList, FlexUserSessionContext.getUser());
    }

    @Override
    public void deletePassiveItem(Long itemId) throws Throwable {
        itemHome.deletePassiveItem(itemId);
    }

	@Override
	public void setUserRequestBypassSetting(Boolean value) throws Throwable {
		itemHome.setUserRequestBypassSetting(value, FlexUserSessionContext.getUser());
		
	}

	@Override
	public Boolean isPIQIntegrationEanabled(Long itemId) throws Throwable {
		return itemHome.isPIQIntegrationEanabled(itemId);
	}

    /* (non-Javadoc)
     * @see com.raritan.tdz.item.service.ItemService#getItemForReport(com.raritan.tdz.reports.json.JSONReportFilterConfig)
     */
    @Override
    public List<JSONReportFilterResult> getItemForReport(JSONReportFilterConfig filterConfig) throws DataAccessException {
        return itemHome.getItemForReport(filterConfig);
    }
}