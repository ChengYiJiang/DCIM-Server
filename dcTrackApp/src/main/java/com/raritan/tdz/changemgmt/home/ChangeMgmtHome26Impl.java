package com.raritan.tdz.changemgmt.home;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemToMove;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


/**
 * Change Management internal business logic implementation.
 * 
 * @author Andrew Cohen
 */

@Transactional(rollbackFor = DataAccessException.class)
public class ChangeMgmtHome26Impl implements ChangeMgmtHome26{

	private SessionFactory sessionFactory;

	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired(required=true)
	private LksCache lksCache;
	
	public ChangeMgmtHome26Impl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public PortMoveDAO<DataPortMove> getDataPortMoveDAO() {
		return dataPortMoveDAO;
	}



	public void setDataPortMoveDAO(PortMoveDAO<DataPortMove> dataPortMoveDAO) {
		this.dataPortMoveDAO = dataPortMoveDAO;
	}



	public PortMoveDAO<PowerPortMove> getPowerPortMoveDAO() {
		return powerPortMoveDAO;
	}



	public void setPowerPortMoveDAO(PortMoveDAO<PowerPortMove> powerPortMoveDAO) {
		this.powerPortMoveDAO = powerPortMoveDAO;
	}



	@Override
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public long disconnectRequest(ICircuitInfo circuit, String linkRequestNo) throws DataAccessException {
		return createConnectionRequest(null, 
				circuit.getConnList(),
				circuit.getStartItemId(),
				"Disconnect Port " + circuit.getStartPortName(),
				"Disconnect",
				circuit.getCircuitType(),
				circuit.getStartConnId(), linkRequestNo
		);		
	}
	
	@Override
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public long disconnectAndMoveRequest(Long itemId, List<Long> connList, long portClassValueCode, String portName) throws DataAccessException {
		return createConnectionRequest(null,
				connList,
				itemId,
				"Disconnect/Reconnect Port " + portName,
				"Disconnect       and Move",
				portClassValueCode,
				null, null
		);		
	}

	@Override
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public long connectRequest(ICircuitInfo circuit)
			throws DataAccessException {
		return createConnectionRequest(circuit.getCircuitId(),
				null,
				circuit.getStartItemId(),
				"New Connect Port " + circuit.getStartPortName(),
				"Connect",
				circuit.getCircuitType(),
				circuit.getStartConnId(), null
		);	
	}

