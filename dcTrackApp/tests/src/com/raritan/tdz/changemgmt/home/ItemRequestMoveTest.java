package com.raritan.tdz.changemgmt.home;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SaveUtils;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.item.request.ItemRequestValidationAspect;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Change management tests.
 * @author Santo Rosario
 */
public class ItemRequestMoveTest extends TestBase {

	private ItemRequest itemRequest;
	private UserInfo testUser;
	private ItemRequestValidationAspect validationAspect;
	private ItemRequestDAO requestDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemRequest = (ItemRequest)ctx.getBean("itemRequest");
		requestDAO = (ItemRequestDAO)ctx.getBean("itemRequestDAO");
		testUser = this.getTestAdminUser();
		validationAspect = (ItemRequestValidationAspect) ctx.getBean("itemRequestValidationAspect");
		validationAspect.setDisableValidate(true);
	}

		
	@Test
	public final void testItemMoveRequest() throws Throwable {
		Map<Long,Long> itemIds = new HashMap<Long,Long>();
		
		ItItem item = this.createNewTestDevice("NewItemMoveTest-001", SystemLookup.ItemStatus.INSTALLED);		
		ItItem item2 = this.createNewTestDevice("NewItemMoveTest-001-MOVE", SystemLookup.ItemStatus.PLANNED);		
		item2.setItemToMoveId(item.getItemId());
		
		itemIds.put(item2.getItemToMoveId(), item2.getItemId());
		
		itemRequest.moveItemRequest(itemIds, testUser, true);
		
		assertTrue(checkRequest(item2.getItemToMoveId(), SystemLookup.RequestStage.REQUEST_ISSUED));	
	}


	// Commented because the dto for when-moved item does not include the port dtos and the ports will not match 
    // @Test
  	public final void testMoveDevice() throws Throwable {
  		Session session = sf.getCurrentSession();
  		long itemToMoveId = 5163;//"ARCH01"
  		List<ValueIdDTO> saveData = getItemSaveFields("CLARITY^^WHEN-MOVE", 1, SystemLookup.ItemStatus.PLANNED);
  		
  		saveData.addAll(SaveUtils.addItemField("_itemToMoveId", itemToMoveId)); 
  		
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		
  		session.flush();
  		
  		Long itemId = SaveUtils.getItemId(itemData);
  		
  		Item item = (Item)session.get(Item.class, itemId);

  		//this.addTestItem(item);
		
		//assertTrue(checkRequest(itemToMoveId, SystemLookup.RequestStage.REQUEST_ISSUED));	
  	}


   // @Test
  	public final void testMoveCabinet() throws Throwable {
  		Session session = sf.getCurrentSession();
  		long itemToMoveId = 33; //33;"4G"
  		List<ValueIdDTO> saveData = getItemSaveFields("ItemMove-CAB-001", 6, SystemLookup.ItemStatus.PLANNED);
  		
  		saveData.addAll(SaveUtils.addItemField("_itemToMoveId", itemToMoveId));
  		
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		
  		session.flush();
  		
  		Long itemId = SaveUtils.getItemId(itemData);
  		
  		Item item = (Item)session.get(Item.class, itemId);

  		this.addTestItem(item);
  		
		assertTrue(checkRequest(itemToMoveId, SystemLookup.RequestStage.REQUEST_ISSUED));	
  	}
 	
  	private List<ValueIdDTO> getItemSaveFields(String itemName, long classLksId, long statusLksId) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();

  		fields.addAll( SaveUtils.addItemField("cmbStatus", statusLksId));
  		fields.addAll( SaveUtils.addItemIdentityFields(itemName, null) );
  		
  		switch((int)classLksId){
  		case 1:
  			fields.addAll( SaveUtils.addItemHardwareFields(20L, 1L) ); //mfr_id=20, model_id=1
  	  		fields.addAll( SaveUtils.addRackablePlacementFields(
						1L,		 						// Site A
						10L,							// Cabinet 1H
						SystemLookup.RailsUsed.BOTH		// Both Rails Used
					));
  	  		fields.addAll( SaveUtils.addItemField("cmbUPosition", 2) );
  			
  			break;
  		case 6:
  			fields.addAll( SaveUtils.addItemHardwareFields(12L, 24L) ); //mfr_id=12, model_id=24
  			break;
  		default:
  			fields.addAll( SaveUtils.addItemHardwareFields(20L, 1L) ); //mfr_id=20, model_id=1
  			break;
  		}
  		
  		return fields;
  	}	

	boolean checkRequest(long itemId, long requestType){
		Session session = sf.getCurrentSession();
		
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(requestType);
		
		List<Request> recList;
		session.flush();
		
		try {
			recList = requestDAO.getRequest(itemId, requestStages, null);
			
			if(recList.size() == 1){
				return true;
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
		return false;
	}  	
}
