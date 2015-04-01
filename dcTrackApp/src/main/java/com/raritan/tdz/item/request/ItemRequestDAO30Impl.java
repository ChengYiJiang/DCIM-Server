/**
 *
 */
package com.raritan.tdz.item.request;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.home.modelchange.ChangeModel;
import com.raritan.tdz.item.home.modelchange.ChangeModelFactory;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestStageHelper;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.RequestDTO;

/**
 * @author prasanna
 * This implementation is only for dcTrack 3.0. In the future, when we have new tables we will be having a  different
 * implementation.
 */
public class ItemRequestDAO30Impl implements ItemRequestDAO {

	@Autowired
	protected ChangeModelFactory changeModelFactory;
	
	@Autowired
	protected ItemDAO itemDAO;
	
	@Autowired(required=true)
	private LksCache lksCache;

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	@Autowired(required=true)
	private CircuitRequest circuitRequest;

	private List<RequestStageHelper> requestIssueUpdateComments;
	
	private SessionFactory sessionFactory;

	private Logger log = Logger.getLogger(ItemRequest.class);

	private final String rTypeList[] = {"Item","Item Remove","Convert to VM", "Item Move"};
	private final String itemToStorageSuffix = "-TO-STORAGE";

	ItemRequestDAO30Impl(SessionFactory sessionFactory,
			List<RequestStageHelper> requestIssueUpdateComments) {
		this.sessionFactory = sessionFactory;
		this.requestIssueUpdateComments = requestIssueUpdateComments;
		
	}


	//Here we don't throw DataAccessException because we capture that and append them to errors
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public Map<Long, Long> insertRequests(List<Long> itemIds, String requestType, String requestDescPrefix, Errors errors, boolean disconnect, Long newStatusValueCode, boolean processChildren)
			throws DataAccessException {

		Map<Long, Long> requestIdMap = new HashMap<Long,Long>();

		boolean itemNotFound = false;
		boolean unexpectedError = false;
		long requestId;
		boolean pendingConnectReqs;
		
		//For each item let us process the request
		for (Long itemId : itemIds){
			Item item = null;
			pendingConnectReqs = false;
			
			try {
				item = itemDAO.loadItem(itemId);

				if(disconnect) { //disconnect all circuits associated with this item
					pendingConnectReqs = circuitRequest.hasConnectionRequests(itemId, errors);
				}
				
				if(pendingConnectReqs) continue;
				
				requestId = submitRequest(requestType, requestDescPrefix, itemId, item, newStatusValueCode);
			
				if(disconnect) { //disconnect all circuits associated with this item
					circuitRequest.disconnectAllRequest(requestId, requestType);
				}
				
				requestIdMap.put(itemId, requestId);

				//do children request
				if(processChildren){
					Session session = this.sessionFactory.getCurrentSession();
					Request request = (Request)session.get(Request.class, requestId);

					List<Long> childrens = getChildrenItemIds(request.getItemId());

					Long requestTypeLkpCode =  (null != request.getRequestTypeLookup()) ? request.getRequestTypeLookup().getLkpValueCode() : null;
					Map<Long,Boolean> pendingReqs = isPendingRequests(childrens, requestTypeLkpCode, errors);

					for(Long childId:childrens){
						pendingConnectReqs = false;
						
						if(pendingReqs.get(childId)) continue;

						if(disconnect) { //disconnect all circuits associated with this item
							pendingConnectReqs = circuitRequest.hasConnectionRequests(childId, errors);
						}
						
						if(pendingConnectReqs) continue;
						
						Item child = itemDAO.loadItem(childId);
						requestId = submitRequest(requestType, requestDescPrefix, childId, child, newStatusValueCode);						
						
						if(disconnect) { //disconnect all circuits associated with this item
							circuitRequest.disconnectAllRequest(requestId, requestType);
						}	
						requestIdMap.put(childId, requestId);
					}
				}
			} catch (HibernateException he){
				//If one of the item is not found, we cannot get the name of that item anyways, so
				//we will append one general message for the user at the end.
				itemNotFound = true;
			} catch (DataAccessException da){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.submitFailedOnItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
			} catch (Throwable t){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.submitFailedOnItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
				t.printStackTrace();
			}
		}

		//If there are errors then append them into the error object to display a generic message to user.
		if (itemNotFound || unexpectedError){
			errors.reject("itemRequest.submitFailed.multiple", "Could not submit request for item");
		}

		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequestDAO#insertOrUpdateRequests(java.util.List, org.springframework.validation.Errors)
	 */
	@Override
	public void insertOrUpdateRequests(List<Request> requests, Errors errors)
			throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();

		for(Request r:requests){
			session.saveOrUpdate(r);
		}

	}

