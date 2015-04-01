package com.raritan.tdz.circuit.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.changemgmt.home.ChangeMgmtHome26;
import com.raritan.tdz.changemgmt.service.ChangeMgmtService26;
import com.raritan.tdz.chassis.home.CompositeItem;
import com.raritan.tdz.chassis.home.NetworkBladeItemImpl;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.ConnectedPortDTO;
import com.raritan.tdz.circuit.dto.DataPortNodeDTO;
import com.raritan.tdz.circuit.dto.PortNodeInterface;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.CircuitRequestInfo;
import com.raritan.tdz.circuit.home.CircuitSearch;
import com.raritan.tdz.circuit.home.ConnectedPort;
import com.raritan.tdz.circuit.home.ConnectionRequest;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.circuit.home.ProposedCircuitInfo;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.circuit.util.ProposedCircuitHelper;
import com.raritan.tdz.circuit.validators.CircuitPermissionValidator;
import com.raritan.tdz.domain.CircuitItemViewData;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.exception.BusinessValidationException.WarningEnum;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.item.home.UPSBank;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestHelper;
import com.raritan.tdz.request.home.RequestHome;
import com.raritan.tdz.user.dao.UserDAO;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.views.DataPortObject;
import com.raritan.tdz.views.ItemObject;
import com.raritan.tdz.views.ItemPortObject;
import com.raritan.tdz.views.PowerPortObject;
import com.raritan.tdz.vpc.home.VPCHome;

public class CircuitPDServiceImpl implements CircuitPDService {
	private CircuitPDHome circuitHome;
	private ItemHome itemHome;
	private ChangeMgmtHome26 changeMgmt;
	private PortHome portHome;

	private ItemService itemService;
	private ChangeMgmtService26 changeMgmtService;
	private ResourceBundleMessageSource messageSource;
	private CircuitEditLock circuitEditLock;
	private CircuitSearch circuitSearch;
	private DataPortDAO dataPortDAO;

	@Autowired
	private PowerPortDAO powerPortDAO;

