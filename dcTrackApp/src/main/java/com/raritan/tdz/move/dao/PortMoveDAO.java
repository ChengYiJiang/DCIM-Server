package com.raritan.tdz.move.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;

/**
 * 
 * @author bunty
 *
 * @param <T>
 */
public interface PortMoveDAO<T extends Serializable> extends Dao<T> {
	
	/**
	 * get the list of move data for the move item id
	 * @param moveItemId
	 * @return
	 */
	public List<T> getPortMoveData(Long moveItemId);
	
	/**
	 * get the move data for the given item id(s) and move port id 
	 * @param moveItemId
	 * @param origItemId
	 * @param movePortId
	 * @return
	 */
	public T getPortMoveData(Long moveItemId, Long origItemId, Long movePortId);
	
	/**
	 * create the port move data in the DB against the provided data
	 * @param origItem
	 * @param moveItem
	 * @param origPort
	 * @param movePort
	 * @param action
	 * @param request
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public T createPortMoveData(Item origItem, Item moveItem,
			IPortInfo origPort, IPortInfo movePort, LksData action,
			Request request) throws InstantiationException, IllegalAccessException;

	/**
	 * get the original item id against the move item id
	 * @param moveItemId
	 * @return
	 */
	public Long getMovingItemId(Long moveItemId);

	/**
	 * get the move item id against the original item id
	 * @param origItemId
	 * @return
	 */
	public Long getWhenMovedItemId(Long origItemId);

	/**
	 * delete all the move data for a given move item id
	 * @param moveItemId
	 */
	public void deleteMoveData(Long moveItemId);

	/**
	 * get the request list for the move item 
	 * @param moveItemId
	 * @return
	 */
	public List<Request> getRequest(Long moveItemId);

	/**
	 * get the when moved item name
	 * @param origItemId
	 * @return
	 */
	public String getWhenMovedItemName(Long origItemId);

	/**
	 * get the move item name against the when moved item
	 * @param moveItemId
	 * @return
	 */
	public String getMovingItemName(Long moveItemId);

	/**
	 * delete all data that has the given port
	 * @param portId
	 */
	public void deletePortMoveData(Long portId);

	/**
	 * get the action against the move port
	 * @param movePortId
	 * @return
	 */
	public LksData getMovePortAction(Long movePortId);
	
	/**
	 * update connection request for the port on whenMoved item
	 * @param portId
	 * @param req
	 */
	public void setPortRequest(Long portId, Request req);

	/**
	 * get the port move data using request id
	 * @param requestId
	 * @return
	 */
	public T getPortMoveDataUsingRequest(Long requestId);

	/**
	 * get move port action lkp value code
	 * @param movePortId
	 * @return
	  */
	public Long getMovePortActionLkpValueCode(Long movePortId);

	/**
	 * get the list of portId and the corresponding action lksData
	 * @param movePortIds
	 * @return
	 */
	public Map<Long, LksData> getMovePortAction(List<Long> movePortIds);

	/**
	 * get list of item ids of the move item
	 * @return
	 */
	public List<Long> getMovingItems();

	/**
	 * informs if the item is a moved item (-when-moved item)
	 * @param moveItemId
	 * @return
	 */
	public boolean isMovedItem(Long moveItemId);

	/**
	 * get list of item ids of the move item
	 * @return
	 */
	public List<Long> getOrigItems();

	/**
	 * get list of move item Ids against the moving item ids
	 * @param moveItemIds
	 * @return
	 */
	public List<Long> getMovingItemId(List<Long> moveItemIds);

	/**
	 * get list of moving item Ids against the move item ids
	 * @param origItemIds
	 * @return
	 */
	public List<Long> getWhenMovedItemId(List<Long> origItemIds);

}
