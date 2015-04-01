package com.raritan.tdz.move.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.birt.chart.extension.aggregate.MovingAverage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.dto.ObjectIdDTO;
import com.raritan.tdz.port.home.InvalidPortObjectException;

/**
 * 
 * @author bunty
 *
 * @param <T>
 */
public class PortMoveDAOImpl<T extends Serializable> extends DaoImpl<T> implements PortMoveDAO<T> {

	protected Class<T> type;
	
	public PortMoveDAOImpl(Class<T> type) {
		super();
		this.type = type;
	}
	
	public Class<T> getType() {
		return type;
	}


	public void setType(Class<T> type) {
		this.type = type;
	}


	@Override
	public List<T> getPortMoveData(Long moveItemId) {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		criteria.createAlias("moveItem", "moveItem");
		criteria.add(Restrictions.eq("moveItem.itemId", moveItemId));
		
		@SuppressWarnings("unchecked")
		List<T> portsMove = criteria.list();
		
		return portsMove;

	}

	@Override
	public T getPortMoveDataUsingRequest(Long requestId) {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		criteria.createAlias("request", "request");
		criteria.add(Restrictions.eq("request.requestId", requestId));
		
		@SuppressWarnings("unchecked")
		T portsMove = (T) criteria.uniqueResult();
		
		return portsMove;

	}
	
	
	private Criteria getPortMoveCriteria(Long moveItemId, Long origItemId,
			Long movePortId) {
		
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName()); 
		if (null != moveItemId) {
			criteria.createAlias("moveItem", "moveItem");
			criteria.add(Restrictions.eq("moveItem.itemId", moveItemId));
		}
		
		if (null != origItemId) {
			criteria.createAlias("origItem", "origItem");
			criteria.add(Restrictions.eq("origItem.itemId", origItemId));
		}
		
		if (null == movePortId) {
			criteria.add(Restrictions.isNull("movePort"));
		}
		else {
			criteria.createAlias("movePort", "movePort");
			criteria.add(Restrictions.eq("movePort.portId", movePortId));
		}

