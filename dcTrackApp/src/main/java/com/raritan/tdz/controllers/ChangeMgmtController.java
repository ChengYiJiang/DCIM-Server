package com.raritan.tdz.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.changemgmt.dto.RequestDTO;
import com.raritan.tdz.changemgmt.dto.WorkOrderDTO;
import com.raritan.tdz.changemgmt.home.ChangeMgmtHome;
import com.raritan.tdz.changemgmt.service.ChangeMgmtService;
import com.raritan.tdz.domain.Request;

/**
 * This controller provides REST APIs for Change Management. 
 * This is parallel access layer to the BlazeDS {@link ChangeMgmtService} class.
 *  
 * @author Andrew Cohen
 * @version 3.0
 */
@Controller
@RequestMapping("/changemgmt")
public class ChangeMgmtController {
	
	private final Logger log = Logger.getLogger("ChangeMgmtREST");
	
	@Autowired(required=true)
	private ChangeMgmtHome changeMgmtHome;
	
	/**
	 * Return a list of requests.
	 * @param workOrderId
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/listRequests", method=RequestMethod.GET)
	public @ResponseBody List<RequestDTO> getRequests(HttpServletResponse response) {
		List<RequestDTO> requests = null;
		log.debug("Listing requests");
		try {
			List<Request> list = changeMgmtHome.getRequests();
			requests = new ArrayList<RequestDTO>( list.size() );
			for (Request req : list) {
				requests.add( new RequestDTO(req) );
			}
		}
		catch (Throwable t) {
			log.error("Error listing requests", t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
		
		if (requests == null) {
			requests = Collections.emptyList();
		}
		
		return requests;
	}
	
	@RequestMapping(value="/listWorkOrders", method=RequestMethod.GET)
	public @ResponseBody List<WorkOrderDTO> getWorkOrders(HttpServletResponse response) {
		List<WorkOrderDTO> workOrders = null;
		log.debug("Listing work orders");
		try {
			List<WorkOrder> results = changeMgmtHome.getWorkOrders();
			if (results != null) {
				workOrders = new ArrayList<WorkOrderDTO>( results.size() );
				for (WorkOrder wo : results) {
					workOrders.add( new WorkOrderDTO(wo) );
				}
			}
		}
		catch (Throwable t) {
			log.error("Error work orders", t);
			response.setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		}
		
		if (workOrders == null) {
			workOrders = Collections.emptyList();
		}
		
		return workOrders;
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
