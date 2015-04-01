package com.raritan.tdz.changemgmt.home.workflow;

import java.util.List;

import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;

/**
 * Generic interface to the Change Management workflow engine.
 * @author Andrew Cohen
 * @version 3.0
 */
public interface WorkflowHome {

	/**
	 * Called at application startup to validate and deploy workflows.
	 */
	public void deployAllWorkflows() throws SystemException;
	
	
	/**
	 * Starts a new Change Management workflow for a New Item.
	 * @param user
	 * @param item
	 * @param requestId
	 * @return the workflow instance identifier
	 */
	public String startNewItemWorkflow(UserInfo user, Item item, long requestId) throws SystemException;
	
	/**
	 * Get all workflow tasks assigned to the specified user.
	 * @param user
	 * @return
	 */
	public List<TaskDTO> getUserTasks(UserInfo user);
	
	/**
	 * Update a workflow task.
	 * @param task
	 */
	public void updateTask(TaskDTO task) throws BusinessValidationException;
	
	/**
	 * Removes all workflow definitions and instances.
	 */
	public void deleteAllWorkflows() throws SystemException;
}
