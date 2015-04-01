package com.raritan.tdz.request.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.RequestDTO;

public class RequestDAOImpl extends DaoImpl<Request> implements RequestDAO {

	private static Logger log = Logger.getLogger("RequestDAO");

	private Long [] pendingReqStages;
	
	public RequestDAOImpl() {
		super();
		
		pendingReqStages =  new Long[] {
				SystemLookup.RequestStage.REQUEST_ISSUED,
				SystemLookup.RequestStage.REQUEST_REJECTED,
				SystemLookup.RequestStage.REQUEST_UPDATED,
				SystemLookup.RequestStage.REQUEST_APPROVED,
				SystemLookup.RequestStage.WORK_ORDER_ISSUED,
				SystemLookup.RequestStage.WORK_ORDER_COMPLETE
			};
	}

	@Override
	public Request loadRequest(Long id) {
		return loadRequest(id, true);
	}

	@Override
	public Request loadRequest(Long id, boolean readOnly) {
		Session session = null;
		Request retval = null;
	
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(Request.class);
				criteria.setFetchMode("statusLookup", FetchMode.JOIN);
				criteria.add(Restrictions.eq("requestId", id));
				criteria.setReadOnly(readOnly);
				retval = (Request)criteria.uniqueResult();
			}
		}
		finally{
			if( session != null ){
				session.close();
			}
		}
		return retval;	
	}

	@Override
	public Request loadRequest(String requestNo, boolean readOnly) {
		Session session = null;
		Request retval = null;
	
		try{
			if( requestNo != null && requestNo.length() > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(Request.class);
				criteria.setFetchMode("statusLookup", FetchMode.JOIN);
				criteria.add(Restrictions.eq("requestNo", requestNo));
				criteria.setReadOnly(readOnly);
				retval = (Request)criteria.uniqueResult();
			}
		}
		finally{
			if( session != null ){
				session.close();
			}
		}
		return retval;	
	}


	@Override
	public Request getRequest(String requestNo, boolean readOnly) {
		Session session = null;
		Request retval = null;
	
		if( requestNo != null && requestNo.length() > 0 ){
			session = this.getSession();
			Criteria criteria = session.createCriteria(Request.class);
			criteria.setFetchMode("statusLookup", FetchMode.JOIN);
			criteria.add(Restrictions.eq("requestNo", requestNo));
			criteria.setReadOnly(readOnly);
			retval = (Request)criteria.uniqueResult();
		}

		return retval;	
	}

	@Override
	public Request getRequest(Long id) {
		return this.read(id);
	}

	@Override
	public List<Request> getRequests(List<Long> requestIds) {
		Session session =  this.getSession();;	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.add(Restrictions.in("requestId", requestIds));
		
		@SuppressWarnings("unchecked")
		List<Request> requests = criteria.list();
		
		return requests;
	}
	
	@Override
	public void delete(Long id) {
		Request object = this.read(id);
		
		if(object != null){
			this.delete(object);
		}
	}
	
	@Override
	public List<Long> getItemIdsForRequests(List<Long> requestIds) { 
		Session session =  this.getSession();;	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.add(Restrictions.in("requestId", requestIds));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");
		criteria.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<Long> itemIds = criteria.list();
		
		return itemIds;
	}
	
	public void setRequestHistoryNotCurrent(Request request) {
		
		Set<RequestHistory> histories = request.getRequestHistories();
		
		for (RequestHistory history: histories) {
			if (history.isCurrent()) {
				history.setCurrent(false);
			}
		}
		
		mergeOnly(request);
		
	}
	
	@Override
	public RequestHistory createReqHist(Request request, long requestStageValueCode, UserInfo userInfo) {
		UserInfo user = userInfo;
		Session session = this.getSession();
		
		RequestHistory hist = new RequestHistory();
		hist.setCurrent(true);
		if (user != null) {
			hist.setRequestedBy(user.getUserName());
		}
        hist.setRequestedOn(GlobalUtils.getCurrentDate());
        hist.setRequestDetail(request);
        hist.setStageIdLookup(SystemLookup.getLksData(session, requestStageValueCode));	
        
        request.addRequestHistory(hist);
        
        mergeOnly(request);
        
        return hist;
	}

	@Override
	public RequestHistory createReqHist(Request request, long requestStageValueCode, UserInfo userInfo, String comment) {
		UserInfo user = userInfo;
		Session session = this.getSession();
		
		RequestHistory hist = new RequestHistory();
		hist.setCurrent(true);
		if (user != null) {
			hist.setRequestedBy(user.getUserName());
		}
        hist.setRequestedOn(GlobalUtils.getCurrentDate());
        hist.setRequestDetail(request);
        hist.setStageIdLookup(SystemLookup.getLksData(session, requestStageValueCode));	
        hist.setComment(comment);
        
        request.addRequestHistory(hist);
        
        mergeOnly(request);
        
        return hist;
	}

	@Override
	public void setWorkOrderComplete(Request request) {
		
		request.getWorkOrder().setCompleted(true);
		
		mergeOnly(request);
	}
	
	@Autowired(required=true)
	private LksCache lksCache;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;

	@SuppressWarnings("serial")
	public static final Map<Long, Long> requestToWorkorderType =
			Collections.unmodifiableMap(new HashMap<Long, Long>() {{
				put(SystemLookup.RequestTypeLkp.ITEM_MOVE, SystemLookup.WorkOrderType.ITEM_MOVE);
				put(SystemLookup.RequestTypeLkp.CONNECT, SystemLookup.WorkOrderType.NEW_CONNECTION);
				put(SystemLookup.RequestTypeLkp.DISCONNECT, SystemLookup.WorkOrderType.DISCONNECT);
				put(SystemLookup.RequestTypeLkp.DISCONNECT_AND_MOVE, SystemLookup.WorkOrderType.DISCONNECT_RECONNECT);
				
			}});	
	
	@SuppressWarnings("serial")
	public static final Map<Long, Long> itemClassToWorkorderType =
			Collections.unmodifiableMap(new HashMap<Long, Long>() {{
				put(SystemLookup.Class.CABINET, SystemLookup.WorkOrderType.CABINETS);
				put(SystemLookup.Class.CRAC, SystemLookup.WorkOrderType.CRAC);
				put(SystemLookup.Class.CRAC_GROUP, SystemLookup.WorkOrderType.CRAC);
				put(SystemLookup.Class.DATA_PANEL, SystemLookup.WorkOrderType.NETWORK_EQUIPMENT);
				put(SystemLookup.Class.DEVICE, SystemLookup.WorkOrderType.DEVICES);
				put(SystemLookup.Class.FLOOR_OUTLET, SystemLookup.WorkOrderType.POWER_OUTLETS);
				put(SystemLookup.Class.FLOOR_PDU, SystemLookup.WorkOrderType.PDU);
				put(SystemLookup.Class.NETWORK, SystemLookup.WorkOrderType.NETWORK_EQUIPMENT);
				put(SystemLookup.Class.PROBE, SystemLookup.WorkOrderType.PROBE);
				put(SystemLookup.Class.RACK_PDU, SystemLookup.WorkOrderType.RACK_PDUS);
				put(SystemLookup.Class.UPS, SystemLookup.WorkOrderType.UPS);
				put(SystemLookup.Class.UPS_BANK, SystemLookup.WorkOrderType.UPS);
				
			}});
	
	// FIXME:: Write the correct alogrithm to get the work order type
	private LksData getWorkOderType(Request request) {
		
		Long workOrderLkp = (null != request.getRequestTypeLookup()) ? requestToWorkorderType.get(request.getRequestTypeLookup().getLkpValueCode()) : null;
		
		if (null != workOrderLkp)  return lksCache.getLksDataUsingLkpCode(workOrderLkp);
		
		Long itemClass = itemDAO.getItemClass(request.getItemId());

		workOrderLkp = (null != itemClass) ? itemClassToWorkorderType.get(itemClass) : null;
		
		if (null != workOrderLkp)  return lksCache.getLksDataUsingLkpCode(workOrderLkp);
		
		return null;
	}
	
	@Override
	public WorkOrder createWorkOrder(Request request, UserInfo userInfo) {
		UserInfo user = userInfo;
		
		WorkOrder workOrder = new WorkOrder();
		workOrder.setArchived(false);
		workOrder.setCompleted(false);
		if (user != null) {
			 workOrder.setSentTo(user.getUserName());
		}
		workOrder.setWorkOrderDueOn(GlobalUtils.getCurrentDate());
		workOrder.setWorkOrderTypeLookup(getWorkOderType(request));
		// Trigger will update the work order number on before insert
		// workOrder.setWorkOrderNumber(getWorkOrderNo(request));
		
		Session session = this.getSession();
		session.save(workOrder);
        session.flush();
        session.refresh(workOrder);
		
        request.setWorkOrder(workOrder);
        
        mergeOnly(request);
        
        return workOrder;
	}

	// FIXME:: Write the correct alogrithm know more about work order number, specially -X
	@Override
	public String getWorkOrderNo(Request request) {
		String workOrderNo = getCurrentWorkOrderNo(request);
		if (workOrderNo != null) {
			workOrderNo = String.valueOf( Long.valueOf(workOrderNo));
		}
		
		//increment request number by 1
		// Format of the work order is <YY>WO-NNNNN
		String date = java.util.Calendar.getInstance().getTime().toString();
    	String workOrderNoPrefix = date.substring(date.length() - 2) + "WO-" + String.format("%5d", (Long.valueOf(workOrderNo) + 1)).replace(' ', '0');
		return workOrderNoPrefix;

	}
	
	private String getCurrentWorkOrderNo(Request request) {
		String workOrderNo = "";
    	
    	Session session = this.getSession();

        //Get the last request number
        Query q = session.createSQLQuery(" select r.WorkOrderNo from tblWorkOrder r where substring(r.WorkOrderNo from 1 for 2) = to_char(current_date,'YY')  order by 1 desc ");

        @SuppressWarnings("rawtypes")
		List recList = q.list();

	    if(recList != null && recList.size() > 0){
	    	String r = (String)recList.get(0);
	    	String t[] = r.split("-");
	    	
	    	workOrderNo = t[1];
	    }
	    else {
	    	workOrderNo = "0000";
	    }

	    workOrderNo = String.valueOf((Long.valueOf(workOrderNo)));

	    return workOrderNo;
	}

	@Override
	public String getNextRequestNo() {
		String requestNo = getCurrentRequestNo();
		if (requestNo != null) {
			requestNo = String.valueOf( Long.valueOf(requestNo) + 1);
		}
		return requestNo;
	}
	
	@SuppressWarnings("rawtypes")
	private String getCurrentRequestNo() {
		String requestNo = "";
    	
    	Session session = this.getSession();

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

	    return requestNo;
	}

	@Override
	public void itemArchived(Request request, UserInfo userInfo) {

		UserInfo user = userInfo;
		String userName = user.getUserName();
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("dcArchivedItem");
		
		query.setParameter("itemId", request.getItemId());
		query.setParameter("requestId", request.getRequestId());
		query.setParameter("userName", userName);		

		String retcode = (String) query.uniqueResult();
		log.debug("move ret result = " + retcode);
		
		session.flush();
	}

	@Override
	public void cabinetElevationArchived(Request request, UserInfo userInfo) {

		UserInfo user = userInfo;
		String userName = user.getUserName();
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("dcArchivedCabinetElevation");
		
		query.setParameter("itemId", request.getItemId());
		query.setParameter("requestId", request.getRequestId());
		query.setParameter("userName", userName);

		String retcode = (String) query.uniqueResult();
		log.debug("move ret result = " + retcode);
		
		session.flush();
	}

	@Override
	public void circuitArchived(Long circuitListId, Request request, UserInfo userInfo) {

		Session session = this.getSession();
		UserInfo user = userInfo;
		String userName = user.getUserName();
		
		Query query = session.getNamedQuery("dcArchivedCircuit");
		
		query.setParameter("circuitListId", circuitListId);
		query.setParameter("requestId", request.getRequestId());
		query.setParameter("userName", userName);

		String retcode = (String) query.uniqueResult();
		log.debug("move ret result = " + retcode);
		
		session.flush();
	}
	
	
	@Override
	public void itemMoveWorkOrderComplete(Request request, UserInfo userInfo) {
		UserInfo user = userInfo;
		String userName = user.getUserName();
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("dcWorkOrderCompleteItemMove");
		
		query.setParameter("requestId", request.getRequestId());
		query.setParameter("userName", userName);		

		String retcode = (String) query.uniqueResult();
		log.debug("move ret result = " + retcode);
		
		session.flush();
	}

	@Override
	public List<Request> getAllPendingRequestsForAnItem(long itemId) {
		
		return getRequestForItem(itemId, pendingReqStages);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Request> getRequestForItem(long itemId, Long rStage[]) {
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.eq("itemId", itemId));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", rStage));
    	criteria.addOrder(Order.desc("requestId"));
		
    	return criteria.list();
	}	

	@Override
	public Map<Long, List<Request>>  getRequestsForItem(List<Long> itemIds, List<Long> requestStageFilters, Errors errors) {
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("itemId", itemIds.toArray()));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStageFilters));
    	criteria.addOrder(Order.desc("itemId"));
		
    	@SuppressWarnings("unchecked")
		List<Request> requests = criteria.list();
    	Map<Long, List<Request>> reqMap = new HashMap<Long, List<Request>>();
    	
    	for (Request request: requests) {
    		
    		Long itemId = request.getItemId();
    		List<Request> reqs = reqMap.get(itemId);
    		if (null == reqs) {
    			reqs = new ArrayList<Request>();
    			reqMap.put(itemId, reqs);
    		}
    		reqs.add(request);
    	}
    	
    	return reqMap;
    	
	}	

	@Override
	public List<Request>  getPendingRequestsForItem(List<Long> itemIds, List<Long> requestType) {
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("itemId", itemIds.toArray()));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	
    	if (null != requestType && requestType.size() > 0) {
    		criteria.createAlias("requestTypeLookup", "requestTypeLookup");
    		criteria.add(Restrictions.in("requestTypeLookup.lkpValueCode", requestType.toArray()));
    	}
    	
    	criteria.addOrder(Order.desc("itemId"));
		
    	@SuppressWarnings("unchecked")
		List<Request> requests = criteria.list();
    	
    	return requests;
    	
	}	

	@Override
	public List<Long>  getPendingRequestIdsForItem(List<Long> itemIds, List<Long> requestType) {
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("itemId", itemIds.toArray()));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	
    	if (null != requestType && requestType.size() > 0) {
    		criteria.createAlias("requestTypeLookup", "requestTypeLookup");
    		criteria.add(Restrictions.in("requestTypeLookup.lkpValueCode", requestType.toArray()));
    	}

    	ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("requestId"), "requestId");
		criteria.setProjection(proList);
		
    	criteria.addOrder(Order.desc("itemId"));
		
    	@SuppressWarnings("unchecked")
		List<Long> requestIds = criteria.list();
    	
    	return requestIds;
    	
	}	

	
	@Override
	public List<Request>  getPendingNonPowerRequestsForItem(List<Long> itemIds, List<Long> requestType) {
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
		criteria.createAlias("requestPointers", "pointer");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("itemId", itemIds.toArray()));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	String[] powerRequests = {"dct_circuits_power", "dct_ports_power"};
    	criteria.add(Restrictions.not(Restrictions.in("pointer.tableName", powerRequests)));
    	
    	if (null != requestType && requestType.size() > 0) {
    		criteria.createAlias("requestTypeLookup", "requestTypeLookup");
    		criteria.add(Restrictions.in("requestTypeLookup.lkpValueCode", requestType.toArray()));
    	}
    	
    	criteria.addOrder(Order.desc("itemId"));
		
    	@SuppressWarnings("unchecked")
		List<Request> requests = criteria.list();
    	
    	return requests;
    	
	}
	
	@Override
	public void circuitInstallWorkOrderComplete(Request request) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Request getRequestById(Long requestId) {
		Session session = this.getSession();	
		Criteria c = session.createCriteria(Request.class);
		c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		c.createAlias("requestHistories", "history");
		c.createAlias("requestPointers", "pointer");
    	c.add(Restrictions.eq("history.current", true));
    	if(requestId != null && requestId > 0){
			c.add(Restrictions.eq("requestId", requestId));
		}
    	return (Request)c.uniqueResult();
	}


	@Override
	public List<Long> getAssociatedRequestIdsForRequest(Request request) {
		Session session = this.getSession();
		
		List<Long> reqList = new ArrayList<Long>();
		
		String requestNo = request.getRequestNo();
		
		if(requestNo.indexOf("-") > 1){
			String temp[] = requestNo.split("-");
			requestNo = temp[0];
		}
		
		//call name query
		Query query = session.getNamedQuery("getAssociatedRequestIdsForRequest");
		query.setString("requestNo", (requestNo + "%"));
		query.setLong("requestId", request.getRequestId());
		
		for(Object obj:query.list()){
			Long id = (Long)obj;
			
			if(id.equals(request.getRequestId())) continue;
			
			reqList.add(id);
		}
		
		return reqList;
	}

	@Override
	public List<Long> getAssociatedItemRequest(Long itemId) {
		
		StringBuffer queryStrBuf = new StringBuffer()
			.append("select distinct ar.id from tblrequesthistory rh ")
			.append(" inner join tblrequest r on r.id = rh.requestid ")
			.append(" inner join dct_lks_data requestStageLks on requestStageLks.lks_id = rh.stageid ")
			.append(" inner join tblrequest ar on ar.requestno like r.requestno + '-%' ")
			.append(" where r.itemid = :itemId and rh.current = true and requestStageLks.lkp_value_code in (501, 502, 503) ");
		
		Query q = this.getSession().createSQLQuery(queryStrBuf.toString());
		q.setLong("itemId", itemId);
		
		@SuppressWarnings("unchecked")
		List<Long> requestIds = q.list();
		
		return requestIds;
		
		// itemRequestDAO.deleteRequestList(requestIds);
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RequestDTO> getAssociatedRequestDTO( List<String> requestNos, List<Long> requestStages ) { 
		List<RequestDTO> retval = null;
		Session session = this.getSession();
		
		if (null == requestNos || requestNos.size() == 0) return new ArrayList<RequestDTO>();
		
		StringBuffer queryBuffer = new StringBuffer()
			.append(" select r.id as requestId, r.requestno as requestNo, i.item_id as itemId, i.item_name as itemName ")
			.append(" from tblrequest r ")
			.append(" inner join dct_items i on i.item_id = r.itemid ")
			.append(" inner join tblrequesthistory rh on rh.requestid = r.id ")
			.append(" inner join dct_lks_data rhlks on rhlks.lks_id = rh.stageid ")
			.append(" where rh.current = true and rhlks.lkp_value_code in (:requestStages) ");
		if (requestNos.size() > 0) {
			queryBuffer.append(" and ( ");
		}
		for (String requestNo: requestNos) {
			queryBuffer.append(" r.requestno ilike '")
			.append(requestNo)
			.append("-%'")
			.append(" or ");
		}
		if (requestNos.size() > 0) {
			queryBuffer.append(" false ) ");
		}
			
		Query q = session.createSQLQuery(queryBuffer.toString());
		
		//If minAmspRating is null, return all ups banks
		q.setParameterList("requestStages", requestStages);
		
		q.setResultTransformer(Transformers.aliasToBean(RequestDTO.class));
		retval = (List<RequestDTO>) q.list();

		return retval;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Request> getAssociatedPendingReqsForReq(Request request) {
		List<Long> idsList = getAssociatedRequestIdsForRequest(request);
		
		if(idsList == null || idsList.size() == 0) return (new ArrayList<Request>());
		
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("requestId", idsList));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	criteria.addOrder(Order.asc("requestId"));
		
    	return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Request> getAssociatedPendingReqsForReqs(List<Request> requests) {
		List<Long> idsList = new ArrayList<Long>(); 
		
		for (Request request: requests) {
			List<Long> idsListPerRequest = getAssociatedRequestIdsForRequest(request);
			
			if(idsListPerRequest == null || idsListPerRequest.size() == 0) continue;
			
			idsList.removeAll(idsListPerRequest);
			idsList.addAll(idsListPerRequest);
		}
		
		if (idsList.size() == 0) return (new ArrayList<Request>());
		
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("requestId", idsList));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	criteria.addOrder(Order.asc("requestId"));
		
    	return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getAssociatedPendingReqIdsForReq(Request request) {
		List<Long> idsList = getAssociatedRequestIdsForRequest(request);
		
		if(idsList == null || idsList.size() == 0) return (new ArrayList<Long>());
		
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
    	criteria.add(Restrictions.eq("history.current", true));
    	criteria.add(Restrictions.in("requestId", idsList));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));
    	criteria.addOrder(Order.asc("requestId"));
		
    	ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("requestId"), "requestId");
		criteria.setProjection(proList);
    	
    	return criteria.list();
    	
	}

	@Override
	public Integer getLastAssociatedRequestCount(Request request) {
		
		if (null == request) return null;
		
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.add(Restrictions.like("requestNo", request.getRequestNo() + "-", MatchMode.START));
		criteria.addOrder(Order.desc("requestNo"));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("requestNo"), "requestNo");
		criteria.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<String> requestNos = criteria.list();
		
		if (null == requestNos || requestNos.size() == 0) return 0;
		
		String lastReqNo = requestNos.get(0);
		
		String lastAssocoiatedNumber = lastReqNo.substring(lastReqNo.lastIndexOf("-") + 1);
		
		return new Integer(lastAssocoiatedNumber);
	}

	private final String rTypeList[] = {"Item","Item Remove","Convert to VM", "Item Move"};
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Request> getItemRequest(long itemId) throws DataAccessException { 
		try {
			Session session = this.getSession();
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", pendingReqStages));

	    	return criteria.list();
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}

	private Criteria getRequestCriteria(long itemId, String[] requestTypeList,
			Session session) {
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestPointers", "pointer");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
		criteria.add(Restrictions.eq("history.current", true));
		criteria.add(Restrictions.eq("itemId", itemId));
		
		if (requestTypeList != null){
			criteria.add(Restrictions.in("requestType", requestTypeList));
		}
		
		return criteria;
	}

	
}

