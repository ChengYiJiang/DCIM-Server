/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;

import org.hibernate.Session;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.TypedValue;
import org.springframework.beans.factory.annotation.Autowired;


import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;

import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessorBase;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author prasanna
 *
 */
public class ItemRulesProcessorImpl extends RulesProcessorBase {
	private Map<String, RulesNodeEditability> itemStatusEditability;
	private Map<String, RulesNodeEditability> itemUserRoleEditability;//Key is access level id	
	private Map<String, RulesNodeEditability> itemStageEditability;//Key is item stage (REQUEST_*, WORK_ORDER_*: look for class SystemLookup.RequestStage)
	private RulesNodeEditability itemWhenMovedEditability;
	private RulesNodeEditability itemMovedEditability;
	private ItemRequest itemRequest;
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	
	public ItemRequest getItemRequest() {
		return itemRequest;
	}

	public void setItemRequest(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}

	public Map<String, RulesNodeEditability> getItemStageEditability() {
		return itemStageEditability;
	}

	public void setItemStageEditability(
			Map<String, RulesNodeEditability> itemStageEditability) {
		this.itemStageEditability = itemStageEditability;
	}

	public Map<String, RulesNodeEditability> getItemStatusEditability() {
		return itemStatusEditability;
	}

	public void setItemStatusEditability(
			Map<String, RulesNodeEditability> itemStatusEditability) {
		this.itemStatusEditability = itemStatusEditability;
	}
	
	

	public Map<String, RulesNodeEditability> getItemUserRoleEditability() {
		return itemUserRoleEditability;
	}

	public void setItemUserRoleEditability(
			Map<String, RulesNodeEditability> itemUserRoleEditability) {
		this.itemUserRoleEditability = itemUserRoleEditability;
	}
	
	public RulesNodeEditability getItemWhenMovedEditability() {
		return itemWhenMovedEditability;
	}

	public void setItemWhenMovedEditability(
			RulesNodeEditability itemWhenMovedEditability) {
		this.itemWhenMovedEditability = itemWhenMovedEditability;
	}

	public RulesNodeEditability getItemMovedEditability() {
		return itemMovedEditability;
	}

	public void setItemMovedEditability(RulesNodeEditability itemMovedEditability) {
		this.itemMovedEditability = itemMovedEditability;
	}

	protected RulesNodeEditability getEditability(Criterion criterion) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		RulesNodeEditability itemEditability = new ItemStatusEditability(new HashMap<String, Boolean>());
		Long statusLkpValueCode = getStatusLookup(criterion);
		
		// handle the field editibility for the item state (PLANNED, INSTALED, STORAGE, ARCHIEVED)
		RulesNodeEditability statusEditablility = getStatusEditability(criterion);
		if (statusEditablility != null) {

			itemEditability.mergeNodeEditable(statusEditablility);
		}
		
		// handle the editiablity for all user roles here
		UserInfo userInfo = FlexUserSessionContext.getUser();
		if (null != userInfo) {
			String userStatusKey = userInfo.getAccessLevelId();
			userStatusKey = userStatusKey + ":" + statusLkpValueCode;
			// Frame the bean id using the user role and item state
			if (itemUserRoleEditability != null && itemUserRoleEditability.get(userStatusKey) != null)
				itemEditability.mergeNodeEditable(itemUserRoleEditability.get(userStatusKey));
		}
		
		// handle the editibility for item stage (INSTALL REQUESTED)
		RulesNodeEditability stageEditablility = getStageEditability(criterion);
		if (null != stageEditablility) {
			itemEditability.mergeNodeEditable(stageEditablility);
		}
		
		// handle the editibility of the moved and when-move items
		RulesNodeEditability movingItemEditability = getMovingItemEditability(criterion);
		if (null != movingItemEditability) {
			itemEditability.mergeNodeEditable(movingItemEditability);
		}
		RulesNodeEditability whenMovedItemEditability = getWhenMovedItemEditability(criterion);
		if (null != whenMovedItemEditability) {
			itemEditability.mergeNodeEditable(whenMovedItemEditability);
		}
		
