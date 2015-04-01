package com.raritan.tdz.changemgmt.home;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.changemgmt.home.workflow.WorkflowHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;

/**
 * <p>This class is the business component for change management operations.</p>
 * <p>It will delegate certain operations to the {@link WorkflowHome} class</p>
 * 
 * @author Andrew Cohen
 * @version 3.0
 */
@Transactional
public interface ChangeMgmtHome {

	/**
	 * Creates a request to add a new item and starts the internal workflow process.
	 * @param item an item in "New" state
	 */
	public Request addNewItem(Item item, UserInfo requestor) throws DataAccessException, SystemException;
	
	/**
	 * Returns the list of all requests.
	 * @return a list of requests
	 */
	public List<Request> getRequests();
	
	/**
	 * Returns a list of all work orders.
	 * @return
	 */
	public List<WorkOrder> getWorkOrders();
}
