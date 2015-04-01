package com.raritan.tdz.changemgmt.home;

import java.util.Date;

import com.raritan.tdz.controllers.WorkflowCallbackController;
import com.raritan.tdz.exception.DataAccessException;

/**
 * This class provides various callback hooks that are meant to be executed by the workflow engine.
 * 
 * The {@link WorkflowCallbackController} class provides the actual REST API access to the workflow
 * engine, and will call business methods in this class.
 * 
 * @author Andrew Cohen
 * @version 3.0
 */
public interface ChangeMgmtCallback {

	/**
	 * Creates a work order for a new item.
	 * @param requestId the request ID
	 * @param itemId the item ID
	 * @param dueOne the due date that the work order should be completed by
	 * @return the work order ID
	 */
	public long createAddItemWorkOrder(long requestId, long itemId, Date dueOne) throws DataAccessException;
	
	/**
	 * Completes the specified work order.
	 * @param workOrderId the work order ID
	 */
	public void completeWorkOrder(long workOrderId) throws DataAccessException;
	
	/**
	 * Updates the status on an item.
	 * @param itemId the item Id
	 * @param itemStatusLkpValueCode the item status lookup value code
	 * TODO: Annotation for LKS value code validation?
	 */
	public void updateItemStatus(long itemId, long itemStatusLkpValueCode) throws DataAccessException;
	
	/**
	 * Updates the stage of a request.
	 * @param requestId the request ID
	 * @param requestStageLkpValueCode the request stage lookup value code.
	 * @throws DataAccessException
	 */
	public void updateRequestStage(long requestId, long requestStageLkpValueCode) throws DataAccessException;
}