		return criteria;
	}

	private Criteria getPortMoveCriteria(List<Long> moveItemIds, List<Long> origItemIds,
			List<Long> movePortIds) {
		
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName()); 
		if (null != moveItemIds) {
			criteria.createAlias("moveItem", "moveItem");
			criteria.add(Restrictions.in("moveItem.itemId", moveItemIds));
		}
		
		if (null != origItemIds) {
			criteria.createAlias("origItem", "origItem");
			criteria.add(Restrictions.in("origItem.itemId", origItemIds));
		}
		
		if (null == movePortIds) {
			criteria.add(Restrictions.isNull("movePort"));
		}
		else {
			criteria.createAlias("movePort", "movePort");
			criteria.add(Restrictions.in("movePort.portId", movePortIds));
		}

		return criteria;
	}

	@Override
	public T getPortMoveData(Long moveItemId, Long origItemId,
			Long movePortId) {
		
		Criteria criteria = getPortMoveCriteria(moveItemId, origItemId, movePortId);
		
		@SuppressWarnings("unchecked")
		T portMove = (T) criteria.uniqueResult();
		
		return portMove;
		
	}

	@Override
	public T createPortMoveData(Item origItem, Item moveItem,
			IPortInfo origPort, IPortInfo movePort, LksData action,
			Request request) throws InstantiationException, IllegalAccessException {
		
		T moveData = type.newInstance();
		
		setValue(moveData, "origItem", origItem);
		setValue(moveData, "moveItem", moveItem);
		if (null != origPort) {
			setValue(moveData, "origPort", origPort);
		}
		if (null != movePort) {
			setValue(moveData, "movePort", movePort);
		}
		if (null != action) {
			setValue(moveData, "action", action);
		}
		if (null != request) {
			setValue(moveData, "request", request);
		}
		
		create(moveData);
		
		return moveData;
		
	}

	private void setValue(Object port, String methodName, Object value) {
		try {
			PropertyUtils.setProperty(port, methodName, value);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + methodName + ": Internal Error");
		}
	}

	// get the original item id against the whenmoved item id
	@Override
	public Long getMovingItemId(Long moveItemId) {
		Criteria criteria = getPortMoveCriteria(moveItemId, null, null);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("origItem.itemId"), "origItemId");
		criteria.setProjection(proList);
		
		return (Long) criteria.uniqueResult();
	}
	
	// get the original item id against the whenmoved item id
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getMovingItemId(List<Long> moveItemIds) {
		if (null == moveItemIds || moveItemIds.size() == 0) return new ArrayList<Long>();
		Criteria criteria = getPortMoveCriteria(moveItemIds, null, null);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("origItem.itemId"), "origItemId");
		criteria.setProjection(proList);
		
		return criteria.list();
	}
	
	@Override
	public String getMovingItemName(Long moveItemId) {
		Criteria criteria = getPortMoveCriteria(moveItemId, null, null);
		criteria.createAlias("origItem", "origItem");
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("origItem.itemName"), "origItemName");
		criteria.setProjection(proList);
		
		return (String) criteria.uniqueResult();
	}

	@Override
	public Long getWhenMovedItemId(Long origItemId) {
		Criteria criteria = getPortMoveCriteria(null, origItemId, null);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("moveItem.itemId"), "moveItemId");
		criteria.setProjection(proList);
		
		return (Long) criteria.uniqueResult();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getWhenMovedItemId(List<Long> origItemIds) {
		Criteria criteria = getPortMoveCriteria(null, origItemIds, null);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("moveItem.itemId"), "moveItemId");
		criteria.setProjection(proList);
		
		return criteria.list();
		
	}

	@Override
	public String getWhenMovedItemName(Long origItemId) {
		Criteria criteria = getPortMoveCriteria(null, origItemId, null);
		criteria.createAlias("moveItem", "moveItem");
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("moveItem.itemName"), "moveItemName");
		criteria.setProjection(proList);
		
		return (String) criteria.uniqueResult();
		
	}
	
	@Override
	public void deleteMoveData(Long moveItemId) {
		List<T> moveDatas = getPortMoveData(moveItemId);
		
		for (T moveData: moveDatas) {
			delete(moveData);
		}
		getSession().flush();
		
	}
	
	@Override
	public void deletePortMoveData(Long portId) {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		
		criteria.createAlias("movePort", "movePort");
		criteria.createAlias("origPort", "origPort");
		
		Criterion origPortCond = Restrictions.eq("origPort.portId", portId);
		Criterion movePortCond = Restrictions.eq("movePort.portId", portId);
		
		criteria.add(Restrictions.or(origPortCond, movePortCond));
		
		@SuppressWarnings("unchecked")
		List<T> moveDatas = criteria.list();
		
		for (T moveData: moveDatas) {
			delete(moveData);
		}
		getSession().flush();
		
	}
	
	@Override
	public LksData getMovePortAction(Long movePortId) {

		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		
		criteria.createAlias("movePort", "movePort");

		criteria.add(Restrictions.eq("movePort.portId", movePortId));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("action"), "action");
		criteria.setProjection(proList);
		
		return (LksData) criteria.uniqueResult();
	}
	
	@Override
	public Map<Long, LksData> getMovePortAction(List<Long> movePortIds) {

		if (null == movePortIds || movePortIds.size() == 0) return new HashMap<Long, LksData>();
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(type.getName());
		
		criteria.createAlias("movePort", "movePort");
		criteria.add(Restrictions.in("movePort.portId", movePortIds));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("movePort.portId"), "id");
		proList.add(Projections.property("action"), "value");
		criteria.setProjection(proList);
		
		criteria.setResultTransformer(Transformers.aliasToBean(ObjectIdDTO.class));

		@SuppressWarnings("unchecked")
		List<ObjectIdDTO> valueList = criteria.list();
		
		Map<Long, LksData> portIdActionMap = new HashMap<Long, LksData>();
		for (ObjectIdDTO dto: valueList) {
			portIdActionMap.put((Long) (dto.getId()), (LksData) (dto.getValue()));
		}
		
		return portIdActionMap;
	}


	@Override
	public Long getMovePortActionLkpValueCode(Long movePortId) {

		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		
		criteria.createAlias("movePort", "movePort");
		criteria.createAlias("action", "action");

		criteria.add(Restrictions.eq("movePort.portId", movePortId));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("action.lkpValueCode"), "action");
		criteria.setProjection(proList);
		
		return (Long) criteria.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Request> getRequest(Long moveItemId) {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());
		criteria.createAlias("moveItem", "moveItem");
		criteria.add(Restrictions.eq("moveItem.itemId", moveItemId));
		criteria.add(Restrictions.isNotNull("request"));
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("request"), "request");
		criteria.setProjection(proList);
		
		return criteria.list();
	}
	
	@Override
	public void setPortRequest(Long portId, Request req) {
		T moveData = (T)getPortMoveData(null, null, portId);
		if (null != moveData) {
			setValue(moveData, "movePortRequest", req);
			update(moveData);
		}
	}

	@Override
	public List<Long> getMovingItems() {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());

		criteria.add(Restrictions.isNotNull("request"));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("moveItem.itemId"), "moveItemId");
		criteria.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<Long> moveItemIds = criteria.list();
		
		return moveItemIds;
		
	}

	@Override
	public List<Long> getOrigItems() {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(type.getName());

		criteria.add(Restrictions.isNotNull("request"));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("origItem.itemId"), "origItemId");
		criteria.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<Long> origItemIds = criteria.list();
		
		return origItemIds;
		
	}

	@Override
	public boolean isMovedItem(Long moveItemId) {
		Long origItemId = getMovingItemId(moveItemId);
		
		return (null != origItemId);
	}
	
}
