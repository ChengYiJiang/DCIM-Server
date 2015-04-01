package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.CircuitItemViewData;
import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.ItemViewData;
import com.raritan.tdz.domain.LicenseDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventStatus;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.BusinessValidationException.WarningEnum;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.home.DataCenterLocationHome;
import com.raritan.tdz.home.TicketHome;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.home.IPHome;
import com.raritan.tdz.ip.home.JSONIPAdapter;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.item.home.itemObject.PortObjectHome;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.item.home.search.ItemSearch;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.item.json.MobileSearchItemInfo;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.item.validators.DeleteItemCommonValidator;
import com.raritan.tdz.item.validators.ItemValidator;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.location.dao.LocationFinderDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.move.home.MoveHome;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.dto.LookupOptionDTO;
import com.raritan.tdz.page.home.ItemActionMenuStatus;
import com.raritan.tdz.page.home.PaginatedHome;
import com.raritan.tdz.piq.dto.SyncAllPDUReadingsStatusDTO;
import com.raritan.tdz.piq.home.PIQItemUnmap;
import com.raritan.tdz.piq.home.PIQProbeLookup;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.piq.home.PIQSyncPorts.TYPE;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.dao.SensorPortDAO;
import com.raritan.tdz.port.home.IPortObject;
import com.raritan.tdz.port.home.PortObjectFactory;
import com.raritan.tdz.port.home.PortObjectTemplate;
import com.raritan.tdz.port.home.SensorPortHelper;
import com.raritan.tdz.port.validators.ItemPortCommonValidator;
import com.raritan.tdz.port.validators.ItemPortValidator;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestHelper;
import com.raritan.tdz.request.home.RequestHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.ticket.home.TicketSaveBehavior;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.util.UniqueValidator;
import com.raritan.tdz.util.ValueIDToDomainAdaptor;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * Extended ItemHome implementation using new DTOs in dcTrackApp.
 * @author Andrew Cohen
 */
public class ItemHomeImpl extends com.raritan.tdz.home.ItemHomeImpl implements ItemHome,PaginatedHome {
	private static Logger log = Logger.getLogger("ItemHome");

    private static Map<Long, String> LOCATION_ID_CODE_MAP = new HashMap<Long, String>();
    private static Map<String, Long> LOCATION_CODE_ID_MAP = new HashMap<String, Long>();
    private static Map<Long, String> CLASS_STATUS_ID_VALUE_MAP = new HashMap<Long, String>();
    private static Map<String, Long> CLASS_STATUS_VALUE_ID_MAP = new HashMap<String, Long>();

	@Autowired(required=true)
	private ItemDTOAdapter itemDTOAdapter;	

	@Autowired
	private PIQProbeLookup piqProbelookup;

	@Autowired
	protected ItemPlacementHome itemPlacementHome;
	
	// this property need to be set via setter function 
	private PIQSyncPDUClient piqSyncpduClient;
	
	@Autowired(required=true)
	private SensorPortHelper sensorPortHelper;
	
	@Autowired
	protected DeleteItemCommonValidator itemDeleteValidatorCommon;
	
	@Autowired
	DataPortDAO dataPortDAO;

	@Autowired(required=true)
	protected SensorPortDAO  sensorPortDao; 

	@Autowired
	ItemFinderDAO itemFinderDAO;
	
	@Autowired
	PowerPortDAO powerPortDAO;
	
	@Autowired
	ModelDAO modelDAO;
	
	@Autowired
	LocationDAO locationDAO;

	/** This is for the file import.. */
	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortCreateItemPortImportValidator;
	
	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortDeleteItemPortImportValidator;

	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortUpdateItemPortImportValidator;

	
	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortDeleteItemPortValidator;

	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortUpdateItemPortValidator;
	
	/** This is for the Assets management controller. */
	@Autowired(required=true)
	private ItemPortValidator<DataPort> dataPortCreateItemPortValidator;
	
	@Autowired(required=true)
	private ItemPortCommonValidator<DataPort> dataPortReadItemValidator;

	@Autowired(required=true)
	private ItemPortValidator<PowerPort> powerPortCreateItemPortValidator;
	
	@Autowired(required=true)
	private ItemPortValidator<PowerPort> powerPortUpdateItemPortValidator;
	
	@Autowired(required=true)
	private Validator itemClassForProxyIndexValidator;
	
	@Autowired(required=true)
	private Validator itemClassForIpAddressValidator;
	
	@Autowired
	PortObjectFactory portObjectFactory;
 
	@Autowired
	private DataPortDTOAdapter dataPortDTOAdapter;

	@Autowired
	private PowerPortDTOAdapter powerPortDTOAdapter;
	
	@Autowired
	private PortObjectHome dataPortObjectHome;
	
	@Autowired
	private PortObjectHome powerPortObjectHome;
	
	@Autowired
	private PortObjectTemplate powerPortObjectTemplate;
	
	@Autowired
	private PortObjectTemplate dataPortObjectTemplate;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired(required=true)
	private UserHome userHome;
	
	@Autowired
	RequestHelper requestHelper;
	
	@Autowired
	BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired
	private EventHome eventHome;
	
	@Autowired
	private MoveHome moveHome;
	
	@Autowired(required=true)
	private IPHome ipHome;

	@Autowired(required=true)
	private JSONIPAdapter jsonNetworkAdapter;
	
	@Autowired(required=true)
	private IPAddressDetailsDAO ipAddressDetailsDAO;
	@Autowired
	private PIQItemUnmap piqItemUnmap;

	private ContainerItemHome containerItemHome;

	private SessionFactory sessionFactory;
	private ItemObjectFactory itemObjectFactory;
	private ItemObjectFactory itemObjectTemplateFactory;
	private ItemSearch itemSearch;
	private ResourceBundleMessageSource messageSource;

	private RulesProcessor rulesProcessor;
	private RemoteRef remoteReference;
	private UniqueValidator systemPropertyValidator;
	private ItemValidator itemValidator;
	private ValueIDToDomainAdaptor itemDomainAdaptor;
	private ItemStateContext itemStateContext;
	private ItemRequest itemRequest;
	
	private FieldHome fieldHome;
	private PaginatedHome paginatedHome;
	
	private ItemModifyRoleValidator itemModifyRoleValidator;
	
	@Autowired
	private ItemRequestDAO itemRequestDAO;

	private Map<String, UniqueValidator> uniqueValidators;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	ItemClone itemClone;
	
	@Autowired
	private TicketSaveBehavior requestTicketSaveBehavior;
	
	@Autowired
	private PassiveItemObjectPassiveItem passiveItemObject;
	
	@Autowired(required=true)
	private TicketSaveBehavior placementShelfPositionTicketSaveBehavior;

	public PIQSyncPDUClient getPiqSyncpduClient() {
		return piqSyncpduClient;
	}

	public void setPiqSyncpduClient(PIQSyncPDUClient piqSyncpduClient) {
		this.piqSyncpduClient = piqSyncpduClient;
	}

	public ContainerItemHome getContainerItemHome() {
		return containerItemHome;
	}

	public void setContainerItemHome(ContainerItemHome containerItemHome) {
		this.containerItemHome = containerItemHome;
	}
	
	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public ItemModifyRoleValidator getItemModifyRoleValidator() {
		return itemModifyRoleValidator;
	}

	public void setItemModifyRoleValidator(
			ItemModifyRoleValidator itemModifyRoleValidator) {
		this.itemModifyRoleValidator = itemModifyRoleValidator;
	}

	public UniqueValidator getSystemPropertyValidator() {
		return systemPropertyValidator;
	}

	public void setSystemPropertyValidator(UniqueValidator systemPropertyValidator) {
		this.systemPropertyValidator = systemPropertyValidator;
	}
	
	public ItemValidator getItemValidator() {
		return itemValidator;
	}

	public void setItemValidator(ItemValidator itemValidator) {
		this.itemValidator = itemValidator;
	}

	public ValueIDToDomainAdaptor getItemDomainAdaptor() {
		return itemDomainAdaptor;
	}

	public void setItemDomainAdaptor(ValueIDToDomainAdaptor itemDomainAdaptor) {
		this.itemDomainAdaptor = itemDomainAdaptor;
	}

	public ItemObjectFactory getItemObjectFactory() {
		return itemObjectFactory;
	}

