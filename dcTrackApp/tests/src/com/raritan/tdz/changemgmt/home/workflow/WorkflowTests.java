package com.raritan.tdz.changemgmt.home.workflow;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.util.List;

import com.raritan.tdz.changemgmt.dto.TaskDTO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.tests.TestBase;

/**
 * Change Management Workflow tests.
 * @author Andrew Cohen
 * @version 3.0
 */
public class WorkflowTests extends TestBase implements WorkflowHome {

	private WorkflowHome workflowHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	//@Test
	@Test
	public final void testDeployWorkFlow() throws SystemException {
		workflowHome.deployAllWorkflows();
	}

	
	// Workflow implementation
	
	@Override
	public void deployAllWorkflows()
			throws SystemException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String startNewItemWorkflow(UserInfo user, Item item, long requestId)
			throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TaskDTO> getUserTasks(UserInfo user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTask(TaskDTO task) throws BusinessValidationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllWorkflows() throws SystemException {
		// TODO Auto-generated method stub
		
	}
}
