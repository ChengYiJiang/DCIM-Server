package com.raritan.tdz.changemgmt.home.workflow;

import java.util.List;

import com.raritan.tdz.changemgmt.domain.WorkflowTask;

/**
 * Manages workflow task settings in dcTrack.
 * @author Andrew Cohen
 * @version 3.0
 */
public interface WorkFlowSettingsHome {
	
	/**
	 * @return a list of all work flow tasks
	 */
	public List<WorkflowTask> getWorkflowTasks();
	
}
