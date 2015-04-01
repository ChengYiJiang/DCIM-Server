package com.raritan.tdz.changemgmt.home.workflow;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BusinessArchiveFactory;

import com.raritan.tdz.annotation.Login;
import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.changemgmt.home.ChangeMgmtWorkflowProcess;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Change Management Workflow implementation using Bonita.
 * @author Andrew Cohen
 * @version 3.0
 */
@Login(
	username="bonita", 
	password="(ManageMyStuff)!", 
	authProviderBeanName="jaasAuthenticationProvider"
)
public class BonitaWorkflow implements WorkflowHome {
	private static final String BAR_PATH = "com/raritan/tdz/workflow/changemgmt/";
	
	private final Logger log = Logger.getLogger("BonitaWorkflow");
	private EnumMap<ChangeMgmtWorkflowProcess, ProcessDefinition> processes;
	
	/**
	 * 
	 * @param settings
	 * @throws SystemException
	 * @throws LoginException
	 */
	public BonitaWorkflow(ApplicationSettings settings) throws SystemException, LoginException {
		processes = new EnumMap<ChangeMgmtWorkflowProcess, ProcessDefinition>(ChangeMgmtWorkflowProcess.class);
	}
	
	@Override
	public void deployAllWorkflows() throws SystemException {
		ManagementAPI mgmt = AccessorUtil.getManagementAPI();
		QueryDefinitionAPI queryAPI = AccessorUtil.getQueryDefinitionAPI();
		
		EnumSet<ChangeMgmtWorkflowProcess> allProcesses = EnumSet.allOf(ChangeMgmtWorkflowProcess.class);
		
		try {
			Set<ProcessDefinition> deployedProcesses = queryAPI.getProcesses();
			
			for (ChangeMgmtWorkflowProcess process : allProcesses) {
				// TODO: Get processGroup and processVersion from Application Settings
				BusinessArchive bizArchive = getBizArchive(process, "", "1.0");
				ProcessDefinition procDef = bizArchive.getProcessDefinition();
				
				validateProcessTasks( procDef );
				
				if (!deployedProcesses.contains(procDef)) {
					mgmt.deploy( bizArchive );
					log.info(procDef.getName() + " was successfully deployed");
				}
				else {
					log.info(procDef.getName() + " has already been deployed");
				}
				
				this.processes.put(process, procDef);
			}
		}
		catch (Throwable t) {
			log.error("Error deploying Change Management Workflow processes", t);
			throw new SystemException( t );
		}
		
		log.info("Successfully deployed change management workflow processes");
	}

	@Override
	public String startNewItemWorkflow(UserInfo user, Item item, long requestId) throws SystemException {
		ProcessInstanceUUID procInstUUID = null;
		RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
		ProcessDefinition procDef = this.processes.get( ChangeMgmtWorkflowProcess.ADD_ITEM );
		
		// TODO: Mapping of item to process variables
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("itemId", item.getItemId());
		variables.put("itemName", item.getItemName());
		variables.put("requestId", requestId);
		variables.put("senderEmailAddr", "raritanbonita@gmail.com");
		variables.put("senderEmailPasswd", "Raritan0$");
		variables.put("receiverEmailAddr", "raritanbonita1@gmail.com");
		String senderEmail = "raritanbonita@raritan.com";
		variables.put("senderUsername",
				senderEmail.substring(0, senderEmail.indexOf("@")));
		variables.put("smtpServerName", "smtp.gmail.com");
		variables.put("smtpServerPort", 465);
		try {
			procInstUUID = runtimeAPI.instantiateProcess(procDef.getUUID(), variables);
		} 
		catch (ProcessNotFoundException e) {
			log.error("Add Item Process not found", e);
			throw new SystemException( e );
		}
		catch (VariableNotFoundException e) {
			log.error("Add Item Process variable not found", e);
			throw new SystemException( e );
		}
		
		if (procInstUUID == null) {
			return null;
		}
		
		return procInstUUID.toString();
	}
	
	@Override
	public List<TaskDTO> getUserTasks(UserInfo user) {
		List<TaskDTO> tasks = new ArrayList<TaskDTO>();
		QueryRuntimeAPI query = AccessorUtil.getQueryRuntimeAPI();
		
		// Get all tasks assigned to the individual user OR any of the user groups that the user belongs to
		String users = user.getGroupName();
		String[] groups = users.split(",");
		List<TaskInstance> userTasks = new LinkedList<TaskInstance>();
		userTasks.addAll( query.getTaskList(user.getUserName(), ActivityState.READY) );
		for (String group : groups) {
			userTasks.addAll( query.getTaskList(group, ActivityState.READY) );
		}
		
		// TODO: Mapping of domain to DTO
		for (TaskInstance task : userTasks) {
			TaskDTO taskDTO = new TaskDTO();
			taskDTO.setTaskId( task.getUUID().toString() );
			taskDTO.setName( task.getActivityName() );
			taskDTO.setIsDone( false );
			taskDTO.setAssignee( user.getUserName() );
			taskDTO.setDueDate( task.getExpectedEndDate() );
			tasks.add( taskDTO );
		}
		
		// Sort by due date
		
		return tasks;
	}
	
