package com.raritan.tdz.changemgmt.service;

import java.util.List;

import com.raritan.tdz.changemgmt.dto.RequestDTO;
import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.changemgmt.home.ChangeMgmtHome;
import com.raritan.tdz.changemgmt.home.workflow.WorkflowHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.session.FlexUserSessionContext;

/**
 * Change Management Service default implementation. 
 * @author Andrew Cohen
 * @version 3.0
 */
public class ChangeMgmtServiceImpl implements ChangeMgmtService {

	private ChangeMgmtHome changeMgmtHome;
	private WorkflowHome workflowHome;
	private ItemHome itemHome;
	
	public ChangeMgmtServiceImpl(WorkflowHome workflowHome, ItemHome itemHome) {
		this.workflowHome = workflowHome;
		this.itemHome = itemHome;
	}
	
	@Override
	public RequestDTO addNewItem(long itemId) throws ServiceLayerException {
		final Item item = itemHome.viewItemEagerById(itemId);
		final Request request = changeMgmtHome.addNewItem(item, FlexUserSessionContext.getUser());
		final RequestDTO dto = new RequestDTO();
		// TODO: Request domain to DTO mapping
		dto.setRequestNo( request.getRequestNo() );
		return dto;
	}

	@Override
	public List<TaskDTO> getMyAssignedTasks() throws ServiceLayerException {
		return workflowHome.getUserTasks( FlexUserSessionContext.getUser() );
	}

	@Override
	public void updateTask(TaskDTO task) throws ServiceLayerException {
		workflowHome.updateTask( task );
	}
}