		RulesNodeEditability whenMovedItemStageEditability = getWhenMovedItemStageEditability(criterion);
		if (null != whenMovedItemStageEditability) {
			itemEditability.mergeNodeEditable(whenMovedItemStageEditability);
		}
		
		
		return itemEditability;
	}
	
	@Override
	protected void applyEditability(Criterion criterion) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Long statusLkpValueCode = getStatusLookup(criterion);
		
		// handle the field editibility for the item state (PLANNED, INSTALED, STORAGE, ARCHIEVED)
		RulesNodeEditability statusEditablility = getStatusEditability(criterion);
		if (statusEditablility != null)
			statusEditablility.setNodeEditable(dcTrack);
		
		// handle the editiablity for all user roles here
		UserInfo userInfo = FlexUserSessionContext.getUser();
		if (null != userInfo) {
			String userStatusKey = userInfo.getAccessLevelId();
			userStatusKey = userStatusKey + ":" + statusLkpValueCode;
			// Frame the bean id using the user role and item state
			if (itemUserRoleEditability != null && itemUserRoleEditability.get(userStatusKey) != null)
				itemUserRoleEditability.get(userStatusKey).setNodeEditable(dcTrack);
		}
		
		// handle the editibility for item stage (INSTALL REQUESTED)
		RulesNodeEditability stageEditablility = getStageEditability(criterion);
		if (null != stageEditablility) {
			stageEditablility.setNodeEditable(dcTrack);
		}
		
		// handle the editibility of the moved and when-move items
		RulesNodeEditability movingItemEditability = getMovingItemEditability(criterion);
		if (null != movingItemEditability) {
			movingItemEditability.setNodeEditable(dcTrack);
		}
		RulesNodeEditability whenMovedItemEditability = getWhenMovedItemEditability(criterion);
		if (null != whenMovedItemEditability) {
			whenMovedItemEditability.setNodeEditable(dcTrack);
		}
		
		RulesNodeEditability whenMovedItemStageEditability = getWhenMovedItemStageEditability(criterion);
		if (null != whenMovedItemStageEditability) {
			whenMovedItemStageEditability.setNodeEditable(dcTrack);
		}
	}
	
	private RulesNodeEditability getStatusEditability(Criterion criterion){
		RulesNodeEditability statusEditability = null;
		
		String statusMountingLookupKey = getStatusMountingLookupKey(criterion);
		if (!statusMountingLookupKey.isEmpty()) {
		
			statusEditability = itemStatusEditability.get(statusMountingLookupKey);
			
			//If we did not find status editiblity for Status:class/subclass:mounting, then try for Status:class/subclass.
			if (statusEditability == null && itemStatusEditability != null && getStatusLookupKey(criterion) != null) {
				String statusLookupKey = getStatusLookupKey(criterion);
				
				if (!statusLookupKey.isEmpty()){
					statusEditability = itemStatusEditability.get(statusLookupKey);
					
					//If we did not find status editiblity for Status:class/subclass, then try just for status.
					if (statusEditability == null && itemStatusEditability != null && getStatusLookup(criterion) != null) {
						statusEditability = itemStatusEditability.get(getStatusLookup(criterion).toString());
					}
				}
			}
		}
		
		return statusEditability;
	}
	
	private RulesNodeEditability getStageEditability(Criterion criterion){
		RulesNodeEditability stageEditability = null;
		try {
			String stageLookupKey = getStageLookupKey(criterion);
			if (stageLookupKey != null && !stageLookupKey.isEmpty()){
				stageEditability = itemStageEditability.get(stageLookupKey);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return stageEditability;
	}
	
	private RulesNodeEditability getMovingItemEditability(Criterion criterion){
		
		if (isItemMoving(criterion)) {
			return itemMovedEditability;
		}
		return null;
	}

	private RulesNodeEditability getWhenMovedItemEditability(Criterion criterion){
		
		if (isWhenMovedItem(criterion)) {
			return itemWhenMovedEditability;
		}
		return null;
	}
	
	private RulesNodeEditability getWhenMovedItemStageEditability(Criterion criterion) {
		RulesNodeEditability stageEditability = null;
		
		if (isWhenMovedItem(criterion)) {
			Long movingItemId = getMovingItemId(criterion);
			
			String stageLookupKey = null;
			try {
				
				stageLookupKey = getStageLookupKey(movingItemId);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			if (stageLookupKey != null && !stageLookupKey.isEmpty()){
				
				stageEditability = itemStageEditability.get(stageLookupKey);
			}
			
			
			return stageEditability;
		}
		
		return stageEditability;
	} 

	
	private Long queryRequestStage(long requestStage, Long itemId) throws DataAccessException, BusinessValidationException {
		List<Request> requests = null;

		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(requestStage);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		Map<Long,List<Request>> requestMap = itemRequest.getRequests(itemIds, requestStages, null);
		if (null != requestMap) {
			requests = requestMap.get(itemId);
		}
		if (null != requests && requests.size() > 0) {
			return requestStage;
		}
		return -1L;
	}

	private Long getItemId(Criterion criterion) {
		TypedValue[] typedValue = criterion.getTypedValues(null, null);
		String itemIdStr = typedValue[0].toString();
		Long itemId = -1L;
		if (null != itemIdStr) {
			itemId = new Long(itemIdStr);
		}
		return itemId;
	}
	
	// is this the original item getting moved
	private boolean isItemMoving(Criterion criterion) {
		Long itemId = getItemId(criterion);
		
		Long whenMovedItemId = powerPortMoveDAO.getWhenMovedItemId(itemId);
		return (null != whenMovedItemId && whenMovedItemId > 0);
	}
	
	// is this the original item getting moved
	private boolean isWhenMovedItem(Criterion criterion) {
		Long movingItemId = getMovingItemId(criterion); 
		
		return (null != movingItemId && movingItemId > 0);
	}

	private Long getMovingItemId(Criterion criterion) {
		Long itemId = getItemId(criterion);
		
		Long movingItemId = powerPortMoveDAO.getMovingItemId(itemId);
		return movingItemId;
	}
	
	private String getStageLookupKey(Criterion criterion) throws DataAccessException, BusinessValidationException {
		TypedValue[] typedValue = criterion.getTypedValues(null, null);
		String itemIdStr = typedValue[0].toString();
		Long itemId = -1L;
		if (null != itemIdStr) {
			itemId = new Long(itemIdStr);
		}
		
		return getStageLookupKey(itemId);
		
	}

	private String getStageLookupKey(Long itemId) throws DataAccessException, BusinessValidationException {
		Long verifyRequestStages[] = {
			SystemLookup.RequestStage.REQUEST_APPROVED,
			SystemLookup.RequestStage.WORK_ORDER_ISSUED,
			SystemLookup.RequestStage.WORK_ORDER_COMPLETE,
			SystemLookup.RequestStage.REQUEST_ISSUED,
			SystemLookup.RequestStage.REQUEST_REJECTED,
			SystemLookup.RequestStage.REQUEST_UPDATED
		};
		
		@SuppressWarnings("unchecked")
		List<Long> verifyRequestStageList = Arrays.asList(verifyRequestStages);
		
		List<Long> itemRequestStages = itemRequest.getItemRequestStages(itemId, verifyRequestStageList);
		
		if (null == itemRequestStages || itemRequestStages.size() == 0) return null;
		
		return itemRequestStages.get(0).toString();
		
		/*for (Long requestStage: verifyRequestStages) {
			if (requestStage.longValue() == queryRequestStage(requestStage, itemId).longValue()) {
				return (new Long(requestStage)).toString();
			}
		}
		
		return null;*/
	}
	
	
	private String getStatusMountingLookupKey(Criterion criterion) {
		StringBuffer key = new StringBuffer();
		
		Long statusLookupLkpValueCode = getStatusLookup(criterion);
		Long subClassLookupLkpValueCode = getSubClassLookup(criterion);
		Long classLookupLkpValueCode = getClassLookup(criterion);
		String mounting = getMountingLookup(criterion);
		
		if (statusLookupLkpValueCode != null){
			key.append(statusLookupLkpValueCode);
		}

		if (subClassLookupLkpValueCode != null){
			key.append(":");
			key.append(subClassLookupLkpValueCode);
		} else if (classLookupLkpValueCode != null){
			key.append(":");
			key.append(classLookupLkpValueCode);
		}
		
		if (null != mounting) {
			key.append(":");
			key.append(mounting);
		}
	
		return key.toString();
	}
	
	private String getStatusLookupKey(Criterion criterion){
		StringBuffer key = new StringBuffer();
		
		Long statusLookupLkpValueCode = getStatusLookup(criterion);
		Long subClassLookupLkpValueCode = getSubClassLookup(criterion);
		Long classLookupLkpValueCode = getClassLookup(criterion);
		
		if (statusLookupLkpValueCode != null){
			key.append(statusLookupLkpValueCode);
		}

		if (subClassLookupLkpValueCode != null){
			key.append(":");
			key.append(subClassLookupLkpValueCode);
		} else if (classLookupLkpValueCode != null){
			key.append(":");
			key.append(classLookupLkpValueCode);
		}
	
		return key.toString();
	}

	private Long getStatusLookup(Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria statusCriteria = session.createCriteria(Item.class);
		statusCriteria.createAlias("statusLookup", "statusLookup");
		statusCriteria.setProjection(Projections.property("statusLookup.lkpValueCode"));
		statusCriteria.add(criterion);
		
		Long statusLkpValueCode = (Long) statusCriteria.uniqueResult();
		return statusLkpValueCode;
	}
	
	private Long getClassLookup(Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria statusCriteria = session.createCriteria(Item.class);
		statusCriteria.createAlias("classLookup", "classLookup");
		statusCriteria.setProjection(Projections.property("classLookup.lkpValueCode"));
		statusCriteria.add(criterion);
		
		Long classLkpValueCode = (Long) statusCriteria.uniqueResult();
		return classLkpValueCode;
	}
	
	private Long getSubClassLookup(Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria statusCriteria = session.createCriteria(Item.class);
		statusCriteria.createAlias("subclassLookup", "subclassLookup");
		statusCriteria.setProjection(Projections.property("subclassLookup.lkpValueCode"));
		statusCriteria.add(criterion);
		
		Long subclassLkpValueCode = (Long) statusCriteria.uniqueResult();
		return subclassLkpValueCode;
	}

	private String getMountingLookup(Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria statusCriteria = session.createCriteria(Item.class);
		statusCriteria.createAlias("model", "model");
		statusCriteria.setProjection(Projections.property("model.mounting"));
		statusCriteria.add(criterion);
		
		String mounting = (String) statusCriteria.uniqueResult();
		return mounting;
	}

	
	@Override
	protected UiView getUiView() {
		JXPathContext jxPath = JXPathContext.newContext(dcTrack);
		UiView uiView = (UiView) jxPath.getValue("uiViews/uiView[@uiId='itemView']");
		return uiView;
	}
}