	@Override
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public long reconnectRequest(Long itemId, Long newCircuitId, long portClassValueCode, String portName, String linkRequestNo) throws DataAccessException {
		return createConnectionRequest(newCircuitId,
				null,
				itemId,
				"Disconnect/Reconnect Port " + portName,
				"Reconnect",
				portClassValueCode,
				null, linkRequestNo
		);		
	}
	
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public long createConnectionRequest(Long circuitId, List<Long> connList, Long itemId, String reqDesc, String reqType, long portClass, Long startConnId, String linkRequestNo) throws DataAccessException {
		String requestNo = null;
		Session session = null;
		Request req = null;
		String tableName;		
		Criteria criteria; 
		String connKeyIdName = "";
		Long portId = null;
		int sortOrder = 1;
		
		try {
			session = this.sessionFactory.getCurrentSession();
	    	//session = HibernateUtil.getSessionFactory().getCurrentSession();
	    	//org.hibernate.Transaction tx = session.beginTransaction();			
    		
    		if(portClass == SystemLookup.PortClass.POWER){
    			criteria = session.createCriteria(PowerConnection.class);
    			connKeyIdName = "powerConnectionId";
    			
    			if(reqType.equals("Connect")){
    				tableName = "dct_circuits_power";
    			}
    			else{
    				tableName = "dct_ports_power";
    			}
    		}
    		else{
    			criteria = session.createCriteria(DataConnection.class);
    			connKeyIdName = "dataConnectionId";
    			
    			if(reqType.equals("Connect")){
    				tableName = "dct_circuits_data";
    			}
    			else{
    				tableName = "dct_ports_data";
    			}
    		}
    		
			if(reqType.equals("Reconnect")){
				tableName = "tblXConnectsToMove";
			}
	    	
			Item item = (Item)session.get(Item.class, itemId);
			
			String reqSuffix = null;
			
			if(reqDesc.startsWith("Disconnect/Reconnect")){
				if(reqType.equals("Disconnect") || reqType.equals("Disconnect       and Move")){
					reqSuffix = "-DX";
				}else if(reqType.equals("Reconnect")){
					reqSuffix = "-RX";
				}			
			}
			/*
			if (reqSuffix != null) {
				if (reqSuffix.equals("-DX")) {
					requestNo = getNextRequestNo() + reqSuffix;
				}
				else {
					requestNo = getCurrentRequestNo() + reqSuffix;
				}
			}
			else {
				requestNo = getNextRequestNo();
			}*/
			
			if(linkRequestNo != null){  //over-ride request number
				requestNo = linkRequestNo;
			}
	
			//Create request 
			req = new Request();
			req.setDescription(reqDesc + " on " + item.getItemName());
			req.setItemId(itemId);
			req.setLocationId( item.getDataCenterLocation().getDataCenterLocationId() );
			req.setRequestNo(requestNo);
			req.setRequestType(reqType);
			req.setRequestTypeLookup(lksCache.getLksDataUsingLkpCode(req.getRequestTypeLookupCode()));
			req.setArchived(false);
			
			session.save(req);
			session.flush();
			session.refresh(req);
			
			Set<RequestPointer> plist =  new HashSet<RequestPointer>(0);
			Set<RequestHistory> hlist =  new HashSet<RequestHistory>(0);
			RequestPointer pointer = null;
			
			//Create request pointer records
	        if(reqType.equals("Disconnect       and Move")){	        	
	    		for(Long connId:connList)
	    		{					    			
	    			if(portClass == SystemLookup.PortClass.POWER){
	    				PowerConnection c = (PowerConnection)session.get(PowerConnection.class, connId);
	    				portId = c.getSourcePowerPort().getPortId();
	    				
	    			}
	    			else{
	    				DataConnection c = (DataConnection)session.get(DataConnection.class, connId);;
	    				portId = c.getSourceDataPort().getPortId();
	    			}
	    			
	    			if (portId != null) {
		    			pointer = new RequestPointer();
		    			pointer.setRecordId(portId);
		    			pointer.setTableName(tableName);
		    			pointer.setRequestDetail(req);
		    			pointer.setSortOrder(sortOrder++);
		    			plist.add(pointer);
	    			}
	    		}	        	
	        }
	        else if(reqType.equals("Disconnect")){
	    		String destItemName = "";
	    		
	    		for(Long connId:connList)
	    		{					    			
	    			if(portClass == SystemLookup.PortClass.POWER){
	    				PowerConnection c = (PowerConnection)session.get(PowerConnection.class, connId);
	    				
	    				portId = c.getSourcePowerPort().getPortId();
		    			pointer = new RequestPointer();
		    			pointer.setRecordId(portId);
		    			pointer.setTableName(tableName);
		    			pointer.setRequestDetail(req);
		    			pointer.setSortOrder(sortOrder++);
		    			plist.add(pointer);	
		    			
		    			//workaround to force PS port going into DB first. Otherwise, if output goes first,
		    			//disconnect will not happen. If we save entire request at the end, sometime
		    			//save() reorders pointers
		    			req.setRequestPointers(plist);
		    			//session.save(req);  new sort_order field should fix the problem
		    			
		    			portId = c.getDestPowerPort().getPortId();
		    			pointer = new RequestPointer();
		    			pointer.setRecordId(portId);
		    			pointer.setTableName(tableName);
		    			pointer.setRequestDetail(req);
		    			pointer.setSortOrder(sortOrder++);
		    			plist.add(pointer);
		    			break;
	    			}
	    			else{
	    				DataConnection c = (DataConnection)session.get(DataConnection.class, connId);;
	    				portId = c.getSourceDataPort().getPortId();
	    				
	    				if(c.getDestDataPort() != null){
	    					Item dItem = c.getDestDataPort().getItem();
	    					
	    					if(dItem.getModel() == null){
	    						destItemName = dItem.getItemName();
	    					}
	    					else{
	    						destItemName = "";
	    					}
	    				}
		    			pointer = new RequestPointer();
		    			pointer.setRecordId(portId);
		    			pointer.setTableName(tableName);
		    			pointer.setRequestDetail(req);
		    			pointer.setSortOrder(sortOrder++);
		    			plist.add(pointer);	 
		    			
		    			req.setRequestPointers(plist);
		    			session.saveOrUpdate(req);
		    			
		    			DataPort firstPort = c.getSourceDataPort();  //first node must be a Virtual
		    			
                        if(firstPort.isVirtual() || firstPort.isLogical()){
                              break;    
                        }    			
	    			}	    			
	    		}	        	
	        }   
	        else if(reqType.equals("Reconnect")){
	        	criteria = session.createCriteria(ConnectionToMove.class);
	        	criteria.add(Restrictions.eq("newCircuitId", circuitId.intValue()));
	        	criteria.addOrder(Order.asc("newSortOrder"));
	        	
	    		for(Object o:criteria.list())
	    		{				
	    			ConnectionToMove rec = (ConnectionToMove)o;

	    			pointer = new RequestPointer();
	    			pointer.setRecordId(rec.getConnectionToMoveId());
	    			pointer.setTableName(tableName);
	    			pointer.setRequestDetail(req);
	    			pointer.setSortOrder(sortOrder++);
	    			plist.add(pointer);	 	    			
	    		}	
	        }
	        else if(reqType.equals("Connect")){
    			pointer = new RequestPointer();
    			pointer.setRecordId(circuitId);   
    			pointer.setTableName(tableName);
    			pointer.setRequestDetail(req);
    			pointer.setSortOrder(sortOrder++);
    			plist.add(pointer);	 	    				        	
	        }			
			
	        //Create request history record
	        RequestHistory hist = createReqHist(req, session, SystemLookup.RequestStage.REQUEST_ISSUED);
	        hlist.add(hist);
	
	        //force to save the request pointer first
	        req.setRequestPointers(plist);
	        session.saveOrUpdate(req);
	        session.flush();
	        
	        //save request history last
	        req.setRequestHistories(hlist);
	        session.saveOrUpdate(req);	        
	        session.flush();
	        
	        /* trigger should handle this - Santo
	        if(circuitId == null){
	        	circuitId = getCircuitIdForConnId(connList.get(0), portClass);
	        }
	        
	        if(circuitId != null){
	        	updateCircuitsList(circuitId, portClass, req.getRequestNo(), hist.getStageIdLookup().getLkpValue());
	        }	
	        */
	        if (portId != null && req != null) {
	        	if(portClass == SystemLookup.PortClass.POWER){
	        		powerPortMoveDAO.setPortRequest(portId, req);
	        	} else if (portClass == SystemLookup.PortClass.DATA){
	        		dataPortMoveDAO.setPortRequest(portId, req);
	        	}
	        }
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}
	
        return req.getRequestId();
	}