	public void updateTask(TaskDTO taskDTO) throws BusinessValidationException {
		final QueryRuntimeAPI query = AccessorUtil.getQueryRuntimeAPI();

		try {
			final TaskInstance task = query.getTask(new ActivityInstanceUUID( taskDTO.getTaskId() ));
			// TODO: May later need to handle reassignment, changing due date, cancellation, etc.
			approveOrRejectTask(taskDTO, task);
			changeTaskState(taskDTO, task);
		}
		catch (TaskNotFoundException e) {
			// TODO: external message
			ExceptionContext ctx = new ExceptionContext(e.getMessage(), this.getClass());
			throw new BusinessValidationException( ctx );
		} 
		catch (IllegalTaskStateException e) {
			// TODO: external message
			ExceptionContext ctx = new ExceptionContext(e.getMessage(), this.getClass());
			throw new BusinessValidationException( ctx );
		}
		catch (InstanceNotFoundException e) {
			// TODO: external message
			ExceptionContext ctx = new ExceptionContext(e.getMessage(), this.getClass());
			throw new BusinessValidationException( ctx );
		}
		catch (VariableNotFoundException e) {
			// TODO: external message
			ExceptionContext ctx = new ExceptionContext(e.getMessage(), this.getClass());
			throw new BusinessValidationException( ctx );
		}
	}
	
	@Override
	public void deleteAllWorkflows() throws SystemException {
		try {
			AccessorUtil.getManagementAPI().deleteAllProcesses();
		} 
		catch (UndeletableInstanceException e) {
			throw new SystemException( e );
		} 
		catch (UndeletableProcessException e) {
			throw new SystemException( e );
		}
	}
	
	//
	// Start private members and methods
	//

	private BusinessArchive getBizArchive(ChangeMgmtWorkflowProcess process, String processGroup, String processVersion) throws Throwable {
		StringBuffer b = new StringBuffer(BAR_PATH);
		if (processGroup != null) {
			b.append(processGroup);
		}
		b.append( WordUtils.capitalizeFully(process.toString().toLowerCase(), new char[]{'_'}) );
		b.append("--");
		b.append(processVersion);
		b.append(".bar");
		
		if (log.isDebugEnabled()) {
			log.debug("Bonita BAR file: " + b.toString());
		}
		
		URL url = Thread.currentThread().getContextClassLoader().getResource( b.toString() );
		return BusinessArchiveFactory.getBusinessArchive( url );
	}
	
	/**
	 * 
	 * @param taskDTO
	 * @param task
	 * @throws IllegalTaskStateException
	 * @throws TaskNotFoundException
	 */
	private void changeTaskState(TaskDTO taskDTO, TaskInstance task) throws IllegalTaskStateException, TaskNotFoundException {
		final ActivityState state = task.getState();
		final RuntimeAPI runtime = AccessorUtil.getRuntimeAPI();
		
		switch (state) {
			case ABORTED:
				break;
			case CANCELLED:
				break;
			case READY:
				if (taskDTO.getIsDone()) {
					runtime.executeTask(task.getUUID(), false);
					//runtime.startTask(task.getUUID(), false);
				}
				break;
			case EXECUTING:
				if (taskDTO.getIsDone()) {
					runtime.executeTask(task.getUUID(), false);
				}
				break;
			case FINISHED:
				break;
		}
	}
	
	/**
	 * 
	 * @param taskDTO
	 * @param task
	 * @throws InstanceNotFoundException
	 * @throws VariableNotFoundException
	 */
	private void approveOrRejectTask(TaskDTO taskDTO, TaskInstance task) throws InstanceNotFoundException, VariableNotFoundException {
		final RuntimeAPI runtime = AccessorUtil.getRuntimeAPI();
		final Boolean approval = taskDTO.getApproval();
		if (approval != null) {
			runtime.setProcessInstanceVariable(task.getProcessInstanceUUID(), "approved", approval);
		}
	}
	
	private void validateProcessTasks(ProcessDefinition procDef) {
		// TODO: Validate all human tasks in the process definition are dcTrack defined workflow tasks
	}
}