	@Override
	public void insertOrUpdateRequest(Request request, Errors errors)
			throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(request);

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequestDAO#getRequest(java.util.List, org.springframework.validation.Errors)
	 */
	@Override
	public Map<Long, List<Request>> getRequest(List<Long> itemIds, Errors errors)
			throws DataAccessException {
		Map<Long,List<Request>> resultMap = new HashMap<Long, List<Request>>();

		boolean itemNotFound = false;
		boolean unexpectedError = false;

		//Loop through each item id to get the request
		for (Long itemId:itemIds){
			Item item = null;
			try {
				item = itemDAO.loadItem(itemId);
				resultMap.put(itemId,getItemRequest(itemId));
			}  catch (HibernateException he){
				//If one of the item is not found, we cannot get the name of that item anyways, so
				//we will append one general message for the user at the end.
				itemNotFound = true;
			} catch (DataAccessException da){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
			} catch (Throwable t){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
			}
		}

		//If there are errors then append them into the error object to display a generic message to user.
		if (itemNotFound || unexpectedError){
			errors.reject("itemRequest.noReqests", "Could not submit request for item");
		}

		return resultMap;

	}


	@Override
	public List<Request> getRequest(Long itemId, Errors errors)
			throws DataAccessException {
		List<Request> requests = new ArrayList<Request>();
		Item item = null;
		try {
			item = itemDAO.loadItem(itemId);
			requests = getItemRequest(itemId);
		}  catch (HibernateException he){
			errors.reject("itemRequest.noReqests", "Could not submit request for item");
		} catch (DataAccessException da){
			//If there are other kinds of errors other than the Hibernate exception we process them here
			if (item != null){
				//Capture this in error object
				Object errorArgs[] = {item.getItemName()};
				errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
			} else {
				errors.reject("itemRequest.noReqests", "Could not submit request for item");
			}
		} catch (Throwable t){
			//If there are other kinds of errors other than the Hibernate exception we process them here
			if (item != null){
				//Capture this in error object
				Object errorArgs[] = {item.getItemName()};
				errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
			} else {
				errors.reject("itemRequest.noReqests", "Could not submit request for item");
			}
		}

		return requests;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<RequestDTO> getRequestDTO( List<Long> itemIds, List<Long> requestStages ) { 
		List<RequestDTO> retval = null;
		Session session = this.sessionFactory.getCurrentSession();
		
		Query q = session.createSQLQuery(" select r.id as requestId, r.requestno as requestNo, i.item_id as itemId, i.item_name as itemName " + 
										" from tblrequest r "  + 
										" inner join dct_items i on i.item_id = r.itemid " + 
										" inner join tblrequesthistory rh on rh.requestid = r.id " + 
										" inner join dct_lks_data rhlks on rhlks.lks_id = rh.stageid " + 
										" where itemid in (:itemIds) and rh.current = true and rhlks.lkp_value_code in (:requestStages) ");
		//If minAmspRating is null, return all ups banks
		q.setParameterList("itemIds", itemIds);
		q.setParameterList("requestStages", requestStages);
		
		q.setResultTransformer(Transformers.aliasToBean(RequestDTO.class));
		retval = (List<RequestDTO>) q.list();

		return retval;
	}

	@Override
	public Map<Long, List<Request>> getRequest(List<Long> itemIds, List<Long> requestStageFilters, Errors errors)
			throws DataAccessException {
		Map<Long,List<Request>> resultMap = new HashMap<Long, List<Request>>();

		boolean itemNotFound = false;
		boolean unexpectedError = false;

		//Loop through each item id to get the request
		for (Long itemId:itemIds){
			Item item = null;
			try {
				item = itemDAO.loadItem(itemId);
				resultMap.put(itemId,getItemRequest(itemId,requestStageFilters));
			}  catch (HibernateException he){
				//If one of the item is not found, we cannot get the name of that item anyways, so
				//we will append one general message for the user at the end.
				itemNotFound = true;
			} catch (DataAccessException da){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
			} catch (Throwable t){
				//If there are other kinds of errors other than the Hibernate exception we process them here
				if (item != null){
					//Capture this in error object
					Object errorArgs[] = {item.getItemName()};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
				} else {
					unexpectedError = true;
				}
			}
		}

		//If there are errors then append them into the error object to display a generic message to user.
		if (itemNotFound || unexpectedError){
			errors.reject("itemRequest.noReqests.multiple", "Could not submit request for item");
		}

		return resultMap;
	}

	@Override
	public List<Request> getRequest(Long itemId,
			List<Long> requestStageFilters, Errors errors)
			throws DataAccessException {
		List<Request> requests = new ArrayList<Request>();

		Item item = null;
		try {
			item = itemDAO.loadItem(itemId);
			requests = getItemRequest(itemId,requestStageFilters);
		}  catch (HibernateException he){
			errors.reject("itemRequest.noReqests.single", "Could not submit request for item");
		} catch (DataAccessException da){
			//If there are other kinds of errors other than the Hibernate exception we process them here
			if (item != null){
				//Capture this in error object
				Object errorArgs[] = {item.getItemName()};
				errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
			} else {
				errors.reject("itemRequest.noReqests.single", "Could not submit request for item");
			}
		} catch (Throwable t){
			//If there are other kinds of errors other than the Hibernate exception we process them here
			if (item != null){
				//Capture this in error object
				Object errorArgs[] = {item.getItemName()};
				errors.reject("itemRequest.noRequestsForItem", errorArgs, "Could not submit request for item");
			} else {
				errors.reject("itemRequest.noReqests.single", "Could not submit request for item");
			}
		}

		return requests;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequestDAO#deleteRequests(java.util.List, org.springframework.validation.Errors)
	 */
	@Override
	public List<Long> deleteRequests(List<Request> requests, Errors errors)
			throws DataAccessException {

		List<Long> requestIds = new ArrayList<Long>();
		
		if (null == requests) return requestIds;
		
		for (Request request: requests) {
			deleteRequest(request, errors);
			requestIds.add(request.getRequestId());
		}
		
		return requestIds;
	}

	@Override
	public void deleteRequest(Request request, Errors errors)
			throws DataAccessException {
		
		if (null == request) return;
		
		deleteRequest(request.getRequestId());

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequestDAO#isPendingRequest(java.util.List, org.springframework.validation.Errors)
	 */
	@Override
	public Map<Long, Boolean> isPendingRequests(List<Long> itemIds, Long requestType, Errors errors)
			throws DataAccessException {
		Map<Long,Boolean> resultMap = new HashMap<Long, Boolean>();

		for (Long itemId:itemIds){
			Boolean result = isPendingRequest(itemId, requestType, errors);
			resultMap.put(itemId, result);
		}

		return resultMap;
	}

	@Override
	public Boolean isPendingRequest(Long itemId, Long requestType, Errors errors)
			throws DataAccessException {
		//Don't use this function for connection, it will not work
		
		if (isWhenMovedItem(itemId) == true) {
			// operation not allowed for whenMoved item
			String itemName = itemDAO.getItemName(itemId);
			Object errorArgs[] = {itemName};
			errors.reject("ItemMoveValidator.NotSupported", errorArgs, "Could not submit request for item");
			return true;
		}
		
		Boolean result = false;
		RequestHistory request = null;
		try {
			request = getCurrentHistory(itemId);
		} catch(DataAccessException da) {
			//As we are testing if there is a request existing for a givien item
			//we do not need to process this exception, since if it is not existing
			//there is no request

			log.debug("isPendingRequest for id = " + itemId + ": No request found");
			result = true;
		}

		if (request != null){
			try {
				if (request.getStageIdLookup() != null && request.getStageIdLookup().getLkpTypeName().equals(SystemLookup.LkpType.REQUEST_STAGE)
						&& request.getStageIdLookup().getLkpValueCode() >= SystemLookup.RequestStage.REQUEST_ISSUED
						&& request.getStageIdLookup().getLkpValueCode() <= SystemLookup.RequestStage.WORK_ORDER_COMPLETE){
					String requestNumber = request.getRequestDetail().getRequestNo();
					String requestDescription = request.getRequestDetail().getDescription();
					Item item = itemDAO.loadItem(itemId);
					String errorCode = "itemRequest.pendingRequest.sameRequestType";
					if (null != requestType &&
							null != request.getRequestDetail().getRequestTypeLookup() &&
							!requestType.equals(request.getRequestDetail().getRequestTypeLookup().getLkpValueCode())) {
						errorCode = "itemRequest.pendingRequest.differentRequestType";
					}
					Object errorArgs[] = {item.getItemName(), requestNumber, requestDescription};
					errors.reject(errorCode, errorArgs, "Could not submit request for item");
					result = true;
				}
			} catch (HibernateException e){
				errors.reject("itemRequest.pendingRequest.itemNotFound", null, "Could not find item");
				result = true;
			}
		}

		return result;
	}


	@Override
	public RequestHistory getCurrentHistory(Long itemId) throws DataAccessException {
		//Don't use this for connection, it will not work
		RequestHistory requestHistory = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			//This criteria will only get the max requestId
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
			criteria.setProjection(Projections.max("requestId"));
			Long requestId = (Long) criteria.uniqueResult();

			if (null != requestId) {
				//This will actually load the Request. Did this to avoid nested query!
				//Don't add any other condition here, only need requestId and current
				Criteria historyCriteria = session.createCriteria(RequestHistory.class);
				historyCriteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				historyCriteria.createAlias("requestDetail", "request");
				historyCriteria.add(Restrictions.eq("request.requestId", requestId));
				historyCriteria.add(Restrictions.eq("current", true));
				requestHistory = (RequestHistory) historyCriteria.uniqueResult();
			}

		} catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}

		return requestHistory;
	}
	
	@Override
	public RequestHistory getCurrentHistoryUsingRequest(Long requestId) throws DataAccessException {
		//Don't use this for connection, it will not work
		RequestHistory requestHistory = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();

			if (null != requestId) {
				//This will actually load the Request. Did this to avoid nested query!
				//Don't add any other condition here, only need requestId and current
				Criteria historyCriteria = session.createCriteria(RequestHistory.class);
				historyCriteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				historyCriteria.createAlias("requestDetail", "request");
				historyCriteria.add(Restrictions.eq("request.requestId", requestId));
				historyCriteria.add(Restrictions.eq("current", true));
				requestHistory = (RequestHistory) historyCriteria.uniqueResult();
			}

		} catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}

