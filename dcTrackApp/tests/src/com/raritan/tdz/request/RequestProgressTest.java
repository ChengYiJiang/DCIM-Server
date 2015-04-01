package com.raritan.tdz.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.DataCircuitFactory;
import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.progress.RequestProgressDTO;
import com.raritan.tdz.request.progress.RequestProgressLookup;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.tests.TestBase;

public class RequestProgressTest extends TestBase {

	RequestFactory requestFact;
	DataCircuitFactory dataCircuitFact;
	DataCenterLocationDetails testLocation;
	ItemFactory itemFact;
	
	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		testLocation = this.getTestLocation("Demo Site A");
		testUser = this.getTestAdminUser();
		testUser.setRequestBypass(true);
		
		requestFact = (RequestFactory)ctx.getBean("requestFact");
		dataCircuitFact = (DataCircuitFactory)ctx.getBean("dataCircuitFact");
		itemFact = (ItemFactory)ctx.getBean("itemFact");
		
	}

	@Test
	public final void testRequestProcessAndProgress() throws InterruptedException {
	    ExecutorService exec = Executors.newFixedThreadPool(16);
	    for (int i = 0; i < 2; i++) {
	    	if ((i % 2) == 0) {
		        exec.execute(new Runnable() {
		             @Override
		             public void run() {
		            	 try {
							testMultiRequestCircuitAndItem_ItemConflictState_dto();
						} catch (BusinessValidationException
								| DataAccessException e) {
							// TODO Auto-generated catch block
							Assert.fail("Exception thrown, checking the working of request processing with progress...");
							e.printStackTrace();
						}
		             }
		        });
	    	}
	    	else {
	    		exec.execute(new Runnable() {
		             @Override
		             public void run() {
		                 getProgressInfo();
		             }
		        });
	    	}
	    }
	    exec.shutdown();
	    exec.awaitTermination(500, TimeUnit.SECONDS);
	}

	@SuppressWarnings("unused")
	private void startRequest() {
	    
		System.out.println("start request starts... ");
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("start request finish ...");
	}
	
	private void getProgressInfo() {

		System.out.println("get progress starts... ");
  		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
  		
		RequestProgressDTO dto = null;
		
		try {
		
			while (null == dto) {

					Thread.sleep(1000);
					
					dto = requestService.getRequestProgress();
			}
			
			while (dto.getProgressState() != RequestProgressLookup.state.REQUEST_PROGRESS_FINISH) {
				
				System.out.println("Request Progress Test: " + dto);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				dto = requestService.getRequestProgress();
				
			} 
		
			System.out.println("get progress finish ...");
		
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		finally {
			TransactionSynchronizationManager.unbindResource(sf);
		}
		

	}
	
  	public final void testMultiRequestCircuitAndItem_ItemConflictState_dto() throws BusinessValidationException, DataAccessException {
		
  		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
  		
  		try {
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	/*Request requestItem2 = requestFact.createRequestInstalled(item2);*/
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2, requestItem1);
    	
    	Errors errors = getErrorObject();
    	
    	UserInfo testUserRequest = FlexUserSessionContext.getUser();
    	testUserRequest.setRequestBypass(true);
    	
		// requestManager.process(requests,  testUserRequest, errors, testUserRequest.isRequestBypass());
		requestHome.processRequests(testUserRequest, requests, errors);

		validateErrors(errors, Arrays.asList("Request.ItemStatusConflict.circuit"));
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.PLANNED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
  		}
  		finally {
		
  			TransactionSynchronizationManager.unbindResource(sf);
  		}
		
	}
  	
	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}

	protected void validateErrors(Errors errors, List<String> errorCodes) {
		
		if (errors.hasErrors() && errorCodes.size() == 0) Assert.fail("errors generated when no error expected");
		
		if (!errors.hasErrors() && errorCodes.size() > 0) Assert.fail("errors not generated when error was expected");
		
		if (errors.hasErrors()) {
			// check the error type
			List<ObjectError> errorList = errors.getAllErrors();
			for (ObjectError errorObj: errorList) {
				Assert.assertTrue(errorCodes.contains(errorObj.getCode()), "unexpcted error generated:" + errorObj.getCode() + " : " + errorObj.getDefaultMessage());
			}
		}
	}
	
	protected void validateDataCircuitState(Long circuitId, Long status) {
		DataCircuit circuit = dataCircuitFact.getCircuit(circuitId);
		
		Long circuitStatus = circuit.getStartConnection().getStatusLookup().getLkpValueCode();
		
		dataCircuitFact.deleteCircuit(circuit);
		
		Assert.assertTrue(circuitStatus.equals(status), "Data Circuit State incorrect");
		
	}

	protected void validateDataCircuitState(Long circuitId, List<Long> status) {
		DataCircuit circuit = dataCircuitFact.getCircuit(circuitId);
		
		Long circuitStatus = circuit.getStartConnection().getStatusLookup().getLkpValueCode();
		
		dataCircuitFact.deleteCircuit(circuit);
		
		Assert.assertTrue(status.contains(circuitStatus), "Data Circuit State incorrect");
		
	}

	protected void validateItemState(Long itemId, Long status) {
		
		Map<Long, Long> itemStateMap = itemDAO.getItemsStatus(Arrays.asList(itemId));
		
		this.addTestItemList(Arrays.asList(itemId));
		
		Assert.assertTrue(itemStateMap.get(itemId).equals(status), "Item state incorrect");
		
	} 
	

}

