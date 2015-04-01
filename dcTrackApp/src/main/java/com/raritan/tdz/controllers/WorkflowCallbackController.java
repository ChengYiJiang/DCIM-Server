package com.raritan.tdz.controllers;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.changemgmt.home.ChangeMgmtCallback;

/**
 * <p>REST APIs for internal Change Management operations such
 * as changing state of item. These APIs serve as callbacks
 * for the workflow engine that is executing various Change
 * Management tasks.</p>
 * <p/> 
 * <p>TODO: Add basic security/authentication.</p>
 * 
 * @author Andrew Cohen
 * @version 3.0
 */
@Controller
@RequestMapping("/changemgmt")
public class WorkflowCallbackController {
	
	private final Logger log = Logger.getLogger("ChangeMgmtWorkflowCallback");
	
	@Autowired(required=true)
	private ChangeMgmtCallback changeMgmtHome;
	
	/**
	 * Creates a work order for an Add Item request.
	 * @param requestId the ID of the existing request
	 * @param itemId the item ID
	 * @param dueOn the due date string in yyyy-MM-dd format
	 * @param response
	 * @throws Throwable
	 */
	@RequestMapping(value="/createAddItemWorkOrder/{requestId}/{itemId}/{dueOn}", method=RequestMethod.GET)
	public @ResponseBody WorkOrderJSON createAddItemWorkOrder(@PathVariable long requestId, @PathVariable long itemId, @PathVariable String dueOn, HttpServletResponse response) {
		log.debug("Creating Add Item Work Order");
		WorkOrderJSON json = new WorkOrderJSON();
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final long workOrderId = changeMgmtHome.createAddItemWorkOrder(requestId, itemId, sdf.parse(dueOn));
			json.setWorkOrderId( workOrderId );
		}
		catch (Throwable t) {
			log.error("Error creating Add Item Work Order (itemId = " + itemId + 
					", requestId = " + requestId + ")", t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
		
		return json;
	}
	
	/**
	 * Updates the status on an item.
	 * @param itemId
	 * @param itemStatusLkpValueCode
	 * @param response
	 * @throws Throwable
	 */
	@RequestMapping(value="/updateItemStatus/{itemId}/{itemStatusLkpValueCode}", method=RequestMethod.GET)
	public @ResponseBody void updateItemStatus(@PathVariable long itemId, @PathVariable long itemStatusLkpValueCode, HttpServletResponse response) {
		log.debug("Updating item status");
		try {
			changeMgmtHome.updateItemStatus(itemId, itemStatusLkpValueCode);
		}
		catch (Throwable t) {
			log.error("Error updating item status (itemId = " + itemId + 
					", itemStatusLkpValueCode = "+itemStatusLkpValueCode +")", t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
	}
	
	/**
	 * Completes a given work order.
	 * @param itemId
	 * @param itemStatusLkpValueCode
	 * @param response
	 */
	@RequestMapping(value="/completeWorkOrder/{workOrderId}", method=RequestMethod.GET)
	public @ResponseBody void completeWorkOrder(@PathVariable long workOrderId, HttpServletResponse response) {
		log.debug("Updating item status");
		try {
			changeMgmtHome.completeWorkOrder( workOrderId );
		}
		catch (Throwable t) {
			log.error("Error completing work order with id = " + workOrderId, t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
	}
	
	/**
	 * Updates the stage of a request.
	 * TODO: Pass in UserInfo?
	 * @param requestId
	 * @param response
	 */
	@RequestMapping(value="/updateRequestStage/{requestId}/{requestStageLkpValueCode}", method=RequestMethod.GET)
	public @ResponseBody void updateRequestStage(@PathVariable long requestId, @PathVariable long requestStageLkpValueCode, HttpServletResponse response) {
		log.debug("Updating request stage");
		try {
			changeMgmtHome.updateRequestStage(requestId, requestStageLkpValueCode);
		}
		catch (Throwable t) {
			log.error("Error approving request with id = " + requestId, t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
	}
	
	//
	// Start Private classes and methods
	//
	
	private class WorkOrderJSON {
		private long workOrderId;

		public long getWorkOrderId() {
			return workOrderId;
		}

		public void setWorkOrderId(long workOrderId) {
			this.workOrderId = workOrderId;
		}
	}
}
