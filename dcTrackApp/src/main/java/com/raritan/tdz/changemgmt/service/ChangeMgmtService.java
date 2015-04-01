package com.raritan.tdz.changemgmt.service;

import java.util.List;

import com.raritan.tdz.changemgmt.dto.RequestDTO;
import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * Client interface to Change Management.
 * @author Andrew Cohen
 * @version 3.0
 */
public interface ChangeMgmtService {

	/**
	 * Submit a request to add a new item.
	 * @param item the item to add
	 * @return
	 */
	public RequestDTO addNewItem(long itemId) throws ServiceLayerException;
	
	/**
	 * Get all tasked assigned to the current user.
	 * The user is obtained from the session.
	 * @return
	 */
	public List<TaskDTO> getMyAssignedTasks() throws ServiceLayerException;
	
	/**
	 * Updates a specific task.
	 * @param task
	 */
	public void updateTask(TaskDTO task) throws ServiceLayerException;
}
