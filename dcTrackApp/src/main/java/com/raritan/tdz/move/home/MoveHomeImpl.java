package com.raritan.tdz.move.home;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.move.dao.PortMoveDAO;

public class MoveHomeImpl implements MoveHome {

	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	@Override
	public void filterMoveCabinets(Criteria criteria) {
		
		List<Long> moveItemIds = powerPortMoveDAO.getMovingItems();
		if (null != moveItemIds && moveItemIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("itemId", moveItemIds)));
		}

	}
	
	@Override
	public List<Long> getExceptionItemIds(Long itemId) {
		
		List<Long> itemIds = new ArrayList<Long>();
		if (itemId > 0) {
			
			itemIds.add(itemId);
			Long otherExceptionItemId = powerPortMoveDAO.getMovingItemId(itemId);
			if (null == otherExceptionItemId) {
				otherExceptionItemId = powerPortMoveDAO.getWhenMovedItemId(itemId);
			}
			if (null != otherExceptionItemId) {
				itemIds.add(otherExceptionItemId);
			}
		}
		
		return itemIds;
		
	}

	@Override
	public List<Long> getExceptionItemIds(List<Long> itemIdList) {
		
		List<Long> itemIds = new ArrayList<Long>();
		
		if (null == itemIdList || itemIdList.size() == 0) return itemIds;
		
		itemIds.addAll(itemIdList);
		List<Long> movingItemIds = powerPortMoveDAO.getMovingItemId(itemIdList);
		if (null != movingItemIds) {
			itemIds.removeAll(movingItemIds);
			itemIds.addAll(movingItemIds);
		}
		List<Long> moveItemIds = powerPortMoveDAO.getWhenMovedItemId(itemIdList);
		if (null != moveItemIds) {
			itemIds.removeAll(moveItemIds);
			itemIds.addAll(moveItemIds);
		}
		
		return itemIds;
		
	}

}
