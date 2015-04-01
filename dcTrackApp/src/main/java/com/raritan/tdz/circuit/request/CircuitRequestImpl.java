package com.raritan.tdz.circuit.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitDelete;
import com.raritan.tdz.circuit.home.CircuitRequestInfo;
import com.raritan.tdz.circuit.home.CircuitSearch;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.domain.WorkOrdersCompleted;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequest.ItemRequestType;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.move.home.ItemMoveHelper;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.dao.RequestHistoryDAO;
import com.raritan.tdz.request.home.RequestInfo;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class CircuitRequestImpl implements CircuitRequest {
	private SessionFactory sessionFactory;
	
	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired
	protected ItemDAO itemDAO;

	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired(required=true)
	private LksCache lksCache;

	@Autowired(required=true)
	DataCircuitDAO dataCircuitDao;

	@Autowired(required=true)
	DataConnDAO dataConnectionDAO;
	
	@Autowired(required=true)
	PowerCircuitDAO powerCircuitDao;

    @Autowired(required=true)
    PowerConnDAO powerConnectionDAO;
 	
	@Autowired
	protected CircuitDelete circuitDelete;
	
	@Autowired(required=true)
	private DataPortDAO dataPortDAO;

	@Autowired(required=true)
	private CircuitSearch circuitSearch;
	
	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	@Autowired
	private CircuitDAO<PowerCircuit> powerCircuitDAOExt;


	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private UserHome userHome;

	CircuitRequestImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Request> getRequestForCircuit(CircuitViewData circuitView) {
		String tableName = getTableNameForCircuit(circuitView.getCircuitType());
				
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestPointers", "pointer");
    	criteria.add(Restrictions.eq("history.current", true));  
		criteria.add(Restrictions.eq("pointer.recordId", circuitView.getCircuitId()));		
		criteria.add(Restrictions.eq("pointer.tableName", tableName));			
		
    	return criteria.list();
	}
			
	@Override
	public long connect(CircuitViewData circuitView) {

		//String requestNo = requestDAO.getNextRequestNo();
		String reqType = getConnectRequestType(circuitView.getStartPortId(), circuitView.getCircuitType());
		String reqDesc =  reqType.equals(ItemRequest.ItemRequestType.connect) ? "New Connect Port " : "Reconnect Port ";
		
		Long itemId = dataPortMoveDAO.getMovingItemId(circuitView.getStartItemId());
		
		if(itemId == null || itemId <= 0){
			itemId = circuitView.getStartItemId();
		}
		
		//Create request 
		Request req = new Request();
		req.setDescription(reqDesc + circuitView.getStartPortName() + " on " + circuitView.getStartItemName());
		req.setItemId(itemId);
		req.setLocationId( circuitView.getLocationId() );
		//req.setRequestNo(requestNo);
		req.setRequestType(reqType);
		req.setRequestTypeLookup(lksCache.getLksDataUsingLkpCode(req.getRequestTypeLookupCode()));
		req.setArchived(false);
		
		RequestPointer pointer = new RequestPointer();
		pointer.setRecordId(circuitView.getCircuitId());   
		pointer.setTableName(getTableNameForCircuit(circuitView.getCircuitType()));
		pointer.setRequestDetail(req);
		pointer.setSortOrder(1);
		
		Long requestId = requestDAO.create(req);
		requestDAO.getSession().refresh(req);
		UserInfo user = circuitView.getUserInfo();
		
		// get the associated request for the parent and create the request history
		String errorMsg = getParentRequestMessage(circuitView);
        RequestHistory hist = requestDAO.createReqHist(req, SystemLookup.RequestStage.REQUEST_ISSUED, user, errorMsg);

		req.addRequestPointer(pointer);
		requestDAO.update(req);
		requestDAO.getSession().refresh(req);


		//need to do this last such that trigger execute correctly
        // req.addRequestHistory(hist);
        // requestDAO.update(req);
        
        // requestHistoryDAO.update(hist);

        // requestDAO.getSession().flush();
        
       	updateItemMoveTables(circuitView.getStartPortId(), circuitView.getCircuitType(), req);
       	updateItemMoveTables(circuitView.getEndPortId(), circuitView.getCircuitType(), req);
       	
       	circuitView.setRequestNumber(req.getRequestNo());
       	circuitView.setRequestStage(hist.getStageIdLookup().getLkpValue());       	
       	sessionFactory.getCurrentSession().update(circuitView);
       	
       	sessionFactory.getCurrentSession().flush();
       	
		return requestId;
	}

	private String getParentRequestMessage(CircuitViewData circuitView) {
		List<Long> connIds = circuitView.getConnList();
		
		List<RequestInfo> requestInfos = null;
		if (circuitView.isDataCircuit()) {
			requestInfos = dataCircuitDAOExt.getParentMoveRequest(connIds);
		}
		else if (circuitView.isPowerCircuit()) {
			requestInfos = powerCircuitDAOExt.getParentMoveRequest(connIds);
		}

		if (null == requestInfos || requestInfos.size() == 0) return null;
		
		StringBuffer errorMsg = new StringBuffer();
		
		errorMsg.append("The following requests exist for associated items:\n");
		
		for (RequestInfo reqInfo: requestInfos) {
			
			errorMsg.append(reqInfo.getItemName()).
						append(" Request: ").
						append(reqInfo.getRequestNumber()).
						append(" ").
						append(reqInfo.getRequestType())
						.append("\n");
		}
		
		return errorMsg.toString();

	}
	
	@Override
	public long disconnect(CircuitViewData circuitView, String linkRequestNo) {
		String requestNo = linkRequestNo;
		/*
		if(requestNo == null){
			requestNo = requestDAO.getNextRequestNo();
		}
		*/
		//Create request 
		if (null != circuitView.getRequestNumber() && circuitView.getRequestNumber().length() > 0) {
			Request req = requestDAO.getRequest(circuitView.getRequestNumber(), false);
			if (null != req) {
				req.setRequestNo(requestNo);
				requestDAO.update(req);
				circuitView.setRequestNumber(requestNo);
		       	sessionFactory.getCurrentSession().update(circuitView);
				return req.getRequestId();
			}
		}
		
		Request req = new Request();
		req.setDescription("Disconnect Port " + circuitView.getStartPortName() + " on " + circuitView.getStartItemName());
		req.setItemId(circuitView.getStartItemId());
		req.setLocationId( circuitView.getLocationId() );
		req.setRequestNo(requestNo);
		req.setRequestType(ItemRequest.ItemRequestType.disconnect);
		req.setRequestTypeLookup(lksCache.getLksDataUsingLkpCode(req.getRequestTypeLookupCode()));
		req.setArchived(false);
		
		RequestPointer pointer = new RequestPointer();
		pointer.setRecordId(circuitView.getCircuitId());   
		pointer.setTableName(getTableNameForCircuit(circuitView.getCircuitType()));
		pointer.setRequestDetail(req);
		pointer.setSortOrder(1);
		
		Long requestId = requestDAO.create(req);
		requestDAO.getSession().refresh(req);
		UserInfo user = circuitView.getUserInfo();
		// get the associated request for the parent and create the request history
		String errorMsg = getParentRequestMessage(circuitView);
        RequestHistory hist = requestDAO.createReqHist(req, SystemLookup.RequestStage.REQUEST_ISSUED, user, errorMsg);

		req.addRequestPointer(pointer);
		requestDAO.update(req);
		requestDAO.getSession().refresh(req);
		
       	circuitView.setRequestNumber(req.getRequestNo());
       	circuitView.setRequestStage(hist.getStageIdLookup().getLkpValue());       	
       	sessionFactory.getCurrentSession().update(circuitView);
		
		return requestId;		
	}

	@Override
	public void delete(Long requestId, boolean doAssociatedRequests) {
		requestDAO.delete(requestId);

	}

	@Override
	public void updateCircuitStatus(CircuitViewData circuitView, Long statusValueCode) throws Throwable {
		if(circuitView.isDataCircuit()){
			setDataCircuitStatus (circuitView.getCircuitId(), statusValueCode);
		}
		else{
			setPowerCircuitStatus (circuitView.getCircuitId(), statusValueCode);
		}
	}

	@Override
	public void updateCircuitStatus(Long circuitId, String tableName, Long statusValueCode) throws Throwable {		
		if (tableName.equals("dct_circuits_data")) {
			setDataCircuitStatus (circuitId, statusValueCode);
			
		} 
		else if (tableName.equals("dct_circuits_power")) {
			setPowerCircuitStatus (circuitId, statusValueCode);
		}
	}
	
	private String getTableNameForCircuit(Long circuitType){

		if(circuitType.equals(SystemLookup.PortClass.DATA)){
			return "dct_circuits_data";			
		}
		else{
			return "dct_circuits_power";			
		}		
	}

	private String getConnectRequestType(Long portId, Long portClass){
		String reqType = ItemRequest.ItemRequestType.connect;
		
		if (portId != null) {
	    	if(portClass == SystemLookup.PortClass.POWER){
	    		PowerPortMove rec = powerPortMoveDAO.getPortMoveData(null, null, portId);
	    		
	    		if(rec != null && rec.getRequest() != null){
	    			reqType = ItemRequest.ItemRequestType.reconnect;
	    		}
	    	} else if (portClass == SystemLookup.PortClass.DATA){
				DataPortMove rec = dataPortMoveDAO.getPortMoveData(null, null, portId);
	    		
	    		if(rec != null && rec.getRequest() != null){
	    			reqType = ItemRequest.ItemRequestType.reconnect;	    		}
	    	}
		}	
		
		return reqType;
	}
	
	private void updateItemMoveTables(Long portId, Long portClass, Request req){
		if (portId != null && req != null) {
	    	if(portClass == SystemLookup.PortClass.POWER){
	    		powerPortMoveDAO.setPortRequest(portId, req);
	    	} else if (portClass == SystemLookup.PortClass.DATA){
	    		dataPortMoveDAO.setPortRequest(portId, req);
	    	}
		}		
	}
	
	private void setDataCircuitStatus (Long circuitId, long connectionStatusValueCoode) {
		Session session = this.sessionFactory.getCurrentSession();
		Long lksId = SystemLookup.getLksDataId(session, connectionStatusValueCoode);
		
    	Query query =  session.getNamedQuery("updateDataCircuitStatus");
    	query.setLong("statusLksId", lksId);
    	query.setLong("circuitId", circuitId);
		query.executeUpdate();
	}
	
	private void setPowerCircuitStatus (Long circuitId, long connectionStatusValueCoode) {
		Session session = this.sessionFactory.getCurrentSession();
		Long lksId = SystemLookup.getLksDataId(session, connectionStatusValueCoode);
		
    	Query query =  session.getNamedQuery("updatePowerCircuitStatus");
    	query.setLong("statusLksId", lksId);
    	query.setLong("circuitId", circuitId);
		query.executeUpdate();
	}

	@Override
	public void archiveWorkOrder(Request request, CircuitViewData circuitView, UserInfo userInfo) throws Throwable{
		if(circuitView.isDataCircuit()){
			archiveDataWorkOrder(request, circuitView, userInfo);
		}
		else{
			archivePowerWorkOrder(request, circuitView, userInfo);
		}
	}
	
	private void archiveDataWorkOrder(Request request, CircuitViewData circuitView, UserInfo userInfo) throws Throwable{
		WorkOrder wo = request.getWorkOrder();
		UserInfo user = userInfo; 
		Session session = this.sessionFactory.getCurrentSession();
		
		DataCircuit dc = dataCircuitDao.getDataCircuit(circuitView.getCircuitId());
		Integer circuitId = Long.valueOf(circuitView.getCircuitId()).intValue();
		
		for(DataConnection conn:dc.getCircuitConnections()){	
			WorkOrdersCompleted woc = createWorkOrdersCompleted(conn);
			
			if(woc == null) continue;
			
			woc.setWorkOrderNo(wo.getWorkOrderNumber());
			woc.setCircuitId(circuitId);
			woc.setComment(request.getComment());
			woc.setEnteredBy(user.getUserName());
			woc.setRequestType(request.getRequestType());
			woc.setTableName("dct_circuits_data");
			woc.setType("data");
			
			session.save(woc);
		}
		session.flush();
	}
	
	private void archivePowerWorkOrder(Request request, CircuitViewData circuitView, UserInfo userInfo) throws Throwable{
		WorkOrder wo = request.getWorkOrder();
		UserInfo user = userInfo;
		Session session = this.sessionFactory.getCurrentSession();
		
		PowerCircuit pc = powerCircuitDao.getPowerCircuit(circuitView.getCircuitId());
		Integer circuitId = Long.valueOf(circuitView.getCircuitId()).intValue();
		
		for(PowerConnection conn:pc.getCircuitConnections()){	
			WorkOrdersCompleted woc = createWorkOrdersCompleted(conn);
			
			if(woc == null) continue;
			
			woc.setWorkOrderNo(wo.getWorkOrderNumber());
			woc.setCircuitId(circuitId);
			woc.setComment(request.getComment());
			woc.setEnteredBy(user.getUserName());
			woc.setRequestType(request.getRequestType());
			woc.setTableName("dct_circuits_power");
			woc.setType("power");
			
			session.save(woc);
		}
		session.flush();
	}
	
	private WorkOrdersCompleted createWorkOrdersCompleted(ICircuitConnection conn) throws Throwable{
		if(conn == null || conn.getSourcePort() == null) return null;
		WorkOrdersCompleted woc = new WorkOrdersCompleted();
		ConnectionCord cord = conn.getConnectionCord();
		woc.setWorkOrdersCompletedId(Long.valueOf(conn.getConnectionId()).intValue());
		woc.setConnectionLabel(cord != null ? cord.getCordLabel() : null);
		woc.setConnectionLength(cord != null ? cord.getCordLength() : 0);
		woc.setConnectionType(conn.getConnectionType().getLksId().intValue());
		woc.setEndPoint(Long.valueOf(conn.getSourcePortId()).intValue());
		woc.setEndPoint2(Long.valueOf(conn.getDestPortId()).intValue());
		woc.setSortOrder(conn.getSortOrder());
		woc.setStatusId(Long.valueOf(conn.getStatusLookup().getLksId()).intValue());
		
		return woc;
	}
		
	@SuppressWarnings("unchecked")
	public List<Long> getDataCircuitIdsForWorkOrder(Long workOrderId){
		Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery("getDataCircuitIdsForWorkOrder");
    	query.setLong("workOrderId", workOrderId);
    	
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> getPowerCircuitIdsForWorkOrder(Long workOrderId){
		Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery("getPowerCircuitIdsForWorkOrder");
    	query.setLong("workOrderId", workOrderId);
    	
		return query.list();
	}	
	
	@Override
	public void checkForPendingRequest(List<CircuitViewData> cirList) throws BusinessValidationException {
		CircuitRequestInfo requestInfo = null;
		String errMsg = "";
		HashMap<Long, CircuitRequestInfo> reqInfoMap = getCircuitRequestInfo(SystemLookup.PortClass.DATA, false);
		HashMap<Long, CircuitRequestInfo> reqInfoMap2 = getCircuitRequestInfo(SystemLookup.PortClass.POWER, false);		
		reqInfoMap.putAll(reqInfoMap2);
		
		for(CircuitViewData circuitView:cirList){
			Long circuitId = circuitView.getCircuitId();
			requestInfo = null;
			
			//Check to see if there are pending request
			if(reqInfoMap.get(circuitId) != null){
				requestInfo = reqInfoMap.get(circuitId);
			}

			if(requestInfo != null && requestInfo.isRequestPending()){
				if(circuitView.isDataCircuit()){
					errMsg =  ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST_LIST.value();
				}else{
					errMsg =  ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST_LIST.value();
				}
				
				errMsg = errMsg.replaceAll("<ItemName>", circuitView.getStartItemName());
				errMsg = errMsg.replaceAll("<PortName>", circuitView.getStartPortName());

				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(errMsg, this.getClass()));
				
				if(circuitView.isDataCircuit()){
					e.setErrorCode(ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST_LIST.errCode());
				}
				else{
					e.setErrorCode(ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST_LIST.errCode());
				}
				
				e.getRecordIds().add(circuitId);
				e.addValidationError(errMsg);
				
				throw e;
			}
		}
	}
	
	//@Override
	public HashMap<Long, CircuitRequestInfo> getCircuitRequestInfo(Long circuitType, boolean useProposeCircuitId){
		HashMap<Long, CircuitRequestInfo> reqInfo = new HashMap<Long, CircuitRequestInfo>();

    	String queryName = "";

    	//this code will change once Change Management is implemented with the new tables structure
    	if(circuitType == SystemLookup.PortClass.DATA){
    		queryName = "getDataCircuitRequestInfoQuery";
    	}
    	else{
    		queryName = "getPowerCircuitRequestInfoQuery";
    	}

    	Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery(queryName);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			CircuitRequestInfo req = new CircuitRequestInfo(row);

			if(useProposeCircuitId){
				if(req.getProposeCircuitId() != null && req.getProposeCircuitId() > 0){
					reqInfo.put(req.getProposeCircuitId(), req);
				}
			}
			else{
				reqInfo.put(req.getCircuitId(), req);
			}
		}

	    return reqInfo;
	}

	@Override
	public void disconnectAllRequest(Long requestId, String requestType) throws DataAccessException, BusinessValidationException {
		Session session = this.sessionFactory.getCurrentSession();
		Request request = (Request)session.get(Request.class, requestId);

   		List<CircuitCriteriaDTO> cirList = itemDAO.getAssociatedCircuitsForItem(request.getItemId());
   			
   		disconnectCircuits(cirList, requestId, requestType, null);  		
	}

	@Override
	public Map<Long, List<Long>> getPlannedCircuits(Long itemId, String requestType) throws DataAccessException {

		List<CircuitViewData> recList = new ArrayList<CircuitViewData>();
		List<CircuitCriteriaDTO> cirList = itemDAO.getAssociatedCircuitsForItem(itemId);
		List<Long> dataCircuitIds = new ArrayList<Long>();
		List<Long> powerCircuitIds = new ArrayList<Long>();
		
		
		//load the circuits
		for(CircuitCriteriaDTO cr:cirList){
			recList.addAll(circuitSearch.searchCircuitsRaw(cr));
		}
		
		//If circuit is installed, and using a partial circuit that is planned, mark partial as install
		matchCircuitStatus(recList);
		
		// check if the chassis of the moving and original item is the same
		Long tempItemId = dataPortMoveDAO.getWhenMovedItemId(itemId);
		boolean chassisChanged = false;
		
		if(tempItemId != null && tempItemId > 0) {
			Item item = itemDAO.getItem(tempItemId);
			item = itemDAO.initializeAndUnproxy(item);
			item.setItemToMoveId(itemId);
			chassisChanged = itemMoveHelper.isMovingBladeChassisChanged(item);
		}

		//collect all planned circuits
		for(CircuitViewData circuitView:recList){					
			if(!allowDisconnectForCircuit(circuitView, requestType, chassisChanged)){
				continue;
			}
			
			if(circuitView.isStatusPlanned() == true) {
				if (circuitView.isDataCircuit() == true ) {
					dataCircuitIds.add(circuitView.getCircuitId());							
				} else if (circuitView.isPowerCircuit() == true) {
					powerCircuitIds.add(circuitView.getCircuitId());
				}
				continue;
			}
		}
		
		Map<Long, List<Long>> plannedCircuits = new HashMap<Long, List<Long>>();
		plannedCircuits.put(SystemLookup.PortClass.DATA, dataCircuitIds);
		plannedCircuits.put(SystemLookup.PortClass.POWER, powerCircuitIds);
		
		return plannedCircuits;
		
	}
	
	@Override
	public Map<Long, List<Long>> getPlannedCircuits(List<Long> itemIdList, String requestType) throws DataAccessException {

		List<CircuitViewData> recList = new ArrayList<CircuitViewData>();
		List<CircuitCriteriaDTO> cirList = itemDAO.getAssociatedCircuitsForItems(itemIdList);
		List<Long> dataCircuitIds = new ArrayList<Long>();
		List<Long> powerCircuitIds = new ArrayList<Long>();
		
		//load the circuits
		for(CircuitCriteriaDTO cr:cirList){
			recList.addAll(circuitSearch.searchCircuitsRaw(cr));
		}
		
		//If circuit is installed, and using a partial circuit that is planned, mark partial as install
		matchCircuitStatus(recList);
		
		//collect all planned circuits
		for(CircuitViewData circuitView:recList){					
			if(!allowDisconnectForCircuit(circuitView, requestType, null)){
				continue;
			}
			
			if(circuitView.isStatusPlanned() == true) {
				if (circuitView.isDataCircuit() == true ) {
					dataCircuitIds.add(circuitView.getCircuitId());							
				} else if (circuitView.isPowerCircuit() == true) {
					powerCircuitIds.add(circuitView.getCircuitId());
				}
				continue;
			}
		}
		
		Map<Long, List<Long>> plannedCircuits = new HashMap<Long, List<Long>>();
		plannedCircuits.put(SystemLookup.PortClass.DATA, dataCircuitIds);
		plannedCircuits.put(SystemLookup.PortClass.POWER, powerCircuitIds);
		
		return plannedCircuits;
		
	}

	
	private boolean allowDisconnectForCircuit(CircuitViewData circuitView, String requestType, Boolean chassisChanged){
		
		if(!requestType.equals(ItemRequestType.moveItem)) return true;
		if(circuitView.isPowerCircuit()) return true;
		
		
		boolean retValue = true;
		Item startItem = itemDAO.loadItem(circuitView.getStartItemId());
		Long endItemId = circuitView.getEndItemId();
		
		if(startItem instanceof ItItem){
			ItItem itStartItem = (ItItem)startItem;
			DataPort startPort = this.dataPortDAO.read(circuitView.getStartPortId());
			DataPort endPort = this.dataPortDAO.read(circuitView.getEndPortId());

			//don't disconnect circuits that end in a logical port
			if(endPort.isLogical()) {
				retValue = false;
			}
			
			//Check to see if itemId is a Chassis/Blade Chassis, and that the start of circuit is a inside the chassis.
			//If that is the case, do not disconnect internal logical/virtual connections
			if(itStartItem.getBladeChassis() != null && endItemId.equals(itStartItem.getBladeChassis().getItemId())){
				if(startPort.isLogical() || startPort.isVirtual()){
					retValue = false;
				}
			}
			
			// if logical circuit and chassis changed: return true.
			if(startPort.isLogical() && chassisChanged != null && chassisChanged == true) {
				retValue = true;
			}
			
		}
		
		return retValue;
	}	
	
	private void matchCircuitStatus(List<CircuitViewData> dataCirList){
		//dataCirList is a list circuits associated with a particular item.
		for(CircuitViewData cir:dataCirList){
			if(cir.getConnList().size() < 3) continue; //a circuit using a shared circuit will have more than 2 nodes
			
			for(CircuitViewData cir2:dataCirList){								
				if(!(cir.getCircuitType().equals(cir2.getCircuitType()))) continue;

				if(cir.getCircuitId() == cir2.getCircuitId()) continue;
				
				if(cir.getEndPortId().equals(cir2.getEndPortId())){
					if(cir.isStatusPlanned() == false && cir2.isStatusPlanned()){
						cir2.setStatusLksCode(cir.getStatusLksCode());
						cir2.setStatus(cir.getStatus());

						//TODO: use new code that Basker is doing to set the circuit status
						Session session = this.sessionFactory.getCurrentSession();
						DataConnection conn  = (DataConnection)session.get(DataConnection.class, cir2.getStartConnId());
						if (conn != null && conn.getSourceDataPort() != null && conn.getSourceDataPort().isLogical() == false) {
							conn.setStatusLookup(SystemLookup.getLksData(session, cir.getStatusLksCode()));
							session.update(conn);
							session.flush();
						}
					}
				}
				
			}
		}
	}	
	
	public Collection<Long> getRequestsForConnection(ICircuitConnection conn) throws DataAccessException {
		List<Long> requestIds = new LinkedList<Long>();
		
		if (null == conn) return requestIds;
		
		try {
			Session session = sessionFactory.getCurrentSession();
			String namedQuery = conn instanceof DataConnection ? "getAllDataConnectionRequests" : "getAllPowerConnectionRequests";
			Query q = session.getNamedQuery( namedQuery );
			q.setLong("connectionId", conn.getConnectionId());
			@SuppressWarnings("unchecked")
			List<Long> results = q.list();
			if (results != null) {
				requestIds.addAll( results );
			}
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		
		return requestIds;
	}
	
	private ICircuitConnection getCircuitStartConn(CircuitViewData circuit){
		Session session = this.sessionFactory.getCurrentSession();
		ICircuitConnection conn = null;

		Long connId = circuit.getStartConnId();

		if(circuit.isDataCircuit()){
			conn = (DataConnection)session.get(DataConnection.class, connId);
		}
		else{
			conn = (PowerConnection)session.get(PowerConnection.class, connId);
		}

		return conn;
	}

	@Override
	public boolean hasConnectionRequests(Long itemId, Errors errors){
		List<CircuitRequestInfo> requestInfoList = getAllPendingCircuitRequestList(itemId); 
		boolean retValue = false;
		
		for (CircuitRequestInfo requestInfo: requestInfoList) {
			if(requestInfo != null ){	
				//loadRequest load the request using a new session. If request was created using current session,
				//the function will return a null
				Request request = requestDAO.loadRequest(requestInfo.getRequestId());
				
				// Do not check for disconnect requests as an error
				if (null != request && null != request.getRequestTypeLookup() && request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.DISCONNECT)) {
					continue;
				}
				
				if(request != null) {
					String requestNumber = request.getRequestNo();
					String requestDescription = request.getDescription();
					Item item = itemDAO.loadItem(itemId);
					Object errorArgs[] = {item.getItemName(), requestNumber, requestDescription};
					errors.reject("itemRequest.pendingRequest.sameRequestType", errorArgs, "Could not submit request for item");
					retValue = true;
				}
			}
		}
		
		return retValue;
	}	
	
	private List<CircuitRequestInfo> getAllPendingCircuitRequestList(Long itemId) {
		List<CircuitRequestInfo> requestInfoList = new ArrayList<CircuitRequestInfo>();
		HashMap<Long, CircuitRequestInfo> reqInfoMap = getCircuitRequestInfo(SystemLookup.PortClass.DATA, false);
		HashMap<Long, CircuitRequestInfo> reqInfoMap2 = getCircuitRequestInfo(SystemLookup.PortClass.POWER, false);		
		reqInfoMap.putAll(reqInfoMap2);
		
		List<CircuitCriteriaDTO> cirList = itemDAO.getAssociatedCircuitsForItem(itemId);
		
		for(CircuitCriteriaDTO cir:cirList){
			long circuitId = cir.getCircuitUID().getCircuitDatabaseId();
			//Check to see if there are pending request
			if(reqInfoMap.get(circuitId) != null){
				requestInfoList.add(reqInfoMap.get(circuitId));
				
			}
		}
		return requestInfoList;
	}
	

	@Override
	public void disconnectCircuits(List<CircuitCriteriaDTO> circuitIds, Long requestId, String requestType, Boolean chassisChanged) throws DataAccessException, BusinessValidationException {
    	int count = 1;
		Session session = this.sessionFactory.getCurrentSession();
		Request request = (Request)session.get(Request.class, requestId);
		Integer assReqCnt = requestDAO.getLastAssociatedRequestCount(request);
		if (null != assReqCnt && assReqCnt > 0) {
			count = assReqCnt + 1;
		}
		String requestNo = request.getRequestNo() + "-" + String.format("%02d", count++);
		
		List<CircuitViewData> recList = new ArrayList<CircuitViewData>();
		UserInfo userInfo = userHome.getCurrentUserInfo();
		
		//load the circuits
		for(CircuitCriteriaDTO cr:circuitIds){
			recList.addAll(circuitSearch.searchCircuitsRaw(cr));
			userInfo = cr.getUserInfo();
		}
		
		//If circuit is installed, and using a partial circuit that is planned, mark partial as install
		matchCircuitStatus(recList);
		
		//perform disconnect
		for(CircuitViewData circuitView:recList){					
			if(!allowDisconnectForCircuit(circuitView, requestType, chassisChanged)){
				continue;
			}
			
			if(circuitView.isStatusPlanned() == true) {
				continue;
			}

			ICircuitConnection conn = getCircuitStartConn(circuitView);

			if (null == conn) continue;
			
			Collection<Long> pendingReqs = getRequestsForConnection(conn);

			if(pendingReqs == null || pendingReqs.size() == 0){
				this.disconnect(circuitView, requestNo);
				requestNo = request.getRequestNo() + "-" + String.format("%02d",count++);
			}else{ //update request number, needed to associated all requests during processing
				for(Long rid:pendingReqs){
					//Request req = requestDAO.loadRequest(rid);
					Request req = requestDAO.getRequest(rid);
		    		
					if(req == null || req.isConnectReq() || req.isReconnectReq() || req.isDiscAndMoveReq()){  
						continue;
					}
											
					req.setRequestNo(requestNo);
					session.update(req);
					session.flush();
					
					//CR 56887 Create new request history for reject request
					requestHistoryDAO.setRequestHistoryNotCurrent(req);
					requestHistoryDAO.createReqHist(req, SystemLookup.RequestStage.REQUEST_UPDATED, userInfo);
					
					requestNo = request.getRequestNo() + "-" + String.format("%02d",count++);
				}
			}
		}    		
	}	
}
