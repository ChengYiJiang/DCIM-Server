package com.raritan.tdz.item.rulesengine;

import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class ItemStateMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	private ItemRequest itemRequest;
	private ItemDAO itemDAO;
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	ProjectionList proList;

	public ItemStateMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public ItemRequest getItemRequest() {
		return itemRequest;
	}

	public void setItemRequest(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}
	
	private ItemRequestDAO itemRequestDAO;

	public ItemRequestDAO getItemRequestDAO() {
		return itemRequestDAO;
	}

	public void setItemRequestDAO(ItemRequestDAO itemRequestDAO) {
		this.itemRequestDAO = itemRequestDAO;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {

		Long itemId = (Long)filterValue;
		Long movingItemId = powerPortMoveDAO.getMovingItemId((Long)filterValue);

		Long reqStageFilter = (null != movingItemId && movingItemId > 0) ? movingItemId : itemId; // (Long)filterValue;
		Item item = itemDAO.getItem((null != movingItemId && movingItemId > 0) ? movingItemId : itemId);
		if (null != item.getSubclassLookup() && item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.CONTAINER) {
			Set<Item> children = item.getChildItems();
			if (null != children && children.size() == 1) {
				for (Item child: children) {
					reqStageFilter = child.getItemId();
				}
			}
		}
		
		String remoteEntityName = remoteRef.getRemoteType(uiViewComponent.getUiValueIdField().getRemoteRef());
		
		
		Session session = sessionFactory.getCurrentSession();
		
		Criteria criteria = session.createCriteria(remoteEntityName);
		criteria.createAlias("statusLookup", "statusLookup");
		proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("statusLookup.lkpValueCode"), "statusLkpValueCode"));
		proList.add(Projections.alias(Projections.property("statusLookup.lkpValue"), "statusLkpValue"));
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		criteria.add(Restrictions.eq(filterField, filterValue));
		@SuppressWarnings("unchecked")
		Map<String,Object> statusMap = (Map<String, Object>) criteria.uniqueResult();
		
		/*if (null != movingItemId && movingItemId > 0) {
			Criteria movingCriteria = session.createCriteria(remoteEntityName);
			movingCriteria.createAlias("statusLookup", "statusLookup");
			proList = Projections.projectionList();
			proList.add(Projections.alias(Projections.property("statusLookup.lkpValue"), "statusLkpValue"));
			movingCriteria.setProjection(proList);
			movingCriteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			movingCriteria.add(Restrictions.eq(filterField, movingItemId));
			@SuppressWarnings("unchecked")
			Map<String,Object> movingStatusMap = (Map<String, Object>) movingCriteria.uniqueResult();
			statusMap.put("statusLkpValue", movingStatusMap.get("statusLkpValue"));
		}*/
		
		String historyLkpValue = null;
		LksData historyLksData = itemRequest.getLatestRequestStage((Long)reqStageFilter);
		if (null != historyLksData && 
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_COMPLETE &&
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_ARCHIVED &&
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_ABANDONED) {
			historyLkpValue = historyLksData.getLkpValue();
		}
		
		if (statusMap != null && statusMap.get("statusLkpValueCode") != null){
			uiViewComponent.getUiValueIdField().setValueId((Long)statusMap.get("statusLkpValueCode"));
			String statusVal = statusMap.get("statusLkpValue").toString();
			if (null != historyLkpValue && historyLkpValue.length() > 0) {
				statusVal += " (";
				statusVal += historyLkpValue;
				statusVal += ")";
			}
			uiViewComponent.getUiValueIdField().setValue(statusVal);
		}
	}
	
	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}

}