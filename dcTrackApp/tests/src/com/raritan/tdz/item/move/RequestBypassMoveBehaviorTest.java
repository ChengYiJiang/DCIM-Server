package com.raritan.tdz.item.move;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.Errors;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.home.RequestBypassMoveBehavior;
import com.raritan.tdz.request.progress.RequestProgressDTO;
import com.raritan.tdz.request.progress.RequestProgressLookup;
import com.raritan.tdz.tests.TestBase;

public class RequestBypassMoveBehaviorTest extends TestBase {
	
		protected RequestBypassMoveBehavior requestBypassMB;;	
		private UserInfo testUser;
		RequestFactory requestFact;
		Errors errors;
		
		@BeforeMethod
		public void setUp() throws Throwable {
			super.setUp();
			requestBypassMB = (RequestBypassMoveBehavior)ctx.getBean("requestBypassMoveBehavior");
			testUser = this.getTestAdminUser();
			requestFact = (RequestFactory)ctx.getBean("requestFact");
			errors = this.getErrorObject(Request.class);
		}

		@AfterMethod
		public void tearDown() throws Throwable {
			// super.tearDown();
			
			List<Long> testItemIds = getTestItemsId();
			
			try {
				if (null != testItemIds && testItemIds.size() > 0) {
				
					itemHome.deleteItemsConfirmed(testItemIds, true, false, testUser);
				}
			}
			finally {
				
				TransactionSynchronizationManager.unbindResource(sf);
			}
			
		}
		
		@Transactional(propagation=Propagation.REQUIRES_NEW)
		public final Item testMoveDeviceData() throws Throwable {
			
			long uPosition = 33;
	    	Item item = this.createNewTestDevice("DEVICE-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);
	  		
	    	Item itemWhenMove = this.createNewTestDevice("DEVICE-PLANNED-01^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);	    	
	    	itemWhenMove.setItemToMoveId(item.getItemId());
	    	itemWhenMove.setUPosition(uPosition);	    	
	    	itemDAO.update(itemWhenMove);
	    	
	    	Request request = requestFact.createRequestMove(item);
	    	
	    	requestFact.addPortMoveToRequest(request, item, itemWhenMove);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		session.flush();
	  		session.clear();
	  		session.refresh(request);
	  		session.refresh(itemWhenMove);
			
	  		return itemWhenMove;
		}
		
		@Transactional(propagation=Propagation.REQUIRES_NEW)
		public final void validateMove(Long itemId, Item itemWhenMove) {

			Item item = itemDAO.loadItem(itemId);
			assertTrue(item.getUPosition() == 33/*uPosition*/);
			
			this.removeTestItem(itemWhenMove);
			
		}
		
		@Transactional(propagation=Propagation.NOT_SUPPORTED)
	    @Test
	  	public final void testMoveDevice() throws Throwable {
			
			Item itemWhenMove = testMoveDeviceData();
	  		
	  		requestBypassMB.postSave(itemWhenMove, testUser, this.getErrorObject(Request.class));
	  		
	  		Thread.sleep(10000);
	  		// waitForRequestTocomplete();
	  		
	  		Long itemId = itemWhenMove.getItemToMoveId();
	  		
	  		this.validateMove(itemId, itemWhenMove);
	  	}
	    
	    @Test
	  	public final void testMoveDeviceChassis() throws Throwable {
	    	long uPosition = 33;	
	    	Item cabinet = this.createNewTestCabinet("CABINET-TEST-01", SystemLookup.ItemStatus.INSTALLED);
	    	Item item = this.createNewTestDeviceChassis("CHASSIS-PLANNED-01", SystemLookup.ItemStatus.INSTALLED, cabinet.getItemId());
	  		
	    	Item itemWhenMove = this.createNewTestDevice("CHASSIS-PLANNED-01^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);	    	
	    	itemWhenMove.setItemToMoveId(item.getItemId());
	    	itemWhenMove.setUPosition(uPosition);	    	
	    	itemDAO.update(itemWhenMove);
	    	
	    	Request request = requestFact.createRequestMove(item);
	    	
	    	requestFact.addPortMoveToRequest(request, item, itemWhenMove);
	    	
	    	requestService.cleanRequestProgressDTO();
	    	
	  		testUser.setRequestBypass(true);
	  		requestBypassMB.postSave(itemWhenMove, testUser, this.getErrorObject(Request.class));
	  		
	  		Thread.sleep(20000);
	  		// waitForRequestTocomplete();
	  		
	  		session.refresh(item);
	  		// item = itemDAO.loadItem(item.getItemId());
			assertTrue(item.getUPosition() == uPosition);
			
			this.removeTestItem(itemWhenMove);
	  	}

	    @Test
	  	public final void testMoveDeviceChassisWithBlades() throws Throwable {
	    	long uPosition = 33;	
	    	Item cabinet = this.createNewTestCabinet("CABINET-TEST-01", SystemLookup.ItemStatus.INSTALLED);
	    	Item cabinet2 = this.createNewTestCabinet("CABINET-TEST-01", SystemLookup.ItemStatus.INSTALLED);
	    	
	    	Item item = this.createNewTestDeviceChassis("CHASSIS-TEST-01", SystemLookup.ItemStatus.INSTALLED, cabinet.getItemId());
	    	Item blade = this.createNewTestDeviceBladeInChassis("BLADE-TEST-01", cabinet.getItemId(), item.getItemId(), SystemLookup.ItemStatus.INSTALLED);
	    	
	    	Item itemWhenMove = this.createNewTestDevice("MOVE-PLANNED-01^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);	    	
	    	itemWhenMove.setItemToMoveId(item.getItemId());
	    	itemWhenMove.setUPosition(uPosition);
	    	itemWhenMove.setParentItem(cabinet2);
	    	itemDAO.update(itemWhenMove);
	    	
	    	Request request = requestFact.createRequestMove(item);
	    	
	    	requestFact.addPortMoveToRequest(request, item, itemWhenMove);
	    	
	    	requestService.cleanRequestProgressDTO();
	    	
	  		testUser.setRequestBypass(true);
	  		requestBypassMB.postSave(itemWhenMove, testUser, this.getErrorObject(Request.class));
	  		
	  		Thread.sleep(20000);
	  		// waitForRequestTocomplete();
	  		
	  		session.refresh(item);
	  		//item = itemDAO.loadItem(item.getItemId());
			assertTrue(item.getUPosition() == uPosition);
			assertTrue(item.getParentItem().getItemId() == cabinet2.getItemId());
			
			blade = itemDAO.loadItem(blade.getItemId());
			assertTrue(blade.getParentItem().getItemId() == cabinet2.getItemId());
			
			this.removeTestItem(itemWhenMove);
	  	}
	    
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


}
