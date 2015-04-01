package com.raritan.tdz.controllers;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.changemgmt.home.ChangeMgmtHome;
import com.raritan.tdz.changemgmt.home.workflow.WorkflowHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemHome;


/** 
 * REST unit tests for Change Management and Workflow APIs.
 * @author Andrew Cohen
 * @version 3.0
 */
@Controller
@RequestMapping("/testchangemgmt")
public class ChangeMgmtTestsController {
	
	//private static final Logger log = Logger.getLogger(ChangeMgmtTestsController.class);
	
	@Autowired(required=true)
	private ChangeMgmtHome changeMgmtHome;
	
	@Autowired(required=true)
	@Qualifier("changeMgmtWorkflow")
	private WorkflowHome workFlowHome;
	
	@Autowired(required=true)
	private ItemHome itemHome;
	
	/**
	 * Tests starting a new workflow for installing a new item.
	 * @param itemId
	 * @param response
	 * @return the request No.
	 * @throws Throwable
	 */
	@RequestMapping(value="/testAddNewItem/{userName}/{itemId}", method=RequestMethod.GET)
	public @ResponseBody String testCreateNewItem(@PathVariable String userName, @PathVariable long itemId, HttpServletResponse response) throws Throwable {
		if (!canAccess(response)) return null;
		
		UserInfo user = new UserInfo();
		user.setUserName(userName);
		
		Item item = itemHome.viewItemEagerById( itemId );
		
		Request req = changeMgmtHome.addNewItem(item, user);
		return req != null ? req.getRequestNo() : "0";
	}

	/**
	 * Tests getting tasks assigned to a user.
	 * @param itemId
	 * @param response
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value="/testGetAssignedTasks/{userName}/{groups}", method=RequestMethod.GET)
	public @ResponseBody Object testGetAssignedTasks(@PathVariable String userName, @PathVariable String groups, HttpServletResponse response) throws Throwable {
		if (!canAccess(response)) return null;
		
		UserInfo user = new UserInfo();
		user.setUserName(userName);
		user.setGroupName(groups);
		
		return workFlowHome.getUserTasks( user );
	}
	
	/**
	 * Tests the execution of a specific workflow task.
	 * @param taskId
	 * @param response
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value="/testExecuteTask", method=RequestMethod.POST)
	public @ResponseBody boolean testExecuteTask(@RequestBody String taskId, HttpServletResponse response) throws Throwable {
		if (canAccess(response)) return false;
		executeTask(taskId, null);
		return true;
	}
	
	/**
	 * Tests the execution and approval of a specific task.
	 * @param taskId
	 * @param response
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value="/testExecuteAndApproveTask", method=RequestMethod.POST)
	public @ResponseBody boolean testExecuteAndApproveTask(@RequestBody String taskId, HttpServletResponse response) throws Throwable {
		if (!canAccess(response)) return false;
		executeTask(taskId, true);
		return true;
	}
	
	/**
	 * Tests the execution and rejection of a specific task.
	 * @param taskId
	 * @param response
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value="/testExecuteAndRejectTask", method=RequestMethod.POST)
	public @ResponseBody boolean testExecuteAndRejectTask(@RequestBody String taskId, HttpServletResponse response) throws Throwable {
		if (!canAccess(response)) return false;
		executeTask(taskId, false);
		return true;
	}
	
	/**
	 * Test deleting all workflow definitions and instances.
	 * @param response
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value="/testDeleteAllWorkflows", method=RequestMethod.GET)
	public @ResponseBody boolean testDeleteAllWorkflows(HttpServletResponse response) throws Throwable {
		if (!canAccess(response)) return false;
		
		workFlowHome.deleteAllWorkflows();
		workFlowHome.deployAllWorkflows();
		
		return true;
	}
	
	private void executeTask(String taskId, Boolean approve) throws BusinessValidationException {
		TaskDTO task = new TaskDTO();
		task.setTaskId( taskId );
		task.setApproval( approve );
		task.setIsDone( true );
		
		workFlowHome.updateTask( task );
	}
	
	//
	// Private methods
	//
	
	/**
	 * Allow only access to these test APIs if running in "developer" mode.
	 * @return
	 */
	private boolean canAccess(HttpServletResponse response) {
		if (!Boolean.parseBoolean(System.getProperty("dcTrack.testRest"))) {
			response.setStatus( HttpStatus.SC_UNAUTHORIZED );
			return false;
		}
		return true;
	}
}