	@Autowired(required = true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;

	@Autowired(required = true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	@Autowired(required = true)
	private CircuitRequest circuitRequest;

	@Autowired
	private VPCHome vpcHome;

	@Autowired
	private ItemDAO itemDAO;

	private RequestHome requestHome;

	@Autowired
	RequestHelper requestHelper;

	@Autowired
	private Validator validateCircuitParentRequest;

	@Autowired(required = true)
	PowerCircuitDAO powerCircuitDao;

	@Autowired(required = true)
	DataCircuitHome dataCircuitHome;


	@Autowired(required = true)
	PowerCircuitHome powerCircuitHome;
	
	@Autowired
	private CircuitPermissionValidator circuitPermissionValidator;

	
	public RequestHome getRequestHome() {
		return requestHome;
	}

	public void setRequestHome(RequestHome requestHome) {
		this.requestHome = requestHome;
	}

	@Autowired
	RequestDAO requestDao;

	@Autowired
	BusinessExceptionHelper businessExceptionHelper;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UserHome userHome;

	public DataPortDAO getDataPortDAO() {
		return dataPortDAO;
	}

	public void setDataPortDAO(DataPortDAO dataPortDAO) {
		this.dataPortDAO = dataPortDAO;
	}

	private static Logger appLogger = Logger
			.getLogger(CircuitPDServiceImpl.class);

	public CircuitPDServiceImpl(CircuitPDHome circuitHome) {
		this.circuitHome = circuitHome;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public void setChangeMgmtService(ChangeMgmtService26 changeMgmtService) {
		this.changeMgmtService = changeMgmtService;
	}

	public void setChangeMgmt(ChangeMgmtHome26 changeMgmt) {
		this.changeMgmt = changeMgmt;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public PortHome getPortHome() {
		return portHome;
	}

	public void setPortHome(PortHome portHome) {
		this.portHome = portHome;
	}

	public CircuitEditLock getCircuitEditLock() {
		return circuitEditLock;
	}

	public void setCircuitEditLock(CircuitEditLock circuitEditLock) {
		this.circuitEditLock = circuitEditLock;
	}

	public void setCircuitSearch(CircuitSearch circuitSearch) {
		this.circuitSearch = circuitSearch;
	}

	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<ItemObject> viewItemsForLocation(Long locationId,
			Long portClassValueCode) throws ServiceLayerException {
		return itemService.viewItemsForLocation(locationId, portClassValueCode);
	}

	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<PortInterface> viewPortListForItem(Long itemId,
			boolean freePortOnly) throws ServiceLayerException {
		return itemService.viewPortListForItem(itemId, freePortOnly);
	}

	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ItemPortObject viewPortsForItem(Long itemId, boolean freePortOnly,
			Long portClassValueCode) throws ServiceLayerException {
		ItemPortObject ports = new ItemPortObject();

		if (portClassValueCode == null
				|| portClassValueCode == SystemLookup.PortClass.DATA) {
			ports.setDataPortList(viewDataPortsForItem(itemId, freePortOnly));
		}

		if (portClassValueCode == null
				|| portClassValueCode == SystemLookup.PortClass.POWER) {
			ports.setPowerPortList(viewPowerPortsForItem(itemId, freePortOnly));
		}

		return ports;
	}

	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<PowerPortObject> viewPowerPortsForItem(Long itemId,
			boolean freePortOnly) throws ServiceLayerException {

		List<PowerPortObject> recList = new ArrayList<PowerPortObject>();
		HashMap<Long, PowerPortObject> portMap = new HashMap<Long, PowerPortObject>();
		HashMap<Long, PowerPortObject> inputCordList = new HashMap<Long, PowerPortObject>();
		long fuseFreeWatts = 0;

		appLogger.debug("viewPowerPortsForItem");

		List<PowerPort> portList = this.itemHome.viewPowerPortsForItem(itemId,
				freePortOnly, null);

		for (PowerPort port : portList) {
			PowerPortObject rec = PowerProc.newPowerPortObject(port,
					this.powerCircuitHome);

			recList.add(rec);

			if (port.isRackPduOutlet()) {
				if (inputCordList.containsKey(port.getInputCordPortId()) == false) {
					List<Long> portIdList = new ArrayList<Long>();
					portIdList.add(port.getInputCordPortId());

					for (PowerPort p : this.itemHome.viewPowerPortsForItem(
							null, false, portIdList)) {
						PowerPortObject inpRec = PowerProc.newPowerPortObject(
								p, this.powerCircuitHome);
						inputCordList.put(port.getInputCordPortId(), inpRec);
					}
				}

				PowerPortObject inCord = inputCordList.get(port
						.getInputCordPortId());

				if (inCord != null) {
					rec.setFreeWatts(inCord.getFreeWatts());
				}

				// For outlet, free watts cannot be greater than Fuse Free Watts
				if (rec.getFuseLkuDesc() != null) {
					fuseFreeWatts = rec.getBreakerTotalWatts()
							- rec.getBreakerUsedWatts();
				} else {
					fuseFreeWatts = rec.getFreeWatts();
				}

				if (fuseFreeWatts < rec.getFreeWatts()) {
					rec.setFreeWatts(fuseFreeWatts);
				}
			}
			appLogger.debug("Item/Port Name: " + rec.getItemName() + "/"
					+ rec.getPortName());
			appLogger.debug("Free Watts: " + rec.getFreeWatts());

		}

		return recList;
	}

	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<DataPortObject> viewDataPortsForItem(Long itemId,
			boolean freePortOnly) throws ServiceLayerException {

		List<DataPortObject> recList = new ArrayList<DataPortObject>();

		Map<Integer, String> netinfo = this.itemHome
				.getDataPortNetInfoAsMap(itemId);

		for (DataPort o : this.itemHome.viewDataPortsForItem(itemId,
				freePortOnly, null)) {
			DataPortObject rec = DataProc.newDataPortObject(o);

			if (netinfo.get(rec.getPortId().intValue()) != null) {
				String t[] = netinfo.get(rec.getPortId().intValue()).split(
						"\\|");
				rec.setIpAddress(t[0]);
				rec.setSubnet(t[1]);
			}

			recList.add(rec);
		}

		return recList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CircuitDTO> viewCircuitByCriteria(CircuitCriteriaDTO cCriteria)
			throws ServiceLayerException {
		List<CircuitDTO> recList = new ArrayList<CircuitDTO>();
		List<CircuitNodeInterface> nodeList = null;
		CircuitDTO rec = null;
		long itemClassCode = -1;
		final CircuitUID circuitUID = cCriteria.getCircuitUID();
		boolean wasPriorNodeLogicalOrVM = false;

		if (appLogger.isDebugEnabled()) {
			appLogger
					.debug("\n\n++++++++++++++viewCircuitByCriteria BEGIN+++++++++++++++++\n");
		}

		if (cCriteria.validateParams()) {
			String errMsg = cCriteria.getInvalidSearchParamsEnumError();
			BusinessValidationException e = new BusinessValidationException(
					new ExceptionContext(errMsg, this.getClass()));
			e.addValidationError(errMsg);
			throw e;
		}

		final long lastPortNodeId = cCriteria.getLastNodePortId() != null ? cCriteria
				.getLastNodePortId() : 0;

		UserInfo userInfo = cCriteria.getUserInfo() == null ? userHome.getCurrentUserInfo() : cCriteria.getUserInfo();

		if (cCriteria.isDataCircuit()) {
			for (DataCircuit cx : this.dataCircuitHome.viewDataCircuitByCriteria(cCriteria)) {
				rec = new CircuitDTO();
				rec.setCircuitId(cx.getCircuitUID().floatValue());
				rec.setCircuitTrace(cx.getCircuitTrace());
				rec.setSharedCircuitTrace(cx.getSharedCircuitTrace());
				rec.setEndConnId(cx.getEndConnId());
				rec.setStartConnId(cx.getStartConnId());
				rec.setPartialCircuitInUse(this.circuitHome
						.isPartialCircuitInUse(cx));
				// rec.setStatusCode(cx.getCircuitConnections().get(0).getStatusLookup().getLkpValueCode());
				rec.setStatusCode(cx.getStartConnection().getStatusLookup()
						.getLkpValueCode());
				rec.setProposeCircuitId(this.circuitHome
						.getProposedCircuitId(cx));
				rec.setUserInfo(userInfo);
				
				nodeList = new ArrayList<CircuitNodeInterface>();

				for (DataConnection dc : cx.getCircuitConnections()) {
					// only need source_port for the node since dest_port will
					// be the next connection node
					// p1 -> p2
					// p2 -> px
					// px -> null
					DataPortNodeDTO dnode = DataProc.newDataPortNodeDTO(dc
							.getSourceDataPort());
					dnode.setId(dc.getDataConnectionId());
					dnode.setSharedConnection(cx.isSharedConnection(dc));

					if (wasPriorNodeLogicalOrVM) {
						dnode.setReadOnly(true);
					} else {
						wasPriorNodeLogicalOrVM = dc.getSourceDataPort()
								.isLogical()
								|| dc.getSourceDataPort().isVirtual();
					}

					// ItemViewData item =
					// this.itemHome.viewItems(dnode.getItemId());
					CircuitItemViewData item = this.itemHome
							.viewCircuitItems(dnode.getItemId());

					if (item != null) {
						ItemObject itemObject = CircuitProc.newItemObject(item);
						dnode.setItemObject(itemObject);
					}

					if (dnode.getItemObject() != null) {
						itemClassCode = dnode.getItemObject().getClassLksCode()
								.longValue();
						dnode.setItemClassLksValueCode(itemClassCode);
					}

					nodeList.add(dnode);

					if (dc.getDestDataPort() != null) {
						WireNodeInterface wireNode = DataProc.newWireNode(dc, userInfo);
						wireNode.setSharedConnection(dnode.isSharedConnection());
						nodeList.add(wireNode);
					}
				}

				if (nodeList.size() > 0) {
					CircuitNodeInterface lastNode = nodeList.get(nodeList
							.size() - 1);
					lastNode.setLastNode(true);
				}

				rec.setNodeList(nodeList);

				rec.setCircuitState(getCircuitState(rec).getState());
				recList.add(rec);
			}
		} else {
			for (PowerCircuit cx : this.powerCircuitHome.viewPowerCircuitByCriteria(cCriteria)) {
				// PowerPort breakerPort = null;

				rec = new CircuitDTO();
				rec.setCircuitId(cx.getCircuitUID().floatValue());
				rec.setCircuitTrace(cx.getCircuitTrace());
				rec.setSharedCircuitTrace(cx.getSharedCircuitTrace());
				rec.setEndConnId(cx.getEndConnId());
				rec.setStartConnId(cx.getStartConnId());
				rec.setPartialCircuitInUse(this.circuitHome
						.isPartialCircuitInUse(cx));
				rec.setStatusCode(cx.getStartConnection().getStatusLookup()
						.getLkpValueCode());
				rec.setProposeCircuitId(this.circuitHome
						.getProposedCircuitId(cx));
				rec.setUserInfo(userInfo);
				
				nodeList = new ArrayList<CircuitNodeInterface>();

				for (PowerConnection dc : cx.getCircuitConnections()) {
					PowerPort powerPort = dc.getSourcePowerPort();
					powerPort = (PowerPort) powerPortDAO.read(powerPort
							.getPortId());
					if (null == powerPort && cCriteria.isVpcRequested()) {
						powerPort = dc.getSourcePowerPort();
					}
					List<Long> movePortIds = new ArrayList<Long>();
					if (null != powerPort.getPortId())
						movePortIds.add(powerPort.getPortId());
					Map<Long, LksData> portsAction = powerPortMoveDAO
							.getMovePortAction(movePortIds);

					PowerPortNodeDTO pnode = (PowerPortNodeDTO) PortsAdaptor
							.adaptPowerPortDomainToDTO(powerPort, true,
									portsAction);
					pnode.setId(dc.getPowerConnectionId());
					pnode.setSharedConnection(cx.isSharedConnection(dc));

					// ItemViewData item =
					// this.itemHome.viewItems(pnode.getItemId());
					CircuitItemViewData item = this.itemHome
							.viewCircuitItems(pnode.getItemId());

					if (item != null) {
						ItemObject itemObject = CircuitProc.newItemObject(item);
						pnode.setItemObject(itemObject);

						itemClassCode = pnode.getItemObject().getClassLksCode()
								.longValue();
						pnode.setItemClassLksValueCode(itemClassCode);

						if (powerPort.isUpsBreaker()) {
							formatUpsBankNode(pnode, powerPort);
						}
					}

					nodeList.add(pnode);

					if (dc.getDestPowerPort() != null) {
						PowerPort srcPowerPort = (PowerPort) powerPortDAO
								.read(dc.getSourcePowerPort().getPortId());
						if (null == srcPowerPort && cCriteria.isVpcRequested()) {
							srcPowerPort = dc.getSourcePowerPort();
						}
						PowerPort dstPowerPort = (PowerPort) powerPortDAO
								.read(dc.getDestPowerPort().getPortId());
						if (null == dstPowerPort && cCriteria.isVpcRequested()) {
							dstPowerPort = dc.getDestPowerPort();
						}
						dc.setSourcePowerPort(srcPowerPort);
						dc.setDestPowerPort(dstPowerPort);
						WireNodeInterface wireNode = PowerProc.newWireNode(dc, userInfo);
						wireNode.setSharedConnection(pnode.isSharedConnection());
						nodeList.add(wireNode);
					}
				}

				if (nodeList.size() > 0) {
					CircuitNodeInterface lastNode = nodeList.get(nodeList
							.size() - 1);
					lastNode.setLastNode(true);
				}

				rec.setNodeList(nodeList);
				rec.setCircuitState(getCircuitState(rec).getState());
				recList.add(rec);
			}
		}

		//
		// Post processing logic to set editability of nodes in the circuit
		//
		for (CircuitDTO circuit : recList) {
			setCircuitEditability(circuit, cCriteria);
			setCircuitPowerUsage(circuit, cCriteria);
		}

		//
		// Special case for uplinks
		//
		if (lastPortNodeId > 0) {
			postProcessUplinks(lastPortNodeId, rec, recList, cCriteria);
		}

		if (nodeList != null) {
			for (CircuitNodeInterface n : nodeList) {
				if (appLogger.isDebugEnabled()) {
					appLogger.debug(n.toString());
				}
			}
		}

		if (appLogger.isDebugEnabled()) {
			appLogger
					.debug("++++++++++++++viewCircuitByCriteria END+++++++++++++++++\n");
		}

		return recList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<PowerPortDTO> viewPowerPortListForItem(Long itemId,
			boolean freePortOnly) throws ServiceLayerException {

		List<PowerPortDTO> recList = new ArrayList<PowerPortDTO>();

		for (PowerPort o : this.itemHome.viewPowerPortsForItem(itemId,
				freePortOnly, null)) {
			PowerPortDTO rec = PowerProc.newPowerPortDTO(o);
			recList.add(rec);
		}

		return recList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<DataPortDTO> viewDataPortListForItem(Long itemId,
			boolean freePortOnly) throws ServiceLayerException {

		List<DataPortDTO> recList = new ArrayList<DataPortDTO>();

		Map<Integer, String> netinfo = this.itemHome
				.getDataPortNetInfoAsMap(itemId);

		for (DataPort o : this.itemHome.viewDataPortsForItem(itemId,
				freePortOnly, null)) {
			List<Long> movePortIds = new ArrayList<Long>();
			movePortIds.add(o.getPortId());
			Map<Long, LksData> portsAction = dataPortMoveDAO
					.getMovePortAction(movePortIds);
			DataPortDTO rec = PortsAdaptor.adaptDataPortDomainToDTO(o,
					portsAction);

			if (netinfo.get(rec.getPortId().intValue()) != null) {
				String t[] = netinfo.get(rec.getPortId().intValue()).split(
						"\\|");
				rec.setIpAddress(t[0]);
				rec.setSubnet(t[1]);
			}

			recList.add(rec);
		}

		return recList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<CircuitListDTO> viewCircuitPDList(CircuitCriteriaDTO cCriteria)
			throws ServiceLayerException {

		if (cCriteria == null) {
			cCriteria = new CircuitCriteriaDTO();
		}

		return circuitSearch.searchCircuits(cCriteria, circuitHome
				.getCircuitRequestInfo(SystemLookup.PortClass.DATA, false),
				circuitHome.getCircuitRequestInfo(SystemLookup.PortClass.POWER,
						false), circuitHome.getSharedDataCircuitTraces(),
				circuitHome.getSharedPowerCircuitTraces());
	}

	@Override
	public CircuitDTO saveCircuit(CircuitDTO circuit)
			throws ServiceLayerException {
		try {
			Map<String, Object> circuitMap = circuitHome.saveCircuitWithDTO(this, circuit);

			CircuitDTO circuitDTO = (CircuitDTO) circuitMap.get(CircuitDTO.class.getName());

			@SuppressWarnings("unchecked")
			List<CircuitCriteriaDTO> requests = (List<CircuitCriteriaDTO>) circuitMap.get(List.class.getName());

			if (null != requests && requests.size() > 0) {
				createRequestForCircuit(requests);
			}

			Request request = (Request) circuitMap.get(Request.class.getName());

			if (null != request) {
				List<Request> reqList = new ArrayList<Request>();
				reqList.add(request);
				processRequestsWorkFlow(circuit.getUserInfo(), reqList, null);
			}

			return circuitDTO;
		} catch (ServiceLayerException e) {
			vpcHome.clearPowerOutletPort();
			throw e;
		}

	}

	@Override
	public Map<String, Object> saveCircuitWithDTO(CircuitDTO circuit)
			throws ServiceLayerException {
		long circuitId = -1;
		if (circuit == null)
			return new HashMap<String, Object>();
		final CircuitUID circuitUID = circuit.getCircuitUID();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId(circuit.getCircuitId());
		cCriteria.setCircuitType(circuit.getCircuitType());
		CircuitEditLock.Lock editLock = null;
		UserInfo userInfo = circuit.getUserInfo() == null ? userHome.getCurrentUserInfo() : circuit.getUserInfo();
		boolean requestBP = userDAO.getUserRequestByPassSetting(new Long(
				userInfo.getUserId()));
		Map<String, Object> circuitMap = new HashMap<String, Object>();
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance()
				.getTimeInMillis());
		String userName = userInfo.getUserName();

		circuit.setUserInfo(userInfo);
		
		try {
			// Acquires a lock before editing an existing circuit - no lock will
			// be acquired for new circuits
			editLock = circuitEditLock.acquireLock(circuit);

			// Find circuit if exists
			CircuitViewData circuitView = null;

			if (circuit.isNewCircuit() == false) {
				circuitView = this.circuitHome.getCircuitViewData(cCriteria);

				if (circuitView == null) {
					String errMsg = cCriteria.getNotFoundEnumError();
					errMsg = errMsg.replaceAll("<CircuitId>", cCriteria
							.getCircuitId().toString());

					BusinessValidationException e = new BusinessValidationException(
							new ExceptionContext(errMsg, this.getClass()));
					e.addValidationError(errMsg);
					throw e;
				}
			}

			this.validateUserPermission(circuitView, userInfo);
			
			preLoadCircuitPorts(circuit);

			// handle propose circuit
			if (ProposedCircuitHelper.getProposedCircuitId(circuit) > 0
					|| isCircuitInstalled(circuit, cCriteria)) {
				if (requestBP) {
					// new circuit is created. Now, delete temporary proposed
					// circuit
					if (ProposedCircuitHelper.getProposedCircuitId(circuit) > 0) {
						circuitHome.deleteProposedCircuit(circuit);
					}
				} else {
					ICircuitInfo newCircuit = validateProposeCircuit(circuit);
					circuitId = this.saveProposeCircuit(circuit, newCircuit);
					CircuitCriteriaDTO circuitCriteria = new CircuitCriteriaDTO();
					circuitCriteria.setCircuitId(circuit.getCircuitId());
					circuitCriteria.setProposeCircuitId(circuitId);
					circuitCriteria.setCircuitType(circuit.getCircuitType());

					List<CircuitDTO> circuitDTOs = viewCircuitByCriteria(circuitCriteria);

					if (circuitDTOs != null && circuitDTOs.size() > 0) {
						CircuitDTO circuitDTO = circuitDTOs.get(0);
						circuitDTO.setProposeCircuitId(circuitId);
						circuitMap.put(CircuitDTO.class.getName(), circuitDTO);
						// return circuitDTO;
						return circuitMap;
					} else {
						circuitMap.put(CircuitDTO.class.getName(), circuit);
						// return circuit;
						return circuitMap;
					}
				}
			}

			// convert Node objects to Connection objects
			DataCircuit dataCircuit = null;
			PowerCircuit powerCircuit = null;

			if (circuit.isDataCircuit()) {
				dataCircuit = DataProc.newDataCircuitFromNodes(circuit, dataCircuitHome);
			} else {
				powerCircuit = PowerProc.newPowerCircuitFromNodes(circuit, itemDAO, vpcHome, powerPortDAO); // VPC
			}

			// check for pending request
			CircuitRequestInfo requestInfo = null;

			if (circuit.isNewCircuit() == false) {
				// Check to see if there are pending request
				requestInfo = this.circuitHome.getRequestInfoForCircuit(
						circuitUID.getCircuitDatabaseId(),
						circuit.getCircuitType(), false);

				if (requestInfo.isRequestPending()) {
					String errMsg = cCriteria.getRequestPendingEnumErrorSave();
					errMsg = errMsg.replaceAll("<ItemName>",
							circuitView.getStartItemName());
					errMsg = errMsg.replaceAll("<PortName>",
							circuitView.getStartPortName());

					BusinessValidationException e = new BusinessValidationException(
							new ExceptionContext(errMsg, this.getClass()));
					e.addValidationError(errMsg);
					throw e;
				}

				// update circuit
				if (circuit.isDataCircuit()) {
					dataCircuit.futureCircuitStatus = circuit.getStatusCode();
					circuitId = this.dataCircuitHome.updateDataCircuit(dataCircuit);
				} else {
					powerCircuit.futureCircuitStatus = circuit.getStatusCode();
					circuitId = powerCircuitHome.updatePowerCircuit(powerCircuit);
				}
			} else { // add circuit
				circuit.setCircuitId(null);

				if (circuit.isDataCircuit()) {
					circuitId = this.dataCircuitHome.addDataCircuit(dataCircuit,
							userName, creationDate);
				} else {
					circuitId = this.powerCircuitHome.addPowerCircuit(powerCircuit,
							userName, creationDate);
				}
			}

			// Create request if needed
			if (circuit.getRequestTypeCode() != null
					&& circuit.getRequestTypeCode() > 0) {
				if (requestInfo != null && requestInfo.getRequestId() > 0) { // re-submit
																				// request
					Request request = this.changeMgmt
							.reSubmitRequest(requestInfo.getRequestId());

					circuitMap.put(Request.class.getName(), request);
				} else {
					List<CircuitCriteriaDTO> requestList = new ArrayList<CircuitCriteriaDTO>();
					CircuitCriteriaDTO request = new CircuitCriteriaDTO();

					request.setCircuitId(CircuitUID.getCircuitUID(circuitId,
							circuit.getCircuitType()));
					request.setCircuitType(circuit.getCircuitType());
					request.setRequestTypeCode(circuit.getRequestTypeCode());

					requestList.add(request);

					// Create requests for this circuit.
					circuitMap.put(List.class.getName(), requestList);
				}
			}
		} catch (InterruptedException e) {
			appLogger.error("Failed to get circuit edit lock", e);
			throw new SystemException(e);
		} finally {
			// Release circuit edit lock
			circuitEditLock.releaseLock(editLock);
		}

		//Don't refresh circuit since import does not use the updated DTO
		if(circuit.isOriginImport()) {
			circuitMap.put(CircuitDTO.class.getName(), circuit);
			return circuitMap;
		}

		float circuitUid = CircuitUID.getCircuitUID(circuitId, circuit
				.isDataCircuit() ? SystemLookup.PortClass.DATA
				: SystemLookup.PortClass.POWER);
				
		return refreshCircuit(circuitMap, circuit, circuitUid);

	}

	// At some point of time we may need to move this method to circuitHome
	// since this belongs there!
	private boolean isCircuitInstalled(CircuitDTO circuit,
			CircuitCriteriaDTO cCriteria) throws DataAccessException,
			BusinessValidationException {
		boolean isCircuitInstalled = false;

		if (circuit.isNewCircuit())
			return isCircuitInstalled;

		if (circuit.isDataCircuit()) {
			List<DataCircuit> dataCircuits = this.dataCircuitHome.viewDataCircuitByCriteria(cCriteria);
			if (dataCircuits != null && dataCircuits.size() > 0) {
				isCircuitInstalled = dataCircuits.get(0).getStartConnection() != null
						&& dataCircuits.get(0).getStartConnection()
								.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.INSTALLED;
			}
		} else {
			List<PowerCircuit> powerCircuits = this.powerCircuitHome.viewPowerCircuitByCriteria(cCriteria);
			if (powerCircuits != null && powerCircuits.size() > 0) {
				isCircuitInstalled = powerCircuits.get(0).getStartConnection() != null
						&& powerCircuits.get(0).getStartConnection()
								.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.INSTALLED;
			}
		}
		return isCircuitInstalled;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long deleteStructuredCablingConnection(Collection<StructureCableDTO> structuredCablingList) throws ServiceLayerException {
		long returnCode = 1;
		return returnCode;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long deleteCircuitByIds(Collection<CircuitCriteriaDTO> cCriteriaList)
			throws ServiceLayerException {
		Collection<Long> dataIds = new ArrayList<Long>();
		Collection<Long> powerIds = new ArrayList<Long>();
		
		CircuitRequestInfo requestInfo = null;
		CircuitViewData circuitView = null;

		String errMsg = "";
		
		

		for(CircuitCriteriaDTO rec:cCriteriaList){
			final CircuitUID circuitUID = rec.getCircuitUID();
			circuitView = this.circuitHome.getCircuitViewData(rec);

			if (circuitView == null) {
				errMsg = rec.getNotFoundInListEnumError();
				errMsg = errMsg.replaceAll("<CircuitId>", rec.getCircuitId()
						.toString());

				BusinessValidationException e = new BusinessValidationException(
						new ExceptionContext(errMsg, this.getClass()));
				// CR#40562 . For special case record not exist sending out the
				// error code and the recordId for client to handle
				e.setErrorCode(rec.getNotFoundInListEnumErrorCode());
				e.getRecordIds().add(circuitUID.getCircuitDatabaseId());
				e.addValidationError(errMsg);
				e.addValidationError("Circuit.cannotDelete", errMsg);
				
				throw e;
			}
			
			UserInfo userInfo = rec.getUserInfo() == null ? userHome.getCurrentUserInfo() : rec.getUserInfo();
			circuitView.setUserInfo(userInfo);
			
			// check user permission
			validateUserPermission(circuitView, userInfo);
			
			if (circuitView.isStatusInstalled()) {
				errMsg = ApplicationCodesEnum.CIRCUIT_DEL_INSTALLED_FAIL
						.value();

				BusinessValidationException e = new BusinessValidationException(
						new ExceptionContext(errMsg, this.getClass()));
				e.setErrorCode(ApplicationCodesEnum.CIRCUIT_DEL_INSTALLED_FAIL
						.errCode());
				e.getRecordIds().add(circuitUID.getCircuitDatabaseId());
				e.addValidationError(errMsg);
				e.addValidationError("Circuit.cannotDelete", errMsg);
				throw e;
			}

			if(circuitView.isStatusPoweredOff()){
				errMsg = ApplicationCodesEnum.CIRCUIT_DEL_POWEREDOFF_FAIL.value();

				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(errMsg, this.getClass()));
				e.setErrorCode(ApplicationCodesEnum.CIRCUIT_DEL_POWEREDOFF_FAIL.errCode());
				e.getRecordIds().add( circuitUID.getCircuitDatabaseId() );
				e.addValidationError(errMsg); 
				e.addValidationError("Circuit.cannotDelete", errMsg);
				throw e;
			}

			//Check to see if there are pending request
			requestInfo = this.circuitHome.getRequestInfoForCircuit(circuitUID.getCircuitDatabaseId(), rec.getCircuitType(), false);

			if (rec.isDataCircuit()) {
				dataIds.add(circuitUID.getCircuitDatabaseId());
			} else {
				// Check to see if there are pending request
				powerIds.add(circuitUID.getCircuitDatabaseId());
			}

			if (requestInfo.isRequestPending()) {
				errMsg = rec.getRequestPendingEnumError();
				errMsg = errMsg.replaceAll("<ItemName>",
						circuitView.getStartItemName());
				errMsg = errMsg.replaceAll("<PortName>",
						circuitView.getStartPortName());

				BusinessValidationException e = new BusinessValidationException(
						new ExceptionContext(errMsg, this.getClass()));
				// CR#40562 . For special case record not exist sending out the
				// error code and the recordId for client to handle
				e.setErrorCode(rec.getNotFoundInListEnumErrorCode());
				e.getRecordIds().add(circuitUID.getCircuitDatabaseId());
				e.addValidationError(errMsg);
				e.addValidationError("Circuit.cannotDelete", errMsg);
				throw e;
			} else if (requestInfo.getRequestId() > 0) { // delete request
				List<IPortInfo> ports = this.changeMgmt.deleteRequest(
						requestInfo.getRequestId(), false);
				if (ports != null) {
					for (IPortInfo port : ports) {
						portHome.unlockPort(port);
					}
				}
			}
		}
		
		long returnCode = 1;
		
		if (dataIds.size() > 0) {
			returnCode = this.dataCircuitHome.deleteDataCircuitByIds(dataIds, false);
		}

		if (returnCode == 1 && powerIds.size() > 0) {
			returnCode = this.powerCircuitHome.deletePowerCircuitByIds(powerIds,
					false);
		}

		return returnCode;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<ConnectedPortDTO> viewAllConnectedPorts()
			throws ServiceLayerException {
		List<ConnectedPortDTO> recList = new ArrayList<ConnectedPortDTO>();
		ConnectedPort priorRec = null;
		ConnectedPortDTO port = null;

		for (ConnectedPort rec : this.circuitHome.viewAllConnectedPorts()) {
			if (rec.isSamePort(priorRec) == false) {
				port = new ConnectedPortDTO();
				port.setCircuitType(rec.getCircuitType());
				port.setItemPortDesc(rec.getItemPortDesc());
				port.setLocationCode(rec.getLocationCode());
				port.setCircuitIdList(new ArrayList<Float>());

				recList.add(port);
			}

			port.getCircuitIdList().add(
					CircuitUID.getCircuitUID(rec.getCircuitId(),
							rec.getCircuitType()));
			priorRec = rec;
		}

		return recList;
	}

	@Override
	// @Transactional(propagation=Propagation.REQUIRED)
	public List<RequestDTO> createRequestForCircuit(Collection<CircuitCriteriaDTO> requestList)	throws ServiceLayerException {
		UserInfo userInfo = userHome.getCurrentUserInfo();
		
		for(CircuitCriteriaDTO cr:requestList) {
			userInfo = cr.getUserInfo() == null ? userInfo : cr.getUserInfo();
			cr.setUserInfo(userInfo);
		}
		
		Map<String, Object> requestMap = circuitHome.createCircuitRequest(this,	requestList, false);

		return circuitHome.postProcessRequest(this, requestMap);
	}

	@Override
	// @Transactional(propagation=Propagation.REQUIRED)
	public List<RequestDTO> createRequestForCircuitConfirmed(
			Collection<CircuitCriteriaDTO> requestList)
			throws ServiceLayerException {

		Map<String, Object> requestMap = circuitHome.createCircuitRequest(this,
				requestList, true);

		return circuitHome.postProcessRequest(this, requestMap);
	}

	@Override
	public Map<String, Object> createCircuitRequest(
			Collection<CircuitCriteriaDTO> requestList, boolean warningConfirmed)
			throws ServiceLayerException { // 3
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		CircuitViewData circuitView = null;
		CircuitRequestInfo circuitRequestInfo = null;
		BusinessValidationException bvex = null;
		Errors circuitInMoveParentErrors = getErrorObject();

		List<RequestDTO> reqList = new ArrayList<RequestDTO>();

		try {
			for (CircuitCriteriaDTO rec : requestList) {
				final CircuitUID circuitUID = rec.getCircuitUID();

				RequestDTO request = new RequestDTO();
				request.setRequestId(0L);
				request.setCircuitType(rec.getCircuitType());
				request.setCircuitId(circuitUID.floatValue());
				request.setUserInfo(rec.getUserInfo());
				
				// check if circuit exist first
				cCriteria.setCircuitId(rec.getCircuitId());
				cCriteria.setCircuitType(rec.getCircuitType());
				circuitView = this.circuitHome.getCircuitViewData(cCriteria);

				if (circuitView == null) {
					String errMsg = rec.getNotFoundInListEnumError();
					errMsg = errMsg.replaceAll("<CircuitId>", rec
							.getCircuitId().toString());

					BusinessValidationException e = new BusinessValidationException(
							new ExceptionContext(errMsg, this.getClass()));
					e.addValidationError(errMsg);
					e.setRequestList(reqList);
					throw e;
				}
				
				circuitView.setUserInfo(rec.getUserInfo());
				
				// check for pending requests
				circuitRequestInfo = this.circuitHome.getRequestInfoForCircuit(
						circuitUID.getCircuitDatabaseId(),
						rec.getCircuitType(), false);

				if (circuitRequestInfo.isRequestPending()) {
					String errMsg = ApplicationCodesEnum.DATA_CIR_PENDING_REQUEST
							.value();
					errMsg = errMsg.replaceAll("<ItemName>",
							circuitView.getStartItemName());
					errMsg = errMsg.replaceAll("<PortName>",
							circuitView.getStartPortName());

					BusinessValidationException e = new BusinessValidationException(
							new ExceptionContext(errMsg, this.getClass()));
					e.addValidationError(errMsg);
					e.setRequestList(reqList);
					throw e;
				}

				// issue requests
				long requestId = circuitRequestInfo.getRequestId().longValue();

				if (requestId > 0) {
					this.changeMgmt.reSubmitRequest(requestId);
				} else {

					if (!warningConfirmed) {
						// validate if the circuit has moving parents, if yes
						// throw a warning for the user to confirm
						Errors errorsPerCircuit = getErrorObject();
						Map<String, Object> targetMap = new HashMap<String, Object>();
						targetMap.put(CircuitViewData.class.getName(),
								circuitView);
						validateCircuitParentRequest.validate(targetMap,
								errorsPerCircuit);
						if (errorsPerCircuit.hasErrors()) {
							circuitInMoveParentErrors
									.addAllErrors(errorsPerCircuit);
							continue;
						}
					}

					if (rec.getRequestTypeCode() == SystemLookup.RequestType.REQUEST_CONNECT) {
						// requestId =
						// this.changeMgmtService.connectRequest(circuitView);
						requestId = this.circuitRequest.connect(circuitView);
					} else if (rec.getRequestTypeCode() == SystemLookup.RequestType.REQUEST_DISCONNECT) {
						if (!circuitHome
								.validateCircuitForDisconnect(circuitView)) {
							appLogger.debug("Circuit " + rec.getCircuitId()
									+ "is shared, cannot disconnect");
						}
						requestId = this.circuitRequest.disconnect(circuitView,
								null);
					}
				}

				if (requestId > 0) {
					Request r = new Request();
					r.setRequestId(requestId);

					for (Request req : this.changeMgmtService.viewRequest(r)) {
						request.setRequestId(req.getRequestId());
						request.setRequestNo(req.getRequestNo());
						request.setRequestType(req.getRequestType());

						for (RequestHistory hist : req.getRequestHistories()) {
							if (hist.isCurrent() == true) {
								request.setRequestStage(hist.getStageIdLookup()
										.getLkpValue());
								request.setRequestStageLksCode(hist
										.getStageIdLookup().getLkpValueCode());

							}
						}
						break;
					}
				}

				reqList.add(request);
			}
			throwWarningMessage(circuitInMoveParentErrors);

		} catch (BusinessValidationException ex) {
			bvex = ex;

			// clear all the warnings if confirmed by the user
			if (warningConfirmed) {
				if (bvex.getValidationWarnings().size() > 0) {
					bvex.setValidationWarnings((List) null);
					bvex.setValidationWarnings((Map) null);
					if (bvex.getValidationErrors().size() == 0) {
						bvex = null;
					}
				}
			}
			// throw the exception showing the warnings
			else {
				if (bvex.getValidationWarnings().size() > 0) {
					bvex.setCallbackURL("circuitPDService.createRequestForCircuitConfirmed");
					bvex.addCallbackArg(requestList);

					// do not flush this transaction and throw the warning
					// message for user to confirm
					TransactionAspectSupport.currentTransactionStatus()
							.setRollbackOnly();

					throw bvex;
				}
			}

		}

		List<Request> reqs = getRequestFromRequestDTO(reqList);

		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put(Request.class.getName(), reqs);
		requestMap.put(RequestDTO.class.getName(), reqList);
		requestMap.put(BusinessValidationException.class.getName(), bvex);

		return requestMap;
	}

	//
	// Private methods
	//

	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult(errorMap, this.getClass().getName());
		return errors;

	}

	@Override
	public List<RequestDTO> postProcessRequest(Map<String, Object> requestMap) throws ServiceLayerException {
		@SuppressWarnings("unchecked")
		List<Request> reqList = (List<Request>) requestMap.get(Request.class.getName());
		
		@SuppressWarnings("unchecked")
		List<RequestDTO> reqDtoList = (List<RequestDTO>) requestMap.get(RequestDTO.class.getName());
		
		BusinessValidationException bvex = (BusinessValidationException) requestMap.get(BusinessValidationException.class.getName());

		UserInfo userInfo = null;
		
		if(reqDtoList != null && reqDtoList.size() > 0) {
			userInfo = reqDtoList.get(0).getUserInfo();
		}
		
		userInfo = userInfo == null ? userHome.getCurrentUserInfo() : userInfo;
		
		processRequestsWorkFlow(userInfo, reqList, bvex);

		return reqDtoList;

	}

	private void throwWarningMessage(Errors errors)
			throws BusinessValidationException {

		List<String> validationWarningCodes = Arrays
				.asList("ItemMoveValidator.parentHasPendingRequest");

		BusinessValidationException e = new BusinessValidationException(
				new ExceptionContext(ApplicationCodesEnum.FAILURE.value(),
						this.getClass()));
		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error : objectErrors) {
				String msg = messageSource.getMessage(error,
						Locale.getDefault());
				if (validationWarningCodes != null
						&& validationWarningCodes.contains(error.getCode())) {
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg,
							WarningEnum.WARNING_YES_NO);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
			throw e;
		}
	}

	@Transactional(rollbackFor = ServiceLayerException.class)
	private ICircuitInfo validateProposeCircuit(CircuitDTO circuit)
			throws ServiceLayerException {
		ICircuitInfo origCircuit = null;
		ICircuitInfo newCircuit = null;

		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId(circuit.getCircuitId());
		cCriteria.setProposeCircuitId(circuit.getProposeCircuitId());
		cCriteria.setCircuitType(circuit.getCircuitType());

		if (circuit.isDataCircuit()) {
			origCircuit = getUniqueDataCircuit(cCriteria);
			newCircuit = DataProc.newDataCircuitFromNodes(circuit, dataCircuitHome);
		} else {
			origCircuit = getUniquePowerCircuit(cCriteria);
			newCircuit = PowerProc.newPowerCircuitFromNodes(circuit, itemDAO,
					vpcHome, powerPortDAO); // VPC
		}

		// Check that original circuit exists
		if (origCircuit == null) {
			String errMsg = cCriteria.getNotFoundEnumError();
			errMsg = errMsg.replaceAll("<CircuitId>", cCriteria.getCircuitId()
					.toString());
			BusinessValidationException e = new BusinessValidationException(
					new ExceptionContext(errMsg, this.getClass()));
			e.addValidationError(errMsg);
			throw e;
		}

		// Check a rare concurrency case - two users are editing an installed
		// circuit (without existing proposed circuit).
		// First user saves creates a proposed circuit. Second user should get
		// an error because they are attempting to edit
		// the installed circuit now that a proposed circuit has been created.
		// Also possible that there is no proposed circuit,
		// but that just patch cord information was changed in the first edit.
		// if (circuit.isStatusInstalled()) {
		if (isCircuitInstalled(circuit, cCriteria)) {
			Long proposedId = circuitHome.getProposedCircuitId(origCircuit);
			if (proposedId != null) {
				String msg = messageSource
						.getMessage(
								"circuit.editInstalledWithExistingProposed",
								null, null);
				BusinessValidationException be = new BusinessValidationException(
						new ExceptionContext(msg, this.getClass()));
				be.addValidationError(msg);
				throw be;
			}
		}

		circuitHome.diffCircuit(origCircuit, newCircuit);

		//
		// Validate the proposed circuit
		//
		circuitHome.validateCircuit(newCircuit);

		return newCircuit;
	}

	@Transactional(rollbackFor = ServiceLayerException.class)
	private long saveProposeCircuit(CircuitDTO circuit, ICircuitInfo newCircuit)
			throws ServiceLayerException {
		final CircuitUID circuitUID = circuit.getCircuitUID();
		Long circuitId = null;
		ICircuitInfo origCircuit = null;
		long proposedCircuitId = ProposedCircuitHelper
				.getProposedCircuitId(circuit);
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId(circuit.getCircuitId());
		cCriteria.setProposeCircuitId(circuit.getProposeCircuitId());
		cCriteria.setCircuitType(circuit.getCircuitType());

		if (circuit.isDataCircuit()) {
			origCircuit = getUniqueDataCircuit(cCriteria);
		} else {
			origCircuit = getUniquePowerCircuit(cCriteria);
		}

		if (proposedCircuitId > 0) {
			newCircuit.setCircuitId(proposedCircuitId);
			origCircuit = circuitHome.getOriginalCircuitForProposed(
					circuitUID.getCircuitDatabaseId(),
					origCircuit.getCircuitType());

			ConnectionRequest req = circuitHome.diffCircuit(origCircuit,
					newCircuit);

			if (req.hasChanges()) {
				circuitId = updateProposedCircuit(req, circuit, origCircuit,
						newCircuit);
			} else {
				// No difference between original and the edited proposed
				// circuit
				String msg = messageSource.getMessage(
						"circuit.modifiedProposedSameAsOriginal", null, null);
				BusinessValidationException ex = new BusinessValidationException(
						new ExceptionContext(msg, this.getClass()));
				ex.addValidationError(msg);
				throw ex;
			}
		} else {
			newCircuit.setCircuitId(0L);

			ConnectionRequest req = circuitHome.diffCircuit(origCircuit,
					newCircuit);

			if (req.hasChanges()) {
				circuitId = createNewProposedCircuit(req, origCircuit,
						newCircuit);
			} else {
				// No difference between original and proposed
				String msg = messageSource.getMessage(
						"circuit.newProposedSameAsOriginal", null, null);
				BusinessValidationException ex = new BusinessValidationException(
						new ExceptionContext(msg, this.getClass()));
				ex.addValidationError(msg);
				throw ex;
			}
		}

		if (circuitId == null) {
			// No difference between original and proposed, return original
			// circuit ID
			circuitId = origCircuit.getCircuitId();
		}

		return circuitId;
	}

	/**
	 * Creates a new proposed circuit.
	 * 
	 * @param circuit
	 * @param cCriteria
	 * @return the new circuit id, or, if there are no changes, returns null
	 */
	private Long createNewProposedCircuit(ConnectionRequest connReq,
			ICircuitInfo origCircuit, ICircuitInfo newCircuit)
			throws DataAccessException, BusinessValidationException {
		final boolean isDataCircuit = origCircuit instanceof DataCircuit;

		// Long newCircuitId = null;
		ProposedCircuitInfo propCircuit = null;

		if (connReq != null) {
			//
			// Save any connections to move (i.e., disconnect/reconnect).
			// This is required for windows client to process the updated
			// circuit properly.
			//
			propCircuit = circuitHome.saveProposedCircuit(
					origCircuit.getCircuitId(), connReq,
					newCircuit.getCircuitId());
			// newCircuitId = circuitHome.saveProposedCircuit(
			// origCircuit.getCircuitId(), connReq, newCircuit.getCircuitId() );

			// Next, lock connections used by proposed circuit
			boolean lockConnection = true;
			for (ICircuitConnection conn : propCircuit.getConnsToUpdate()) {
				if (lockConnection) {
					lockConnection = circuitHome.lockConnection(conn);
				}
			}

			//
			// Last, issue change management request for disconnect/reconnects.
			//
			final long portClassValueCode = isDataCircuit ? SystemLookup.PortClass.DATA
					: SystemLookup.PortClass.POWER;
			final ConnectionRequest relatedReq = connReq.getRelatedRequest();

			String requestNo = processConnectionRequest(connReq,
					propCircuit.getCircuitId(), portClassValueCode, null);

			if (relatedReq != null) {
				processConnectionRequest(relatedReq,
						propCircuit.getCircuitId(), portClassValueCode,
						requestNo + "-01");
			}
		}

		return propCircuit.getCircuitId();
	}

	private String processConnectionRequest(ConnectionRequest connReq,
			Long newCircuitId, Long portClassValueCode, String requestNo)
			throws DataAccessException {
		Long itemId = null;
		String portName = null;
		ICircuitInfo circuit = connReq.getCircuit();
		ICircuitConnection conn = connReq.getFirstConnection();
		Long requestId = null;
		CircuitViewData circuitView = null;

		if (circuit != null) {
			itemId = circuit.getStartItemId();
			portName = circuit.getStartPortName();
			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(
					circuit.getCircuitId(), circuit.getCircuitType()));
			cCriteria.setCircuitType(circuit.getCircuitType());
			circuitView = this.circuitHome.getCircuitViewData(cCriteria);
		} else if (conn != null) {
			Item sourceItem = conn.getSourceItem();
			if (sourceItem != null) {
				itemId = sourceItem.getItemId();
			}
			portName = conn.getSourcePortName();
		}

		switch (connReq.getType()) {
		case CONNECT:
			if (circuit != null) {
				requestId = this.circuitRequest.connect(circuitView);
			}
			break;
		case DISCONNECT:
			if (circuit != null) {
				requestId = this.circuitRequest.disconnect(circuitView, null);
			}
			break;
		case DISCONNECT_AND_MOVE:
			if (itemId != null) {
				requestId = changeMgmt.disconnectAndMoveRequest(itemId,
						connReq.getConnList(), portClassValueCode, portName);
			}
			break;
		case RECONNECT:
			if (itemId != null) {
				requestId = changeMgmt.reconnectRequest(itemId, newCircuitId,
						portClassValueCode, portName, requestNo);
			}
			break;
		}

		if (requestId != null && requestId > 0) {
			return this.requestDao.read(requestId).getRequestNo();
		}

		return null;
	}

	private DataCircuit getUniqueDataCircuit(CircuitCriteriaDTO cCriteria)
			throws ServiceLayerException {
		List<DataCircuit> circuits = dataCircuitHome.viewDataCircuitByCriteria(cCriteria);
		if (circuits != null && circuits.size() > 0) {
			return circuits.get(0);
		}
		return null;
	}

	private PowerCircuit getUniquePowerCircuit(CircuitCriteriaDTO cCriteria)
			throws ServiceLayerException {
		List<PowerCircuit> circuits = powerCircuitHome.viewPowerCircuitByCriteria(cCriteria);
		if (circuits != null && circuits.size() > 0) {
			return circuits.get(0);
		}
		return null;
	}

	/*
	 * Updates an existing proposed circuit.
	 */
	private long updateProposedCircuit(ConnectionRequest connReq,
			CircuitDTO circuit, ICircuitInfo origCircuit,
			ICircuitInfo newProposedCircuit) throws DataAccessException,
			BusinessValidationException {
		final CircuitUID circuitUID = circuit.getCircuitUID();
		CircuitRequestInfo requestInfo = this.circuitHome
				.getRequestInfoForCircuit(circuitUID.getCircuitDatabaseId(),
						circuit.getCircuitType(), true);

		// Check to see if there are pending request
		if (requestInfo.isRequestPending()) {
			String errMsg = "There is a pending request on this circuit";
			// FIXME: add itemname and portname to error message
			// errMsg = errMsg.replaceAll("<ItemName>",
			// proposedCircuit.getStartItemName());
			// errMsg = errMsg.replaceAll("<PortName>",
			// proposedCircuit.getStartPortName());
			BusinessValidationException e = new BusinessValidationException(
					new ExceptionContext(errMsg, this.getClass()));
			e.addValidationError(errMsg);
			throw e;
		}

		// Deletes the existing proposed circuit and all related CM requests
		ProposedCircuitInfo propCircuit = circuitHome
				.deleteProposedCircuit(circuit);

		// Create a new proposed circuit
		long newCircuitId = createNewProposedCircuit(connReq, origCircuit,
				newProposedCircuit);

		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitType(origCircuit.getCircuitType());
		cCriteria.setCircuitId(CircuitUID.getCircuitUID(newCircuitId,
				circuit.getCircuitType(), true));
		cCriteria.setProposeCircuitId(newCircuitId);
		List<? extends ICircuitInfo> results = circuitHome
				.viewCircuitByCriteria(cCriteria);

		if (results != null && !results.isEmpty()) {
			ICircuitInfo newCircuit = results.get(0);
			// Set all associated CM request statuses to updated.
			// From the user perspective, we have simply updated the existing
			// requests associated with the original proposed circuit.
			for (Long requestId : changeMgmt.getRequestsForCircuit(newCircuit)) {
				changeMgmt.reSubmitRequest(requestId);
			}
		}

		return newCircuitId;
	}

	private void postProcessUplinks(long lastPortNodeId, CircuitDTO rec,
			List<CircuitDTO> recList, CircuitCriteriaDTO cCriteria)
			throws BusinessValidationException, DataAccessException {

		if (recList == null || recList.isEmpty()) {
			// The circuit that we are tracing has been deleted (CR #41412)
			String msg = messageSource.getMessage("uplink.circuitDeleted",
					null, null);
			BusinessValidationException ex = new BusinessValidationException(
					new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(msg);
			throw ex;
		}

		List<CircuitNodeInterface> nodes = recList.get(0).getNodeList();

		if (nodes != null && !nodes.isEmpty()) {
			CircuitNodeInterface firstNode = nodes.get(0);

			if ((firstNode != null) && (firstNode instanceof PortInterface)) {
				PortInterface portNode = (PortInterface) firstNode;
				// System.out.println("lastPortNodeId: " + lastPortNodeId +
				// ", portNodeId: " + portNode.getPortId());

				boolean reverseNodeList = false;

				final long lastNodeItemId = cCriteria.getLastNodeItemId() != null ? cCriteria
						.getLastNodeItemId() : 0;
				if (lastNodeItemId > 0) {
					Item item = itemHome.viewItemEagerById(lastNodeItemId);
					com.raritan.tdz.item.home.ItemObject itemObject = circuitHome
							.getItemObjectFactory().getItemObject(item);

					if (itemObject != null
							&& (itemObject instanceof NetworkBladeItemImpl)) {
						ItItem blade = (ItItem) item;

						long chassisId = blade.getBladeChassis() != null ? blade
								.getBladeChassis().getItemId() : 0;

						item = itemHome.viewItemEagerById(chassisId);
						itemObject = circuitHome.getItemObjectFactory()
								.getItemObject(item);
					}

					if (itemObject != null
							&& (itemObject instanceof CompositeItem)) {
						// Check if the item associated with this port node is a
						// composite item
						// If any of the contained items don't match the itemId
						// in the criteria,
						// then we need to reverse the node list.
						List<Item> childItems = ((CompositeItem) itemObject)
								.getChildItems();

						if (!childItems.isEmpty()) {
							Set<Long> itemIds = new HashSet<Long>(
									childItems.size());
							for (Item child : childItems) {
								itemIds.add(child.getItemId());
							}
							if (!itemIds.contains(portNode.getItemId())) {
								reverseNodeList = true;
							}
						} else {
							reverseNodeList = portNode.getItemId() != lastNodeItemId;
						}
					} else {
						// Not a composite item
						reverseNodeList = (portNode.getPortId() != null)
								&& (portNode.getPortId() != lastPortNodeId);
					}
				} else {
					reverseNodeList = (portNode.getPortId() != null)
							&& (portNode.getPortId() != lastPortNodeId);
				}

				if (reverseNodeList) {
					// Reverse the node list
					nodes = rec.reverseNodeList();
				}

				addStartingPortAndWireNodes(lastPortNodeId, nodes,
						cCriteria.isDataCircuit());
			}
		}
	}

	private void addStartingPortAndWireNodes(long lastPortNodeId,
			List<CircuitNodeInterface> nodes, boolean isDataCircuit) {
		if (lastPortNodeId <= 0 || nodes == null)
			return;

		// Add fake starting node to match lastPortNodeId
		if (isDataCircuit) {
			DataPortNodeDTO startNode = new DataPortNodeDTO();
			startNode.setPortId(lastPortNodeId);
			startNode.setReadOnly(true);
			nodes.add(0, startNode);
		} else {
			PowerPortNodeDTO startNode = new PowerPortNodeDTO();
			startNode.setPortId(lastPortNodeId);
			startNode.setReadOnly(true);
			nodes.add(0, startNode);
		}

		// Add dummy virtual wire node
		VirtualWireDTO vw = new VirtualWireDTO();
		vw.setReadOnly(true);
		vw.setClickable(false);
		nodes.add(1, vw);
	}

	private void formatPanelNode(PowerPortNodeDTO node, PowerPort port,
			double deRatedFactor) throws DataAccessException {
		MeItem itemPB = (MeItem) port.getItem();
		node.setReadOnly(true);

		node.setAmpsMax(itemPB.getRatingAmps());
		node.setAmpsRated(GlobalUtils.formatNumberTo0Dec(itemPB.getRatingAmps()
				* deRatedFactor));
		node.setPhaseLksDesc(itemPB.getPhaseLookup().getLkpValue());
		node.setVoltsLksDesc(String.valueOf(itemPB.getLineVolts()));

		node.setPolesLksDesc(itemPB.getPhasePoleDesc());
		node.setColorLkuDesc(itemPB.getPhaseColor());

		// set power usage
		Double totalWatts = 0.0;

		if (this.powerCircuitHome.isThreePhase(itemPB.getPhaseLookup().getLksId())) {
			totalWatts = itemPB.getLineVolts() * node.getAmpsRated()
					* Math.sqrt(3);
		} else {
			totalWatts = itemPB.getLineVolts() * node.getAmpsRated();
		}

		long usedWatts = node.getUsedWatts();
		node.setUsedWatts(usedWatts);
		node.setFreeWatts(totalWatts.longValue() - usedWatts);
		node.setTotalWatts(totalWatts.longValue());
	}

	private void formatFloorPDUNode(PowerPortNodeDTO node, PowerPort port,
			double deRatedFactor) throws DataAccessException {
		MeItem itemPDU = (MeItem) port.getItem();
		node.setReadOnly(true);

		ItemObject itemObject = node.getItemObject();

		node.setItemObject(itemObject);
		node.setAmpsMax(itemPDU.getRatingAmps());
		node.setAmpsRated(GlobalUtils.formatNumberTo0Dec(itemPDU
				.getRatingAmps() * deRatedFactor));
		node.setPolesLksDesc(itemPDU.getPhasePoleDesc());
		node.setColorLkuDesc(itemPDU.getPhaseColor());

		// set power usage
		Double totalWatts = 0.0;
		if (this.powerCircuitHome.isThreePhase(itemPDU.getPhaseLookup().getLksId())) {
			totalWatts = itemPDU.getLineVolts() * node.getAmpsRated()
					* Math.sqrt(3);
		} else {
			totalWatts = itemPDU.getLineVolts() * node.getAmpsRated();
		}

		long usedWatts = node.getUsedWatts();
		node.setUsedWatts(usedWatts);
		node.setFreeWatts(totalWatts.longValue() - usedWatts);
		node.setTotalWatts(totalWatts.longValue());
	}

	private void formatUpsBankNode(PowerPortNodeDTO node, PowerPort port)
			throws DataAccessException {
		ItemObject itemObject = node.getItemObject();

		UPSBank upsBank = (UPSBank) itemHome.getFactory().getItemObject(
				node.getItemId());
		itemObject.setUnitsInUpsBank(upsBank.getLinkedUPSCount());

		if (node.getPhaseLksValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_WYE) {
			itemObject.setOutputWiring("4-Wire + Ground");
		} else if (node.getPhaseLksValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_DELTA) {
			itemObject.setOutputWiring("3-Wire + Ground");
		}

		// set power usage
		String redun = itemObject.getRedundancy();

		int redunQty = 0;

		if (redun == null)
			redun = "N";

		if (redun.indexOf("+") > 0) {
			redunQty = Integer.parseInt(redun.substring(2));
		}

		long ratingKw = (long) (itemObject.getRatingKW()
				* itemObject.getUnitsInUpsBank() - itemObject.getRatingKW()
				* redunQty);
		long ratingKva = (long) (itemObject.getRatingKva()
				* itemObject.getUnitsInUpsBank() - itemObject.getRatingKva()
				* redunQty);

		itemObject.setRatingKva(ratingKva);
		itemObject.setRatingKW(ratingKw);

		node.setTotalWatts(ratingKw * 1000);
	}

	private boolean isProposedCircuitWithPendingRequest(CircuitDTO circuit,
			CircuitCriteriaDTO cCriteria) {

		boolean isProposedCircuitWithPendingRequest = false;
		final CircuitUID circuitUID = circuit.getCircuitUID();

		final boolean isProposedCircuit = cCriteria.getProposeCircuitId() != null
				&& cCriteria.getProposeCircuitId() > 0
				&& cCriteria.getProposeCircuitId().equals(
						circuit.getCircuitId());

		// This is a proposed circuit AND there is a pending request
		if (isProposedCircuit) {
			CircuitRequestInfo req = circuitHome.getRequestInfoForCircuit(
					circuitUID.getCircuitDatabaseId(),
					cCriteria.getCircuitType(), true);
			if (req != null) {
				long stage = req.getRequestStageCode() != null ? req
						.getRequestStageCode() : 0;
				if (stage >= SystemLookup.RequestStage.REQUEST_APPROVED
						&& stage < SystemLookup.RequestStage.REQUEST_COMPLETE) {
					isProposedCircuitWithPendingRequest = true;
				}
			}
		}
		return isProposedCircuitWithPendingRequest;
	}

	private void setCircuitReadOnly(CircuitDTO circuit) {
		circuit.setReadOnly(true);
		for (CircuitNodeInterface node : circuit.getNodeList()) {
			node.setReadOnly(true);
		}
	}

	private void setPartialCircuitInUseEditability(CircuitDTO circuit) {

		boolean foundEditablePortNode = false;
		boolean foundEditableWireNode = false;
		for (CircuitNodeInterface node : circuit.getNodeList()) {
			if (!foundEditablePortNode && node instanceof PowerPortNodeDTO) {
				// let circuit be writable and allow editing only the second
				// node
				// (the one that comes after Input Cord)
				PowerPortNodeDTO powerNode = (PowerPortNodeDTO) node;
				if (powerNode.isRackPduOutPut() || powerNode.isOutLet()) {
					foundEditablePortNode = true;
					continue;
				}
			} else if (node instanceof WireNodeInterface) {
				WireNodeInterface wireNode = (WireNodeInterface) node;
				if ((wireNode.isInputCord() && !foundEditableWireNode)) {
					// for power circuit, only first inputcord wire node is
					// writable
					foundEditableWireNode = true;
					node.setReadOnly(false);
					continue;
				} else if ((wireNode.isPatchCord() && !wireNode
						.isSharedConnection())) {
					// for data cricuit wire nodes that are part of the circuit
					// are
					// editable. Shared circuit's wire nodes are not allowed to
					// edit.
					node.setReadOnly(false);
					continue;
				}
			}
			node.setReadOnly(true);
		}
	}

	/**
	 * Business logic for determining what part of a circuit is editable.
	 */
	private void setCircuitEditability(CircuitDTO circuit,	CircuitCriteriaDTO cCriteria) {
		UserInfo userInfo = circuit.getUserInfo();

		if (userInfo == null || userInfo.isViewer()) {
			circuit.setReadOnly(true);
			circuit.setCircuitState(0);
			return;
		}

		final Long lastPortNodeId = cCriteria.getLastNodePortId();
		final Long proposedCircuitId = circuit.getProposeCircuitId();
		final Long startPortId = cCriteria.getStartPortId();
		final boolean isInstalled = circuit.isStatusInstalled();
		final boolean hasProposedCircuit = (isInstalled && (proposedCircuitId != null && proposedCircuitId > 0));

		boolean firstSharedConnection = true;
		boolean isFarEndNode = false;
		boolean remainingNodesAreShared = false;

		// These variables are for calculating shared partial circuit length
		PortNodeInterface startPartialCircuitNode = null;
		int partialCircuitLength = 0;
		int portNodeIdx = 0;

		// If circuit is a partial circuit in use, then the entire partial
		// circuit is read only.
		if (circuit.isPartialCircuitInUse()) {
			setPartialCircuitInUseEditability(circuit);
			return;
		}

		if (circuit.isDataCircuit()) {
			Long portId = ((DataPortNodeDTO) circuit.getNodeList().get(0))
					.getPortId();

			if (this.dataCircuitHome.getFanoutCircuitIdForStartPort(portId) != null) {
				setPartialCircuitInUseEditability(circuit);
				return;
			}
		}

		boolean isProposedCircuitWithPendingRequests = isProposedCircuitWithPendingRequest(
				circuit, cCriteria);

		for (CircuitNodeInterface node : circuit.getNodeList()) {
			if (node.getReadOnly())
				continue; // if node is already read only, skip it
			if (startPartialCircuitNode != null)
				partialCircuitLength++; // Track partial circuit length

			// check if this is a VPC power outlet, if yes, make it editable.
			// if (node instanceof PortNodeInterface) {
			// PortNodeInterface portNode = (PortNodeInterface) node;
			// if (portNode.isVpcPowerOutlet()) {
			// node.setReadOnly( false );
			// continue;
			// }
			// }

			if (remainingNodesAreShared) {
				node.setReadOnly(true);
				continue;
			}

			// All nodes are read only in the case of tracing uplinks
			if (lastPortNodeId != null && lastPortNodeId > 0) {
				node.setReadOnly(true);
				continue;
			}

			// Node is read only if this is the far end of the circuit
			if (isFarEndNode) {
				node.setReadOnly(true);
				isFarEndNode = false;
				continue;
			}

			// All nodes are read only for installed circuits that have a
			// proposed circuit
			if (hasProposedCircuit) {
				node.setReadOnly(true);
				continue;
			}

			if (isProposedCircuitWithPendingRequests) {
				node.setReadOnly(true);
				continue;
			}

			if (node instanceof PortNodeInterface) {
				PortNodeInterface portNode = (PortNodeInterface) node;
				portNodeIdx++;

				if (portNode.isSharedConnection()
						|| (startPortId != null && startPortId > 0)) {
					// First node of partial circuit in use is editable, but not
					// the rest
					if (firstSharedConnection) {
						startPartialCircuitNode = portNode;
						partialCircuitLength = 1;
					} else {
						portNode.setReadOnly(true);
					}

					firstSharedConnection = false;
				}

				if (node instanceof PowerPortNodeDTO) {
					PowerPortNodeDTO powerNode = (PowerPortNodeDTO) node;

					if (portNodeIdx == 2) {
						remainingNodesAreShared = true;
					}

					if (portNode.isSharedConnection()) {
						portNode.setReadOnly(true);
						remainingNodesAreShared = true;
					}

					if (powerNode.isOutLet()) {
						remainingNodesAreShared = true;
					}
				}
			} else if (node instanceof WireNodeInterface) {
				WireNodeInterface wireNode = (WireNodeInterface) node;

				// If this is a structured cable, next end is the far end node
				if (wireNode instanceof StructureCableDTO) {
					isFarEndNode = true;
				}

				// We are traversing nodes of shared partial circuit
				if (remainingNodesAreShared) {
					node.setReadOnly(true);
					continue;
				}

				// Wire nodes are always editable unless they are part of
				// another partial circuit
				if (wireNode.isSharedConnection()) {
					wireNode.setReadOnly(true);
					remainingNodesAreShared = true;
					continue;
				}

				// This is a request to view a partial circuit - the structured
				// cable should be editable
				if ((startPortId != null) && (startPortId > 0) && !isFarEndNode) {
					wireNode.setReadOnly(true);
					remainingNodesAreShared = true;
					continue;
				}
			}
		}

		if (startPartialCircuitNode != null) {
			startPartialCircuitNode
					.setPartialCircuitLength(partialCircuitLength);
		}
	}

	/**
	 * Business logic for determining the used watts at each nodes for power
	 * circuit.
	 */
	private void setCircuitPowerUsage(CircuitDTO circuit,
			CircuitCriteriaDTO cCriteria) {
		if (circuit.isDataCircuit())
			return;

		final CircuitUID circuitUID = circuit.getCircuitUID();
		long rackFreeWatts = 0;
		long rackUsedWatts = 0;
		long fuseFreeWatts = 0;
		long fuseUsedWatts = 0;
		long usedWatts = 0;
		long loadWatts = 0;
		long netWatts = 0;
		final boolean isProposedCircuit = cCriteria.getProposeCircuitId() != null
				&& cCriteria.getProposeCircuitId() > 0
				&& cCriteria.getProposeCircuitId().equals(
						circuitUID.getCircuitDatabaseId());

		if (cCriteria.isDataCircuit() || cCriteria.isSkipPowerCalc())
			return;

		for (int i = circuit.getNodeList().size() - 1; i >= 0; i--) {
			if (!(circuit.getNodeList().get(i) instanceof PowerPortNodeDTO)) {
				continue;
			}

			PowerPortNodeDTO rec = (PowerPortNodeDTO) circuit.getNodeList()
					.get(i);

			if (rec.isRackPduOutPut()) {
				if (rec.getFuseLkuId() != null) {
					long totalWatts = rec.getBreakerTotalWatts();

					fuseUsedWatts = powerCircuitHome.getPowerPortUsedWatts(
							rec.getInputCordPortId(), rec.getFuseLkuId());
					fuseFreeWatts = totalWatts - fuseUsedWatts;

					rec.setBreakerUsedWatts(fuseUsedWatts);

					if (appLogger.isDebugEnabled()) {
						appLogger.debug(rec.getPortName() + " fuseUsedWatts = "
								+ fuseUsedWatts);
					}
				} else {
					fuseFreeWatts = rackFreeWatts;
				}

				if (fuseFreeWatts < rackFreeWatts) {
					rec.setFreeWatts(fuseFreeWatts);
				} else {
					rec.setFreeWatts(rackFreeWatts);
				}

				usedWatts = powerCircuitHome.getPowerPortUsedWatts(rec.getPortId(),
						null);
				rec.setUsedWatts(usedWatts);
			} else if (rec.isPowerSupply()) {
				if (isProposedCircuit) {
					loadWatts = rec.getWattsBudget();
					netWatts = loadWatts;

					// get watts from propose circuit
					HashMap<Long, Long> netWattList = circuitHome
							.getProposeCircuitPortsNetWatts();
					PowerPortNodeDTO outletRec = (PowerPortNodeDTO) circuit
							.getNodeList().get(i + 2);

					if (netWattList.containsKey(rec.getPortId())) {
						netWatts = netWattList.get(rec.getPortId());
					}

					if (outletRec.isRackPduOutPut() || outletRec.isOutLet()) {
						outletRec.setUsedWatts(0); // this value will be set
													// below
						outletRec.setFreeWatts(outletRec.getTotalWatts()
								- loadWatts);

						if (outletRec.getFuseLkuId() != null) {
							outletRec.setBreakerUsedWatts(outletRec
									.getBreakerUsedWatts() + netWatts);
							outletRec.setFreeWatts(outletRec.getTotalWatts()
									- outletRec.getBreakerUsedWatts());

							if (appLogger.isDebugEnabled()) {
								appLogger
										.debug("Breaker Load/Limit/Free Watts = "
												+ outletRec
														.getBreakerUsedWatts()
												+ "/ "
												+ outletRec
														.getBreakerTotalWatts()
												+ "/ "
												+ outletRec.getFreeWatts());
							}
						}
					}
					netWatts = loadWatts;
				} else {
					rec.setUsedWatts(rec.getWattsBudget());
				}
			} else {
				long watts = powerCircuitHome.getPowerPortUsedWatts(rec.getPortId(),
						null);
				rec.setUsedWatts(watts);
				rec.setFreeWatts(rec.getTotalWatts() - watts);

				if (rec.isInputCord()) {
					rackFreeWatts = rec.getFreeWatts();
				}
			}
		}

		if (appLogger.isDebugEnabled()) {
			appLogger.debug("Load/Limit/Free");
		}

		// Max the free watts for a node to the next node. A PDU cannot have
		// more power available than a UPS.
		CircuitNodeInterface lastCirNode = (CircuitNodeInterface) circuit
				.getNodeList().get(circuit.getNodeList().size() - 1);
		if (lastCirNode instanceof PowerPortNodeDTO) {
			PowerPortNodeDTO lastNode = (PowerPortNodeDTO) lastCirNode;
			long freeWatts = lastNode.getFreeWatts();

			for (int i = circuit.getNodeList().size() - 1; i >= 0; i--) {
				if (!(circuit.getNodeList().get(i) instanceof PowerPortNodeDTO)) {
					continue;
				}

				PowerPortNodeDTO rec = (PowerPortNodeDTO) circuit.getNodeList()
						.get(i);

				if (rec.getUsedWatts() == 0) {
					rec.setUsedWatts(loadWatts);
				} else if (rec.isPowerSupply() == false) {
					rec.setUsedWatts(rec.getUsedWatts() + netWatts);
				}

				if (rec.getFuseLkuId() != null
						&& rec.getBreakerUsedWatts() == 0) {
					rec.setBreakerUsedWatts(loadWatts);
					rec.setFreeWatts(rec.getTotalWatts() - loadWatts);
				}

				if (freeWatts < rec.getFreeWatts()) {
					rec.setFreeWatts(freeWatts);
				}

				freeWatts = rec.getFreeWatts();

				if (appLogger.isDebugEnabled()) {
					appLogger.debug(rec.getPortName()
							+ " Load/Limit/Free Watts = " + rec.getUsedWatts()
							+ "/ " + rec.getTotalWatts() + "/ " + freeWatts);
				}
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long getCircuitTotalCount() throws ServiceLayerException {
		return circuitHome.getCircuitTotalCount();
	}

	// Please refer to:
	// http://cfrrwiki.raritan.com/index.php?title=Product_Sites/dcTrack/dcTrack_2.6/Committed_Features/Connectivity_Management_-_Data_%26_Power_Circuits/Build_Connection_-_Common_functions
	// for details on different states.
	public enum CircuitState {
		circuitStateMinus_1(-1), circuitState_0(0), circuitState_1(1), circuitState_2(
				2), circuitState_3(3), circuitState_11(11), circuitState_12(12), circuitState_21(
				21), circuitState_22(22), circuitState_23(23);

		private final int state;

		private CircuitState(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}
	};

	private CircuitState getCircuitState(CircuitDTO circuit)
			throws DataAccessException, BusinessValidationException {
		final CircuitUID circuitUID = circuit.getCircuitUID();
		CircuitState state = CircuitState.circuitStateMinus_1;

		if (circuitUID.isTransient()) {
			return state; // Circuit is transient
		}

		final boolean isProposedCkt = circuitUID.isProposedCircuit();

		// Check if circuit exist first
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		float circuitId;
		if (isProposedCkt) {
			circuitId = this.circuitHome
					.getOriginalCircuitIdForProposed(
							circuitUID.getCircuitDatabaseId(),
							circuit.getCircuitType()).floatValue();
		} else {
			circuitId = circuitUID.floatValue();
		}

		cCriteria.setCircuitId(circuitId);
		cCriteria.setCircuitType(circuit.getCircuitType());

		CircuitViewData circuitView = this.circuitHome.getCircuitViewData(cCriteria);

		if (circuitView == null) { // no need to check state since circuit does
									// not exist
			String errMsg = cCriteria.getNotFoundInListEnumError();
			errMsg = errMsg.replaceAll("<CircuitId>", cCriteria.getCircuitId()
					.toString());

			BusinessValidationException e = new BusinessValidationException(
					new ExceptionContext(errMsg, this.getClass()));
			e.addValidationError(errMsg);
			throw e;
		}
		
		circuitView.setUserInfo(circuit.getUserInfo());
		
		if (this.circuitHome.canOperateOnCircuit(circuitView, isProposedCkt)) {
			// isNewCircuit just checks if the circuit id in the CircuitDTO
			// object exists and greater than zero. If so it returns false.
			// However, in the case when we are editing a circuit, the circuit
			// id will exist and greater than zero. Therefore we need
			// to check for the status code new in this case. If user is saving
			// the circuit for the first time the CircuitDTO does not
			// have a circuit id set and therefore isNewCircuit will return
			// true. Note that in this case the circuit.getStatusCode() will be
			// null
			// and therefore we have an OR instead of AND in the if check!

			HashMap<Long, CircuitRequestInfo> reqInfoList = this.circuitHome
					.getCircuitRequestInfo(circuit.getCircuitType(),
							isProposedCkt);

			CircuitRequestInfo req = reqInfoList.get(circuit.getCircuitUID()
					.getCircuitDatabaseId());

			state = (circuit.isNewCircuit() || (circuitView.getStatusLksCode() < SystemLookup.ItemStatus.INSTALLED)) ? getCircuitStateForNewState(req)
					: getCircuitStateForInstalled(req, isProposedCkt);
		} else {
			if (this.circuitHome.canCreateCircuit()) {
				state = CircuitState.circuitState_1;
			} else {
				state = CircuitState.circuitState_0;
			}
		}

		return state;
	}

	private CircuitState getCircuitStateForNewState(CircuitRequestInfo req)
			throws DataAccessException {
		CircuitState state = CircuitState.circuitStateMinus_1;

		if (req == null) {
			state = CircuitState.circuitState_11;
		} else {
			if ((req.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_APPROVED)
					|| (req.getRequestStageCode() == SystemLookup.RequestStage.WORK_ORDER_ISSUED)
					|| (req.getRequestStageCode() == SystemLookup.RequestStage.WORK_ORDER_COMPLETE)) {
				state = CircuitState.circuitState_1;
			} else {
				state = CircuitState.circuitState_12;
			}
		}
		return state;
	}

	private CircuitState getCircuitStateForInstalled(CircuitRequestInfo req,
			boolean isProposedCkt) {
		CircuitState state = CircuitState.circuitStateMinus_1;
		if (req == null) {
			state = CircuitState.circuitState_21;
		} else {
			final String reqType = req.getRequestType();
			if (reqType != null && reqType.equalsIgnoreCase("Disconnect")) {
				return CircuitState.circuitState_1; // CR 44164 - Cannot edit
													// circuit with disconnect
													// request in ANY stage
			}

			if ((req.getRequestStageCode() != SystemLookup.RequestStage.REQUEST_APPROVED)
					&& (req.getRequestStageCode() != SystemLookup.RequestStage.WORK_ORDER_ISSUED)
					&& (req.getRequestStageCode() != SystemLookup.RequestStage.WORK_ORDER_COMPLETE)) {
				state = isProposedCkt ? CircuitState.circuitState_23
						: CircuitState.circuitState_22;
			} else {
				state = isProposedCkt ? CircuitState.circuitState_3
						: CircuitState.circuitState_2;
			}
		}
		return state;
	}

	private void preLoadCircuitPorts(CircuitDTO circuit) {
		// Hibernate keep port object in memory after object is read the first
		// time
		// This function pre-load the ports such that objects are not proxies.

		for (CircuitNodeInterface node : circuit.getNodeList()) {
			if (node instanceof DataPortNodeDTO) {
				Long portId = ((DataPortNodeDTO) node).getPortId();

				try {
					dataPortDAO.loadPort(portId);
				} catch (DataAccessException e) {
					// if port cannot be pre-load, don't do anything
					// code that validate circuit will check for valid port id
					// but print output to log file for debuging purpose
					e.printStackTrace();

				}
			}
			if (node instanceof PowerPortNodeDTO) {
				Long portId = ((PowerPortNodeDTO) node).getPortId();

				try {
					powerPortDAO.loadPort(portId);
				} catch (DataAccessException e) {
					// if port cannot be pre-load, don't do anything
					// code that validate circuit will check for valid port id
					// but print output to log file for debuging purpose
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String getCircuitButtonStatus(List<Double> CircuitIdList)
			throws ServiceLayerException {

		String errMsg = "";

		if (CircuitIdList != null && CircuitIdList.size() > 0
				&& CircuitIdList.get(0).isNaN()) {
			throw new IllegalArgumentException("CircuitIdList is incorrect");
		}

		// Get user info
		UserInfo userInfo = userHome.getCurrentUserInfo();
		if (userInfo != null) {
			String userId = userInfo.getUserId();
			boolean isAdmin = userInfo.isAdmin();
			boolean isManager = userInfo.isManager();
			boolean isMember = userInfo.isMember();
			boolean isViewer = userInfo.isViewer();

			appLogger.debug("[ getItemActionMenuStatus ] userId : " + userId
					+ ", isAdmin : " + isAdmin + ", isManager : " + isManager
					+ ", isMember : " + isMember + ", isViewer : " + isViewer);
		} else {
			appLogger.debug("[ getItemActionMenuStatus ] userInfo is null ");
			errMsg = "UserInfo is null.";
		}

		String rtnString = "";

		// Set it true in the beginning. If there is any condition is mismatch,
		// set it false.
		boolean enableDelete = true;
		boolean enableRequest = true;
		boolean enableDisconnect = true;

		if (userInfo.isViewer() == false) {

			// Loop for circuits
			for (Object obj : CircuitIdList) {
				if (obj != null) {
					String circuitId = obj.toString().trim();
					if (!"".equals(circuitId)) {

						// Create criteria
						CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
						float id = Float.parseFloat(circuitId);
						cCriteria.setCircuitId(id);
						CircuitUID circuitUID = cCriteria.getCircuitUID();

						if (circuitUID.isDataCircuit()) {
							cCriteria
									.setCircuitType(SystemLookup.PortClass.DATA);
						} else {
							cCriteria
									.setCircuitType(SystemLookup.PortClass.POWER);
							cCriteria.setSkipPowerCalc(true);
						}

						List<CircuitDTO> list = viewCircuitByCriteria(cCriteria);

						for (CircuitDTO circuitDTO : list) {
							int state = circuitDTO.getCircuitState();

							// There should be only one dto in the list.

							// Is readonly?
							/*if (circuitDTO.isReadOnly()) {
								enableDelete = false;
							}
							else*/ {

								// Is state correct?
								if (state == CircuitState.circuitState_11.state
										|| state == CircuitState.circuitState_12.state) {
									// can be delete
								} else {
									enableDelete = false;
								}

							}

							// Is partialCircuitInUse?
							if (circuitDTO.isPartialCircuitInUse()) {
								enableDelete = false;
							}

							// Is state correct?
							if (state == CircuitState.circuitStateMinus_1.state) {
								if (circuitDTO.getNodeList().size() >= 3) {
								} else {
									enableRequest = false;
								}
							} else if (state == CircuitState.circuitState_11.state
									|| state == CircuitState.circuitState_12.state) {
							} else {
								enableRequest = false;
							}

							if (circuitDTO.getNodeList().size() < 3) {
								enableRequest = false;
							}

							// Report state for debug purpose
							rtnString += "\"state\":" + state + ",";

							CircuitRequestInfo requestInfo = this.circuitHome
									.getRequestInfoForCircuit(
											circuitUID.getCircuitDatabaseId(),
											circuitDTO.getCircuitType(), false);

							boolean localDelete = false;
							boolean localRequest = false;
							boolean localDisconnect = false;

							if (circuitDTO.getStatusCode() == SystemLookup.ItemStatus.PLANNED) {
								// Status is New. now we have to check for
								// request if a request is open before making
								// this deletable
								if (requestInfo.getRequestNo() != null) {
									if ((requestInfo.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_ISSUED)
											|| (requestInfo
													.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_UPDATED)
											|| (requestInfo
													.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_REJECTED)) {
										// For the above request states, we can
										// disconnect? No, we cannot because it
										// is a planned circuit with connect
										// request
										localDelete = true;
										localRequest = true;
									}
								} else {
									localDelete = true;
									localRequest = true;
								}
							} else if (circuitDTO.getStatusCode() == SystemLookup.ItemStatus.INSTALLED
									|| circuitDTO.getStatusCode() == SystemLookup.ItemStatus.POWERED_OFF) {
								// If circuit status is installed/powered-off
								// please check for request number and stage now
								if (requestInfo.getRequestNo() != null) {
									// if the request is not yet approved and it
									// is a disconnect request then disconnect
									// shall be enabled
									if (requestInfo.getRequestNo() != null) {
										if (((requestInfo.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_ISSUED)
												|| (requestInfo
														.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_UPDATED) || (requestInfo
												.getRequestStageCode() == SystemLookup.RequestStage.REQUEST_REJECTED))
												&& requestInfo.getRequestType()
														.equals("Disconnect")) {
											localDisconnect = true;
										}
									}
								} else {
									// installed/powered-off circuit and no
									// request number. So we can disconnect
									localDisconnect = true;
								}
							}

							if (localDelete == false)
								enableDelete = false;
							if (localRequest == false)
								enableRequest = false;
							if (localDisconnect == false)
								enableDisconnect = false;

							// Report other info for debug purpose
							rtnString += "\"requestNo\":"
									+ requestInfo.getRequestNo() + ",";
							rtnString += "\"stateCode\":"
									+ circuitDTO.getStatusCode() + ",";
							rtnString += "\"requestStage\":"
									+ requestInfo.getRequestStageCode() + ",";
						}
					}
				}
			}
		} else {
			enableDelete = false;
			enableDisconnect = false;
			enableRequest = false;
		}

		if (enableDelete && enableDisconnect) {
			enableDelete = false;
			enableDisconnect = false;
			enableRequest = false;
		}

		rtnString += "\"delete\":" + enableDelete + ",";
		rtnString += "\"disconnect\":" + enableDisconnect + ",";
		rtnString += "\"request\":" + enableRequest + ",";

		// ByPass
		rtnString += "\"req_bypass_menu\":" + isByPassMenuEnabled(userInfo)
				+ ",";
		rtnString += "\"req_bypass_checked\":"
				+ getRequestByPassFromDB(userInfo) + ",";

		return "{" + rtnString + "}";

	}

	private boolean getRequestByPassFromDB(UserInfo userInfo) {
		return userDAO.getUserRequestByPassSetting(new Long(userInfo
				.getUserId()));
	}

	private boolean isByPassMenuEnabled(UserInfo userInfo) {
		boolean bpMenuEnabled = false;
		Long userId = new Long(userInfo.getUserId());

		// is user gate keeper
		BigInteger accessLevelLkpValueCode = userDAO
				.getUserAccessLevelLkpValueCode(userId);
		boolean gk = (accessLevelLkpValueCode != null && accessLevelLkpValueCode
				.longValue() == SystemLookup.AccessLevel.GATEKEEPER);

		// is lock Request bypass for gate keeper set
		String lock = userDAO.getLockRequestByPassButtonSetting();
		boolean rbLocked = ((lock == null)
				|| (lock != null && lock
						.equals(UserDAO.GKCanToggleRequestBypass)) ? false
				: true);

		// user is gate keeper and request bypass is not locked for gate keeper.
		if (gk && !rbLocked) {
			bpMenuEnabled = true;
		}

		return bpMenuEnabled;
	}

	private List<Request> getRequestFromRequestDTO(List<RequestDTO> reqDtoLst) {
		List<Request> requests = new ArrayList<Request>();

		for (RequestDTO reqDto : reqDtoLst) {
			if (reqDto != null) {
				Long id = reqDto.getRequestId();
				if (id != null && id.longValue() > 0) {
					Request r = requestDao.getRequest(id);
					requests.add(r);
				}
			}
		}
		return requests;
	}


	private void processRequestsWorkFlow(UserInfo userInfo, List<Request> requests, BusinessValidationException bvex) throws ServiceLayerException {
		boolean requestBypassSetting = requestHelper
				.getRequestBypassSetting(userInfo);

		if (requestBypassSetting) {

			requestHome.processRequests(userInfo, requests, bvex);
		} else {

			if (null != bvex)
				throw bvex;
		}

	}

	private Map<String, Object> refreshCircuit(Map<String, Object> circuitMap,
			CircuitDTO circuit, float circuitUid) throws ServiceLayerException {

		CircuitCriteriaDTO circuitCriteria = new CircuitCriteriaDTO();
		circuitCriteria.setCircuitId(circuitUid /* circuit.getCircuitId() */);
		circuitCriteria.setCircuitType(circuit.getCircuitType());

		List<CircuitDTO> circuitDTOs = viewCircuitByCriteria(circuitCriteria);

		if (circuitDTOs != null && circuitDTOs.size() > 0) {
			circuitMap.put(CircuitDTO.class.getName(), circuitDTOs.get(0));

			// return circuitDTOs.get(0);
			return circuitMap;
		} else {
			circuitMap.put(CircuitDTO.class.getName(), circuit);
			// return circuit;
			return circuitMap;
		}
	}

	/**
	 * validate users permission to operate on the circuit
	 * @param circuitView
	 * @param userInfo
	 * @throws BusinessValidationException
	 */
	private void validateUserPermission(CircuitViewData circuitView, UserInfo userInfo) throws BusinessValidationException {
		Errors errors = businessExceptionHelper.getErrorObject(this.getClass());
		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		targetMap.put("circuitView", circuitView);
		targetMap.put("userInfo", userInfo);
		circuitPermissionValidator.validate(targetMap, errors);
				
		if (errors.hasErrors()) {
			businessExceptionHelper.throwBusinessValidationException(null, errors, null);
		}
	}

}