		return requestHistory;
	}


	@Override
	public Request getLatestRequest(Long itemId) throws DataAccessException {
		Request request = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			//This criteria will only get the max requestId
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
			criteria.setProjection(Projections.max("requestId"));
			Long requestId = (Long) criteria.uniqueResult();

			if (requestId != null){
				//This will actually load the Request. Did this to avoid nested query!
				request = (Request)session.get(Request.class, requestId);
			}
		} catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}

		return request;
	}

	@Override
	public Request getLatestPortRequest(Long portId, String tableName) throws DataAccessException {
		
		Request request = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			//This criteria will only get the max requestId
			Criteria criteria = getCurrentPortRequestCriteria(portId, tableName, session);
			criteria.setProjection(Projections.max("requestId"));
			Long requestId = (Long) criteria.uniqueResult();

			if (requestId != null){
				//This will actually load the Request. Did this to avoid nested query!
				request = (Request)session.get(Request.class, requestId);
			}
		} catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}

		return request;
		
	}

	@Override
	public List<RequestDTO> getRequestDTOForItems(List<Long> itemIds) {
		if(itemIds == null || itemIds.size() == 0) return new ArrayList<RequestDTO>();
		
		Session session = sessionFactory.getCurrentSession();
		StringBuffer query = new StringBuffer();
		query.append("select distinct request.requestId as requestId, request.requestNo as requestNo");
		query.append(", stageIdLookup.lkpValue as requestStage, stageIdLookup.lkpValueCode as requestStageLksCode ");
		query.append(", request.itemId as itemId, request.requestType as requestType  ");
		query.append("from Request request join request.requestHistories histories join histories.stageIdLookup stageIdLookup");
		query.append(" where histories.current = :currentHistory and request.itemId in( :itemIds ) and request.requestType in( :requestTypes )");

		Query requestQuery = session.createQuery(query.toString());
		requestQuery.setBoolean("currentHistory", true);
		requestQuery.setParameterList("requestTypes", rTypeList);
		requestQuery.setParameterList("itemIds", itemIds);

		requestQuery.setResultTransformer(Transformers.aliasToBean(RequestDTO.class));
		@SuppressWarnings("unchecked")
		List<RequestDTO> requestDTOs = requestQuery.list();

		//This following code is totally unnecessary if we had Item domain object referred in Request domain object
		//We unfortunately use a itemId instead of a proper hibernate mapping to item class.
		//There are a number of places we use the Request domain object and dont want to break them all!
		//Thus this following UGLY code :-(

		fillItemNameToRequestDTO(itemIds, session, requestDTOs);

		return requestDTOs;
	}



	@Override
	public List<RequestDTO> getRequestDTOForRequests(List<Long> requestIds) {
		if(requestIds == null || requestIds.size() == 0) return new ArrayList<RequestDTO>();
		
		Session session = sessionFactory.getCurrentSession();
		StringBuffer query = new StringBuffer();
		query.append("select distinct request.requestId as requestId, request.requestNo as requestNo");
		query.append(", stageIdLookup.lkpValue as requestStage, stageIdLookup.lkpValueCode as requestStageLksCode ");
		query.append(", request.itemId as itemId, request.requestType as requestType ");
		query.append("from Request request join request.requestHistories histories join histories.stageIdLookup stageIdLookup");
		query.append(" where request.requestId in( :requestIds ) and histories.current = true");

		Query requestQuery = session.createQuery(query.toString());
		requestQuery.setParameterList("requestIds", requestIds);

		requestQuery.setResultTransformer(Transformers.aliasToBean(RequestDTO.class));
		List<RequestDTO> requestDTOs = requestQuery.list();
		List<Long> itemIds = new ArrayList<Long>();
		for (RequestDTO requestDTO: requestDTOs){
			itemIds.add(requestDTO.getItemId());
		}

		//This following code is totally unnecessary if we had Item domain object referred in Request domain object
		//We unfortunately use a itemId instead of a proper hibernate mapping to item class.
		//There are a number of places we use the Request domain object and dont want to break them all!
		//Thus this following UGLY code :-(

		fillItemNameToRequestDTO(itemIds, session, requestDTOs);
		return requestDTOs;
	}

	// ------------------------- private methods -------------------------------------

	//This following code is totally unnecessary if we had Item domain object referred in Request domain object
	//We unfortunately use a itemId instead of a proper hibernate mapping to item class.
	//There are a number of places we use the Request domain object and dont want to break them all!
	//Thus this following UGLY code :-(
	private void fillItemNameToRequestDTO(List<Long> itemIds, Session session,
			List<RequestDTO> requestDTOs) {
		if(itemIds == null || itemIds.size() == 0) return;
		
		Criteria criteriaItems = session.createCriteria(Item.class);
		ProjectionList proListItems = Projections.projectionList();
		proListItems.add(Projections.property("itemId"));
		proListItems.add(Projections.property("itemName"));

		criteriaItems.setProjection(proListItems);
		criteriaItems.add(Restrictions.in("itemId", itemIds));
		criteriaItems.setResultTransformer(Transformers.TO_LIST);

		@SuppressWarnings("unchecked")
		List<List<Object>> items = criteriaItems.list();
		HashMap<Long, String> itemNameMap = new HashMap<Long, String>();
		for (List<Object> item:items){
			itemNameMap.put((Long)item.get(0), (String)item.get(1));
		}


		for (RequestDTO requestDTO:requestDTOs){
			requestDTO.setItemName(itemNameMap.get(requestDTO.getItemId()));
		}
	}

	@Override
	public List<Request> getItemRequest(long itemId) throws DataAccessException {
		return getAllRequestForAnItem (itemId, rTypeList);
	}
	
	@Override
	public List<Request> getAllRequestForAnItem(long itemId, String[] requestTypes) throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = getRequestCriteria(itemId, requestTypes, session);

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
	public List<Request> getPendingItemRequests(long itemId) throws DataAccessException {
		
		return getItemRequest(itemId, Arrays.asList(SystemLookup.RequestStage.REQUEST_APPROVED, 
				SystemLookup.RequestStage.REQUEST_ISSUED,
				SystemLookup.RequestStage.REQUEST_UPDATED,
				SystemLookup.RequestStage.REQUEST_REJECTED,
				SystemLookup.RequestStage.WORK_ORDER_ISSUED,
				SystemLookup.RequestStage.WORK_ORDER_COMPLETE));
		
	}
	
	private List<Request> getItemRequest(long itemId, List<Long> requestStageValueCodes) throws DataAccessException { 
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStageValueCodes));

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
	public boolean itemRequestExistInStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException { // Bunty
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStageValueCodes));

	    	ProjectionList proListStages = Projections.projectionList();
	    	proListStages.add(Projections.rowCount());

			criteria.setProjection(proListStages);
	    	
	    	return ((long) criteria.uniqueResult() > 0);
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
	}
	
	@Override
	public List<Long> getItemRequestStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException { // Bunty
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStageValueCodes));

	    	ProjectionList proListStages = Projections.projectionList();
	    	proListStages.add(Projections.property("historyStages.lkpValueCode"));

			criteria.setProjection(proListStages);
	    	
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

	private Criteria getCurrentPortRequestCriteria(long portId, String tableName, 
			Session session) {
		
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestPointers", "pointer");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
		criteria.add(Restrictions.eq("history.current", true));
		criteria.add(Restrictions.eq("pointer.recordId", portId));
		criteria.add(Restrictions.eq("pointer.tableName", tableName));
	
		return criteria;
	}
	
	private StringBuffer getDescription(String requestDescPrefix, Item item){
		StringBuffer reqDesc = new StringBuffer();
		reqDesc.append(requestDescPrefix);
		reqDesc.append(": ");
		reqDesc.append(item.getItemName());
		return reqDesc;
	}

	@Override
	public LksData loadLks(Long lkpValueCode) {
		return SystemLookup.getLksData(sessionFactory.getCurrentSession(), lkpValueCode);
	}


	private long submitRequest(String requestType, String requestDescPrefix, Long itemId, Item item, Long statusValueCode) throws DataAccessException, Throwable {
		String reqDesc = getDescription(requestDescPrefix, item).toString();
		String tableName = "tblEntity";
		String reqType = requestType;
		//String requestNo;
		Session session = null;
		Request req = null;
		int sortOrder = 1;

		try {
			session = this.sessionFactory.getCurrentSession();

			//requestNo = getNextRequestNo();

			req = new Request();
			req.setDescription( reqDesc );
			req.setItemId(itemId);
			req.setLocationId( item.getDataCenterLocation().getDataCenterLocationId() );
			//req.setRequestNo(requestNo);
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
			/*if(tableName.equals("tblItemsToMove")){
				pointer = new RequestPointer();
				pointer.setRecordId(item.getItemId());
				pointer.setTableName("tblEntity");
	        	pointer.setRequestDetail(req);
	        	pointer.setSortOrder(sortOrder++);
	        	plist.add(pointer);
			}*/

			if(statusValueCode != null){
				pointer = new RequestPointer();
				pointer.setRecordId(SystemLookup.getLksDataId(session, statusValueCode));
				pointer.setTableName("tlksItemStatus");
	        	pointer.setRequestDetail(req);
	        	pointer.setSortOrder(sortOrder++);
	        	plist.add(pointer);
			}

	        //force to save the request pointer first
	        req.setRequestPointers(plist);
	        session.saveOrUpdate(req);
	        session.flush();
	        session.refresh(req);

	        //Create request history record
	        RequestHistory hist = createReqHist(req, session, SystemLookup.RequestStage.REQUEST_ISSUED);
	        hlist.add(hist);

	        //save request history last, force trigger to fire in DB, Pointer record should be available
	        req.setRequestHistories(hlist);
	        session.saveOrUpdate(req);
	        session.flush();
	        
	        updateRequestComment(req);
	        /*session.saveOrUpdate(req);
	        session.flush();*/
	        
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_SAVE_FAIL, this.getClass(), e));
		}

        return req.getRequestId();
	}


	//HELPER FUNCTIONS
	private void updateRequestComment(Request req) throws Throwable {
		List<Long> noCommentForRequests = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, 
        		SystemLookup.RequestTypeLkp.ITEM_POWER_ON);
        
        if (!noCommentForRequests.contains(req.getRequestTypeLookupCode())) { 
        
        	for (RequestStageHelper updateReqComment: requestIssueUpdateComments) {
        		updateReqComment.update(req, req.getRequestTypeLookup().getLkpValueCode(), null);
        	}
        	
        }
	}
	
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

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void reSubmitRequest(Long requestId) throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();

    		Request req = loadRequest(requestId);
    		
			if(req == null){  //request not found
				return;
			}

			String requestNo1 = req.getRequestNo();
			String requestNo2 = req.getRequestNo() + "-%";
			
			/*
			if(requestNo.endsWith("-RX")){ //Then its a Disconnect or Reconnect request then upate both
				requestNoList.add(requestNo.substring(0, requestNo.lastIndexOf("X")) + "-DX");
			}
			else if(requestNo.endsWith("-DX")){ //Then its a Disconnect or Reconnect request then upate both
				requestNoList.add(requestNo.substring(0, requestNo.lastIndexOf("X")) + "-RX");
			}
			*/
			List<Long> requestStages = new ArrayList<Long>();
			requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
			requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
			requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
			
			Criteria criteria = session.createCriteria(RequestHistory.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
	    	criteria.createAlias("requestDetail", "request");
	    	criteria.createAlias("stageIdLookup", "historyStages");
	    	criteria.add(Restrictions.eq("current", true));
	    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStages));
	    	criteria.add(Restrictions.or(Restrictions.eq("request.requestNo", requestNo1), Restrictions.like("request.requestNo", requestNo2)));

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
			}
		}
		catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_DEL_FAIL, this.getClass(), e));
		}
	}

	@Override
	public Request loadRequest(Long requestId) {
		Session session = this.sessionFactory.getCurrentSession();

    	Criteria criteria = session.createCriteria(Request.class);
    	criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
    	criteria.createAlias("requestPointers", "pointer");
    	criteria.createAlias("requestHistories", "hist");
    	criteria.add(Restrictions.eq("archived", false));
    	criteria.add(Restrictions.eq("hist.current", true));
		criteria.add(Restrictions.eq("requestId", requestId));
		return (Request) criteria.uniqueResult();
	}
	//requestIdMap = requestDAO.insertRequests(itemIds, "Item", "Power off", errors, false, SystemLookup.ItemStatus.POWERED_OFF);


	private List<Long> getChildrenItemIds(long itemId){
		Session session = this.sessionFactory.getCurrentSession();

		String exCludeClasses = " and dct_lks_data.lkp_value_code not in (" + SystemLookup.Class.PASSIVE + ") ";

		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id where i.parent_item_id = :itemId ")
		.append(exCludeClasses)
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id  where it.chassis_id = :itemId ")
		.append(exCludeClasses)
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_items as chassis on it.chassis_id = chassis.item_id  inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id where chassis.parent_item_id = :itemId ")
		.append(exCludeClasses)
		.append(" order by item_id")
		.toString()
	    );

		q.setLong("itemId", itemId);

		List<Long> recList = new ArrayList<Long>();

		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}

		return recList;
	}

	@Override
	public Long createVmItem(Long itemId) throws BusinessValidationException {
		Session session = this.sessionFactory.getCurrentSession();
		UserInfo user = FlexUserSessionContext.getUser();
		
		//Get current item information
		Item item = itemDAO.read(itemId);//itemDAO.itemDAO.loadItem(itemId);
		String userName = item.getItemServiceDetails().getSysCreatedBy();
		String itemName = item.getItemName();

		//if userName is null, Postgres complaint about finding correct store procedure
		userName = user == null ? userName : user.getUserName();
		
		//create VM item using clone store procedure
		CloneItemDTO dto = new CloneItemDTO();
		dto.setIncludeCustomFieldData(true);
		dto.setIncludePowerPort(false);
		dto.setItemIdToClone(itemId);
		
		Long firstItemId = itemDAO.cloneItem(dto, userName);

		session.clear();
		
		//Convert clone item into a VM
		ItItem newItem = (ItItem) itemDAO.loadItem(firstItemId);
		newItem.setItemName(itemName);
		newItem.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE) );
		newItem.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		newItem.setNumPorts(0);
		newItem.setLocationReference(null);
		
		itemDAO.update(newItem);

		ChangeModel changeModel = changeModelFactory.getChangeModel(item, newItem);
		changeModel.change(item, newItem);

		//Rename current item		
		item.setItemName(itemName + itemToStorageSuffix);
		itemDAO.update(item);
		itemDAO.update(newItem);
		
		return firstItemId;
	}


	@Override
	public  List<Long> getRequestItemsForItem(List<Long> itemList){
		if(itemList == null || itemList.size() == 0) return new ArrayList<Long>();

    	Session session = this.sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getRequestItemsForItem");
    	query.setParameterList("itemList", itemList);

    	return query.list();
	}
	
	// get the original item id against the whenmoved item id
	@Override
	public Long getMovingItemId(Long newItemId) {
		Session session = this.sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery(new StringBuffer()
		.append("SELECT r.itemid as itemToMoveId FROM tblRequest r ")
		.append(" INNER JOIN tblRequestPointer p ON r.id = p.requestid ")
		.append(" INNER JOIN tblRequestHistory h ON r.id = h.requestid ")
		.append(" INNER JOIN dct_lks_data stagelks ON h.stageid = stagelks.lks_id ")
		.append(" WHERE ")
		.append(" h.current = true ")
		.append(" AND stagelks.lkp_value_code NOT IN (506, 507, 508, 509) ")
		.append(" AND p.\"table\" = 'dct_items-when-move' ")
		.append(" AND p.recordid = :itemId ")
		.toString()
	    );

		q.setLong("itemId", newItemId);
		
		Integer retValue = (Integer) q.uniqueResult();
		Long movedItemId = retValue == null ? null : retValue.longValue();
		
		return movedItemId;
		
	}
	
	// get the whenmoved item id against the original item id
	@Override
	public Long getWhenMovedItemId(Long oldItemId) {
		Session session = this.sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery(new StringBuffer()
		.append("SELECT p.recordid as whenMovedItemId FROM tblRequest r ")
		.append(" INNER JOIN tblRequestPointer p ON r.id = p.requestid ")
		.append(" INNER JOIN tblRequestHistory h ON r.id = h.requestid ")
		.append(" INNER JOIN dct_lks_data stagelks ON h.stageid = stagelks.lks_id ")
		.append(" WHERE ")
		.append(" h.current = true ")
		.append(" AND stagelks.lkp_value_code NOT IN (506, 507, 508, 509) ")
		.append(" AND p.\"table\" = 'dct_items-when-move' ")
		.append(" AND r.itemid = :itemId ")
		.toString()
	    );

		q.setLong("itemId", oldItemId);
		
		Integer retValue = (Integer) q.uniqueResult();
		Long movedItemId = retValue == null ? null : retValue.longValue();
		
		return movedItemId;
		
	}

	@Override
	public void deleteRequest(Long requestId) {
		Session session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from tblRequestPointer using tblRequest where tblRequestPointer.requestid = tblRequest.id and tblRequest.id = :requestId");
		q.setLong("requestId", requestId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblRequestHistory using tblRequest where tblRequestHistory.requestid = tblRequest.id and tblRequest.id = :requestId");
		q.setLong("requestId", requestId);
		q.executeUpdate();

		//Get list of work orders link to request
		//field request.workOrderId is map as a long, not int
		q = session.createSQLQuery("select workorderid from tblrequest request where id = :requestId and workorderid is not null");
		q.setLong("requestId", requestId);
		List<Long> recList = q.list();

		q = session.createSQLQuery("delete from tblRequest where id = :requestId");
		q.setLong("requestId", requestId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " requests");
		}

		//Delete work orders link to request
		for(Object rec:recList){
			//Integer wid = (Integer)rec;
			Integer wid = null;
			if (rec instanceof Long) {
				wid = ((Long) rec).intValue(); 
			}
			else {
				wid = (Integer) rec;
			}
			deleteItemWorkOrder(wid.intValue());
		}
		
		session.flush();
	}

	@Override
	public void deleteRequestList(List<Long> requestIds) {
		Session session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from tblRequestPointer using tblRequest where tblRequestPointer.requestid = tblRequest.id and tblRequest.id in (:requestId)");
		q.setParameterList("requestId", requestIds.toArray());
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblRequestHistory using tblRequest where tblRequestHistory.requestid = tblRequest.id and tblRequest.id in (:requestId)");
		q.setParameterList("requestId", requestIds.toArray());
		q.executeUpdate();

		//Get list of work orders link to request
		//field request.workOrderId is map as a long, not int
		q = session.createSQLQuery("select workorderid from tblrequest request where id in (:requestId) and workorderid is not null");
		q.setParameterList("requestId", requestIds.toArray());
		List<Long> recList = q.list();

		q = session.createSQLQuery("delete from tblRequest where id in (:requestId)");
		q.setParameterList("requestId", requestIds.toArray());
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " requests");
		}

		//Delete work orders link to request
		for(Object rec:recList){
			//Integer wid = (Integer)rec;
			Integer wid = null;
			if (rec instanceof Long) {
				wid = ((Long) rec).intValue(); 
			}
			else {
				wid = (Integer) rec;
			}
			deleteItemWorkOrder(wid.intValue());
		}
		
		session.flush();
	}

	
	private void deleteItemWorkOrder(Integer workOrderId) {
		Session session = sessionFactory.getCurrentSession();

		//check that this work order is only link to this item before delete
		/*Query q = session.createQuery("select request.itemId from Request request where request.itemId != :itemId and request.workOrderId = :workOrderId");
		q.setInteger("itemId", itemId);
		q.setLong("workOrderId", workOrderId);
		List<Integer> recList = q.list();*/

		// if(recList.size() == 0){
			Query q = session.createSQLQuery("delete from tblworkorder where id  = :workOrderId");
			q.setInteger("workOrderId", workOrderId);
			int deleted = q.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("Item Delete: Deleted " + deleted + " work orders");
			}
		// }
	}

	private boolean isWhenMovedItem(Long itemId ) {
		Long id = powerPortMoveDAO.getMovingItemId(itemId);
		return (id != null && id > 0); 
	}
}