	// FIXME - This code was taken from ItemServiceHomeImpl and needs refactoring!
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public long createRequest(long itemId, String reqDesc, String tableName, String reqType) throws DataAccessException {
		String requestNo;
		Session session = null;
		Request req = null;
		int sortOrder = 1;
		
		try {
			session = this.sessionFactory.getCurrentSession();
			Item item = (Item)session.get(Item.class, itemId);
			if (item == null) {
				throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL.value(), this.getClass()));
			}
			
			requestNo = null;//getNextRequestNo();
	
			//System.out.println(requestNo);
	
			req = new Request();
			req.setDescription( reqDesc );
			//rec.setDescription(reqDesc + item.getItemName());
			req.setItemId(itemId);
			req.setLocationId( item.getDataCenterLocation().getDataCenterLocationId() );
			req.setRequestNo(requestNo);
			req.setRequestType(reqType);
			req.setRequestTypeLookup(lksCache.getLksDataUsingLkpCode(req.getRequestTypeLookupCode()));
			req.setArchived(false);
	
			Set<RequestPointer> plist =  new HashSet<RequestPointer>(0);
			Set<RequestHistory> hlist =  new HashSet<RequestHistory>(0);
	
			RequestPointer pointer = new RequestPointer();
			pointer.setRecordId(itemId);
			pointer.setTableName(tableName);
			pointer.setRequestDetail(req);
			pointer.setSortOrder(sortOrder++);
			plist.add(pointer);
	