	public void setItemObjectFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectFactory = itemObjectFactory;
	}
	public ItemObjectFactory getItemObjectTemplateFactory() {
		return itemObjectTemplateFactory;
	}

	public void setItemObjectTemplateFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectTemplateFactory = itemObjectFactory;
	}

	public ItemStateContext getItemStateContext() {
		return itemStateContext;
	}

	public void setItemStateContext(ItemStateContext itemStateContext) {
		this.itemStateContext = itemStateContext;
	}

	public FieldHome getFieldHome() {
		return fieldHome;
	}

	public void setFieldHome(FieldHome fieldHome) {
		this.fieldHome = fieldHome;
	}
	
	public ItemRequest getItemRequest() {
		return itemRequest;
	}

	public void setItemRequest(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}

	public ItemHomeImpl(SessionFactory sessionFactory,
			DataCenterLocationHome dataCenterLocationHome, 
			ModelHome modelHome,
			TicketHome ticketHome,
			ItemObjectFactory itemObjectFactory
			) {
		super(sessionFactory, dataCenterLocationHome, modelHome, ticketHome);
		this.sessionFactory = sessionFactory;
		this.itemObjectFactory = itemObjectFactory;
	}
	
	@Override
	public List<CircuitItemViewData> viewItemsForLocation(Long locationId, boolean activeItemOnly, Long itemId, Long portClassValueCode) throws DataAccessException {
		List<CircuitItemViewData> recList = new ArrayList<CircuitItemViewData>();

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(CircuitItemViewData.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			if(itemId != null){
				criteria.add(Restrictions.eq("itemId", itemId));
			}
			else{
				if(locationId != null && locationId > 0){
					criteria.add(Restrictions.eq("locationId", locationId));
				}

				if(activeItemOnly){
					//criteria.createAlias("statusLookup","status");
					criteria.add( Restrictions.le("statusLksId", SystemLookup.getLksDataId(session, SystemLookup.ItemStatus.INSTALLED)));
				}

				List<Long> tempList = new ArrayList<Long>();
				tempList.add(SystemLookup.Class.DATA_PANEL);
				tempList.add(SystemLookup.Class.DEVICE);
				tempList.add(SystemLookup.Class.FLOOR_OUTLET);
				tempList.add(SystemLookup.Class.NETWORK);
				tempList.add(SystemLookup.Class.RACK_PDU);
				tempList.add(SystemLookup.Class.PROBE);
				tempList.add(SystemLookup.Class.FLOOR_PDU);
				tempList.add(SystemLookup.Class.UPS);
				tempList.add(SystemLookup.Class.CRAC);

				criteria.add(Restrictions.in("classLkpValueCode", tempList));
			}
			
			if(portClassValueCode != null && portClassValueCode > 0){
				if(portClassValueCode == SystemLookup.PortClass.DATA){
					List<Long> tempList = new ArrayList<Long>();
					tempList.add(SystemLookup.SubClass.BLADE);
					tempList.add(SystemLookup.SubClass.CHASSIS);
					tempList.add(SystemLookup.SubClass.RACKABLE);
					
					criteria.add(Restrictions.or(Restrictions.gt("freeDataPortCount", 0),  Restrictions.in("subclassLkpValueCode", tempList)));
				}
				else{
					//this will force that user does not connect a input cord that has not outlets
					criteria.add(Restrictions.gt("freePowerPortCount", 0));
				}
			}

			for(Object o:criteria.list())
			{
				CircuitItemViewData rec = (CircuitItemViewData)o;
								
				if(rec.getStatusLkpValueCode() == SystemLookup.SubClass.RACKABLE && rec.getFreeDataPortCount() == 0
						&& rec.getVmClusterLkuId() == null){
					continue;
				}
				
				recList.add(rec);
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return recList;
	}
	
	@Override
	public List<DataPort> viewDataPortsForItem(Long itemId, boolean freePortOnly, List<Long>portList) throws DataAccessException {
		List<DataPort> recList = new ArrayList<DataPort>();

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(DataPort.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			if(itemId != null && itemId != 0){
				criteria.add(Restrictions.eq("item.itemId", itemId));
			}

			if(freePortOnly){
				criteria.add(Restrictions.eq("used", false));
			}

			if(portList != null && portList.size() > 0){
				criteria.add(Restrictions.in("portId", portList));
			}

			criteria.addOrder(Order.asc("portName"));

			for(Object o:criteria.list())
			{
				recList.add((DataPort)o);
			}
		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return recList;
	}

 	public List<PowerPort> viewPowerPortsForItem(Long itemId, boolean freePortOnly, List<Long>portList) throws DataAccessException {
 		List<PowerPort> recList = new ArrayList<PowerPort>();
 
 		try{
 			Session session = this.sessionFactory.getCurrentSession();
 			Criteria criteria = session.createCriteria(PowerPort.class);
 			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
 			
 			if(itemId != null && itemId != 0){
 				criteria.add(Restrictions.eq("item.itemId", itemId));
 			}
 
 			if(freePortOnly){
 				criteria.add(Restrictions.eq("used", false));
 			}
 
 			if(portList != null && portList.size() > 0){
 				criteria.add(Restrictions.in("portId", portList));
 			}
 
 			criteria.addOrder(Order.asc("portName"));
 
 			for(Object o:criteria.list())
 			{
 				PowerPort port = (PowerPort)o;
 				lazyLoadPowerPort(port);
 				
 				if(port.getBreakerPort() != null){
 					lazyLoadPowerPort(port.getBreakerPort());
 				}
 				
 				recList.add(port);
 			}
 		}catch(HibernateException e){
 
 			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
 
 		}catch(org.springframework.dao.DataAccessException e){
 
 			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
 		}
 
 		return recList;
 	}
	@Override
	public Map<Integer, String> getDataPortNetInfoAsMap(Long itemId) {
		HashMap<Integer, String> netinfo = new HashMap<Integer, String>();

    	Session session = this.sessionFactory.getCurrentSession();

    	//These tables need to be move to dct_????
	    String SQL_QUERY = " select tblipteaming.portid, tblipaddresses.ipaddress, tblnetworks.subnet " +
	    "from tblipaddresses inner join tblipteaming on tblipaddresses.id = tblipteaming.ipaddressid " +
	    "inner join tblnetworks on tblipaddresses.networkid = tblnetworks.id " +
	    "inner join dct_ports_data on tblipteaming.portid = dct_ports_data.port_data_id " +
	    "where dct_ports_data.item_id = " + itemId.toString() + " " +
	    "order by tblipteaming.portid ";

		org.hibernate.SQLQuery query = session.createSQLQuery(SQL_QUERY);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			netinfo.put((Integer)row[0], (String)row[1] + "|" + (String)row[2] );
		}

	    return netinfo;
	}
	
	
	//???why we need railsUsed here?
	private int getCabinetRuHeight(Long cabinetId, long railsUsed)  throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria hibernateCriteria = session.createCriteria(CabinetItem.class);
		hibernateCriteria =hibernateCriteria.add( Restrictions.eq("itemId", cabinetId));
		CabinetItem cabinet = (CabinetItem) hibernateCriteria.uniqueResult() ;

		if (null != cabinet && null != cabinet.getModel()) {
			return cabinet.getModel().getRuHeight();
		}
		else {
			return 0;
		}
	}
	
	private Collection<Long> processCabinetView(String[] view, long modelId) {
		Collection<Long> availableUPos = new ArrayList<Long>();
		ModelDetails model = getModelDetails(modelId);
		int itemRUHeight = model.getRuHeight();

		// process view
		for (Integer i = 1; i < view.length; i++) {
			if (null == view[i]) {
				view[i] = "A";
			}					
		}
		int iIncr = 1;
		for (Integer i = 1; i < view.length; i += iIncr) {
			iIncr = 1;
			if (null != view[i] && view[i].equals("R")) {
				continue;
			}
			if (null != view[i] && view[i].startsWith("D")) {
				for (int dummyI = i; 
						dummyI + 1 < view.length && view[dummyI].equals(view[dummyI + 1]); 
						dummyI++) {
					iIncr++;
				}
			}
			int terminating_index = i + itemRUHeight -1; 
			if (terminating_index >= view.length) {
				break;
			}
			boolean found = true;
			for (Integer j = i; j < terminating_index; j++) {
				if (!view[j].equals(view[j + 1]) && !view[j + 1].equals("A")) {
					found = false;
					break;
				}
			}
			if (found) {
				availableUPos.add(i.longValue());
			}
		}
		return availableUPos;
	}
	
	private Collection<Long> processAvailableUPosition(
			String[] cabinetFrontView, String[] cabinetBackView, long modelId, long railsUsed) {
		Collection<Long> availableUPos = new ArrayList<Long>();
		if (SystemLookup.RailsUsed.FRONT == railsUsed) {
			availableUPos = processCabinetView(cabinetFrontView, modelId);
		}
		else if (SystemLookup.RailsUsed.REAR == railsUsed) {
			// process cabinetBackView
			availableUPos = processCabinetView(cabinetBackView, modelId);
		}
		else {
			// process both views
			Collection<Long> availableRearUPos = new ArrayList<Long>();
			availableUPos = processCabinetView(cabinetFrontView, modelId);
			availableRearUPos = processCabinetView(cabinetBackView, modelId);
			availableUPos.retainAll(availableRearUPos);
		}
		return availableUPos;
	}
	
	@Override
	public Collection<Long> getNonRackableUPosition(Long cabinetId,
			long modelId, long railsUsed, long editItemId) throws DataAccessException {
		
		ModelDetails model = getModelDetails(modelId);
		if (null == model || model.getClassLookup().getLkpValueCode() == SystemLookup.Class.FLOOR_OUTLET) {
			return new ArrayList<Long>();
		}
		
    	Session session = this.sessionFactory.getCurrentSession();
    	Collection<Long> availableUPositions = null;

    	Criteria criteria = session.createCriteria(Item.class);
    	criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
    	criteria.add(Restrictions.eq("parentItem.itemId", cabinetId));
    	criteria.add(Restrictions.gt("uPosition", 0L));
    	
    	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    
		@SuppressWarnings("unchecked")
		List<Item> itemList = criteria.list();
		assert(itemList != null);

		availableUPositions = getAvailableNRUPositions(itemList, cabinetId, railsUsed, modelId, editItemId);
    	
	    return availableUPositions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> getAvailableShelfPosition(Long cabinetId, long uPosition, long railsUsed, long editItemId) {
    	Criteria criteria = getNonRackableItemsCriteria(cabinetId, uPosition, railsUsed);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("shelfPosition"))));

		criteria.addOrder(Order.asc("shelfPosition"));
    	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    
		List<Integer> shelfPositionList = criteria.list();

		if (null == shelfPositionList || shelfPositionList.size() == 0) {
			shelfPositionList.add(new Integer(1));
		}
		else {
			/* if editing a non rackable item and if this item is in the same u position, do not add new position */
			Item editItem = getItemDomainObject (editItemId);
			if (null != editItem && 
					null != editItem.getParentItem() &&
					null != editItem.getModel() &&
					editItem.getModel().getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {
				Long mountedRails = (null != editItem.getMountedRailLookup()) ? editItem.getMountedRailLookup().getLkpValueCode() : SystemLookup.RailsUsed.BOTH;
				if (editItem.getParentItem().getItemId() != cabinetId ||
					mountedRails != railsUsed ||
					editItem.getUPosition() != uPosition) {
					shelfPositionList.add(shelfPositionList.get(shelfPositionList.size() - 1) + 1);
				}
			}
			else {
				if (null != shelfPositionList.get(shelfPositionList.size() - 1)) {
					shelfPositionList.add(shelfPositionList.get(shelfPositionList.size() - 1) + 1);
				}
				else {
					shelfPositionList.add(new Integer(1));
				}
			}
		}
		
		if (!shelfPositionList.contains(null)){
			@SuppressWarnings("rawtypes")
			Comparator comparator = Collections.reverseOrder();
			Collections.sort((List<Integer>) shelfPositionList, comparator);

        } else {
            // The shelfPositionList contains "null" shelf position.

            List<Integer> tempList = new ArrayList<Integer>();
            for (Integer shelfPositon: shelfPositionList) {
                // Filter out the "null".
                if (shelfPositon != null) {
                    tempList.add(shelfPositon);
                }
            }

            if (tempList.size() == 0) {
                // The shelfPositionList can't be empty, add a default shelfPositon.
                tempList.add(new Integer(1));
            }

            shelfPositionList = tempList;
        }

	    return shelfPositionList;
	}

	@SuppressWarnings("unchecked")
	private List<Item> getNRItems (long cabinetId, long uPosition, long railsLkpValueCode) {
		// get list of items on shelf order by num port
		Criteria c = getNonRackableItemsCriteria(cabinetId, uPosition, railsLkpValueCode);
		c.addOrder(Order.asc("shelfPosition"));
		c.addOrder(Order.desc("itemId"));
		
		return (List<Item>) c.list();
	}
	
	/*
	 * TODO:: to be completed this will be called when the cabinet and/or the u
	 * position of the current non-rackable item is changed the shelf position
	 * where the item is currently placed need to be updated the shelf position
	 * where the item is getting placed need to be updated
	 */
	@Override
	public void updateShelfPosition (long cabinetId, long uPosition,
			long railsLkpValueCode, Object itemDomain, Item ignoreItem) throws DataAccessException {
		boolean debug = false; 	
		Item newItem = null;
		Session session = this.sessionFactory.getCurrentSession();

		if (itemDomain != null) {
			newItem = (Item) itemDomain;
			if (debug) log.debug ("newItemShelfPosition = " + newItem.getShelfPosition());
		}

		List <Item> itemList = getNRItems (cabinetId, uPosition, railsLkpValueCode);
		List <Item> itemList1 = new ArrayList<Item>(); 
		if (null != ignoreItem) {
			for (Item item: itemList) {
				if (item.getItemId() != ignoreItem.getItemId()) {
					itemList1.add(item);
				}
			}
		}
		else {
			itemList1 = itemList;
		}
		if (itemList1 == null) return;

		if (debug) {
			log.debug( "BEFORE: " );
			for (Item item : itemList1) {
				log.debug("itemId = " + item.getItemId() + 
						", itemName = " + item.getItemName()	+ 
						" => " + item.getShelfPosition()); 
			}
		}	

		// newItem is an item updated/added
		if (newItem != null) {
			int newItemShelfPosition = newItem.getShelfPosition();
			for (Item item : itemList1) {
				int shelfPosition = item.getShelfPosition();
				// if parent does not match it means, item has moved to different cabinet
				// moving item has created gap, skip this loop and fix it. 
				if ((item.getParentItem().getItemId() != newItem.getParentItem().getItemId()) ||
						(item.getUPosition() != newItem.getUPosition()) ||
						(item.getMountedRailLookup().getLkpValueCode() != newItem.getMountedRailLookup().getLkpValueCode())) break;
				
				if (shelfPosition < newItemShelfPosition
						|| (shelfPosition == newItemShelfPosition && item.getItemId() == newItem
								.getItemId())) {
					// do nothing, just move to next item
					continue;
				}
				if (debug) {
					log.debug("DURING : itemId = " + item.getItemId() + 
						", itemName = " + item.getItemName() +
						" => " + shelfPosition + "-> " + shelfPosition +1);
				}
				// Move item to the right
				item.setShelfPosition(shelfPosition + 1);
				Object[] additionalArgs = null;
				placementShelfPositionTicketSaveBehavior.update(item, additionalArgs);
				session.update(item);
			}
		}
			
		itemList = getNRItems (cabinetId, uPosition, railsLkpValueCode);
		List <Item> itemList2 = new ArrayList<Item>();
		if (null != ignoreItem) {
			for (Item item: itemList) {
				if (item.getItemId() != ignoreItem.getItemId()) {
					itemList2.add(item);
				}
			}
		}
		else {
			itemList2 = itemList;
		}

		if (itemList2 == null) return;
	
		if (debug) {	
			log.debug ( "BEFORE Moving back: \n" );
			for (Item item : itemList2) {
				log.debug("itemId = " + item.getItemId() + 
						", itemName = " + item.getItemName() +
						" => " + item.getShelfPosition());
			}
		}
		
		int position = 1;
		// update any hole in the list.
		for (Item item : itemList2) {
			int shelfPosition = item.getShelfPosition();
			if (position == shelfPosition) {
				// do nothing, just move to next item
			} else if (position != shelfPosition) {
				if ((newItem != null && 
						item.getParentItem().getItemId() == newItem.getParentItem().getItemId() &&
						item.getUPosition() == newItem.getUPosition() &&
						item.getMountedRailLookup().getLkpValueCode() == newItem.getMountedRailLookup().getLkpValueCode() && 
						item.getItemId() == newItem.getItemId())) {
					continue;
				} else {
					if ( newItem != null && 
							item.getParentItem().getItemId() == newItem.getParentItem().getItemId() && 
							item.getUPosition() == newItem.getUPosition() && 
							item.getMountedRailLookup().getLkpValueCode() == newItem.getMountedRailLookup().getLkpValueCode() && 
							position == newItem.getShelfPosition())position ++;
					item.setShelfPosition(position);
					Object[] additionalArgs = null;
					placementShelfPositionTicketSaveBehavior.update(item, additionalArgs);
					session.update(item);
					if (debug) log.debug("Changing item shelfPosition from " + shelfPosition + " to " + position);
				}
			}
			position++;
		}

		if (debug) {
			List <Item> itemList3 = getNRItems (cabinetId, uPosition, railsLkpValueCode);
			if (itemList3 == null) return;
			log.debug( "After adjusting shelf position: " );
			for (Item item : itemList3) {
				log.debug("itemId = " + item.getItemId() + 
						", itemName = " + item.getItemName()	+ 
						" => " + item.getShelfPosition()); 
			}
		}
	}
	
	private Item getItemInfo(Long itemId) throws DataAccessException {
		Item item = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria itItemCriteria = session.createCriteria(Item.class);
			
			itItemCriteria.add(Restrictions.eq("itemId", itemId));
		
			item = (Item) itItemCriteria.uniqueResult();
		}
		catch(HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		return item;
	}

	private String getZeroULayout(long cabinetId, long depth_lkp_value, long side_lkp_value, 
			int expItemUPosition, int expItemRUHeight) {
		
		Map<String, Object> itemLayout = (Map<String, Object>) itemDAO.getItemLayout (cabinetId);
		
		String layout = new String();
		if (SystemLookup.RailsUsed.LEFT_REAR == side_lkp_value) {
			if (SystemLookup.ZeroUDepth.FRONT == depth_lkp_value) {
				layout = (String)itemLayout.get("verticalLeft");
			}
			else if (SystemLookup.ZeroUDepth.REAR == depth_lkp_value) {
				layout = (String)itemLayout.get("verticalLeftBack");
			}
		} else if (SystemLookup.RailsUsed.RIGHT_REAR == side_lkp_value) {
			if (SystemLookup.ZeroUDepth.FRONT == depth_lkp_value) {
				layout = (String)itemLayout.get("verticalRight");
			}
			else if (SystemLookup.ZeroUDepth.REAR == depth_lkp_value) {
				layout = (String)itemLayout.get("verticalRightBack");
			}
		}

		if (expItemUPosition > 0 && expItemRUHeight > 0) {
			char[] manipuilatedLayout = layout.toCharArray();
			for (int i = expItemUPosition - 1; i < expItemUPosition + expItemRUHeight - 1; i++) {
				manipuilatedLayout[i] = '0';
			}
			return new String(manipuilatedLayout);
		}
		return layout;
	}
	
	private Collection<Long> processZeroULayout(String layout, int itemRUHeight) {
		Collection<Long> availableUPos = new ArrayList<Long>();

		// process view
		if (null == layout) return availableUPos;
		
		for (Integer i = 0; i < layout.length(); i++) {
			if (layout.charAt(i) == '1') {
				continue;
			}
			if (i + itemRUHeight > layout.length()) {
				break;
			}
			boolean found = true;
			for (int j = i; j < i + itemRUHeight; j++) {
				if (layout.charAt(j) == '1') {
					found = false;
					break;
				}
			}
			if (found) {
				availableUPos.add(i.longValue() + 1);
			}
		}
		return availableUPos;
	}

	private ModelDetails getModelDetails(long modelId) {
		// TODO Auto-generated method stub
		
		Session session = this.sessionFactory.getCurrentSession();
		Criteria modelCriteria = session.createCriteria(ModelDetails.class);
		
		modelCriteria.add(Restrictions.eq("modelDetailId", modelId));
	
		ModelDetails model = (ModelDetails) modelCriteria.uniqueResult();
		return model;
	}
	
	/* depth_lkp_value: 7083: zerou-front, 7084: zerou-back 
	 * side_lkp_value: 8004: left on cabinet rear, 8005: right on cabinet rear */
	@Override
	public Collection<Long> getAvailableZeroUPositions(Long cabinetId, 
			long depth_lkp_value, long side_lkp_value, int itemRUHeight, 
			Long exceptionItemId) 
					throws DataAccessException {
		Item expItem = null;
		if (null != exceptionItemId && exceptionItemId > 0) {
			expItem = getItemInfo(exceptionItemId);
		}
		
		int expItemUPosition = 0;
		int expItemRUHeight = 0;
		
		if(null != expItem && null != expItem.getParentItem()){
			if (null != expItem.getMountedRailLookup() && null != expItem.getFacingLookup() &&
					null != expItem.getModel() &&
					expItem.getFacingLookup().getLkpValueCode() == depth_lkp_value &&
					expItem.getMountedRailLookup().getLkpValueCode() == side_lkp_value &&
					expItem.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U) &&
					expItem.getParentItem().getItemId() == cabinetId) {
				expItemUPosition = new Long(expItem.getUPosition()).intValue();
				expItemRUHeight = expItem.getModel().getRuHeight();
			}
		}
		
		String layout = getZeroULayout(cabinetId, depth_lkp_value, side_lkp_value, expItemUPosition, expItemRUHeight);
		Collection<Long> availableZeroUPosition = processZeroULayout(layout, itemRUHeight);
		return availableZeroUPosition;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object[]> getAllCabinets(long locationId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CabinetItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("dataCenterLocation","dataCenterLocation", Criteria.LEFT_JOIN);
		criteria.createAlias("statusLookup","statusLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("itemId"))
				.add(Projections.property("itemName"))));
		criteria.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId));
		criteria.add(Restrictions.isNotNull("model"));
		criteria.add(Restrictions.and(Restrictions.isNotNull("statusLookup"),     
                Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));
		criteria.addOrder(Order.asc("itemName"));

		return criteria.list();
	}
	
	@Override
	public List<ValueIdDTO> getAllAvailableCabinetForZeroUModel(long locationId, long zeroUModelId, long depth_lkp_value, long side_lkp_value, long zeroUId) throws DataAccessException {
		List<ValueIdDTO> availableCabinets = new ArrayList<ValueIdDTO>();
		
//		ModelDetails model =  getModelDetails(zeroUModelId);
//		if (null == model || !model.getMounting().equals("ZeroU")) {
//			return availableCabinets;
//		}
		List<String> projectionFields = new ArrayList<String>(Arrays.asList("mounting", "ruHeight"));
		Map<String, Object> modelDetail = modelDAO.getModelMountingAndRuHeight(zeroUModelId, projectionFields);
		if (modelDetail.size() == 0 || !((String)modelDetail.get("mounting")).equals("ZeroU")) {
			return availableCabinets;
		}
		
		List<Object[]> cabinetList = getAllCabinets(locationId);
		Item zeroUItem = null;
		long exceptionZeroUParentItemId = -1;
		int expItemUPosition = -1;
		int expItemRUHeight = -1;
		
		if (zeroUId > 0) {
			Session session = sessionFactory.getCurrentSession();
			zeroUItem = (Item)session.get(Item.class, zeroUId);
			if (null != zeroUItem && 
					null != zeroUItem.getParentItem() && 
					null != zeroUItem.getModel() && 
					zeroUItem.getModel().getMounting().equals("ZeroU")) {
				final long savedDepthPos = zeroUItem.getFacingLookup() != null ? zeroUItem.getFacingLookup().getLkpValueCode() : 0;
				final long savedCabinetSide = zeroUItem.getMountedRailLookup() != null ? zeroUItem.getMountedRailLookup().getLkpValueCode() : 0;
				
				// Saved cabinet side and depth position must match the selected cabinet side and depth position
				// in order to include the item's current position in the list of available U-positions
				if (savedDepthPos == depth_lkp_value && savedCabinetSide == side_lkp_value) {
					exceptionZeroUParentItemId = zeroUItem.getParentItem().getItemId();
					expItemUPosition = (new Long(zeroUItem.getuPosition())).intValue();
					expItemRUHeight = zeroUItem.getModel().getRuHeight();
				}
			}
		}
		
		List<Long> moveItemIds = powerPortMoveDAO.getMovingItems();
		if (null == moveItemIds) moveItemIds = new ArrayList<Long>();
		
		//TODO: This is a very costly call. We are looping on each cabinet to find available upos
		//TODO: And then filling up the dto. We have to change this to make it more efficient.
		int itemRUHeight = (Integer)modelDetail.get("ruHeight");
		for (Object[] cabinet: cabinetList) {
			if (null == cabinet[0] || null == cabinet[1]) {
				continue;
			}
			long cabinetItemId = (Long)cabinet[0];
			
			// filter out all cabinets that are -when-moved cabinets 
			if (moveItemIds.contains(cabinetItemId)) continue;
			
			String cabinetItemName = (String)cabinet[1];

			Collection<Long> availableUPos = null;
			if (exceptionZeroUParentItemId == cabinetItemId) {
				availableUPos = getAvailableZeroUPositions(cabinetItemId, 
					depth_lkp_value, side_lkp_value, itemRUHeight, 
					zeroUItem.getItemId());
			}
			else {
				availableUPos = getAvailableZeroUPositions(cabinetItemId, 
						depth_lkp_value, side_lkp_value, itemRUHeight, -1L);
			}
			if (null != availableUPos && !availableUPos.isEmpty()) {
				ValueIdDTO dto = new ValueIdDTO();
				dto.setData(cabinetItemId);
				dto.setLabel(cabinetItemName);
				availableCabinets.add(dto);
			}
		}
		return availableCabinets;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Long> getAvailableNRUPositions(List<Item> itemList,
			long cabinetId, long railsUsed, long modelId, long editItemId) throws DataAccessException {
		Collection<Long> resultArray = new ArrayList<Long>();
		int cabinetRuHeight = getCabinetRuHeight(cabinetId, railsUsed);
		if (cabinetRuHeight <= 0) {
			return resultArray;
		}
		String[] cabinetFrontView = new String[cabinetRuHeight + 1];
		String[] cabinetBackView = new String[cabinetRuHeight + 1];
		String mountType = new String();
		long mountRail = SystemLookup.RailsUsed.BOTH;
		long u_position = (long) -1;
		int ru_height = -1;
		boolean isShelf = false;
		
		for(Object o:itemList)
		{
			Item item = (Item)o;
			if (item.getModel() == null) {
				// return new ArrayList<Long>();
				continue;
			}
			
			mountType = item.getModel().getMounting();
			mountRail = (null != item.getMountedRailLookup()) ? item.getMountedRailLookup().getLkpValueCode() : SystemLookup.RailsUsed.BOTH;
			ru_height = item.getModel().getRuHeight();
			u_position = item.getUPosition();
			isShelf = (null != item.getSubclassLookup()) ? (item.getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.SHELF) : false;  
			if (u_position <= 0) {
				continue;
			}
			if (mountType.equals(SystemLookup.Mounting.NON_RACKABLE) || isShelf) {
				for (Long i = u_position; i < u_position + ru_height  && i < cabinetRuHeight + 1; i++) {
					if (mountRail == SystemLookup.RailsUsed.FRONT) {
						cabinetFrontView[i.intValue()] = "D" + u_position;
					}
					else if (mountRail == SystemLookup.RailsUsed.REAR) {
						cabinetBackView[i.intValue()] = "D" + u_position;
					}
					else {
						cabinetFrontView[i.intValue()] = "D" + u_position;
						cabinetBackView[i.intValue()] = "D" + u_position;
					}
				}
			}
			else if (mountType.equals(SystemLookup.Mounting.RACKABLE)) {
				for (Long i = u_position; i < u_position + ru_height && i < cabinetRuHeight + 1; i++) {
					if (mountRail == SystemLookup.RailsUsed.FRONT) {
						cabinetFrontView[i.intValue()] = "R";
					}
					else if (mountRail == SystemLookup.RailsUsed.REAR) {
						cabinetBackView[i.intValue()] = "R";
					}
					else {
						cabinetFrontView[i.intValue()] = "R";
						cabinetBackView[i.intValue()] = "R";
					}
				}
			}
			else {
				// Do not support mountType in the placement algorithm
			}
		}
		if (editItemId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Item exceptionItem = (Item)session.get(Item.class, editItemId);
			Long mountedRails = (null != exceptionItem.getMountedRailLookup()) ? exceptionItem.getMountedRailLookup().getLkpValueCode() : SystemLookup.RailsUsed.BOTH;
			if (null != exceptionItem && null != exceptionItem.getParentItem() && null != mountedRails && null != exceptionItem.getModel() && null != exceptionItem.getModel().getMounting() && 
					exceptionItem.getParentItem().getItemId() == cabinetId && 
					mountedRails == railsUsed &&
					exceptionItem.getModel().getMounting().equals(SystemLookup.Mounting.RACKABLE)) {
				u_position = exceptionItem.getUPosition();
				ru_height = exceptionItem.getModel().getRuHeight();
				if (u_position > 0) {
					if (SystemLookup.RailsUsed.FRONT == railsUsed || SystemLookup.RailsUsed.BOTH == railsUsed) {
						for (Long i = u_position; i < u_position + ru_height  && i < cabinetRuHeight + 1; i++) {
							cabinetFrontView[i.intValue()] = "D" + u_position;
						}
					}
					if (SystemLookup.RailsUsed.REAR == railsUsed || SystemLookup.RailsUsed.BOTH == railsUsed) {
						for (Long i = u_position; i < u_position + ru_height  && i < cabinetRuHeight + 1; i++) {
							cabinetBackView[i.intValue()] = "D" + u_position;
						}
					}
				}
			}
		}
		resultArray = processAvailableUPosition(cabinetFrontView, cabinetBackView, modelId, railsUsed);
		@SuppressWarnings("rawtypes")
		Comparator comparator = Collections.reverseOrder();
		Collections.sort((List<Long>) resultArray, comparator);
		return resultArray;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public HashMap<Long, Long> getItemPortCount(Long portClass, boolean freePortOnly){
		HashMap<Long, Long> portCount = new HashMap<Long, Long>();

    	Session session = this.sessionFactory.getCurrentSession();
	    String SQL_QUERY = "";

	    if(portClass == SystemLookup.PortClass.POWER){
	    	SQL_QUERY = "select port.item.itemId, count(*) from PowerPort port ";
	    }
	    else{
	    	SQL_QUERY = "select port.item.itemId, count(*) from DataPort port ";
	    }

	    if(freePortOnly){
	    	SQL_QUERY += " where port.used = false ";
	    }

	    SQL_QUERY +=  "  group by port.item.itemId";

		Query query = session.createQuery(SQL_QUERY);

		for (Iterator it = query.iterate(); it.hasNext();) {
			Object[] row = (Object[]) it.next();
			portCount.put((Long)row[0], (Long)row[1]);
		}

	    return portCount;
	}
	
        /**
        * Added by Bunty Nasta
        */
        @Override
        public Collection<Long> getAvailUPositionsForItem(Long cabinetId, int itemRUHeight, int itemUPosition, int exceptionBlockSize, long railsUsed) throws DataAccessException {

                //fix railsUsed value to default - RAILS_BOTH_CODE when input incorrect value
                if(railsUsed!=GlobalConstants.RAILS_FRONT_CODE &&
                        railsUsed!=GlobalConstants.RAILS_REAR_CODE &&
                        railsUsed!=GlobalConstants.RAILS_BOTH_CODE) {
                        railsUsed=GlobalConstants.RAILS_BOTH_CODE;
                }

                if(railsUsed==GlobalConstants.RAILS_FRONT_CODE ||
                        railsUsed==GlobalConstants.RAILS_REAR_CODE) {

                		Integer[] exceptionBlockSizeList = new Integer[] {exceptionBlockSize};
                		Long[] exceptionIndexList = new Long[] {(long) itemUPosition};
                		Long[] exceptionRailsUsed = new Long[] {SystemLookup.RailsUsed.BOTH};
                        return getAvailableUPositionsForItem(cabinetId, itemRUHeight, 1, exceptionIndexList, exceptionBlockSizeList, exceptionRailsUsed, railsUsed);
                } else if(railsUsed==GlobalConstants.RAILS_BOTH_CODE) {

                		Integer[] exceptionBlockSizeList = new Integer[] {exceptionBlockSize};
	            		Long[] exceptionIndexList = new Long[] {(long) itemUPosition};
	            		Long[] exceptionRailsUsed = new Long[] {SystemLookup.RailsUsed.BOTH};

                        ArrayList<Long> frontList=(ArrayList<Long>)getAvailableUPositionsForItem(cabinetId, itemRUHeight, 1, exceptionIndexList, exceptionBlockSizeList, exceptionRailsUsed, SystemLookup.RailsUsed.FRONT);
                        ArrayList<Long> rearList=(ArrayList<Long>)getAvailableUPositionsForItem(cabinetId, itemRUHeight, 1, exceptionIndexList, exceptionBlockSizeList, exceptionRailsUsed, SystemLookup.RailsUsed.REAR);
                        ArrayList<Long> bothList = new ArrayList<Long>();
                        frontList.retainAll(rearList);
                        for (Long uPos: rearList) {
                                if (frontList.contains(uPos)) {
                                        bothList.add(uPos);
                                }
                        }
                        return bothList;
                }

                return null;
        }

	/**
	* Added by Randy Chen
	*/
	@Override
	public Collection<Long> getAvailableUPositions(Long cabinetId, int itemRUHeight, long itemId, long railsUsed, Long reservationId) throws DataAccessException {
		
		
		List<Long> itemIds = moveHome.getExceptionItemIds(itemId);
		
		//fix railsUsed value to default - RAILS_BOTH_CODE when input incorrect value
		if(railsUsed!=GlobalConstants.RAILS_FRONT_CODE && 
			railsUsed!=GlobalConstants.RAILS_REAR_CODE &&
			railsUsed!=GlobalConstants.RAILS_BOTH_CODE) {
			railsUsed=GlobalConstants.RAILS_BOTH_CODE;
		}
	
		if(railsUsed==GlobalConstants.RAILS_FRONT_CODE || 
			railsUsed==GlobalConstants.RAILS_REAR_CODE) {
			
			ArrayList<Long> availableUPositions = (ArrayList<Long>) getAvailableUPositionsByRails(cabinetId, itemRUHeight, itemIds, railsUsed, reservationId);
			
			return availableUPositions;
			
		} else if(railsUsed==GlobalConstants.RAILS_BOTH_CODE) {
			
			
			
			ArrayList<Long> frontList=(ArrayList<Long>)getAvailableUPositionsByRails(cabinetId, itemRUHeight, itemIds, GlobalConstants.RAILS_FRONT_CODE, reservationId);
			ArrayList<Long> rearList=(ArrayList<Long>)getAvailableUPositionsByRails(cabinetId,itemRUHeight, itemIds, GlobalConstants.RAILS_REAR_CODE, reservationId);
			ArrayList<Long> bothList = new ArrayList<Long>();
			frontList.retainAll(rearList);
			for (Long uPos: rearList) {
				if (frontList.contains(uPos)) {
					bothList.add(uPos);
				}
			}
			
			return bothList;
		}
		
		return null;
	}

	@Override
	public ItemObjectFactory getFactory() {
		return itemObjectFactory;
	}
	
	public void lazyLoadPowerPort(PowerPort port){
		if(port == null){
			return;
		}
		
		port.deRatingFactor = 1;
		port.upRatingFactor = 1;
		
		if(port.getItem() != null){
			port.getItem().getItemId();

			DataCenterLocaleDetails loc = port.getItem().getDataCenterLocation().getDcLocaleDetails();			
			String country = loc.getCountry().toUpperCase();
			
			if(country.equals("UNITED STATES")){
				port.deRatingFactor = 0.80;
				port.upRatingFactor = 1.25;
			}			
		}
		
		if(port.getConnectorLookup() != null){
			port.getConnectorLookup().getConnCompatList();
			port.getConnectorLookup().getConnCompat2List();
		}			
	}

	@Override
	public ItemViewData viewItems(Long itemId) throws DataAccessException {
		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(ItemViewData.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			if(itemId != null){
				criteria.add(Restrictions.eq("itemId", itemId));
				
				for(Object o:criteria.list())
				{
					return (ItemViewData)o;
				}
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}

	@Override
	public List<SystemLookupDTO> getAllItemClassLookup(){
		List<SystemLookupDTO> allItemClassLookup = new ArrayList<SystemLookupDTO>();
		
		//We will be adding a few more additional lookup for the client here!
		SystemLookupDTO systemLookupAllInCabinet = new SystemLookupDTO();
		systemLookupAllInCabinet.setLabel(SystemLookup.SpecialClass.ALL_IN_CABINET_VALUE);
		systemLookupAllInCabinet.setData(SystemLookup.SpecialClass.ALL_IN_CABINET);
		allItemClassLookup.add(systemLookupAllInCabinet);
		
		SystemLookupDTO systemLookupAllClass = new SystemLookupDTO();
		systemLookupAllClass.setLabel(SystemLookup.SpecialClass.ALL_CLASS_VALUE);
		systemLookupAllClass.setData(SystemLookup.SpecialClass.ALL_CLASS);
		allItemClassLookup.add(systemLookupAllClass);
		
		SystemLookupDTO systemLookupAllFreeStanding = new SystemLookupDTO();
		systemLookupAllFreeStanding.setLabel(SystemLookup.SpecialClass.ALL_FREE_STANDING_VALUE);
		systemLookupAllFreeStanding.setData(SystemLookup.SpecialClass.ALL_FREE_STANDING);
		allItemClassLookup.add(systemLookupAllFreeStanding);
		
		for (SystemLookupDTO systemLookup:getSystemLookup(SystemLookup.LkpType.CLASS)){
			SystemLookupDTO classSystemLookup = new SystemLookupDTO();
			classSystemLookup.setLabel(systemLookup.getLabel() + "items");
			classSystemLookup.setData(systemLookup.getData());
		}
		
		return allItemClassLookup;
	}


	@Override
	public CircuitItemViewData viewCircuitItems(Long itemId) throws DataAccessException {
		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(CircuitItemViewData.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			if(itemId != null){
				criteria.add(Restrictions.eq("itemId", itemId));
				
				for(Object o:criteria.list())
				{
					return (CircuitItemViewData)o;
				}
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}

	@Override
	public List<SystemLookupDTO> getSystemLookup(String lkpValue) {
		
		List<SystemLookupDTO> systemLookups = new ArrayList<SystemLookupDTO>();

		Session session = this.sessionFactory.getCurrentSession();

		List<LksData> classLkpList = SystemLookup.getLksData(session, lkpValue);

		for (LksData lksData: classLkpList){
			SystemLookupDTO systemLookup = new SystemLookupDTO();
			systemLookup.setLabel(lksData.getLkpValue());
			systemLookup.setData(lksData.getLkpValueCode());
			
			systemLookups.add(systemLookup);
		}
		
		return systemLookups;
	}

	@Override
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTO criteraDTO) throws SystemException, BusinessValidationException {
		// TODO Auto-generated method stub
		return itemSearch.search(criteraDTO);
	}

	public ItemSearch getItemSearch() {
		return itemSearch;
	}

	public void setItemSearch(ItemSearch itemSearch) {
		this.itemSearch = itemSearch;
	}
	
	@Override
	public Long getTotalItemCountForClass(Long classLkpValueCode) {
		return itemSearch.getTotalItemCountForClass(classLkpValueCode);
	}
	
	@Override
	public String getItem(Long itemId, String xPath, UserInfo userInfo) throws Throwable {
		String xmlString = null;
		if (xPath == null || xPath.isEmpty()){
			xPath = "uiView[@uiId='itemView']/";
		}
		xmlString = rulesProcessor.getXMLData(xPath, "itemId", itemId, "=", new LongType(), ((userInfo != null) ? userInfo.getUnits() : "1"));
		return xmlString;
	}

	@Override
	public Map<String, UiComponentDTO> getItem(Long itemId, UserInfo userInfo)  throws Throwable{
		Integer unit = null;
		Map<String, UiComponentDTO> dtos = new HashMap<String, UiComponentDTO>();
		UiView uiView = rulesProcessor.getData("uiView[@uiId='itemView']/", "itemId", itemId, "=", new LongType(), ((userInfo != null) ? userInfo.getUnits(): "1"));
		JXPathContext jc = JXPathContext.newContext(uiView);
		List<UiComponentDTO> componentList = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent");
		for (UiComponentDTO uiViewComponent : componentList){
			dtos.put(uiViewComponent.getUiId(), uiViewComponent);
		}
		
		return dtos;
	}

	public RemoteRef getRemoteReference() {
		return remoteReference;
	}

	public void setRemoteReference(RemoteRef remoteReference) {
		this.remoteReference = remoteReference;
	}

	public Map<String, UniqueValidator> getUniqueValidators() {
		return uniqueValidators;
	}

	public void setUniqueValidators(Map<String, UniqueValidator> uniqueValidators) {
		this.uniqueValidators = uniqueValidators;
	}
	
	private String getEntityForUiId( String uiId){
		String entity = remoteReference.getRemoteType(rulesProcessor.getRemoteRef(uiId));
		return entity;
	}
	private String getPropertyForUiId( String uiId ){
		RemoteRefConstantProperty prop = RemoteRef.RemoteRefConstantProperty.FOR_VALUE;
		String property = remoteReference.getRemoteAlias(rulesProcessor.getRemoteRef(uiId), prop);
		return property;
	}
	
	public Boolean isUnique( String uiId, Object value, String siteCode, Long parentId, String ignoreProperty, Object ignorePropertyValue) throws DataAccessException, ClassNotFoundException{
		Boolean isUnique = false;

		Assert.isTrue(uniqueValidators.containsKey(uiId), "No available validator");
		UniqueValidator validator = uniqueValidators.get(uiId);

		String entity = getEntityForUiId(uiId);
		String property = getPropertyForUiId(uiId);
		if( entity != null && property != null ){
			isUnique = validator.isUnique(entity, property, value, siteCode, parentId, ignoreProperty, ignorePropertyValue);
		}else log.error("cannot obtain entity & property from " + uiId);
		return isUnique;
	}
	
	//Increment sequence number and obtain new value
	private long getNextSequenceNumber(){		
		long next_seq = 0;
		Session session = this.sessionFactory.getCurrentSession();
		Query query =
				session.createSQLQuery("select nextval ('dct_storage_item_name_seq')");

	    next_seq = ((BigInteger) query.uniqueResult()).longValue();
	    log.debug("sequence number=" + next_seq);
	    return next_seq;        
	}
	
	//Generate item name from sequence number
	private String generateItemName(long seq_num) throws DataAccessException {
		String name = String.format("NoName_%05d", seq_num);
		log.debug("generated storage item name: " + name);
		return name;
	}
	
		
	//@Transactional
	public String getGeneratedStorageItemName() throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		boolean unique = false;
		final int max_seq = 99999;
		String name = null;
		int i;
		//find first unused name or loop through entire sequence
		for (i=0; i<max_seq; i++){
			name = generateItemName(getNextSequenceNumber());
			if( unique = systemPropertyValidator.isUnique("com.raritan.tdz.domain.Item", "itemName", name, null, -1L, null, null)) break;
		}
		//all names already taken throw exception 
		if( !unique && i>=max_seq){
			log.error("Cannot find unique name in DB, all used");
			String code = "itemHome.allGeneratedNamesUsed";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
            ex.addValidationError( msg );
            ex.addValidationError(code, msg);
            throw ex;
		}
		return name;
	}

	@Override
	public List<ValueIdDTO> getAllLocations() {
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(DataCenterLocationDetails.class);
		
		proList.add(Projections.property("dataCenterLocationId"), "data");
		proList.add(Projections.property("code"), "label");
		criteria.createAlias("componentTypeLookup","componentTypeLookup");
		criteria.createAlias("parentLocation","parentLocation");
		criteria.addOrder(Order.asc("code"));
		
		criteria.add(Restrictions.eq("componentTypeLookup.lkpValueCode", SystemLookup.DcLocation.ROOM));
		
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}

	@Override
	public List<SystemLookupDTO> getStatusLookupForCurrentState(Long itemId, Long modelId, UserInfo userInfo) throws DataAccessException, ClassNotFoundException {
		List<SystemLookupDTO> resultDtos = new ArrayList<SystemLookupDTO>();
		
		List<Long> statusList = null;
		List<SystemLookupDTO> dtos = null;
		if (itemId < 0){
			dtos = getSystemLookup(SystemLookup.LkpType.ITEM_STATUS);
			ModelDetails model = modelId != null ? modelDAO.loadModel(modelId) : null;
			statusList = itemStateContext.getStatusList(model); // statusLookupMap.get(SystemLookup.ItemStatus.PLANNED);
		}
		else {
		    Item item = (Item) itemDAO.read(itemId);
	
		    if (null != item) {
			    //On the itemStateContext get the available states
			    statusList = itemStateContext.getAllowableStates(item, userInfo);
			    dtos = getSystemLookup(item.getStatusLookup().getLkpTypeName());
		    }
		}
		
		if( statusList != null && dtos != null ){       
		    for (Long status:statusList){
		    	for (SystemLookupDTO dto: dtos) {
		    		if (dto.getData().equals(status)){
		    			resultDtos.add(dto);
		    		}
		    	}
		    }
		}
		return resultDtos;
	}
		
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, UserInfo userInfo)
				throws Throwable {
		Map<String, UiComponentDTO> dtos;
		
		//First load the item to get the correct item object
//		ItemObject obj = itemObjectFactory.getItemObject(itemId);
		ItemObjectTemplate obj = itemObjectTemplateFactory.getItemObjectFromItemId(itemId);
		if (obj != null){
			dtos = obj.getItemDetails(itemId, ((userInfo != null) ? userInfo.getUnits() : "1"));
		}else{
			String code = "ItemValidator.invalidItem";
			String msg = messageSource.getMessage(code, null, Locale.getDefault());
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			e.addValidationError(msg);
			e.addValidationError(code, msg);
			throw e;
		}
		
		return dtos;
	}
		
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getItemDetailsExtAPI(Long itemId, UserInfo user)
				throws Throwable {
		
		// Filter out all -when-moved items for the REST API
		if (powerPortMoveDAO.isMovedItem(itemId)) return new HashMap<String, Object>();
		
		Map<String, UiComponentDTO> itemUiDto = getItemDetails(itemId, user);
		
		removeInvalidUiId(itemId, itemUiDto);
		
		Map<String, Object> retval = itemDTOAdapter.convertItemDetails( itemUiDto );
		
		return retval;
	}



		
	public void setPaginatedHome(PaginatedHome paginatedHome) {
		this.paginatedHome = paginatedHome;
	}

	//@Transactional(readOnly = true)
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
	
		return paginatedHome.getPageList(listCriteriaDTO,pageType);
	}
	
	
	@Transactional(readOnly = true)
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException {
		return paginatedHome.getUserConfig(pageType);
	}
	
	
	/*
	@Transactional(readOnly = true)
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException {
		return paginatedHome.getDefUserConfig(pageType);
	}
	*/
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return paginatedHome.saveUserConfig(itemListCriteria, pageType);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return paginatedHome.deleteUserConfig(itemListCriteria, pageType);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		return paginatedHome.resetUserConfig(itemListCriteria, pageType, fitRows);
	}
	
	//TODO: Fix this so that we do not return just object, maybe more concrete type?
	@Transactional(readOnly = true)
	public Object getAllItemsExtAPI(String listType) throws DataAccessException {
		ListCriteriaDTO listCriteria = itemDTOAdapter.createCriteriaForGetAllItems();
		ListResultDTO resultList = paginatedHome.getPageList(listCriteria, listType);
		return itemDTOAdapter.convertItemList( resultList );
	}
	
	@Transactional(readOnly = true)
	public List<LookupOptionDTO> getLookupOption(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
		return paginatedHome.getLookupOption(listCriteriaDTO,pageType);
	}
	
	@Transactional(readOnly = true)
	public List<Map> getLookupData(String fieldName,String lkuTypeName,String pageType) throws DataAccessException {
		return paginatedHome.getLookupData(fieldName,lkuTypeName,pageType);
	}

	@Transactional
	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo sessionUser) throws ClassNotFoundException, BusinessValidationException, Throwable {
/*		ItemDomainAdaptor adaptor = (ItemDomainAdaptor)itemDomainAdaptor;
		
		//Get copy of original DB item
		//Item oldItem = (Item)adaptor.getOriginalItem(itemId);
		Item oldItem = (Item)adaptor.loadItem(Item.class, itemId, true);
		
		//do the actual save operation
		//Second get the item Object from the dto list
		Item itemDomain = (Item)adaptor.createItem(itemId, dtoList);
		
		if (itemDomain != null){
			ItemObject itemBusinessObject = itemObjectFactory.getItemObject(itemDomain);
			if (itemBusinessObject != null) {
				try {
					//itemBusinessObject.captureItemData(itemDomain, itemPlacementHome, itemId);
					itemBusinessObject.captureItemData(oldItem, itemPlacementHome, itemId);
					
					log.debug("ITEM BUSINESS OBJECT");
					log.debug(itemBusinessObject.toString());
					
					return itemBusinessObject.saveItem(itemId, dtoList, sessionUser);
				} catch (BusinessValidationException be){
					//This is to handle any warning and put the data into the exception
					if (be.getCallbackURL() != null && !be.getCallbackURL().isEmpty()){
						ValueIdDTO dto = new ValueIdDTO();
						dto.setLabel("_tiSkipValidation");
						dto.setData(new Boolean(true));
						dtoList.add(dto);
						
						be.addCallbackArg(itemId);
						be.addCallbackArg(dtoList);
					}
					
					throw be;
				} finally {
					itemBusinessObject.clearCapturedItemData(itemId);
				}
			} 
		}*/
		
		ItemObjectTemplate itemObjectTemplate = itemObjectTemplateFactory.getItemObject(itemId, dtoList);
		if (null == itemObjectTemplate) {
			throwBusinessValidationException("ItemConvert.cannotSaveItem", null, null);
		}
		try {
			setItemOrigin(itemId, dtoList);
			return (itemObjectTemplate.saveItem(itemId, dtoList, sessionUser));
		}  catch (BusinessValidationException be){
			//This is to handle any warning and put the data into the exception
			if (be.getCallbackURL() != null && !be.getCallbackURL().isEmpty()){
				ValueIdDTO dto = new ValueIdDTO();
				dto.setLabel("_tiSkipValidation");
				dto.setData(new Boolean(true));
				dtoList.add(dto);
				
				be.addCallbackArg(itemId);
				be.addCallbackArg(dtoList);
			}
			
			throw be;
		} 
		
		// This item could not be properly saved for some reason...
//		String code = "ItemConvert.cannotSaveItem";
//		final String msg = messageSource.getMessage(code, null, null);
//		BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
//		be.addValidationError( msg );
//		be.addValidationError(code, msg);
//		throw be;
	}
	
	private void setItemOrigin(Long itemId, List<ValueIdDTO> dtoList) {
		if (itemId > 0) return;

		Object ticketNumberObj = ValueIdDTOHolder.getCurrent().getValue("tiTicketNumber");
		Integer ticketNumber = null;
		if (ticketNumberObj instanceof Long) {
			ticketNumber = Integer.valueOf(((Long)ticketNumberObj).toString());
		}
		else if (ticketNumberObj instanceof String) {
			ticketNumber = Integer.valueOf((String)ticketNumberObj);
		}
		else {
			ticketNumber = (Integer) ticketNumberObj;
		}
		Long originLkpValueCode = (Long) ValueIdDTOHolder.getCurrent().getValue("_tiOrigin");
		if (null == originLkpValueCode) {
			originLkpValueCode = SystemLookup.ItemOrigen.CLIENT;
		}
		
		if (null != ticketNumber && ticketNumber > 0) {
			originLkpValueCode = SystemLookup.ItemOrigen.TICKET;
		}
		dtoList.add(new ValueIdDTO( "_tiOrigin", originLkpValueCode));
	}

	private void removeInvalidUiId(Long itemId,
			Map<String, UiComponentDTO> itemUiDto) {
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item)session.get(Item.class, itemId);
		List<String> uiIdsToRemove = new ArrayList<String>();
		
		for (Map.Entry<String, UiComponentDTO> entry:itemUiDto.entrySet()){
			String key = entry.getKey();
			if (!rulesProcessor.isValidUiIdForUniqueValue(key, item.getClassMountingFormFactorValue())){
				uiIdsToRemove.add(key);
			}
		}
		
		for (String uiId:uiIdsToRemove){
			itemUiDto.remove(uiId);
		}
	}
	
	//NOTE: Set any additional data that the convert could not cover here.
	private void setAdditionalItemData(UserInfo sessionUser, Object itemDomain) {
		Item item = (Item)itemDomain;
		setCreatedByUser(sessionUser, item);
	}

	private void setCreatedByUser(UserInfo sessionUser, Item item) {
		//Generally the itemservice details will always exist. If it does not, we
		//may need to add one to ensure the user who created this exist.
		if (item.getItemServiceDetails() == null){
			item.setItemServiceDetails(new ItemServiceDetails());
		}
		
		if (sessionUser != null)
			item.getItemServiceDetails().setSysCreatedBy(sessionUser.getUserName());
	}

	private void validate(Object itemDomain) throws BusinessValidationException {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, itemDomain.getClass().getName());
		itemValidator.validate(itemDomain, errors);
		//Create a business validation exception out of the errors
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg );
			}
		}

		//If we have validation errors for any of the DTO arguments, then throw that.
		if (e.getValidationErrors().size() > 0){
			throw e;
		}
	}

	@Override
	public Boolean isItemTagVerified(Long itemId) {
		Session session = sessionFactory.getCurrentSession();
	
		Criteria c = session.createCriteria(Item.class); 
		c.add(Restrictions.eq("itemId", itemId));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return ((Item)c.uniqueResult()).getIsAssetTagVerified();
	}
	
	@Override
	public Integer getLicenseCount() {
		Integer count = 0;
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(LicenseDetails.class);
		
		@SuppressWarnings("unchecked")
		List<LicenseDetails> ldList = c.list();
		for (LicenseDetails ld : ldList){
			String vendorStr = ld.getVendorString();
			int idx = vendorStr.indexOf("=") + 1;
			String lnStr = vendorStr.substring(idx);
			count += Integer.parseInt(lnStr); 
		}
		return count;
	}
	
	@Override
	public Boolean isLicenseAvailable(int requestedCabQty) {
		Boolean licenseAvailable = false;
		Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(CabinetItem.class);
		
		c.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		c.add(Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED));
		moveHome.filterMoveCabinets(c);
		c.setProjection(Projections.rowCount());
		Long itemCount = (Long)c.uniqueResult() + requestedCabQty;
						
		licenseAvailable = (getLicenseCount() - itemCount) >= 0;
		
		return licenseAvailable;
	}
	
    @Override
    public List<String> getProjectNumbers(Long locationId) {
            Session session = sessionFactory.getCurrentSession();                
            
            String HQL_QUERY = "select distinct d.projectNumber from Item as i inner join i.dataCenterLocation as loc inner join i.itemServiceDetails as d ";
            
            if(locationId == null || locationId.longValue() < 1){
            	HQL_QUERY += " where d.projectNumber is not null order by d.projectNumber";
            }
            else{
            	HQL_QUERY += " where loc.dataCenterLocationId = " + locationId + " and d.projectNumber is not null order by d.projectNumber";
            }
            
            List<String> recList = session.createQuery(HQL_QUERY).list();

            return recList;
    }
    	

    @Override
    public List<String> getContractNumbers(Long locationId) {
            Session session = sessionFactory.getCurrentSession();                
            
            String HQL_QUERY = "select distinct d.contractNumber from Item as i inner join i.dataCenterLocation as loc inner join i.itemServiceDetails as d ";
            
            if(locationId == null || locationId.longValue() < 1){
            	HQL_QUERY += " where d.contractNumber is not null order by d.contractNumber";
            }
            else{
            	HQL_QUERY += " where loc.dataCenterLocationId = " + locationId + " and d.contractNumber is not null order by d.contractNumber";
            }
            
            List<String> recList = session.createQuery(HQL_QUERY).list();

            return recList;
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<?> getAllStackItems(long itemId) throws DataAccessException, BusinessValidationException {
		Session session = sessionFactory.getCurrentSession();
		
		if (itemId <= 0) return new ArrayList<Long> (); 
			
		Item item = (Item)session.get(Item.class, itemId);
		if (null == item || null == item.getCracNwGrpItem()) {
			throwBusinessValidationException("StackableValidation.invalidItem", null, null);
		}
		Long primaryItemId = item.getCracNwGrpItem().getItemId();

		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);
		c.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "id");
		proList.add(Projections.property("itemName"), "name");
		proList.add(Projections.property("model.modelName"), "modelName");
		proList.add(Projections.property("parentItem.itemName"), "cabinetName");
		proList.add(Projections.property("uPosition"), "UPosition");
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");
		proList.add(Projections.property("sibling.itemId"), "siblingId");

		c.addOrder(Order.asc("name"));
		
		// sibling item id of input item is eq to sibling item id of the items returned
		
		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("sibling.itemId", primaryItemId));
		c.setProjection(proList);
		c.setResultTransformer(Transformers.TO_LIST);
		return c.list();
	}
	
	List<Object> getStackablesItemIds() {
		ArrayList<Long> itemIds = new ArrayList<Long>();
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("getStackableItems");
		query.setLong("subclassLkpValueCode", SystemLookup.SubClass.NETWORK_STACK);
		query.setString("modelMounting", SystemLookup.Mounting.RACKABLE);
		query.setString("modelFormFactor", SystemLookup.FormFactor.FIXED);
	
		return query.list();
	}

	/**
	* List of stackable items that have sibling id occurred only once.
	* it will return all items that can be added to the network stack and is currently not in any network stack
	* @return List<StackableItemDTO>
	* @throws DataAccessException
	*/
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getAddableStackItems() throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		
		//get stackable itemIds
		List<Object> itemIds = getStackablesItemIds();
		if (itemIds.size() == 0) {
			return null;
		}
		
		// For each itemIds in the above list, project the fields to fill up StackItemDTO 
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		criteria.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		criteria.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "id");
		proList.add(Projections.property("itemName"), "name");
		proList.add(Projections.property("model.modelName"), "modelName");
		proList.add(Projections.property("parentItem.itemName"), "cabinetName");
		proList.add(Projections.property("uPosition"), "UPosition");
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");
		proList.add(Projections.property("sibling.itemId"), "siblingId");
		
		criteria.addOrder(Order.asc("itemName"));
		criteria.add(Restrictions.in("itemId", itemIds));
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.TO_LIST);
		
		return criteria.list();
	}
	
	private String getStackGroupName (String itemName) {
		int indexSeperator = itemName.lastIndexOf('-');
		if (indexSeperator < 0)	return itemName;
		return itemName.substring(0, indexSeperator);
	}
	
	private Object getLastStackItemNumber(long primaryItemId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("sibling.itemId", primaryItemId));

		ProjectionList proj = Projections.projectionList();
		proj = proj.add(Projections.max("numPorts"));
		c.setProjection(proj);
		
		return c.uniqueResult();
	}
	
	private String getNextNameInNetworkGroup(long primaryItemId) throws DataAccessException, BusinessValidationException {
		String nextName = null;
		// get Item with primary item Id and check if it is valid
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item)session.get(Item.class,primaryItemId);
		if (item == null || item.getCracNwGrpItem() == null) {
			// Throw exception
		}
		String groupName = getStackGroupName (item.getItemName());
		Integer lastStackNumber = (Integer)getLastStackItemNumber(primaryItemId);
		if (lastStackNumber == null || lastStackNumber.intValue() >= 64) {
			// Throw Exception 
		}
		lastStackNumber += 1;
		nextName = new StringBuffer()
				.append(groupName)
				.append("-")
				.append((lastStackNumber.intValue() < 10 ? "0" : ""))
				.append(lastStackNumber.intValue()).toString();
		
		return nextName;
	}

	private boolean isAvailableNetworkItem(long itemId) throws DataAccessException, BusinessValidationException {
		return (getAllStackItems(itemId).size() == 1);
	}
	
	private Item getNetworkStackItem(long itemId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("itemId", itemId));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  (Item)c.uniqueResult();
	}
	
	private boolean isPrimaryNetworkItem(long primaryItemId) {
		Item item = getNetworkStackItem(primaryItemId);
		// check for item not null
		boolean primaryItem = false;
		if (null != item) {
			primaryItem = (item.getCracNwGrpItem().getItemId() == primaryItemId);
		}
		return primaryItem;
	}
	
	private boolean isValidNetworkItem(long itemId) {
		Item item = getNetworkStackItem(itemId);
		// check for item not null
		return (null != item && itemId == item.getItemId());
	}


	@Transactional(readOnly = true)
	private Item getItemDomainObject (Long primaryItemId) {
		// NOTE: Warning: openSession caused DB deadlock for getAvailableShelfPosition() call.  
		//Session s = sessionFactory.openSession();
		Session s = sessionFactory.getCurrentSession();
		return (Item)s.get(Item.class, primaryItemId);
	}
	
	@Override
	public List<?> addStackItem(long newItemId, long primaryItemId) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		// check if the item id is a network stack item
		if (isAvailableNetworkItem(newItemId) && isPrimaryNetworkItem(primaryItemId)) {
			// get the item name to be the next to the last name in the item list
			String newName = getNextNameInNetworkGroup(primaryItemId);
			// get the primary item
			// NOTE : WARNING: getItemDomainObject is modified to get data from current session.
			//        Verify addStackItem to make sure that it is working as expected.
			Item siblingItem = getItemDomainObject(primaryItemId);
			//FIXME: Make this section transactional in a private function
			if (null != newName && null != siblingItem) {
				validateItemName (newName, siblingItem.getDataCenterLocation().getCode());
				Session session = sessionFactory.getCurrentSession();
				Item item = (Item)session.get(Item.class, newItemId);
				// update item name and sibling item id (primaryItemId of the stack)
				item.setItemName(newName);
				item.setCracNwGrpItem(siblingItem);
				session.update(item);
			}
		}
		else {
			// TODO:: Raise please provide the correct item to add to the network list exception
			throwBusinessValidationException("StackableValidation.notANetworkItem", null, null);
		}
		return  getAllStackItems(primaryItemId);
	}

	@SuppressWarnings("unused")
	private String getRemovedNetworkItemName(String itemName) {
		String nextName = null;
		int indexSeperator = itemName.lastIndexOf('-');
		String groupName = itemName.substring(0, indexSeperator);
		String lastRunningNumber = itemName.substring(indexSeperator + 1, itemName.length());
		Long availableNumber = Long.valueOf(lastRunningNumber) - 1;
		String fillName = "-";
		if (availableNumber < 10) {
			fillName = "-0";
		}
		nextName = new StringBuffer()
					.append(groupName)
					.append(fillName)
					.append(availableNumber).toString();
		return nextName;
	}
	
	private void validateItemName(String newName, String location) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		
		if (null == newName) {
			throwBusinessValidationException("StackableValidation.invalidItemName", null, null);
		}
		if (isUnique("tiName", newName, location, -1L, null, null) == false) {
			Object[] errorArgs = { newName };
			throwBusinessValidationException("StackableValidation.uniqueName", errorArgs, null);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void validateItemNames(Map<String, String> nameLocationMap) throws ClassNotFoundException, DataAccessException, BusinessValidationException {

		int errIdx = 0;
		Object[] errorArgs = new String[128];
		Iterator it = nameLocationMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String itemName = (String)entry.getKey();
			String location = (String)entry.getValue();
			if (isUnique("tiName", itemName, location, -1L, null, null) == false) {
				errorArgs[errIdx++] = itemName; 
			}
		}
		if (errIdx > 0) {
			throwBusinessValidationException("StackableValidation.uniqueName", errorArgs, null);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private long setNewPrimaryForStackItems(long primaryItemId)  throws DataAccessException, BusinessValidationException {
		Session session = sessionFactory.getCurrentSession();
		Item newPrimaryItem = null;
		/* if removing a primary item itself, 
		 * then assign the next in the list as primary and 
		 * update the primary item id of all others in the list */
		List<List> dtos = (List<List>)getAllStackItems(primaryItemId);
		if (dtos.size() > 0) {
			dtos.remove(0);
		}
		if (dtos.size() > 0) {
			List<?> dto0 = (List<?>)dtos.get(0);
			newPrimaryItem = (Item)session.get(Item.class, (Long)dto0.get(0));
			
			for (List dto: dtos) {
				Item networkItem = (Item)session.get(Item.class, (Long)dto.get(0));
				/* update primary id */
				networkItem.setCracNwGrpItem(newPrimaryItem);
				session.update(networkItem);
			}
			return newPrimaryItem.getItemId();
		}
		
		return -1;
	}

	@Override
	public List<?> removeStackItem(long itemId, String newName) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		Session session = sessionFactory.getCurrentSession();
		if (isValidNetworkItem(itemId) == false) {
			throwBusinessValidationException("StackableValidation.invalidItem", null, null);
		}

		Item item = (Item)session.get(Item.class, itemId);
		// Note  primayItemId of the item being removed.
		// may be required for assigning next in the stack as primary.
		Long primaryItemId = item.getCracNwGrpItem().getItemId();

		// validate new name for uniqueness
		validateItemName(newName, item.getDataCenterLocation().getCode());

		// if both name and item are valid, set new name and sibling pointing to itself.
		item.setCracNwGrpItem(item);
		item.setItemName(newName);
		session.update(item);

		if (primaryItemId == item.getItemId()) {
			// if item being remove id primary item, assign next item in the
			// stack as primary.
			setNewPrimaryForStackItems(primaryItemId);
		}
		return  getAllStackItems(primaryItemId); 
	}
	
	/* 
	 * called by StackableToNetworkChassis, StackableToNetworkChassisFS, StackableToNetworkBlade
	 * when subclass of stackable item is changed.  
	 * */
	@Override
	public List<?> removeStackItem(long itemId) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		long newPrimary = -1;
		Item item = getNetworkStackItem(itemId);
		if (item != null) {
			//throwBusinessValidationException("StackableValidation.invalidItem", null, null);

			// Note  primayItemId of the item being removed.
			// may be required for assigning next in the stack as primary.
			Long primaryItemId = item.getCracNwGrpItem().getItemId();
			if (primaryItemId == item.getItemId()) {
				// if item being remove id primary item, assign next item in the
				// stack as primary.
				newPrimary = setNewPrimaryForStackItems(primaryItemId);
			}
		}

		return  getAllStackItems(newPrimary); 
	}

	
	@SuppressWarnings("unchecked")
	private List<Item> getStackItems(Long itemId)  throws DataAccessException, BusinessValidationException  {
		Session session = sessionFactory.getCurrentSession();

		Item item = (Item)session.get(Item.class, itemId);
		if (null == item || null == item.getCracNwGrpItem()) {
			throwBusinessValidationException("StackableValidation.invalidItem", null, null);
		}
		Long primaryItemId = item.getCracNwGrpItem().getItemId();

		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("sibling.itemId", primaryItemId));
		return c.list();
	}
	
	private Map<Long, String> validateStackItemsName( List<Item> stackItems, String stackName) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		String[] names = new String[128];
		Map<Long, String> idNameMap = new HashMap<Long, String>();
		Map<String, String> nameLocationMap = new HashMap<String, String>();
		
		int i = 0;
		//FIXME: exclude current satack name
		for( Item item: stackItems) {
			String newName = stackName;
			String itemName = item.getItemName();
			int idx = itemName.lastIndexOf('-');
			if (idx > 0) {
				String stackNumberStr = itemName.substring(idx + 1);
				newName = new StringBuffer().append(newName).append("-").append(stackNumberStr).toString();
			}
			idNameMap.put(item.getItemId(), newName);
			nameLocationMap.put(newName, item.getDataCenterLocation().getCode());
		}
		// verify if new name is valid
		validateItemNames(nameLocationMap);

		return idNameMap;
	}

	@Override
	public List<?> setStackName(long itemId, String stackName) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		Session session = sessionFactory.getCurrentSession();
		List<Item> stackItems = getStackItems(itemId);
		Map<Long, String> idNameMap = validateStackItemsName (stackItems, stackName);
		for( Item item: stackItems) {
			item.setItemName(idNameMap.get(item.getItemId()));
			session.update(item);
		}

		return getAllStackItems(itemId);
	}
	 
	public List<?> setStackNumber(long itemId, long stackNumber) throws  ClassNotFoundException, DataAccessException, BusinessValidationException {
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item)session.get(Item.class, itemId);
		if (null == item || null == item.getCracNwGrpItem()) {
			throwBusinessValidationException("StackableValidation.invalidItem", null, null);
		}
		if (stackNumber < 0 || stackNumber > 64) {
			throwBusinessValidationException("StackableValidation.invalidStackNumber", null, null);
		}
		String itemName = item.getItemName();
		String stackName = itemName;
		int idx = itemName.lastIndexOf('-');
		if (idx > 0) {
			stackName = itemName.substring(0, idx);
		}
		String newName = new StringBuffer(stackName).
				append("-").
				append((stackNumber < 10 ? "0" : ""))
				.append(String.valueOf(stackNumber)).toString();

		//verify if the new name is valid
		validateItemName(newName, item.getDataCenterLocation().getCode());
		
		item.setItemName(newName);
		session.update(item);

		return getAllStackItems(itemId);
	}
	
	private void throwBusinessValidationException(String code, Object[] args, Locale locale ) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		String msg = messageSource.getMessage(code, args, locale);
		e.addValidationError( msg);
		e.addValidationError(code, msg);
		throw e;
	}
	
	String getItemName(long itemId) {
		if (itemId > 0) {
			Criteria c = sessionFactory.getCurrentSession().createCriteria(Item.class);
			c.add(Restrictions.eq("itemId", itemId));
			ProjectionList proList = Projections.projectionList();
			proList.add(Projections.property("itemName"), "itemName");
			c.setProjection(proList);
			
			return (String)c.uniqueResult();
		}
		return "<Unknown>";
	}
	
	private void addPanelsUsingConnections(Long itemId, List<Long> primaryItemIdList) {
		
		List<Long> panelItemIds = getPanelItemIdsToDelete(itemId);
		
		for (Long panelItemId: panelItemIds) {
			if (!primaryItemIdList.contains(panelItemId)) {
				primaryItemIdList.add(panelItemId);
			}
		}
		
	}

	private void addPanelsUsingConnections(List<Long> itemIds, List<Long> primaryItemIdList) {
		
		List<Long> panelItemIds = itemDAO.getPanelItemIdsToDelete(itemIds);
		
		for (Long panelItemId: panelItemIds) {
			if (!primaryItemIdList.contains(panelItemId)) {
				primaryItemIdList.add(0, panelItemId);
			}
		}
		
	}

	private void addBuswayPowerOutlets(List<Long> primaryItemIdList) {
		
		// add power outlets associated with the busway panel 
		List<Long> buswayPowerOutletList = getBuswayPowerOutletItemIdsToDelete(primaryItemIdList);
		primaryItemIdList.removeAll(buswayPowerOutletList);
		primaryItemIdList.addAll(0, buswayPowerOutletList);
		
	}

	private List<Long> getAssociatedPowerOutlets(List<Long> itemIdList) {
		List<Long> localPowerOutletList = getLocalPowerOutletItemIdsToDelete(itemIdList);
		List<Long> remotePowerOutletList = getRemotePowerOutletItemIdsToDelete(itemIdList);
		List<Long> powerOutlets = new ArrayList<Long>(localPowerOutletList); 
		powerOutlets.addAll(remotePowerOutletList);
		
		return powerOutlets;
	}
	
	private List<Long> addLocalNRemotePowerOutlets(List<Long> primaryItemIdList) {
		
		List<Long> localPowerOutletList = getLocalPowerOutletItemIdsToDelete(primaryItemIdList);
		List<Long> remotePowerOutletList = getRemotePowerOutletItemIdsToDelete(primaryItemIdList);

		List<Long> powerOutlets = new ArrayList<Long>(localPowerOutletList); 
		powerOutlets.addAll(remotePowerOutletList);
		powerOutlets.removeAll(primaryItemIdList);

		// include all the local power outlets
		primaryItemIdList.removeAll(localPowerOutletList);
		primaryItemIdList.addAll(0, localPowerOutletList);
	
		// include all the remote power outlets
		primaryItemIdList.removeAll(remotePowerOutletList);
		primaryItemIdList.addAll(0, remotePowerOutletList);
		
		return powerOutlets;
	}


	private void addPowerOutlets(List<Long> primaryItemIdList) {
		
		// add power outlets associated with the busway panel 
		List<Long> buswayPowerOutletList = getBuswayPowerOutletItemIdsToDelete(primaryItemIdList);
		primaryItemIdList.removeAll(buswayPowerOutletList);
		primaryItemIdList.addAll(0, buswayPowerOutletList);

		List<Long> localPowerOutletList = getLocalPowerOutletItemIdsToDelete(primaryItemIdList);
		List<Long> remotePowerOutletList = getRemotePowerOutletItemIdsToDelete(primaryItemIdList);
		
		// include all the local power outlets
		primaryItemIdList.removeAll(localPowerOutletList);
		primaryItemIdList.addAll(0, localPowerOutletList);
		// primaryItemIdList.addAll(localPowerOutletList);
		
		// include all the remote power outlets
		primaryItemIdList.removeAll(remotePowerOutletList);
		primaryItemIdList.addAll(0, remotePowerOutletList);
		// primaryItemIdList.addAll(remotePowerOutletList);
		
	}
	
	@Transactional
	@Override
	public List<Long> deleteItemsConfirmed(List<Long> itemIds, Boolean deleteAssociatedItems, Boolean validate, UserInfo userInfo) throws ClassNotFoundException,	BusinessValidationException, Throwable {
		BusinessValidationException be = null;
		List<Long> recList = new ArrayList<Long>();
		
		// if itemId is parent itemId, then get all child items for deletion
		List<Long> itemIdList = itemDAO.getItemIdsToDelete(itemIds);

		// add power panels to the list if the parent item id is not set for the panel
		addPanelsUsingConnections(itemIds, itemIdList);

		//add current items to list
		itemIdList.removeAll(itemIds);
		itemIdList.addAll(itemIds);

		// get all the busway items
		addBuswayPowerOutlets(itemIdList);

		// get extra power outlets added
		List<Long> extraPowerOutlets = null;
		if (deleteAssociatedItems) {
			extraPowerOutlets = addLocalNRemotePowerOutlets(itemIdList);
		}
		else {
			List<Long> powerOutlets = getAssociatedPowerOutlets(itemIdList);
			
			if (null != powerOutlets && powerOutlets.size() > 0) {
				itemDAO.clearPowerOutletAssociationWithPanel(powerOutlets);
			}
		}

		// addPowerOutlets(itemIdList, includeAssociatedItems);
		
		for(Long itemId:itemIdList){
			try{
				if(itemId != null){
					// deleteItem(itemId.longValue(), true, userInfo);
					deleteSingleItem(itemId.longValue(), validate, userInfo);
					recList.add(itemId.longValue());
				}
			}
			catch(BusinessValidationException ex) {

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
		
		if (null != be && validate) {
			throw be;
		}
			
		return recList;
	}

	@Transactional
	@Override
	public List<Long> deleteItems(List<Long> itemIds, UserInfo userInfo) throws ClassNotFoundException,	BusinessValidationException, Throwable {
		BusinessValidationException be = null;
		List<Long> recList = new ArrayList<Long>();
		
		// if itemId is parent itemId, then get all child items for deletion
		List<Long> itemIdList = itemDAO.getItemIdsToDelete(itemIds);

		// add power panels to the list if the parent item id is not set for the panel
		addPanelsUsingConnections(itemIds, itemIdList);

		//add current item to list
		itemIdList.removeAll(itemIds);
		itemIdList.addAll(itemIds);

		addBuswayPowerOutlets(itemIdList);

		// get extra power outlets added
		List<Long> extraPowerOutlets = addLocalNRemotePowerOutlets(itemIdList);
		
		List<String> itemNames = new ArrayList<String>();
		
		for(Long itemId:itemIdList){
			try{
				if(itemId != null){
					validateDeleteSingleItem(itemId.longValue(), true, userInfo);
				}
			}
			catch(BusinessValidationException ex) {

				if(be == null) {
					be = new BusinessValidationException(ex.getExceptionContext());
				}
				
				for(Map.Entry<String, String> s : ex.getValidationErrorsMap().entrySet()) {
					if (s.getKey().equals("ItemValidator.deleteConnected")) {
						String str = s.getValue();
						String itemNameList = str.substring(str.indexOf('\n'));
						
						String itemName[] = itemNameList.split("\\r?\\n");
						for (String name: itemName) {
							if (!itemNames.contains(name) && !StringUtils.isBlank(name)) {
								itemNames.add(name);
							}
						}
					}
					else {
						be.addValidationError(s.getKey(), s.getValue());
						be.addValidationError(s.getValue());
					}
				}
			}
		}
		
		StringBuffer itemNameList = new StringBuffer();
		if (itemNames.size() > 0) {

			int count =0;

			for(String s:itemNames) {
				itemNameList.append(s).append("\n");
				
				if(itemNameList.length() > 500 || (count == 12 && itemNames.size() > 12)){
					itemNameList.append("..........");
					break;
				}
				
				count++;
				
			}
			Object args[]  = {count, itemNameList.toString() };
			String code = "ItemValidator.deleteConnected";
			String message = messageSource.getMessage(code, args, Locale.getDefault());
			be.addValidationError("ItemValidator.deleteConnected", message);
			be.addValidationError(message);
		}

		
		if (extraPowerOutlets.size() > 0) {
			String errorCode = "ItemValidator.deletePowerPanelWithPowerOutlet";
			String defaultMsg = "The panel you are deleting has breakers circuited to outlets. Do you want to delete these power outlets as well?";
			Object[] args = {};
			String msg = messageSource.getMessage("ItemValidator.deletePowerPanelWithPowerOutlet", args, defaultMsg, Locale.getDefault());
			if ( be == null) {
				be = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			}
			be.addValidationWarning(msg);
			be.addValidationWarning(errorCode, msg, BusinessValidationException.WarningEnum.WARNING_YES_NO_CANCEL);
			/*be.setCallbackURL("itemService.deleteItemsExt");
			be.addCallbackArg(itemIds);*/
		}
		
		if(be != null ) {
			if (be.getValidationErrors().size() > 0){
				be.setCallbackURL(null);
				throw be;
			} else if (be.getValidationWarnings().size() > 0){
				be.orderValidationWarnings(new ArrayList<WarningEnum>(){{
					add(WarningEnum.WARNING_YES_NO);
					add(WarningEnum.WARNING_YES_NO_CANCEL);
				}});
				be.setCallbackURL("itemService.deleteItemsExt");
				be.addCallbackArg(itemIds);
				be.addCallbackArg(itemIdList);
				be.addCallbackArg(extraPowerOutlets);
				throw be;
			}
		}
		
		for(Long itemId:itemIdList){
			if(itemId != null){
				// deleteItem(itemId.longValue(), true, userInfo);
				deleteSingleItem(itemId.longValue(), false, userInfo);
				recList.add(itemId.longValue());
			}
		}
		
		return recList;
	}
	
	private boolean validateDeleteSingleItem(Long id, boolean validate, UserInfo userInfo) throws Throwable {
		boolean ret = false;
		ItemObjectTemplate itemObject = null; 
		if (id != null) itemObject = itemObjectTemplateFactory.getItemObjectFromItemId(id);
		if (itemObject != null) {
			try {
				ret = true;
				itemObject.validateDeleteItem(id, validate, userInfo);
			} catch (BusinessValidationException be) {
	 			//This is to handle any warning and put the data into the exception
				throw be;
			} catch (Throwable e) {
				ret = false;
				throw e;
			}
		} else {
			String itemName = getItemName(id);
			itemName = (null != itemName) ? itemName : " ";
			Object args[] = {itemName, "delete"};
			String code = "ItemValidator.deleteInvalidItem";
			ret = false;
			throwBusinessValidationException (args, code);
		}
		return ret;
	}
	
	public boolean deleteSingleItem(Long id, boolean validate, UserInfo userInfo) throws Throwable {
		boolean ret = false;
		ItemObjectTemplate itemObject = null; 
		if (id != null) itemObject = itemObjectTemplateFactory.getItemObjectFromItemId(id);
		if (itemObject != null) {
			try {
				ret = true;
				itemObject.deleteItem(id, validate, userInfo);
			} catch (BusinessValidationException be) {
	 			//This is to handle any warning and put the data into the exception
				throw be;
			} catch (Throwable e) {
				ret = false;
				throw e;
			}
		} else {
			String itemName = getItemName(id);
			itemName = (null != itemName) ? itemName : " ";
			Object args[] = {itemName, "delete"};
			String code = "ItemValidator.deleteInvalidItem";
			ret = false;
			throwBusinessValidationException (args, code);
		}
		return ret;
	}
	
	@Transactional
	@Override
	public boolean deleteItem(long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable {
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		deleteItemsConfirmed(itemIds, true, validate, userInfo);
		
		return true;
	}
	
	@Override
	public void unmapItem(String locationCode, String itemName, UserInfo userInfo) throws BusinessValidationException {

		Errors errors = new MapBindingResult( new HashMap<String, String>(), Item.class.getName() );
		
		piqItemUnmap.unmap(locationCode, itemName, userInfo, errors);
		
	}
	
	
	private void throwBusinessValidationException (Object args[], String code) throws BusinessValidationException {
		String msg = messageSource.getMessage(code, args, null);	
		BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
		be.addValidationError( msg );
		be.addValidationError(code, msg);
		throw be;
	}
	
	private void throwBusinessValidationException(Errors errors, List<String> warningCodeList, String callbackURL, List<Object> callbackArgs) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (warningCodeList != null && warningCodeList.contains(error.getCode())){
					e.addValidationWarning(error.getCode(), msg);
					e.addValidationWarning(msg);
				} else	{
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			e.setCallbackURL(callbackURL);
			e.setCallbackArgs(callbackArgs);
			throw e;
		}
	}

	private boolean verifyDeleteItemPermission(List<Long> itemIdList, UserInfo userInfo) throws BusinessValidationException {

		// check if user has permission to delete children in itemIdList
		for (Long id: itemIdList) {
			if (id != null) verifyDeleteItemPermission (id, userInfo);
		}
		return true;
	}
	
	private List<String> getItemsToDeleteInvalidStages (List<Long> itemIdList) throws DataAccessException, IllegalArgumentException {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
 		
		try {
			// find out if any item in itemIdList has approved pending request
			Session session = this.sessionFactory.getCurrentSession();	
			Criteria criteria = session.createCriteria(Request.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			criteria.createAlias("requestPointers", "pointer");
			criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
			criteria.add(Restrictions.eq("history.current", true));		    
			criteria.add(Restrictions.in("itemId", itemIdList));
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStages));
			criteria.createAlias("requestHistories", "history");
			
			// collect list of item ids matching above criteria 
	    	List<Long> itemIds = new ArrayList<Long>();
	    	for (Object r: criteria.list()) {
	    		itemIds.add(((Request)r).getItemId());
	    	}

	    	// find out item names for logging 
			List<String>  itemNames = new ArrayList<String>();;
	    	if (itemIds.size() > 0) {
				Criteria c = session.createCriteria(Item.class);
				c.add(Restrictions.in("itemId", itemIds));
				c.addOrder(Order.asc("itemName"));

				ProjectionList proList = Projections.projectionList();
				proList.add(Projections.property("itemName"), "itemName");
				c.setProjection(proList);
				c.setResultTransformer(Transformers.TO_LIST);

				for(Object rec:c.list()){
					List row = (List) rec;
					itemNames.add((String)row.get(0));
				}
			}
			return itemNames;
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}
	
	
	private boolean verifyItemStagesBeforeDeletion(List<Long> itemIdList) throws BusinessValidationException, DataAccessException {

		// check if item stage allowes modification
		List<String> itemNameList = getItemsToDeleteInvalidStages (itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString(), "delete" };
			String code = "ItemValidator.itemDeleteRequestStageNotAllowed";
			throwBusinessValidationException (args, code); // ItemValidator.deleteError
		}
		return true;
	}

	private boolean verifyDeleteItemPermission(long itemId, UserInfo userInfo) throws BusinessValidationException {

		Item item = null;

		String operation = "delete";

		if (userInfo == null) {
			Object args[] = {"<Unknown>", operation};
			String code = "ItemValidator.deleteAccessDenied";
			throwBusinessValidationException (args, code);
		}
		
		if (itemId > 0) item = (Item)sessionFactory.getCurrentSession().get(Item.class, itemId);
		
		if (item == null) {
			Object args[] = {itemId, operation};
			String code = "ItemValidator.deleteInvalidItem";
			throwBusinessValidationException (args, code);
		}
	
		// verify if user has permission to delete items
		if (itemModifyRoleValidator.canTransition(item, userInfo) == false) {
			Object args[] = {userInfo.getUserName(), operation};
			String code = "ItemValidator.deleteAccessDenied";
			throwBusinessValidationException (args, code);
		}
		return true;
	}
	
	private StringBuffer getFormattedItemNameList(List<String> itemNameList) {
		StringBuffer itemNames = new StringBuffer();

		if (itemNameList.size() > 0) {
			int count =0;
			
			for(String s:itemNameList){
				itemNames.append("\t").append(s).append("\n");
				
				if(itemNames.length() > 500 || (count == 12 && itemNameList.size() > 12)){
					itemNames.append("..........");
					break;
				}
				count++;								
			}
		}
		return itemNames;
	}
	
	private void verifyItemStatusBeforeDeletion(List<Long> itemIdList) throws BusinessValidationException {
	
		// you can delete only those items in 'installed' state
		List<String> itemNameList = getItemsToDeleteNotNew(itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString() };
			String code = "ItemValidator.deleteNotNew";
			throwBusinessValidationException (args, code);
		}
	}
	
	private void verifyItemConnectionsBeforeDeletion(List<Long> itemIdList) throws BusinessValidationException {

		// cannot delete item if there are connection
		List<String >itemNameList = getItemToDeleteConnected(itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString() };
			String code = "ItemValidator.deleteConnected";
			throwBusinessValidationException (args, code);
		}
	}

	private void verifyItemDataConnectionsBeforeDeletion(List<Long> itemIdList) throws BusinessValidationException {

		// cannot delete item if there are connection
		List<String >itemNameList = getItemToDeleteConnectedData(itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString() };
			String code = "ItemValidator.deleteConnected";
			throwBusinessValidationException (args, code);
		}
	}
	
	private List<Long> getPanelItemIdsToDelete(Long itemId) {
		/**
		 select item_id from dct_items where 
			item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = 5084))) and
			class_lks_id = 11 and subclass_lks_id is not null;
		 */
		Session session = this.sessionFactory.getCurrentSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select item_id from dct_items where ")
		.append(" item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = :itemId))) and ")
		.append(" class_lks_id = 11 and subclass_lks_id is not null ")
		.toString()
	    );
		
		q.setLong("itemId", itemId.longValue());

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;

	}

	private List<Long> getPowerOutletItemIdsToDelete(List<Long> itemIds){
		
		return itemDAO.getPowerPanelConnectedPowerOutlet(itemIds);
	}
	
	private List<Long> getBuswayPowerOutletItemIdsToDelete(List<Long> itemIds){
		
		List<Long> powerOutlets = itemDAO.getBuswayPowerPanelConnectedPowerOutlet(itemIds);
		// itemIds.removeAll(powerOutlets);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	

	private List<Long> getLocalPowerOutletItemIdsToDelete(List<Long> itemIds){
		List<Long> powerOutlets = itemDAO.getLocalPowerPanelConnectedPowerOutlet(itemIds);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	

	private List<Long> getRemotePowerOutletItemIdsToDelete(List<Long> itemIds){
		
		List<Long> powerOutlets = itemDAO.getRemotePowerPanelConnectedPowerOutlet(itemIds);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	


	private List<String> getItemsToDeleteNotNew(List<Long> itemIds){
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("statusLookup", "status");
		c.createAlias("classLookup", "itemClass");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"), "itemName");		
		c.setProjection(proList);
		c.setResultTransformer(Transformers.TO_LIST);
		
		//These two classes are set to install by default in windows client
		List<Long> classLks = new ArrayList<Long>();
		//classLks.add(SystemLookup.Class.BLANKING_PLATE);
		classLks.add(SystemLookup.Class.PERFORATED_TILES);
		classLks.add(SystemLookup.Class.PASSIVE);
		
		c.add(Restrictions.in("itemId", itemIds));
		c.add(Restrictions.ne("status.lkpValueCode", SystemLookup.ItemStatus.PLANNED));
		c.add(Restrictions.not(Restrictions.in("itemClass.lkpValueCode", classLks)));
		c.addOrder(Order.asc("itemName"));
				
		List<String> recList = new ArrayList<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		
		return recList;
	}	

	private List<String> getItemToDeleteConnectedData(List<Long> itemIds){
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}

	private List<String> getItemToDeleteConnectedPower(List<Long> itemIds){
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(PowerPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}

		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}

	private List<String> getItemToDeleteConnected(List<Long> itemIds){
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}

		//DO POWER
		c = session.createCriteria(PowerPort.class);
		c.createAlias("item", "item");
		
		proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		
		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}
	
	@Transactional(readOnly = true)
	public Map getColumnGroup(String pageType) throws DataAccessException {
		return paginatedHome.getColumnGroup(pageType);
	}
	
	@Transactional(readOnly = true)
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType) {
		return paginatedHome.getValueList(listCriteriaDTO,pageType);
	}

	private Criteria getNonRackableItemsCriteria(Long cabinetId, long uPosition, long railsUsed) { 
    	Session session = this.sessionFactory.getCurrentSession();
	
		Criteria criteria = session.createCriteria(Item.class);
	
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("mountedRailLookup", "mountedRailLookup", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
	
		criteria.add(Restrictions.eq("parentItem.itemId", cabinetId));
		criteria.add(Restrictions.eq("uPosition", uPosition));
		criteria.add(Restrictions.eq("mountedRailLookup.lkpValueCode", railsUsed));
		criteria.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.NON_RACKABLE));
		criteria.add(Restrictions.ne("model.mounting", "ZeroU"));
		return criteria;
	}
	
	@Override
	@Transactional(rollbackFor=BusinessValidationException.class,propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> saveItemImportExtAPI( long itemId, Map<String, Object> itemDetails, UserInfo user ) 
			throws BusinessValidationException, ClassNotFoundException, Throwable{
		Map<String, Object> retval = null;
		Long origin = SystemLookup.ItemOrigen.IMPORT;
		
		UiComponentDTO dto = (UiComponentDTO)itemDetails.get("_tiOrigin");
		if( dto != null ){
			origin = (Long)dto.getUiValueIdField().getValue();
		}
     

		retval = saveItemExtAPI( itemId, itemDetails, user, origin);
		if(retval.size() > 0 ){
			Map<String, Object> ipRetVal = updateProxyIndexAndIpAddress( (Long)retval.get("id"), itemDetails, user);
			
			if( ipRetVal != null ) retval.putAll(ipRetVal);
		}
		return retval;
		
	}

	@Override
	@Transactional(rollbackFor=BusinessValidationException.class,propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> updateItemImportExtAPI( long itemId, Map<String, Object> itemDetails, UserInfo user ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Map<String, Object> retval = null;
		
		retval = updateItemExtAPI( itemId, itemDetails, user);
		if(retval.size() > 0 ){
			Map<String, Object> ipRetVal = updateProxyIndexAndIpAddress( (Long)retval.get("id"), itemDetails, user);
			if( ipRetVal != null ) retval.putAll(ipRetVal);
		}
		return retval;
	}

	// TODO: Currently this function checks only the 1st item in the list. 
	private void checkRequiredFieldForDeletingPassiveItem (Long itemId, Map<String, Object> itemDetails, Errors errors) {
		if (itemDAO.isPassiveItem(itemId)) {
			String itemName = (String)itemDetails.get("tiName");
			String location = (String) itemDetails.get("cmbLocation");
			String cabinet = (String) itemDetails.get("cmbCabinet");
			String uPosition = (String)itemDetails.get("cmbUPosition");
			String railsUsed = (String)itemDetails.get("radioRailsUsed");
			List<String> args = new ArrayList<String>();
			if (location == null || location.isEmpty())	args.add("Location");
			if (itemName == null ||	itemName.isEmpty())	args.add("Item Name");
			if (cabinet == null || cabinet.isEmpty()) args.add("Cabinet");
			if (uPosition == null || uPosition.isEmpty()) args.add("U Position");
			if ( railsUsed == null || railsUsed.isEmpty()) args.add("Rails Used");
			if (!args.isEmpty() ) {
				String missingReqdFields = args.toString().replaceAll("\\[","").replaceAll("\\]", "");
				Object[] errArgs = { missingReqdFields };
				errors.reject("itemValidator.ReqdFieldsForDeleteOfPassiveItem", errArgs, "Required fields are missing for deleting passive item");
			}
		}
	}

	@Override
	@Transactional(rollbackFor=BusinessValidationException.class,propagation=Propagation.REQUIRES_NEW)
	public List<Long> deleteItemsImportExtAPI(List<Long> itemIds, Map<String, Object> itemDetails, UserInfo userInfo)
			throws ClassNotFoundException, BusinessValidationException,
			Throwable {
		
		/*
		 *  if user is deleting passive item, make sure required fields
		 * for uniquely identifying passive item is provided. If not, show error. 
		 */
		Errors errors = new MapBindingResult( new HashMap<String, String>(), Item.class.getName() );
		
		for (Long itemId: itemIds) {
			checkRequiredFieldForDeletingPassiveItem (itemId, itemDetails, errors);
		}
		
		if (errors.hasErrors()) throwBusinessValidationException(errors, null);
		
		List<Long> ids = new ArrayList<Long>();
		try {
			ids = deleteItems(itemIds, userInfo);
		} catch (BusinessValidationException bve) {
			if (bve.getValidationErrors().size() > 0) throw bve;
			if (bve.getValidationWarnings().size() > 0) { 
				ids = handleItemDeleteWarnings(bve, userInfo);
			}
		}
		return ids;
	}

	private List<Long> handleItemDeleteWarnings(BusinessValidationException bve, UserInfo userInfo) throws Throwable {
		List<Object> args = bve.getCallbackArgs();
		return deleteItemsConfirmed((List)args.get(1), true, true, userInfo);
	}
	
	public Map<String, Object> updateProxyIndexAndIpAddress(Long itemId, Map<String, Object> newItemDetails, UserInfo userInfo) throws BusinessValidationException{
		
		Map<String, Object> retVal = new HashMap<String, Object>();
		
		String portName = (String) newItemDetails.get("ipAddressPortName");
		String ipAddress = (String) newItemDetails.get("ipAddress");
		String proxyIndex = (String) newItemDetails.get("proxyIndex");
		
	
		MapBindingResult errors = getErrorsObject(Item.class);
		
		Item item = null;
		if( itemId == null || itemId.longValue() <= 0 || ((item = itemDAO.getItem(itemId)) == null)){
			log.error("Invalid item id");
			Object[] errorArgs = {};
			errors.reject("ItemValidator.invalidItem", errorArgs, 
					"Specified item does not exists");
			return retVal;
		}
		Map<String, Object> target = new HashMap<String, Object>();
		target.put("item",  item);
		Object[] errorArgs = {};
		target.put("errorArgs", errorArgs);
		
		if( ipAddress != null ){
			target.put("errorCodes", "ipAddressValidator.unsupportedClass");
			target.put("defaultMsg", "IpAddress cannot be set/changed for this class of the item");
			itemClassForIpAddressValidator.validate(target, errors);
		}
		if(proxyIndex != null ){
			target.put("errorCodes", "proxyIndexValidator.unsupportedClass");
			target.put("defaultMsg", "proxyIndex cannot be set/changed for this class of the item");
			itemClassForProxyIndexValidator.validate(target, errors);
		}
		if( errors.hasErrors()) throwBusinessValidationException(errors, null);
		
		DataPort dataPort = dataPortDAO.getDataPortForItem(itemId, portName);

		
		JSONIpAssignment retIpAssignment = ipHome.saveIpAddressAndProxyForDataPort(dataPort, ipAddress, proxyIndex, userInfo, errors);
		if( errors.hasErrors()) throwBusinessValidationException(errors, null) ;
		if( retIpAssignment != null ){	
			retVal.put("ipAddress", retIpAssignment.getIpAddress());
			retVal.put("ipAddressPortName", retIpAssignment.getPortName());
			retVal.put("proxyIndex", item.getGroupingNumber());

		}

		return retVal;
	}
	
	


	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> saveItemExtAPI( long itemId, Map<String, Object> itemDetails, UserInfo user, 
			long origin ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Map<String, Object> retval = null;
		List<ValueIdDTO> dtoList = itemDTOAdapter.convertToDTOList(itemId, itemDetails);
		// set origin field for this new item.
		if (itemId <= 0)  dtoList.add(new ValueIdDTO( "_tiOrigin", origin));
		Map<String, UiComponentDTO> itemUiDto = saveItem(itemId, dtoList, user);
		UiComponentDTO itemNameComponent = itemUiDto.get("tiName");
		removeInvalidUiId((Long)itemNameComponent.getUiValueIdField().getValueId(), itemUiDto);
		if( itemUiDto != null ) retval = itemDTOAdapter.convertItemDetails( itemUiDto );
		return retval;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> updateItemExtAPI( long itemId, Map<String, Object> itemDetails, UserInfo user ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Map<String, Object> retval = null;
		
		if(itemDTOAdapter.itemExists(itemId)){
			List<ValueIdDTO> dtoList = itemDTOAdapter.convertToDTOList(itemId, itemDetails);
			Map<String, UiComponentDTO> itemUiDto = saveItem(itemId, dtoList, user);
			removeInvalidUiId(itemId, itemUiDto);
			if( itemUiDto != null ) retval = itemDTOAdapter.convertItemDetails( itemUiDto );
		}else{
			String code ="ItemConvert.cannotUpdateItem";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(code, msg);
			throw ex;
		}
		return retval;
	}	
	
	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		return paginatedHome.getItemActionMenuStatus(itemIdList);
	}
	
	@Override
	public Boolean doesItemStageAllowModification(Long itemId) throws BusinessValidationException, DataAccessException {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		/*List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		Map<Long,List<Request>> requestMap = itemRequest.getRequests(itemIds, requestStages, null);
		List<Request> requests = null;
		if (null != requestMap) {
			requests = requestMap.get(itemId);
		}
		return (!(null != requests && requests.size() > 0));*/
		boolean itemReqExistInStages = itemRequest.itemRequestExistInStages(itemId, requestStages);
		
		return !itemReqExistInStages;
	}

	private boolean doesItemSubClassAllowModification(Item item) throws BusinessValidationException, DataAccessException {
		// 2/7/2013 11:00AM: All subclass is allowed to be modified.
		// return (item == null || item.getSubclassLookup() == null || item.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.CONTAINER);
		return true;
	}

	private RequestHome requestHome;
	
	public RequestHome getRequestHome() {
		return requestHome;
	}

	public void setRequestHome(RequestHome requestHome) {
		this.requestHome = requestHome;
	}

	@Autowired(required=true)
	private RequestDAO requestDAO;

	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
	}

	// @Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public void processRequest(List<RequestDTO> requestDTOs, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException {
		
		UserInfo userInfo = userHome.getCurrentUserInfo();

		boolean requestBypassSetting = requestHelper.getRequestBypassSetting(userInfo);
		
		if (requestBypassSetting) {
		
			requestHome.processRequestDTO(userInfo, requestDTOs, bvex);
		}
		else {
			if (null != bvex) throw bvex;
		}
	}

	@Transactional(rollbackFor=BusinessValidationException.class)
	@Override
	public void processRequestUsingIds(List<Long> requestIds) throws BusinessValidationException, DataAccessException {
		
		UserInfo userInfo = userHome.getCurrentUserInfo();
		
		boolean requestBypassSetting = requestHelper.getRequestBypassSetting(userInfo);
		
		if (requestBypassSetting) {
		
			Errors errors = requestHome.processRequestUsingIds(userInfo, requestIds);
			
			if (errors.hasErrors()) {
				
				businessExceptionHelper.throwBusinessValidationException(null, errors, null);
			}
		}

	}
	
	private void throwBusinessValidationException(Errors errors, String warningCallBack) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}
	
	

	@Transactional(noRollbackFor={BusinessValidationException.class}, propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> itemRequest( List<Long> itemIds, String typeOfRequest, boolean warningConfirmed ) throws BusinessValidationException, DataAccessException {
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		ArrayList<RequestDTO> reqList = new ArrayList<RequestDTO>();
		
		typeOfRequest = typeOfRequest==null ? null : typeOfRequest.trim();
		if ( itemIds==null || itemIds.size()==0 || typeOfRequest==null || typeOfRequest.length()==0 ) {
			returnMap.put(List.class.getName(), reqList);
			return returnMap;
			//return reqList;
		}
		
		List<Long> _itemIds = new ArrayList<Long>();
		for ( Object itemId : itemIds ) {
			_itemIds.add( new Long( itemId.toString() ) );
		}
			
		UserInfo userInfo = userHome.getCurrentUserInfo();
		
		Map<Long, Long> map = null;
		
		String inStr = "";
		
		try {
			// String inStr = "";
			//itemRequest.clearErrors();
			
			if ( ItemActionMenuStatus.ID_REQ_VM.equals( typeOfRequest ) ) {
				
				map = itemRequest.convertToVMRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_OFF_SITE.equals( typeOfRequest ) ) {
				
				map = itemRequest.takeItemOffsiteRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_ON_SITE.equals( typeOfRequest ) ) {
				
				map = itemRequest.bringItemOnsiteRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_POWER_OFF.equals( typeOfRequest ) ) {
				
				map = itemRequest.powerOffItemRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_POWER_ON.equals( typeOfRequest ) ) {
				
				map = itemRequest.powerOnItemRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_TO_STORAGE.equals( typeOfRequest ) ) {
				
				map = itemRequest.decommisionItemToStorageRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_TO_ARCHIVE.equals( typeOfRequest ) ) {
				
				map = itemRequest.decommisionItemToArchiveRequest( _itemIds, userInfo );
			} else if ( ItemActionMenuStatus.ID_REQ_INSTALL.equals( typeOfRequest ) ) {
				
				map = itemRequest.installItemRequest( _itemIds, userInfo );
				requestTicketSaveBehavior.updateTicketFields(map, null);
				
			} else if ( ItemActionMenuStatus.ID_REQ_RESUBMIT.equals( typeOfRequest ) ) {
				//We need to get request ids from itemIds and pass that to resubmitRequest
				
			
				Map<Long, Long> itemRequestMap = new HashMap<Long, Long>();
				List<Long> requestIds = new ArrayList<Long>();
				for (Long itemId:_itemIds){
					Request request = itemRequestDAO.getLatestRequest(itemId);
					if (request == null){
						Errors errors = itemRequest.getErrors();
						Item item = itemDAO.loadItem(itemId);
						Object errorArgs[] = {item != null ? item.getItemName():"<Unknown>"};
						errors.reject("itemRequest.cannotResubmit", errorArgs, "Cannot resubmit. No requests on this item.");
					} else {
						requestIds.add(request.getRequestId());
						itemRequestMap.put(itemId, request.getRequestId());
					}
				}
				
			    map = itemRequest.resubmitRequest( requestIds, userInfo ); 
			    requestTicketSaveBehavior.updateTicketFields(map, null);
			    
			    for ( Long requestId : map.keySet() ) {
					inStr += requestId + ", ";
				}
			    
			} else {

				returnMap.put(List.class.getName(), reqList);
				return returnMap;
				// return reqList;
			}

			if ("".equals( inStr )){
				for ( Long itemId : map.keySet() ) {
					Long requestId = map.get( itemId );
					inStr += requestId + ", ";
				}
			}
			
			if ( "".equals( inStr ) ) {
				returnMap.put(List.class.getName(), reqList);
				return returnMap;
				// return reqList;
			}
			
			List<?> retList = queryTblRequest( "(" + inStr + "null)" );
			
			for ( Object req : retList ) {
				Object[] reqRow = (Object[])req;
				RequestDTO reqdto = new RequestDTO();
				reqdto.setRequestId( new Long( reqRow[0].toString() ) );
				reqdto.setRequestNo( reqRow[1].toString() );
				reqdto.setItemId( new Long( reqRow[2].toString() ) );
				reqdto.setItemName( reqRow[3].toString() );
				reqList.add( reqdto );
			}
		} catch ( BusinessValidationException bvex ) {
			
			// clear all the warnings if confirmed by the user
			if (warningConfirmed) {
				if (bvex.getValidationWarnings().size() > 0) {
					bvex.setValidationWarnings((List)null);
					bvex.setValidationWarnings((Map)null);
					if (bvex.getValidationErrors().size() == 0) {
						bvex = null;
					}
				}
			}
			// throw the exception showing the warnings 
			else {
				if (bvex.getValidationWarnings().size() > 0) {
					bvex.setCallbackURL("itemService.itemRequestConfirmed");
					bvex.addCallbackArg(itemIds);
					bvex.addCallbackArg(typeOfRequest);

					// do not flush this transaction and throw the warning message for user to confirm
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

					throw bvex;
				}
			}
			
			List<RequestDTO> requestList = bvex.getRequestList();
			if ( requestList==null || requestList.size()==0 ) {
				returnMap.put(List.class.getName(), getAssociatedRequests(reqList));
				returnMap.put(BusinessValidationException.class.getName(), bvex);
				return returnMap;
				// throw bvex;
			}
			
			HashMap<String, RequestDTO> requestHashMap = new HashMap<String, RequestDTO>();
			
			// String inStr = "";
			for ( RequestDTO requestDTO : requestList ) {
				Long requestId = requestDTO.getRequestId();
				if ( requestId==null ) continue;
				inStr += requestId + ", ";
				requestHashMap.put( requestId.toString(), requestDTO );
			}

			if ( "".equals( inStr ) ) {
				returnMap.put(List.class.getName(), getAssociatedRequests(reqList));
				returnMap.put(BusinessValidationException.class.getName(), bvex);
				return returnMap;
				// throw bvex;
			}
			
			List<?> retList = queryTblRequest( "(" + inStr + "null)" );
			
			for ( Object req : retList ) {
				Object[] reqRow = (Object[])req;
				RequestDTO reqdto = (RequestDTO)requestHashMap.get( reqRow[0].toString() );
				reqdto.setRequestNo( reqRow[1].toString() );
				reqdto.setItemId( new Long( reqRow[2].toString() ) );
				reqdto.setItemName( reqRow[3].toString() );
			}
			
			returnMap.put(List.class.getName(), getAssociatedRequests(requestList));
			returnMap.put(BusinessValidationException.class.getName(), bvex);
			return returnMap;
			// throw bvex;
		} 
		//We should not consume any other exceptions other than the one you have to translate/process!
//		catch ( DataAccessException daex ) {
//			log.error( "", daex );
//		} catch ( Exception ex ) {
//			log.error( "", ex );
//		}
		
		/*List<RequestDTO> associatedDtos = requestDAO.getAssociatedRequestDTO(requestNos, requestStages);
		
		for (RequestDTO dto: associatedDtos) {
			if (!requestNos.contains(dto.getRequestNo()))
					reqList.add(dto);
		}*/
		
		returnMap.put(List.class.getName(), getAssociatedRequests(reqList));
		return returnMap;
		// return reqList;
	}
	
	private List<RequestDTO> getAssociatedRequests(List<RequestDTO> requestDTOs) {
		
		List<Long> requestStages = new ArrayList<Long>();
		
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		
		List<String> requestNos = new ArrayList<String>();
		for (RequestDTO dto: requestDTOs) {
			requestNos.add(dto.getRequestNo());
		}
		
		List<RequestDTO> associatedDtos = requestDAO.getAssociatedRequestDTO(requestNos, requestStages);
		
		for (RequestDTO dto: associatedDtos) {
			if (!requestNos.contains(dto.getRequestNo()))
				requestDTOs.add(dto);
		}
		
		return requestDTOs;
	}
	
	private List<?> queryTblRequest( String reqIdSet ) {
		try {
			String sqlStr = 
					"SELECT req.id, req.requestno, item.item_id, item.item_name\n" +
					"FROM tblrequest req\n" + 
					"LEFT JOIN dct_items item ON item.item_id=req.itemid\n" + 
					"WHERE id IN " + reqIdSet + " AND req.id is not null\n" +
					"ORDER BY item.item_name";
			
			Session session = sessionFactory.getCurrentSession();	
			Query query = session.createSQLQuery( sqlStr );
			return query.list();
		} catch ( Exception ex ) {
			log.error( "", ex );
		}
		return new ArrayList<Object>();
	}
	
	@Override
	public Boolean getEditableStatusForAnItem(long itemId, UserInfo userInfo )  throws BusinessValidationException, DataAccessException {

	    if (itemId > 0) {
		    Session session = sessionFactory.getCurrentSession();
		    Item item = (Item) session.get(Item.class, itemId);
		    
		    
	    	if (item != null && 
	    			itemModifyRoleValidator.canTransition(item, userInfo)== true) { 
    	    	return doesItemStageAllowModification(itemId);
	    	}
	    }
	    return false;
	}

	private boolean verifyItemDeletableStatus (long itemId, UserInfo userInfo )
		throws BusinessValidationException, DataAccessException {
		
		
		Item item = null;
	    if (itemId > 0) {
		    Session session = sessionFactory.getCurrentSession();
		    item = (Item) session.get(Item.class, itemId);
	    }
	    
	    return verifyItemDeletableStatus (item, userInfo );
	}
	
	private boolean verifyItemDeletableStatus (Item item, UserInfo userInfo )
			throws BusinessValidationException, DataAccessException {
			
			boolean canDeleteItem = false;

			if (item != null && itemModifyRoleValidator.canTransition(item, userInfo) == true) {
				canDeleteItem = item.isStatusPlanned();
		    }
			
			return canDeleteItem;
		}
	
	@Override
	public Boolean getDeletableStatusForAnItem(long itemId, UserInfo userInfo ) 
				throws BusinessValidationException, DataAccessException {
		
		boolean canDeleteItem = false;

		Item item = null;
	    if (itemId > 0) {
		    Session session = sessionFactory.getCurrentSession();
		    item = (Item) session.get(Item.class, itemId);
	    }

		/* check whether delete is allowed for current item 
		 * 1. because the status do not allow modification
		 * 2. Item subclass do not allow modifications (example: all subclass are allowed to be modified) */
		canDeleteItem = verifyItemDeletableStatus (item, userInfo);

		// check permission for child items if any (e.g. cabinet and devices under it)
		if (canDeleteItem) {
			List<Long> itemIdList = itemDAO.getItemIdsToDelete(itemId);
			
			itemIdList.add(itemId);
			
			List<String> nameList = itemDAO.getItemsToDeleteNotNew(itemIdList);
			if(nameList.size() > 0) return false;
			
			nameList = itemDAO.getItemsToDeleteInvalidStages(itemIdList);
			if(nameList.size() > 0) return false;
					
			
			for (Long id: itemIdList) {
				canDeleteItem = verifyItemDeletableStatus (id, userInfo);
				if (!canDeleteItem) break;
			}
		}
	    return canDeleteItem;
	}
	
	@Override
	public Long getItemClass(long itemId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("classLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		Long classLkpValueCode = (Long) criteria.uniqueResult();
		
		return classLkpValueCode; 
	}
		
	@Override
	public List<PowerPortDTO> getPowerPortDTOs(long itemId) {
		//First get the data ports for the given item
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);
		List<PowerPort> powerPortList = new ArrayList<PowerPort>();
		if( item.getPowerPorts() != null){
			powerPortList.addAll(item.getPowerPorts());
		}
		
		List<Long> movePortIds = new ArrayList<Long>();
		if (null != powerPortList) {
			for (PowerPort pp: powerPortList) {
				movePortIds.add(pp.getPortId());
			}
		}
		Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);
		
		// Process the powerPortList and update as a list of PowerPortDTO 
		List<PowerPortDTO> powerPortDTOList = new ArrayList<PowerPortDTO>();
		for (PowerPort pp: powerPortList) {
			PowerPortDTO powerPortDTO = PortsAdaptor.adaptPowerPortDomainToDTO(pp, portsAction);
			powerPortDTOList.add(powerPortDTO);
		}
		return powerPortDTOList;
	}
	
	@Override
	public List<DataPortDTO> getDataPortDTOs(long itemId) {
		Item item = (Item)itemFinderDAO.findById(itemId).get(0);
		return getDataPortDTOs(item); 
	}
	
	private List<DataPortDTO> getDataPortDTOs(Item item) {

		List<DataPort> dataPortList = new ArrayList<DataPort>();
			if(item.getDataPorts() != null){
			dataPortList.addAll(item.getDataPorts());
		}
		// Process the dataPortList and update as a list of DataPortDTO 
		List<DataPortDTO> dataPortDTOList = new ArrayList<DataPortDTO>();
		for (DataPort dp: dataPortList) {
			DataPortDTO dataPortDTO = dataPortDTOAdapter.adaptDataPortToDTO( dp, item.getItemId() );
			dataPortDTOList.add(dataPortDTO);
		}
		return dataPortDTOList; 
	}
	
	@Override
	public CloneItemDTO cloneItem(CloneItemDTO itemToClone, UserInfo userInfo)	throws DataAccessException, BusinessValidationException{
		Long newItemId = itemClone.clone(itemToClone, userInfo, this);
		
		String itemName = itemDAO.getItemName(newItemId);
		
		itemToClone.setNewItemName(itemName);
		
		return itemToClone;
	}

	@Override
	public List<CloneItemDTO> cloneItemRemoveDup(List<CloneItemDTO> recList) throws DataAccessException{
		
		return itemClone.removeChildren(recList);
	}
	
	private Item  getRackPduItem(Item item) {
		Item rackPduItem = null;
		if (item != null) { 
			if (item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.RACK_PDU)) {
				rackPduItem = item;
			} else if (item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.PROBE)) {
				// For probes, get linked dummyRackPduItem;
				rackPduItem = piqProbelookup.getDummyRackPDUForProbeItem(item.getItemId());
			}
		}
		return rackPduItem;
	}

	private List<Item> getItems(List<Long> ids) {
		List<Item> items = new ArrayList<Item>();
		// get items 
		for (Long id: ids) {
			Item item = itemDAO.getItem(id);
			if (item != null) {
				// if item is probe, get dummy rack pdu item 
				Item rackPduItem = getRackPduItem(item);
				if (rackPduItem != null) items.add(rackPduItem);
			}
		}
		return items;
	}
		
	@Override
	@Transactional
	public List<String> syncPduReadings(List<Long> ids) throws BusinessValidationException {
		// get Item domain objects for the ids provided as input
		List<Item> items = getItems(ids);
		List<String> response = null;
		try {
			if (items != null && items.size() > 0) {
				// sync inlet/outlet/sensor readings for PDU items
				response = piqSyncpduClient.syncPduReadings(items);
			
				//save items with sensor info.  
				itemDAO.merge(items);
			}
		} 
		catch (RemoteDataAccessException e) {
			throwBusinessValidationException("piqSync.communicationFailure", null, null);
		}
		catch (DataAccessException e) {
			throwBusinessValidationException("piqSync.readingsUpdate", null, null);
		} 
		catch (Exception e){
			if (e.getCause() != null && e.getCause().getCause() instanceof BusinessValidationException)
				throw (BusinessValidationException)e.getCause().getCause();
			else if (e.getCause() != null && e.getCause().getCause() instanceof RemoteDataAccessException)
				throwBusinessValidationException("piqSync.communicationFailure", null, null);
			else if (e.getCause() != null && e.getCause().getCause() instanceof DataAccessException)
				throwBusinessValidationException("piqSync.readingsUpdate", null, null);
			else
				throw new IllegalStateException("Cannot get Readings due to unknown Reason", e);
		}
		return response;
	}

	
	@Override
	@Transactional
	public List<SyncAllPDUReadingsStatusDTO> syncAllPDUReadings(long locationId, List<TYPE> portTypes)
			throws BusinessValidationException {
		// get the location based on location id. If it is not provided (-1) get all locations
		List<Long> locationDetailList = new ArrayList<Long>();
		
		if (locationId > 0)
			locationDetailList.add(locationId);
		else {
			LocationFinderDAO locationFinderDAO = (LocationFinderDAO) locationDAO;
			locationDetailList.addAll(locationFinderDAO.findAllVisibleLocationsId());
		}
		

		
		List<String> response = null;
		List<SyncAllPDUReadingsStatusDTO> dtoList = new ArrayList<SyncAllPDUReadingsStatusDTO>();
		try {
			if (locationDetailList != null && locationDetailList.size() > 0) {
				
				for (Long location: locationDetailList){
					LocationFinderDAO locationFinderDAO = (LocationFinderDAO) locationDAO;
					String piqHost = locationDAO.getPiqHostByLocationId(location);
					List<String> locationCodes = locationFinderDAO.findLocationCodeById(location);
					String locationCode = locationCodes.size() > 0 ? locationCodes.get(0) : "";
					
					// sync inlet/outlet/sensor readings for PDU items
					if (piqHost != null) {
						response = piqSyncpduClient.syncAllPduReadings(piqHost, locationCode, portTypes);
						
						SyncAllPDUReadingsStatusDTO dto = getSyncAllPDUReadingsStatusDTO(location, locationCode, piqHost, 
								portTypes.toString().replace("[", "").replace("]",""), 
								getReadingsLastUpdatedTimestamp(locationCode));
						dtoList.add(dto);
					}
				}
			}
		}
		catch (DataAccessException e) {
			throwBusinessValidationException("piqSync.readingsUpdate", null, null);
		} 
		catch (Exception e){
			if (e.getCause() != null && e.getCause().getCause() instanceof RemoteDataAccessException)
				throwBusinessValidationException("piqSync.communicationFailure", null, null);
			else if (e.getCause() != null && e.getCause().getCause() instanceof DataAccessException)
				throwBusinessValidationException("piqSync.readingsUpdate", null, null);
			else
				throw new IllegalStateException("Cannot get Readings due to unknown Reason", e);
		}
		
		return dtoList;
	}

	@Transactional
	@Override
	public List<SyncAllPDUReadingsStatusDTO> getSyncAllPDUReadingsStatus(long locationId) throws BusinessValidationException {
		List<Long> locationDetailList = new ArrayList<Long>();
		
		if (locationId > 0)
			locationDetailList.add(locationId);
		else {
			LocationFinderDAO locationFinderDAO = (LocationFinderDAO) locationDAO;
			locationDetailList.addAll(locationFinderDAO.findAllVisibleLocationsId());
		}
		
		List<SyncAllPDUReadingsStatusDTO> dtoList = new ArrayList<SyncAllPDUReadingsStatusDTO>();
		List<TYPE> portTypes = new ArrayList<TYPE>();
		
		if (locationDetailList != null && locationDetailList.size() > 0) {
			
			for (Long location: locationDetailList){
				LocationFinderDAO locationFinderDAO = (LocationFinderDAO) locationDAO;
				String piqHost = locationDAO.getPiqHostByLocationId(location);
				List<String> locationCodes = locationFinderDAO.findLocationCodeById(location);
				String locationCode = locationCodes.size() > 0 ? locationCodes.get(0) : "";
				
				// sync inlet/outlet/sensor readings for PDU items
				if (piqHost != null) {
					SyncAllPDUReadingsStatusDTO dto = null;
					try {
						dto = getSyncAllPDUReadingsStatusDTO(location, locationCode, piqHost,
								getReadingsLastUpdatedPortTypes(locationCode), getReadingsLastUpdatedTimestamp(locationCode));
					} catch (DataAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					dtoList.add(dto);
				}
			}
		}
		
		return dtoList;
	}
	
	private SyncAllPDUReadingsStatusDTO getSyncAllPDUReadingsStatusDTO(Long locationId, 
											String locationCode, String piqHost,
											String portType, String lastUpdated) {
		
		SyncAllPDUReadingsStatusDTO dto = new SyncAllPDUReadingsStatusDTO();
		dto.setLocationId(locationId);
		dto.setLocationName(locationCode);
		dto.setPowerIQHost(piqHost);
		List<String> portTypes = new ArrayList<String>();
		
		for (String type: portType.split(","))
			portTypes.add(type);
		dto.setTypes(portTypes);
		dto.setLastUpdatedTimeStamp(lastUpdated);
		return dto;
	}
	
	
	private String getReadingsLastUpdatedTimestamp(String locationName) throws DataAccessException {
		Event event = findLastReadingsEvent(locationName);
		
		if (event != null){
			Timestamp timestamp = event.getCreatedAt();
		
			DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
			return (format.format(timestamp));
		} 
		
		return "";
	}

	
	private String getReadingsLastUpdatedPortTypes(String locationName) throws DataAccessException {
		Event event = findLastReadingsEvent(locationName);
		
		if (event != null){
			EventParam eventParam = event.getEventParams().get("Port Type(s)");
			return(eventParam.getValue());
		}
		return "";
	}
	
	/**
	 * Find the last updated sync all readings event
	 * @param locationName
	 * @return
	 * @throws DataAccessException 
	 */
	private Event findLastReadingsEvent(String locationName) throws DataAccessException{
		return (eventHome.getMostRecentEvent(EventType.SYNC_FLOORMAP_DATA, EventStatus.ACTIVE, EventSeverity.INFORMATIONAL, locationName));
	}

	
	@Override
	public List<SensorPort> viewSensorPortsForItem(Long itemId) throws DataAccessException {
		List<SensorPort> recList = new ArrayList<SensorPort>();
		try{
			// change this to DAO call.	
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(SensorPort.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			if(itemId != null && itemId != 0) {
				criteria.add(Restrictions.eq("item.itemId", itemId));
			}
			criteria.addOrder(Order.asc("portName"));

			for(Object o:criteria.list()) {
				recList.add((SensorPort)o);
			}
		}catch(HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		return recList;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ValueIdDTO> getAvailableCabinetsForSensor(String siteCode, Long sensorTypeLksValueCode, Long includeCabinetId) {
		return sensorPortHelper.getAvailableCabinetsForSensor(siteCode, sensorTypeLksValueCode, null, includeCabinetId);
	}


	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getItemsChildrenExtAPI(Long itemId, boolean includeContainer, boolean includeGrandchildren, UserInfo userInfo) throws Throwable{

		Map<String, Object> ret = null;
		
		
		List<Object> childrenItems = null;
		List<Item> itemList = itemFinderDAO.findById(itemId);
		if( itemList.size() > 0 ){
			Item item = itemList.get(0);
			
			childrenItems = containerItemHome.getAllItemsInContainer(item, includeContainer, includeGrandchildren);
			ret = new HashMap<String, Object>();
			ret.put("items", childrenItems);
		}else{
			String code = "ItemValidator.invalidItem";
			String msg = messageSource.getMessage(code, null, Locale.getDefault());
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			e.addValidationError(msg);
			e.addValidationError(code, msg);
			throw e;
		}

		return ret;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<UPSBankDTO> getAllUpsBanks(Long minAmpsRating ) {

		return itemDAO.getAllUPSBanksMatchingRating(minAmpsRating);
	}
	
	@Override
	public List<BreakerDTO> getAllBreakers(Long ampsRating, Boolean[] isUsed, Long[] breakerLkpValueCodes,  
			Long[] phases, Long breakerPortId, Long fpduItemId) {
		StringBuffer msg = new StringBuffer();
		
		// ampsRating and breakerPortId can be null.
		if (isUsed == null)  {
			msg.append("\"isUsed\" cannot be null\n");
		}
		if (breakerLkpValueCodes == null)  {
			msg.append("\"breakerLkpValueCodes\" cannot be null\n");
		}
		if (phases == null)  {
			msg.append("\"phases\" cannot be null\n");
		}

		if (isUsed != null  &&  breakerLkpValueCodes != null && phases != null) {
			for (Long breakerPortLkpValueCode : breakerLkpValueCodes) {
				if (breakerPortLkpValueCode != SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER &&
						breakerPortLkpValueCode != SystemLookup.PortSubClass.PANEL_BREAKER &&
						breakerPortLkpValueCode != SystemLookup.PortSubClass.PDU_INPUT_BREAKER ) {
					msg.append("Invalid breakerPort ("); 
					msg.append(breakerPortLkpValueCode);
					msg.append(") in the request\n");
				}
			}
			
			for (Long phaseValue : phases) {
				if (phaseValue != SystemLookup.PhaseIdClass.THREE_DELTA &&
						phaseValue != SystemLookup.PhaseIdClass.THREE_WYE)  {
					msg.append("Invalid phase (" );
					msg.append(phaseValue);
					msg.append(") in the request\n");
				}
			}
		}
		
		if (msg.toString().length() > 0) {
			// initialize errors object
			throw new IllegalArgumentException(msg.toString());
		}
		
		return powerPortDAO.getBreakers(ampsRating, isUsed, breakerLkpValueCodes, phases, breakerPortId, fpduItemId);
	}

	@Override
	@Transactional(readOnly = true)
	public DataPortDTO getItemPortDetailsExtAPI(long itemId,
			long portId, UserInfo userInfo) throws DataAccessException, BusinessValidationException {

		DataPortDTO retDto = null;
        MapBindingResult errors = getErrorsObject(DataPort.class);
        
        Map<String, Object> targetMap = new HashMap<String, Object>();
        targetMap.put("portId", portId);
        targetMap.put("portClass", DataPort.class);
        targetMap.put("itemId", itemId);
        targetMap.put("UserInfo", userInfo);
                        
        //1. validate params in target map on DTO level
        dataPortReadItemValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

		Item item = (Item)itemFinderDAO.findById( itemId ).get(0);

		DataPort dp = dataPortDAO.loadPort(portId);
		if( dp != null && dp.getItem().getItemId() == itemId){
			retDto = dataPortDTOAdapter.adaptDataPortToDTO( dp, itemId );
		}else{
			String itemName = item != null ? item.getItemName() : "";
			Object[] args = {itemName};
			throwBusinessValidationException( args, "PortValidator.portDoesNotBelongToItem");
		}

		return retDto;
	}
	
	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, noRollbackFor=BusinessInformationException.class, propagation=Propagation.REQUIRES_NEW)
	    public DataPortDTO createItemDataPortExtAPI(long itemId,
            DataPortDTO portDetails, UserInfo userInfo)
            throws ClassNotFoundException, BusinessValidationException, BusinessInformationException, Throwable {

		Long portId = new Long(-1);
		portDetails.setPortId(portId);
		portDetails.setItemId(itemId);
		
        MapBindingResult errors = getErrorsObject(DataPort.class);
        
        Map<String, Object> targetMap = new HashMap<String, Object>();
        targetMap.put("portId", portDetails.getPortId());
        targetMap.put("portClass", DataPort.class);
        targetMap.put("itemId", portDetails.getItemId());
        targetMap.put("UserInfo", userInfo);
        targetMap.put("objectUnderTest", portDetails);
        
        //1. validate params in target map on DTO level
        dataPortCreateItemPortValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

        //2. adapt DTO from JSON to server DTO
        dataPortDTOAdapter.adaptDataPortFromJson(portDetails, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
                
        //3. Save data port using DTO and give us back DataPort object
        DataPort dp = null;
        try {
        	dp = (DataPort) dataPortObjectTemplate.saveApplyCommonAttribute(portDetails, userInfo, errors);
        }
        catch (BusinessInformationException e) {
        	dp = (DataPort) e.getDomainObject();
        	DataPortDTO dataPortDTO = dataPortDTOAdapter.adaptDataPortToDTO( dp, itemId );
        	e.setDomainObject(dataPortDTO);
        	throw e;
        }
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
        
        //4. Adapt DataPort object to Server DTO and then to Json DTO
        DataPortDTO dataPortDTO = dataPortDTOAdapter.adaptDataPortToDTO( dp, itemId );
        
        return dataPortDTO;
    }

	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, noRollbackFor=BusinessInformationException.class, propagation=Propagation.REQUIRES_NEW)
    public DataPortDTO updateItemDataPortExtAPI(long itemId, long port,
            DataPortDTO portDetails, UserInfo userInfo)
            throws BusinessValidationException, ClassNotFoundException, BusinessInformationException,
            Throwable {

        Long portId = new Long(port);
        Map<String, Object> targetMap = new HashMap<String, Object>();
        MapBindingResult errors = getErrorsObject(DataPort.class);
		
        targetMap.put("portId",portId);
		targetMap.put("itemId",itemId);
		targetMap.put("UserInfo",userInfo);
		targetMap.put("portClass", DataPort.class);
		targetMap.put("objectUnderTest", portDetails);
		
		//1. validate params in target map on DTO level
		dataPortUpdateItemPortValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
		
        //2. adapt DTO from JSON to server DTO
        dataPortDTOAdapter.adaptDataPortFromJson(portDetails, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }        
        
        //3. Save data port using DTO and give us back DataPort object
        // DataPort dp = (DataPort)dataPortObjectHome.saveItemPort(itemId, portId, /*portType,*/ portDetails, userInfo, errors);
        portDetails.setItemId(itemId);
        portDetails.setPortId(portId);
        DataPort dp = null;
        try {
        	dp = (DataPort) dataPortObjectTemplate.saveApplyCommonAttribute(portDetails, userInfo, errors);
        } 
        catch (BusinessInformationException e) {
        	dp = (DataPort) e.getDomainObject();
        	DataPortDTO dataPortDTO = dataPortDTOAdapter.adaptDataPortToDTO( dp, itemId );
        	e.setDomainObject(dataPortDTO);
        	throw e;
        }

        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

        //4. Adapt DataPort object to Server DTO and then to Json DTO
        DataPortDTO dataPortDTO = dataPortDTOAdapter.adaptDataPortToDTO( dp, itemId );
        return dataPortDTO;

	}
	
	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, noRollbackFor=BusinessInformationException.class, propagation=Propagation.REQUIRES_NEW)
	public PowerPortDTO createItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo) 
			throws BusinessValidationException, BusinessInformationException, Throwable {
		
        Long portId = new Long(-1);
        Long itemId = portDetails.getItemId();
        MapBindingResult errors = getErrorsObject(PowerPort.class);
        
        Map<String, Object> targetMap = new HashMap<String, Object>();
        targetMap.put("portId", portId);
        targetMap.put("portClass", PowerPort.class);
        targetMap.put("itemId", itemId);
        targetMap.put("UserInfo", userInfo);
        
        //1. validate params in target map on DTO level
        powerPortCreateItemPortValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

        //2. adapt DTO from JSON to server DTO
        powerPortDTOAdapter.adaptPowerPortFromJson(portDetails, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
                
        //3. Save power port using DTO and give us back DataPort object
        portDetails.setPortId(-1L);
        PowerPort pp = null;
        try {
        	pp = (PowerPort) powerPortObjectTemplate.saveApplyCommonAttribute(portDetails, userInfo, errors);
        } 
        catch (BusinessInformationException e) {
        	pp = (PowerPort) e.getDomainObject();
        	PowerPortDTO powerPortDTO = powerPortDTOAdapter.adaptPowerPortToDTO( pp, itemId );
        	e.setDomainObject(powerPortDTO);
        	throw e;
        }
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
        
        //4. Adapt DataPort object to Server DTO and then to Json DTO
        PowerPortDTO powerPortDTO = powerPortDTOAdapter.adaptPowerPortToDTO( pp, itemId );
        
        return powerPortDTO;
	}
	
	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, noRollbackFor=BusinessInformationException.class, propagation=Propagation.REQUIRES_NEW)
    public PowerPortDTO updateItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo)
            throws BusinessValidationException, ClassNotFoundException, BusinessInformationException, 
            Throwable {

        Long portId = portDetails.getPortId(); 
        Long itemId = portDetails.getItemId();
        Map<String, Object> targetMap = new HashMap<String, Object>();
        MapBindingResult errors = getErrorsObject(PowerPort.class);
		
        targetMap.put("portId",portId);
		targetMap.put("itemId",itemId);
		targetMap.put("UserInfo",userInfo);
		targetMap.put("portClass", PowerPort.class);
		
		//1. validate params in target map on DTO level
		powerPortUpdateItemPortValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }
		
        //2. adapt DTO from JSON to server DTO
        powerPortDTOAdapter.adaptPowerPortFromJson(portDetails, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }        
        
        //3. Save data port using DTO and give us back DataPort object
        PowerPort pp = null;
        try {
        	pp = (PowerPort) powerPortObjectTemplate.saveApplyCommonAttribute(portDetails, userInfo, errors);
        } 
        catch (BusinessInformationException e) {
           	pp = (PowerPort) e.getDomainObject();
           	PowerPortDTO powerPortDTO = powerPortDTOAdapter.adaptPowerPortToDTO( pp, itemId );
           	e.setDomainObject(powerPortDTO);
           	throw e;
        }
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

        //4. Adapt PowerPort object to Server DTO and then to Json DTO
        PowerPortDTO powerPortDTO = powerPortDTOAdapter.adaptPowerPortToDTO( pp, itemId );
        return powerPortDTO;

	}

	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
    public void deleteItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo) throws BusinessValidationException, DataAccessException, BusinessInformationException {
		
		Long portId = portDetails.getPortId(); 
        Long itemId = portDetails.getItemId();
		MapBindingResult errors = getErrorsObject(DataPort.class);
		
		powerPortObjectTemplate.delete(portDetails, userInfo, errors);
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public Object getAllItemDataPortsExtAPI(long itemId, UserInfo userInfo) throws BusinessValidationException {
		log.debug("getAllDataPortsExtAPI(), itemId=" + itemId );
		List<DataPortDTO> retval = null;
        MapBindingResult errors = getErrorsObject(DataPort.class);
        
        Map<String, Object> targetMap = new HashMap<String, Object>();
        targetMap.put("portId", -1L);
        targetMap.put("portClass", DataPort.class);
        targetMap.put("itemId", itemId);
        targetMap.put("UserInfo", userInfo);
                        
        //1. validate params in target map on DTO level
        dataPortReadItemValidator.validate(targetMap, errors);
        if (errors.hasErrors()) {
            throwBusinessValidationException(errors, new ArrayList<String>(), null, null);
        }

		Item item = (Item)itemFinderDAO.findById( itemId ).get(0);
		
		retval =  getDataPortDTOs(itemId);

		return retval;
	}

	
	
	@Override
	@Transactional(rollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	public void deleteItemDataPortExtAPI(Long itemId, Long portId, boolean skipValidation, UserInfo userInfo)
			throws BusinessValidationException, BusinessInformationException, DataAccessException {
		
		MapBindingResult errors = getErrorsObject(DataPort.class);
		
		dataPortObjectTemplate.delete(itemId, portId, userInfo, skipValidation, errors);
		
	}



	
	@Override
	@Transactional (readOnly = true)
	public Set<BasicItemInfo> searchItemsExtAPI(String searchString, UserInfo user) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,  BusinessValidationException, DataAccessException{
        //TODO: Create valdiator for RESTAPI search and move functionality from
        //itemDTOAdapter.validateSearchString into that validator
	
		List<Item> items = null;
		itemDTOAdapter.validateSearchString(searchString);
		items = itemDAO.searchItemsBySearchString(searchString);
		List<Object>matchingItems = null;
		Set<BasicItemInfo> itemsSet = new LinkedHashSet<BasicItemInfo>();
	
		if( items != null ){
			for( Item item : items){
				BasicItemInfo b = itemDTOAdapter.convertDomainItemToBasicItemInfo(item);
				
				itemsSet.add(b);
			}	
		}
		
		return itemsSet;
	}

	@Override
	@Transactional (readOnly = true)
	public Set<MobileSearchItemInfo> searchItemsWithLocationExtAPI(String searchString, String locationString, UserInfo user, int limit, int offset) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,  BusinessValidationException, DataAccessException{
        //TODO: Create valdiator for RESTAPI search and move functionality from
        //itemDTOAdapter.validateSearchString into that validator
	
		List<Item> items = null;
		itemDTOAdapter.validateSearchString(searchString);
		items = itemDAO.searchItemsBySearchStringWithLocation(searchString, locationString, limit, offset);
		List<Object>matchingItems = null;
		Set<MobileSearchItemInfo> itemsSet = new LinkedHashSet<MobileSearchItemInfo>();
	
		if( items != null ){
			for( Item item : items){
				MobileSearchItemInfo b = itemDTOAdapter.convertDomainItemToMobileSearchItemInfo(item);				
				itemsSet.add(b);
			}	
		}
		
		return itemsSet;
	}
	
	/*
	Map<String, Set<MobileSearchItemInfo>> organizeMobileSearchResultWithLocation(Set<MobileSearchItemInfo> setBefore, )
		Session session = this.sessionFactory.getCurrentSession();
		String SQL_QUERY = "select count(*) from dct_items where dct_items.parent_item_id = " + String.valueOf(itemId);
		org.hibernate.SQLQuery query = session.createSQLQuery(SQL_QUERY);
		Object rec = query.list();
		if (rec.toString().equals("0"))
			return false;
		else
			return true;		
		
	}*/
	
	private MapBindingResult getErrorsObject(Class<?> errorBindingClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, errorBindingClass.getName());
		return errors;
	}

	@Override
	public Boolean isMoveRequestAllowed(List<Integer> itemIds, UserInfo userInfo) throws BusinessValidationException, DataAccessException {
		return itemRequest.getIsMoveRequestAllowed(itemIds, userInfo);
	}

    @Override
    public List<Map<String, Object>> getCabinetItemList(Long cabinetId) throws Throwable {
        Session session = this.sessionFactory.getCurrentSession();

        StringBuffer queryStr = new StringBuffer().append("FROM Item item WHERE item.parentItem.itemId = :itemId ");
        Long movingItemId = powerPortMoveDAO.getMovingItemId(cabinetId);
        if (null != movingItemId && movingItemId > 0) {
        	cabinetId = movingItemId;
        	
        	// Exclude items above and below
        	queryStr.append(" AND item.uPosition NOT IN ( ");
        	queryStr.append(new Long(SystemLookup.SpecialUPositions.ABOVE).toString());
        	queryStr.append(", ");
        	queryStr.append(new Long(SystemLookup.SpecialUPositions.BELOW).toString());
        	queryStr.append(" )");

        	// Exclude Data Panels 
        	queryStr.append(" AND item.classLookup.lkpValueCode NOT IN ( ");
        	queryStr.append(new Long(SystemLookup.Class.DATA_PANEL).toString());
        	queryStr.append(" )");
        }
        
    	// Exclude hidden items 
    	queryStr.append(" AND item.statusLookup.lkpValueCode NOT IN ( ");
    	queryStr.append(new Long(SystemLookup.ItemStatus.HIDDEN).toString());
    	queryStr.append(" )");
    	
        Query query = session.createQuery(queryStr.toString());
        query.setLong("itemId", cabinetId);

        // Item list for return
        List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
        // The attributes of a single item
        Map<String, Object> itemMap;

        // Query result
        List<Object> results = query.list();

        ItemServiceDetails itemDetails;
        for (Object result:results) {

            Item item= (Item) result;
            itemMap = new HashMap<String, Object>();

            itemMap.put("id", item.getItemId());

            if (item.getMountedRailLookup() != null) {
                itemMap.put("radioRailsUsed", item.getMountedRailLookup().getLkpValue());
            }

            itemMap.put("cmbUPosition", item.getuPosition());
            itemMap.put("tiName", item.getItemName());
            itemMap.put("tiAlias", item.getItemAlias());

            itemDetails = item.getItemServiceDetails();

            if (itemDetails.getPurposeLookup() != null) {
                itemMap.put("cmbType", itemDetails.getPurposeLookup().getLkuValue());
            }

            if (itemDetails.getFunctionLookup() != null) {
                itemMap.put("cmbFunction", itemDetails.getFunctionLookup().getLkuValue());
            }

            ModelDetails model = item.getModel();
            if (model != null) {
	            itemMap.put("cmbMake", model.getModelMfrDetails().getMfrName());
	            itemMap.put("cmbModel", model.getModelName());
	            itemMap.put("modelId", model.getModelDetailId());
	            itemMap.put("mounting", model.getMounting());

	            if (model.getFormFactor() != null) {
	                itemMap.put("formFactor", model.getFormFactor());
	            }

	            itemMap.put("tiWeight", model.getWeight());
	            itemMap.put("tiRackUnits", model.getRuHeight());
            }
            
            itemMap.put("classId", item.getClassLookup().getLksId());
            itemMap.put("className", item.getClassLookup().getLkpValue());
            itemMap.put("classCode", item.getClassLookup().getLkpValueCode());

            if (item.getSubclassLookup() != null) {
                itemMap.put("subClassId", item.getSubclassLookup().getLksId());
                itemMap.put("subClassName", item.getSubclassLookup().getLkpValue());
                itemMap.put("subClassCode", item.getSubclassLookup().getLkpValueCode());
            }
            
            itemMap.put("tiEffectivePower", getItemsEffectivePower(item));

            itemList.add(itemMap);
        }
        return itemList;
    }

    private Double getItemsEffectivePower(Item item) {
		Double retval = null;
		if( item instanceof ItItem ){
			ItItem itItem = (ItItem)item;
			retval = itItem.getEffectivePower();
		}
		return retval;
	}

	/** Get the Long object from string. */
    private Long getLongFromCode(Object code){
    	Double did = null;
    	Long id = null;
    	try{
    		did = Double.parseDouble(code.toString());
    		id = did.longValue();
    	}catch(NumberFormatException nf){
    		id = null;
    	}catch(Exception e){
    		id = null;
    	}
    	return id;
    }

    @Override
    public Long savePassiveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo userInfo) throws Throwable {
        return passiveItemObject.saveItem(itemId, dtoList, userInfo);
    }

    @Override
    public void deletePassiveItem(Long itemId) throws Throwable {
        passiveItemObject.deleteItem(itemId);
    }

	@Override
	@Transactional
	public void setUserRequestBypassSetting(Boolean value, UserInfo userInfo) throws Throwable {
		userHome.setUserRequestByPassSetting(value, new Long(userInfo.getUserId()));
	}

	@Override
	public Boolean isPIQIntegrationEanabled(Long itemId) throws Throwable {
		return itemDAO.getItemsPIQIntegrationStatus(itemId);
	}

    /* (non-Javadoc)
     * @see com.raritan.tdz.item.home.ItemHome#getItemForReport(com.raritan.tdz.reports.json.JSONReportFilterConfig)
     */
    @Override
    public List<JSONReportFilterResult> getItemForReport(JSONReportFilterConfig filterConfig) throws DataAccessException {
        Session session = sessionFactory.getCurrentSession();

        StringBuilder query = new StringBuilder();
        query.append("SELECT _item_.item_id, _item_.item_name, _location_.code, _class_.lkp_value AS class_name, _status_.lkp_value AS status_name, _cabinet_.item_name AS cabinet_name");

        query.append(" FROM dct_items AS _item_ ");
        query.append(" LEFT JOIN dct_item_details AS _details_ ON _item_.item_id = _details_.item_detail_id ");
        query.append(" LEFT JOIN dct_lks_data AS _origin_lks_ ON _details_.origin_lks_id = _origin_lks_.lks_id ");
        query.append(" LEFT JOIN dct_items _cabinet_ ON _item_.parent_item_id = _cabinet_.item_id ");

        query.append(" LEFT JOIN (SELECT location_id, code FROM dct_locations) AS _location_ ON _item_.location_id = _location_.location_id ");
        query.append(" LEFT JOIN (SELECT lks_id, lkp_value FROM dct_lks_data WHERE lkp_type_name = 'CLASS') AS _class_ ON _item_.class_lks_id = _class_.lks_id ");
        query.append(" LEFT JOIN (SELECT lks_id, lkp_value FROM dct_lks_data WHERE lkp_type_name = 'ITEM_STATUS') AS _status_ ON _item_.status_lks_id = _status_.lks_id ");

        query.append(" WHERE _item_.item_id > 0" );
        query.append(" AND _item_.item_id NOT IN (SELECT move_item_id FROM dct_ports_move_power)"); // excluding when moved items from the list
        query.append(" AND _origin_lks_.lkp_value_code != 9007" ); // exclude vpc items

        // filter location by code
        if (filterConfig != null && filterConfig.getLocationList() != null && filterConfig.getLocationList().size() > 0) {
            query.append(" AND UPPER(_location_.code) IN (");
            for (int i = 0; i < filterConfig.getLocationList().size(); i++) {
                query.append("UPPER('").append(filterConfig.getLocationList().get(i)).append("')");
                if (i < filterConfig.getLocationList().size() - 1) {
                    query.append(",");
                }
            }
            query.append(")");
        }


        // filter class by name
        if (filterConfig != null && filterConfig.getClassList() != null && filterConfig.getClassList().size() > 0) {
            query.append(" AND UPPER(_class_.lkp_value) IN (");
            for (int i = 0; i < filterConfig.getClassList().size(); i++) {
                query.append("UPPER('").append(filterConfig.getClassList().get(i)).append("')");
                if (i < filterConfig.getClassList().size() - 1) {
                    query.append(",");
                }
            }
            query.append(")");
        } else {
            List<Long> classIdList = new ArrayList<Long>();
            // default
            query.append(" AND UPPER(_class_.lkp_value) IN (");
            query.append("UPPER('Device'),"); // 1
            query.append("UPPER('Network'),"); // 2
            query.append("UPPER('Data Panel'),"); // 3
            query.append("UPPER('Power Outlet'),"); // 4
            query.append("UPPER('Rack PDU'),"); // 5
            query.append("UPPER('Probe'),"); // 7
            query.append("UPPER('UPS'),"); // 12
            query.append("UPPER('CRAC')"); // 13
            query.append(")");
        }


        // filter status by name
        if (filterConfig != null && filterConfig.getStatusList() != null && filterConfig.getStatusList().size() > 0) {
            query.append(" AND UPPER(_status_.lkp_value) IN (");
            for (int i = 0; i < filterConfig.getStatusList().size(); i++) {
                query.append("UPPER('").append(filterConfig.getStatusList().get(i)).append("')");
                if (i < filterConfig.getStatusList().size() - 1) {
                    query.append(",");
                }
            }
            query.append(")");
        } else {
            // default
            query.append(" AND UPPER(_status_.lkp_value) IN (");
            query.append("UPPER('Planned'),"); // 301
            query.append("UPPER('Installed'),"); // 303
            query.append("UPPER('Powered-off'),"); // 304
            query.append("UPPER('Storage'),"); // 305
            query.append("UPPER('Off-Site'),"); // 308
            query.append("UPPER('Archived')"); // 310
            query.append(")");
        }

        // filter item name
        if (filterConfig != null && filterConfig.getName() != null && filterConfig.getName().length() > 0) {
            query.append(" AND ");
            query.append("UPPER(_item_.item_name) LIKE UPPER('%").append(filterConfig.getName()).append("%')");
        }

        // filter cabinet name
        if (filterConfig != null && filterConfig.getCabinet() != null && filterConfig.getCabinet().length() > 0) {
            query.append(" AND ");
            query.append("UPPER(_cabinet_.item_name) LIKE UPPER('%").append(filterConfig.getCabinet()).append("%')");
        }

        // sort
        if (filterConfig != null && filterConfig.getSortMap() != null && filterConfig.getSortMap().size() > 0) {
            query.append(" ORDER BY ");

            String key;
            Iterator<String> itr = filterConfig.getSortMap().keySet().iterator();
            while (itr.hasNext()) {
                key = itr.next();

                switch(key) {
                case "location":
                    query.append("_location_.code");
                    break;
                case "class":
                    query.append("class_name");
                    break;
                case "status":
                    query.append("status_name");
                    break;
                case "name":
                    query.append("_item_.item_name");
                    break;
                case "cabinet":
                    query.append("cabinet_name");
                    break;
                }

                if (filterConfig.getSortMap().get(key)) {
                    query.append(" DESC");
                }

                if (itr.hasNext()) {
                    query.append(", ");
                }
            }
        }

        query.append(";");

        List<Object[]> list = session.createSQLQuery(query.toString()).list();

        JSONReportFilterResult result;
        List<JSONReportFilterResult> results = new ArrayList<JSONReportFilterResult>();
        for (Object[] objects : list) {
            result = new JSONReportFilterResult();

            result.setId(((BigInteger) objects[0]).longValue());
            result.setName((String) objects[1]);

            if(objects[2] != null) {
                result.setLocation((String) objects[2]);
            }
            if(objects[3] != null) {
                result.setClassName((String) objects[3]);
            }
            if(objects[4] != null) {
                result.setStatus((String) objects[4]);
            }
            if(objects[5] != null) {
                result.setCabinet((String) objects[5]);
            }

            results.add(result);
        }

        return results;
    }



}
