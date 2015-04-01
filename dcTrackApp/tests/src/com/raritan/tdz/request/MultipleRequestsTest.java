package com.raritan.tdz.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.DataCircuitFactory;
import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.ItItem;
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

/**
 * 
 * @author bunty
 *
 */
public class MultipleRequestsTest extends TestBase {

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
	
	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}
	
	protected void submitInstallRequestForAllChassisChildren(Item item, List<Request> requests) throws DataAccessException {
		
		Collection<ItItem> blades = itemDAO.getAllBladesForChassis(item.getItemId());
		
		for (ItItem blade: blades) {
			requests.add(requestFact.createRequestInstalled(blade));
			this.addTestItemList(Arrays.asList(blade.getItemId()));
		}
				
	}

	protected void submitOffSiteRequestForAllChassisChildren(Item item, List<Request> requests) throws DataAccessException {
		
		Collection<ItItem> blades = itemDAO.getAllBladesForChassis(item.getItemId());
		
		for (ItItem blade: blades) {
			requests.add(requestFact.createRequestOffSite(blade));
			this.addTestItemList(Arrays.asList(blade.getItemId()));
		}
				
	}

	protected void submitPowerOffRequestForAllChassisChildren(Item item, List<Request> requests) throws DataAccessException {
		
		Collection<ItItem> blades = itemDAO.getAllBladesForChassis(item.getItemId());
		
		for (ItItem blade: blades) {
			requests.add(requestFact.createRequestPowerOff(blade));
			this.addTestItemList(Arrays.asList(blade.getItemId()));
		}
				
	}

	protected void submitOffSiteRequestForAllCabinetChildren(Item item, List<Request> requests, Boolean includingChassis, Boolean includingBlades, Boolean includingNonChassis) throws DataAccessException {
		
		for (Item child: itemDAO.getChildrenItems(item)) {
			
			this.addTestItemList(Arrays.asList(child.getItemId()));
			
			if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
				if (includingChassis) {
					requests.add(requestFact.createRequestOffSite(child));
				}
				if (includingBlades) {
					submitOffSiteRequestForAllChassisChildren(child, requests);
				}
				
			}
			else {
				if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
					if (includingBlades) {
						
						requests.add(requestFact.createRequestOffSite(child));
					}
				}
				
				else {
					if (includingNonChassis) {
						requests.add(requestFact.createRequestOffSite(child));
					}
				}
			}
		}
	}

	
	protected void submitPowerOffRequestForAllCabinetChildren(Item item, List<Request> requests, Boolean includingChassis, Boolean includingBlades, Boolean includingNonChassis) throws DataAccessException {
		
		for (Item child: itemDAO.getChildrenItems(item)) {
			
			this.addTestItemList(Arrays.asList(child.getItemId()));
			
			if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
				if (includingChassis) {
					requests.add(requestFact.createRequestPowerOff(child));
				}
				if (includingBlades) {
					submitPowerOffRequestForAllChassisChildren(child, requests);
				}
				
			}
			else {
				if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
					if (includingBlades) {
						
						requests.add(requestFact.createRequestPowerOff(child));
					}
				}
				
				else {
					if (includingNonChassis) {
						requests.add(requestFact.createRequestPowerOff(child));
					}
				}
			}
		}
	}

	
	protected void submitInstallRequestForAllCabinetChildren(Item item, List<Request> requests, Boolean includingChassis, Boolean includingBlades, Boolean includingNonChassis) throws DataAccessException {
		
		for (Item child: itemDAO.getChildrenItems(item)) {
			
			this.addTestItemList(Arrays.asList(child.getItemId()));
			
			if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
				if (includingChassis) {
					requests.add(requestFact.createRequestInstalled(child));
				}
				if (includingBlades) {
					submitInstallRequestForAllChassisChildren(child, requests);
				}
				
			}
			else {
				if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
					if (includingBlades) {
						
						requests.add(requestFact.createRequestInstalled(child));
					}
				}
				
				else {
					if (includingNonChassis) {
						requests.add(requestFact.createRequestInstalled(child));
					}
				}
			}
		}
	}

	protected void submitStorageRequestForAllCabinetChildren(Item item, List<Request> requests, Boolean includingChassis, Boolean includingBlades, Boolean includingNonChassis) throws DataAccessException {
		
		for (Item child: itemDAO.getChildrenItems(item)) {
			
			this.addTestItemList(Arrays.asList(child.getItemId()));
			
			if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
				if (includingChassis) {
					requests.add(requestFact.createRequestToStorage(child));
				}
				if (includingBlades) {
					submitInstallRequestForAllChassisChildren(child, requests);
				}
				
			}
			else {
				if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
					if (includingBlades) {
						
						requests.add(requestFact.createRequestToStorage(child));
					}
				}
				
				else {
					if (includingNonChassis) {
						requests.add(requestFact.createRequestToStorage(child));
					}
				}
			}
		}
	}

	protected void validateAllChassisChildrenState(Item item, Long expectedState) throws DataAccessException {

		Collection<ItItem> blades = itemDAO.getAllBladesForChassis(item.getItemId());
		
		for (ItItem blade: blades) {
			validateItemState(blade.getItemId(), expectedState);
		}
				
	}

	
	protected void validateAllCabinetChildrenState(Item item, Long expectedState, Boolean includingNonChassis, Boolean includingChassis, Boolean includingBlades) throws DataAccessException {
		
		for (Item child: itemDAO.getChildrenItems(item)) {
			
			if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
				
				if (includingChassis) {
					validateItemState(child.getItemId(), expectedState);
				}
				
				if (includingBlades) {
					validateAllChassisChildrenState(child, expectedState);
				}
				
			}
			else {
				
				if (includingNonChassis) {
					
					if (null != child.getSubclassLookup() && (child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || child.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
						if (includingBlades) {
							validateItemState(child.getItemId(), expectedState);
						}
					}
					else {
						validateItemState(child.getItemId(), expectedState);
					}
					
				}
				
			}
		}
		
	}


	
	/* ***********************
	 * Data Circuit Requests testing 
	 * ************************/
	
	/**
	 * 1. create 2 items with 2 circuits in planned state
	 * 2. create request for 1 item in installed, both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * + the item and the circuit that has item request going together should pass
	 * - the circuit that has item in planned state should fail
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItem_ItemConflictState() throws BusinessValidationException, DataAccessException {
		
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
    	
		// requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ItemStatusConflict.circuit"));
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.PLANNED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in planned state
	 * 2. create request for 1 item in installed, 1 item in storage, both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * + the item and the circuit that has item request going together should pass
	 * - the circuit that has item in request in storage, should fail
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItem_ItemConflictRequest() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestToStorage(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2, requestItem1, requestItem2);
    	
    	Errors errors = getErrorObject();
    	
		// requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ItemStatusConflict.circuit"));
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.PLANNED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in planned state
	 * 2. create request for both items in installed, both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItem_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestInstalled(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2, requestItem1, requestItem2);
    	
    	Errors errors = getErrorObject();
    	
		// requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in powered off state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in powered-off state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemPowerOff_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.POWERED_OFF);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.POWERED_OFF);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	// Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	// Request requestItem2 = requestFact.createRequestInstalled(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2/*, requestItem1, requestItem2*/);
    	
    	Errors errors = getErrorObject();
    	
		// requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemOffSite_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.OFF_SITE);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.OFF_SITE);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	// Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	// Request requestItem2 = requestFact.createRequestInstalled(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2/*, requestItem1, requestItem2*/);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemRequests_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestPowerOff(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestPowerOff(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestItem1, requestItem2, requestConn1, requestConn2);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED));
		
		validateDataCircuitState(circuit2.getCircuitId(), Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED));
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemRequests1_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestPowerOff(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestPowerOff(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2, requestItem1, requestItem2);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED));
		
		validateDataCircuitState(circuit2.getCircuitId(), Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED));
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemRequests2_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestOffSite(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestOffSite(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestItem1, requestItem2, requestConn1, requestConn2);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
	}

	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemRequests3_allOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	Request requestItem1 = requestFact.createRequestOffSite(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	Request requestItem2 = requestFact.createRequestOffSite(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2, requestItem1, requestItem2);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		
	}


	/**
	 * 1. create 2 items with 2 circuits in off site state
	 * 2. create request for both circuits in installed
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all pass: item allowed in off-site state when circuit is getting installed
	 * @throws DataAccessException 
	 * @throws BusinessValidationException 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItemStorage_notOK() throws BusinessValidationException, DataAccessException {
		
    	DataCircuit circuit1 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.STORAGE);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit1));
    	
    	Item item1 = itemDAO.getItem(circuit1.getStartItemId());
    	
    	DataCircuit circuit2 = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.STORAGE);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit2));
    	
    	Item item2 = itemDAO.getItem(circuit2.getStartItemId());

    	// Request requestItem1 = requestFact.createRequestInstalled(item1);
    	Request requestConn1 = requestFact.createRequestConnect(circuit1);
    	
    	// Request requestItem2 = requestFact.createRequestInstalled(item2);
    	Request requestConn2 = requestFact.createRequestConnect(circuit2);
    	
    	List<Request> requests = Arrays.asList(requestConn1, requestConn2/*, requestItem1, requestItem2*/);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ItemStatusConflict.circuit"));
		
		validateDataCircuitState(circuit1.getCircuitId(), SystemLookup.ItemStatus.STORAGE);
		
		validateDataCircuitState(circuit2.getCircuitId(), SystemLookup.ItemStatus.STORAGE);
		
		validateItemState(item1.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		validateItemState(item2.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
	}


	/**
	 * 1. create cabinet in planned state, create a data circuit with device and n/w item
	 * 2. create request for device, circuit to install with the cabinet still in planned state
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all request fails
	 * @throws Throwable 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItem_cirOKitemFail() throws Throwable {
	
		CabinetItem cabinet = itemFact.createCabinet("DEFAULT-CAB-01", SystemLookup.ItemStatus.PLANNED);
		
		this.addTestItemList(Arrays.asList(cabinet.getItemId()));
		
    	DataCircuit circuit = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED, cabinet);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit));
    	
    	Item item = itemDAO.getItem(circuit.getStartItemId());
    	
    	Request requestItem = requestFact.createRequestInstalled(item);
    	Request requestConn = requestFact.createRequestConnect(circuit);
    	
    	List<Request> requests = Arrays.asList(requestConn, requestItem);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentStatusConflict"));
		
		validateDataCircuitState(circuit.getCircuitId(), SystemLookup.ItemStatus.PLANNED);
		
		validateItemState(item.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
	}

	/**
	 * 1. create cabinet in planned state, create a data circuit with device and n/w item
	 * 2. create request for device, cabinet and circuit to install 
	 * 3. send request to process in request bypass
	 * expected result: 
	 * all request pass
	 * @throws Throwable 
	 * 
	 */
	@Test
  	public final void testMultiRequestCircuitAndItem_cirOKitemOK() throws Throwable {
	
		CabinetItem cabinet = itemFact.createCabinet("DEFAULT-CAB-01", SystemLookup.ItemStatus.PLANNED);
		
		this.addTestItemList(Arrays.asList(cabinet.getItemId()));
		
    	DataCircuit circuit = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.PLANNED, cabinet);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit));
    	
    	Item item = itemDAO.getItem(circuit.getStartItemId());
    	
    	Request requestCab = requestFact.createRequestInstalled(cabinet);
    	Request requestItem = requestFact.createRequestInstalled(item);
    	Request requestConn = requestFact.createRequestConnect(circuit);
    	
    	List<Request> requests = Arrays.asList(requestConn, requestItem, requestCab);
    	
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateDataCircuitState(circuit.getCircuitId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(item.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		validateItemState(cabinet.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
	}

	/* ********************************
	 * Cabinet with child items request testing 
	 * *********************************/

	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis and create requests for all items in install state.
	 * expected result:
	 * - no errors
	 * - all items in install state
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetsWithItems_allOK() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		
		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabRequests = Arrays.asList(requestFact.createRequestInstalled(cabItem1), requestFact.createRequestInstalled(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabRequests); 

		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		
	}
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - for 1st cabinet, create requests for all items in install state.
	 * - for 2nd cabinet, keep the cabinet in planned state and issue request for all childrens including the blades
	 * expected result:
	 * - no errors for 1st cabinet and cabinet and all its children items in install state
	 * - errors for all childrens of the 2nd cabinet and no state change
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetsWithItems_oneCabPlanned() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		
		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabRequests = Arrays.asList(requestFact.createRequestInstalled(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabRequests); 

		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.PLANNED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		
	}
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - for 1st cabinet, create requests for all items in install state.
	 * - for 2nd cabinet, cabinet has storage request and issue install request for all childrens including the blades
	 * expected result:
	 * - no errors for 1st cabinet and cabinet and all its children items in install state
	 * - errors for all childrens of the 2nd cabinet and no state change (planned) and cabinet goes to storage
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	@Test
	public final void testMultiRequestCabinetsWithItems_oneCabConflictReq() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-INSTALL-01", null);
		
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		
		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabRequests = Arrays.asList(requestFact.createRequestInstalled(cabItem2), requestFact.createRequestToStorage(cabItem1));
		
		List<Request> requests = new ArrayList<Request>(cabRequests); 

		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ChildrenStatusConflict", "Request.ParentRequestConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		
	}
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - for 1st cabinet, cabinet in planned state and issue request for all children only and not cabinet in install state
	 * - for 2nd cabinet, cabinet in install state and issue request for all children only and not cabinet in install state
	 * expected result:
	 * - errors for 1st cabinet all children
	 * - no errors for 2nd cabinet
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	@Test
	public final void testMultiRequestCabinetsWithItems_oneCabConflictState() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-INSTALL-01", null);
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		
		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabRequests = new ArrayList<Request>();
		
		List<Request> requests = new ArrayList<Request>(cabRequests); 

		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.PLANNED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.PLANNED, true, true, true);
		
		
	}
	

	/* *********************************
	 * Install Chassis with blade request test
	 * *********************************/
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - for 1st cabinet, create requests for all items in install state.
	 * - for 2nd cabinet, keep the cabinet in planned state and issue request for all childrens including the blades
	 * expected result:
	 * - no errors for 1st cabinet and cabinet and all its children items in install state
	 * - errors for all childrens of the 2nd cabinet and no state change
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestChassisWithBlades_OneChassisPlanned() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		// place cabinet in installed state
		cabItem2.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem2);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> requests = new ArrayList<Request>(); 

		// Chassis remains in planned state and no request is generated for chassis, but request is generated for blades
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, false, true, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, false, false);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		
	}

	@Test
	public final void testMultiRequestChassisWithBlades_OneChassisConflictReq() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		// place cabinet in installed state
		cabItem2.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem2);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> requests = new ArrayList<Request>(); 

		// blade is still in planned state
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, false, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, false); // all children in installed state but blades
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, false, true); // all blades in planned state
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);

		// Now create a storage request for the chassis in storage (current state installed) and blades in installed state (current status planned)
		requests.clear();
		
		submitStorageRequestForAllCabinetChildren(cabItem1, requests, true, false, false);
		
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, false, true, false);
		
		Errors errorsChassis = getErrorObject();

		requestManager.process(requests,  testUser, errorsChassis, testUser.isRequestBypass());

		validateErrors(errorsChassis, Arrays.asList("Request.ParentRequestConflict", "Request.ChildrenStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED); // Cabi still installed
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, false); // children including chassis in installed state
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, false, true); // blades in planned
		
	}

	@Test
	public final void testMultiRequestChassisWithBlades_OneChassisConflictState() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		// place cabinet in installed state
		cabItem2.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem2);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> requests = new ArrayList<Request>(); 

		// blade and chassis are still in planned state
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, false, false, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, false, false); // all children in installed state but blades and chassis
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, true, true); // all blades and chassis in planned state
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);

		// Now create install request for the blades and chassis continue to be in planned state
		requests.clear();
		
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, false, true, false);
		
		Errors errorsChassis = getErrorObject();

		requestManager.process(requests,  testUser, errorsChassis, testUser.isRequestBypass());

		validateErrors(errorsChassis, Arrays.asList("Request.ParentStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED); // Cabinet still installed
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, false, false); // children in installed state but blades and chassis
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, true, true); // blades and chassis in planned
		
	}

	@Test
	public final void testMultiRequestChassisWithBlades_OneChassisConflictReqNotIncluded() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
		// place cabinet 1 in installed state
		cabItem1.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem1);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", null);
		// place cabinet in installed state
		cabItem2.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
		session.update(cabItem2);
		session.flush();

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> requests = new ArrayList<Request>(); 

		// blade is still in planned state
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, true, false, true);
		
		submitInstallRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, false); // all children in installed state but blades
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, false, true); // all blades in planned state
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.INSTALLED, true, true, true);

		// Now create a storage request for the chassis in storage (current state installed) and blades in installed state (current status planned)
		requests.clear();
		
		List<Request> excludedRequests = new ArrayList<Request>();
		submitStorageRequestForAllCabinetChildren(cabItem1, excludedRequests, true, false, false); // storage request for chassis, not included in the request list
		
		submitInstallRequestForAllCabinetChildren(cabItem1, requests, false, true, false); // blades request to install
		
		Errors errorsChassis = getErrorObject();

		requestManager.process(requests,  testUser, errorsChassis, testUser.isRequestBypass());

		validateErrors(errorsChassis, Arrays.asList("Request.ParentRequestConflict", "Request.ChildrenStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED); // Cabi still installed
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, false); // children including chassis in installed state
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.PLANNED, false, false, true); // blades in planned
		
	}

	
	/* *********************************
	 * Power Off Cabinet test
	 * *********************************/
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - issue power off request for all 
	 * expected result:
	 * - no errors and all items in powered off state
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetPowerOff_AllOK() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestPowerOff(cabItem1), requestFact.createRequestPowerOff(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		submitPowerOffRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitPowerOffRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.POWERED_OFF, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.POWERED_OFF, true, true, true);
		
	}


	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - issue power off request for all 
	 * expected result:
	 * - no errors and all items in powered off state
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetPowerOff_ChildStateConflict() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestPowerOff(cabItem1), requestFact.createRequestPowerOff(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		// submitPowerOffRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitPowerOffRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ChildrenStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.POWERED_OFF, true, true, true);
		
	}


	@Test
	public final void testMultiRequestCabinetPowerOff_ChildRequestConflict() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestPowerOff(cabItem1), requestFact.createRequestPowerOff(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		// conflicting request
		submitStorageRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitPowerOffRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentRequestConflict", "Request.ChildrenStatusConflict", "Request.ChildrenRequestConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, false, true, false);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.STORAGE, true, false, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.POWERED_OFF);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.POWERED_OFF, true, true, true);
		
	}

	
	/* *********************************
	 * Off Site Cabinet test
	 * *********************************/
	
	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - issue power off request for all 
	 * expected result:
	 * - no errors and all items in powered off state
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetOffSite_AllOK() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestOffSite(cabItem1), requestFact.createRequestOffSite(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		submitOffSiteRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitOffSiteRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, new ArrayList<String>());
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.OFF_SITE, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.OFF_SITE, true, true, true);
		
	}


	/**
	 * create 2 cabinets with multiple items, including one chassis and blades in chassis 
	 * - issue power off request for all 
	 * expected result:
	 * - no errors and all items in powered off state
	 * 
	 * @throws DataAccessException 
	 * @throws Throwable
	 */
	
	@Test
	public final void testMultiRequestCabinetOffSite_ChildStateConflict() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestOffSite(cabItem1), requestFact.createRequestOffSite(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		// submitPowerOffRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitOffSiteRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
		session.flush();
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ChildrenStatusConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, true, true, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.OFF_SITE, true, true, true);
		
	}


	@Test
	public final void testMultiRequestCabinetOffSite_ChildRequestConflict() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestOffSite(cabItem1), requestFact.createRequestOffSite(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		// conflicting request
		submitStorageRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitOffSiteRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		processRequests(requests, errors);

		validateErrors(errors, Arrays.asList("Request.ParentRequestConflict", "Request.ChildrenStatusConflict", "Request.ChildrenRequestConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, false, true, false);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.STORAGE, true, false, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.OFF_SITE, true, true, true);
		
	}
	
	@Test
	public final void testMultiRequestCabinetOffSite_inContext() throws Throwable {
		
		Item cabItem1 = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);

		Item cabItem2 = this.createNewTestCabinetWithItems("CABINET-PLANNED-02", SystemLookup.ItemStatus.INSTALLED);

		this.addTestItemList(Arrays.asList(cabItem1.getItemId()));
		this.addTestItemList(Arrays.asList(cabItem2.getItemId()));
		
		List<Request> cabinetRequests = Arrays.asList(requestFact.createRequestOffSite(cabItem1), requestFact.createRequestOffSite(cabItem2));
		
		List<Request> requests = new ArrayList<Request>(cabinetRequests); 

		// conflicting request
		submitStorageRequestForAllCabinetChildren(cabItem1, requests, true, true, true);
		
		submitOffSiteRequestForAllCabinetChildren(cabItem2, requests, true, true, true);
		
    	Errors errors = getErrorObject();
    	
		// requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
    	processRequests(requests, errors);
    	
		validateErrors(errors, Arrays.asList("Request.ParentRequestConflict", "Request.ChildrenStatusConflict", "Request.ChildrenRequestConflict"));
		
		validateItemState(cabItem1.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.INSTALLED, false, true, false);
		validateAllCabinetChildrenState(cabItem1, SystemLookup.ItemStatus.STORAGE, true, false, true);
		
		validateItemState(cabItem2.getItemId(), SystemLookup.ItemStatus.OFF_SITE);
		validateAllCabinetChildrenState(cabItem2, SystemLookup.ItemStatus.OFF_SITE, true, true, true);
		
	}
	
	@Test
	public final void testNewInstance() throws InstantiationException, IllegalAccessException {

		AtomicLong aLong = AtomicLong.class.newInstance();  
		
		System.out.println("A Long= " + aLong);
		
	}
	
	@SuppressWarnings("unused")
	private void waitForRequestTocomplete() {

		System.out.println("get progress starts... ");
  		
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
		}

	}
	
	private void processRequests(List<Request> requests, Errors errors) throws BusinessValidationException, DataAccessException {

		UserInfo userInfo = FlexUserSessionContext.getUser();
		
		// requestService.cleanRequestProgressDTO();
		
		// requestHome.processRequests(userInfo, requests, errors);
		
		// waitForRequestTocomplete();
		
		requestManager.process(requests, userInfo, errors, true);
		
	}


}