	        //** need this pointer to tblEntity so that qryRequests can include the Item Move Request record
			if(tableName.equals("tblItemsToMove")){
				pointer = new RequestPointer();
				pointer.setRecordId(item.getItemId());
				pointer.setTableName("tblEntity");
	        	pointer.setRequestDetail(req);
	        	pointer.setSortOrder(sortOrder++);
	        	plist.add(pointer);
			}
				
	        hlist.add(createReqHist(req, session, SystemLookup.RequestStage.REQUEST_ISSUED));
	        
	        req.setRequestHistories(hlist);
	        req.setRequestPointers(plist);
	
			// save new request
	        session.save(req);
	        session.flush();
	        session.refresh(req);
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}

        return req.getRequestId();
	}

	
	@Override
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional(propagation=Propagation.REQUIRED)
	public List<IPortInfo> deleteRequest(Long requestId, boolean doAssociatedRequests) throws DataAccessException {
		List<IPortInfo> ports = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Request req = (Request)session.get(Request.class, requestId);
			
			if (req == null) { //requestId does not exists
				return null; 
			}
			
			//First delete records in the Connection/Item To Move tables
        	List<Long> idList = new ArrayList<Long>();
        	
			for (RequestPointer pointer:req.getRequestPointers()) {
				if(pointer.getTableName().equals("tblXConnectsToMove")){
					//will get more than one record
					idList.add(pointer.getRecordId());
				}
				else if(pointer.getTableName().equals("tblItemsToMove")){ 
					//should be one record
					deleteItemToMove(pointer.getRecordId(), session);
				}				
			}			
			
			//delete records in the tblXConnectionsToMove table
			if (idList.size() > 0) {
				ports = deleteConnToMove(idList, session);
			}
									
			//Delete Request, for request Pointer/History the operation does a CASCADE DELETE			
			session.delete( req );
			//session.flush();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}
		
		return ports;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Request reSubmitRequest(long requestId) throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();
			
	    	Criteria criteria = session.createCriteria(Request.class);
	    	criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
	    	criteria.createAlias("requestPointers", "pointer");
	    	criteria.createAlias("requestHistories", "hist");
	    	criteria.add(Restrictions.eq("archived", false));
	    	criteria.add(Restrictions.eq("hist.current", true));	    	
    		criteria.add(Restrictions.eq("requestId", requestId));
    		
	    	List<String> requestNoList = new ArrayList<String>();
	    	
			for(Object obj:criteria.list()){
				Request req = (Request)obj;				
				requestNoList.add(req.getRequestNo());				
			}	
			
			if(requestNoList.size() == 0){  //request not found
				return null;
			}
					
			String requestNo = requestNoList.get(0);
			
			if(requestNo.endsWith("-RX")){ //Then its a Disconnect or Reconnect request then upate both
				requestNoList.add(requestNo.substring(0, requestNo.lastIndexOf("X")) + "-DX");
			}
			else if(requestNo.endsWith("-DX")){ //Then its a Disconnect or Reconnect request then upate both
				requestNoList.add(requestNo.substring(0, requestNo.lastIndexOf("X")) + "-RX");
			}
	    	
			criteria = session.createCriteria(RequestHistory.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
	    	criteria.createAlias("requestDetail", "request");
	    	criteria.add(Restrictions.in("request.requestNo", requestNoList));
	    	criteria.add(Restrictions.eq("current", true));
	    	
			for(Object obj:criteria.list()){
				RequestHistory h = (RequestHistory)obj;	
				Request request = h.getRequestDetail();
				
				h.setCurrent(false);
				session.update(h);
				session.flush();  //for trigger execution
				
				RequestHistory hist = createReqHist(request, session, SystemLookup.RequestStage.REQUEST_UPDATED);
		        session.save(hist);		        
								
				request.getRequestHistories().add(hist);
				session.update(request);
				
				session.flush();
				
				return request;
			}	
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}		
		
		return null;
	}

	
	@Override
	public List<Request> viewRequest(Request request) throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();	
			Criteria criteria = session.createCriteria(Request.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			criteria.createAlias("requestHistories", "history");
			criteria.createAlias("requestPointers", "pointer");
	    	criteria.add(Restrictions.eq("history.current", true));
		    
			if(request != null){
				if(request.getRequestId() != 0){
					criteria.add(Restrictions.eq("requestId", request.getRequestId()));
				}
				else if(request.getRequestNo() != null){
					criteria.add(Restrictions.eq("requestNo", request.getRequestNo()));
				}
			}
			
	    	return criteria.list();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public long getRequestStage(Request request) throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Request.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			if(request != null){
				if(request.getRequestId() != 0){
					criteria.add(Restrictions.eq("requestId", request.getRequestId()));
				}
				else if(request.getRequestNo() != null){
					criteria.add(Restrictions.eq("requestNo", request.getRequestNo()));
				}
			}
			
			// criteria.createAlias("requestHistories", "history");
			// criteria.createAlias("requestPointers", "pointer");
	    	// criteria.add(Restrictions.eq("history.current", true));
	    	
	    	for(Object obj:criteria.list()){
	    		Request req = (Request)obj;
	    		
	    		for(RequestHistory hist:req.getRequestHistories()){
	    			if (hist.isCurrent()) {
	    				return hist.getStageIdLookup().getLkpValueCode();
	    			}
	    		}
	    		break;
	    	}
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		
		return -1;
	}
	
	//@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public Collection<Long> getRequestsForCircuit(ICircuitInfo circuit) throws DataAccessException {
		Set<Long> requestIds = new HashSet<Long>();
		
		try {
			for (ICircuitConnection conn : circuit.getCircuitConnections()) {
				requestIds.addAll( this.getRequestsForConnection(conn) );
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
	
	//@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public Collection<Long> getRequestsForConnection(ICircuitConnection conn) throws DataAccessException {
		List<Long> requestIds = new LinkedList<Long>();
		
		try {
			Session session = sessionFactory.getCurrentSession();
			String namedQuery = conn instanceof DataConnection ? "getAllDataConnectionRequests" : "getAllPowerConnectionRequests";
			Query q = session.getNamedQuery( namedQuery );
			q.setLong("connectionId", conn.getConnectionId());
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
	
	//HELPER FUNCTIONS 
	private RequestHistory createReqHist(Request request, Session session, long requestStageValueCode){
		UserInfo user = FlexUserSessionContext.getUser();
		
		RequestHistory hist = new RequestHistory();
		hist.setCurrent(true);
		if (user != null) {
			hist.setRequestedBy(user.getUserName());
		}
        hist.setRequestedOn(new Timestamp(java.util.Calendar.getInstance().getTimeInMillis()));
        hist.setRequestDetail(request);	
        hist.setStageIdLookup(SystemLookup.getLksData(session, requestStageValueCode));	
        
        return hist;
	}
	
	private String getNextRequestNo() {
		String requestNo = getCurrentRequestNo();
		if (requestNo != null) {
			requestNo = String.valueOf( Long.valueOf(requestNo) + 1);
		}
		return requestNo;
	}
	
	// FIXME - This code was taken from itemServiceHomeImpl and needs to be refactored!
	private String getCurrentRequestNo() {
		String requestNo = "";
    	Session session = null;
    	
    	//try {
	    	session = this.sessionFactory.getCurrentSession();
	        //org.hibernate.Transaction tx = session.beginTransaction();
	
	        //Get the last request number
	        Query q = session.createSQLQuery("select r.requestNo from tblRequest r where substring(r.requestNo from 1 for 2) = to_char(current_date,'YY')  order by 1 desc ");
	
	        List recList = q.list();
	
	        //format for request number is YY#####, where YY is year and # is a digit
		    if(recList != null && recList.size() > 0){
		    	String r = (String)recList.get(0);
		    	String t[] = r.split("-");
		    	
		    	requestNo = t[0];
		    }
		    else {
		    	String date = java.util.Calendar.getInstance().getTime().toString();
		    	requestNo = date.substring(date.length() - 2) + "00001";
		    }
	
		    //increment request number by 1
		    requestNo = String.valueOf((Long.valueOf(requestNo)));
    	//}
    	//catch

	    return requestNo;
	}
	
	private void deleteItemToMove(Long itemToMoveId, Session session){
		if(itemToMoveId != null && itemToMoveId > 0){
	    	Criteria criteria = session.createCriteria(ItemToMove.class);
	    	criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
	    	criteria.add(Restrictions.eq("itemToMoveId", itemToMoveId));
	    	
	    	for(Object obj:criteria.list()){
	    		ItemToMove i = (ItemToMove)obj;
	    		session.delete(i);
	    	}	        	
		}
	}
	
	private List<IPortInfo> deleteConnToMove(List<Long> idList, Session session){
		if (idList == null || idList.isEmpty()) return null;
		List<IPortInfo> ports = new LinkedList<IPortInfo>();
		
    	Criteria criteria = session.createCriteria(ConnectionToMove.class);
    	criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
    	criteria.add(Restrictions.in("connectionToMoveId", idList));

    	for (Object obj:criteria.list()) {
    		ConnectionToMove m = (ConnectionToMove)obj;
    		session.delete( m) ;
    		
    		//save list of port ID
    		if (m.getPortMovingId() != null) {
    			final long portId = m.getPortMovingId().longValue();
    			IPortInfo port = null;
    			if (m.getConnType().equals("data")) {
    				port = new DataPort();
    			}
    			else {
    				port = new PowerPort();
    			}
    			
    			port.setPortId( portId );
    			ports.add( port );
    		}
    	}
    	
    	return ports;
	}
	
	@Override
	public List<Request> getItemRequest(long itemId) throws DataAccessException {
		try {
			Long rStage[] = {SystemLookup.RequestStage.REQUEST_ABANDONED, 
					SystemLookup.RequestStage.REQUEST_ARCHIVED, 
					SystemLookup.RequestStage.REQUEST_COMPLETE};
			Session session = sessionFactory.getCurrentSession();	
			Criteria criteria = session.createCriteria(Request.class);
			criteria.createAlias("requestHistories", "history");
			criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
	    	criteria.add(Restrictions.eq("history.current", true));
	    	criteria.add(Restrictions.eq("itemId", itemId));
	    	criteria.add(Restrictions.not(Restrictions.in("historyStages.lkpValueCode", rStage)));
	    	criteria.addOrder(Order.desc("requestId"));
			
	    	return criteria.list();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public List<Request> getItemRequest(long itemId, Long rStage[]) throws DataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();	
			Criteria criteria = session.createCriteria(Request.class);
			// criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			criteria.createAlias("requestHistories", "history");
			criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
	    	criteria.add(Restrictions.eq("history.current", true));
	    	criteria.add(Restrictions.eq("itemId", itemId));
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", rStage));
	    	criteria.addOrder(Order.desc("requestId"));
			
	    	return criteria.list();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}

	public void updateCircuitsList(long circuitId, long circuitType, String reqNumber, String reqStage) throws DataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();	
			Criteria criteria = session.createCriteria(CircuitViewData.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
	    	criteria.add(Restrictions.eq("circuitId", circuitId));
	    	criteria.add(Restrictions.eq("circuitType", circuitType));
	    	
	    	for(Object rec:criteria.list()){
	    		CircuitViewData cir = (CircuitViewData)rec;
	    		cir.setRequestNumber(reqNumber);
	    		cir.setRequestStage(reqStage);
	    		
	    		session.update(cir);
	    	}
	    	session.flush();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}

	public Long getCircuitIdForConnId(Long connectionId, long circuitType) throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		
		if(circuitType == SystemLookup.PortClass.DATA){
			Query q = session.createSQLQuery("select circuit_data_id from dct_circuits_data where circuit_trace like '%," + connectionId + ",%' and COALESCE(shared_circuit_trace, '') not like '%," + connectionId + ",%'");				
			
			for(Object rec:q.list()){
				BigInteger id = (BigInteger)rec;
				return id.longValue();
			}
		}
		else{
			Query q = session.createSQLQuery("select circuit_power_id from dct_circuits_power where circuit_trace like '%," + connectionId + ",%' and COALESCE(shared_circuit_trace, '') not like '%," + connectionId + ",%'");				
			
			for(Object rec:q.list()){
				BigInteger id = (BigInteger)rec;
				return id.longValue();
			}
			
		}
		
		return null;			
	}	
}
