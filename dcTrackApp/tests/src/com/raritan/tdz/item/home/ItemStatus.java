package com.raritan.tdz.item.home;

import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.hibernate.Session;


import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import com.raritan.dctrack.xsd.UiValueIdField;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LkuData;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

import com.raritan.tdz.tests.TestBase;

public class ItemStatus extends TestBase{

	//private Long m_itemId;
	private Long testCabinetId;
	
	private void cleanRackableItem( Long itemId) throws Throwable{
	    System.out.println("$$$$$$ cleanup");
	    
	    if( itemId == -1L) return; 
	    
		try {
			itemHome.deleteItem(itemId, false, null);
		}
		catch(Exception ex){
			ex.printStackTrace();
		} /*catch (DataAccessException e) {
			e.printStackTrace();
		}*/ catch (BusinessValidationException e) {
			e.printStackTrace();
		}	    
	   
/*
	    Session mySession = sf.getCurrentSession();
	    
	    ItItem itItem = (ItItem)mySession.load(ItItem.class, itemId);    
	    if( itItem != null){
	    	System.out.println("--- deleting rackable item: " + itItem.getItemId());
	    	mySession.delete(itItem);
	    }
	    
	    Item item = itItem;
	    if( item != null){
	    	mySession.delete(item);
	    }
	    
	    ItemServiceDetails itemDetails = (ItemServiceDetails)itItem.getItemServiceDetails();
	    if( itemDetails != null){
	    	System.out.println("==== deleting itemDetails for item:" + itemDetails.getItemServiceDetailId());
	    	mySession.delete(itemDetails);
	    }
	    mySession.flush();
	    */
	}


    private List<ValueIdDTO>addOwnerIdToItemValueDTOList(Long ownerId, List<ValueIdDTO> valueIdDTOList){
    	ValueIdDTO dto = new ValueIdDTO();
    	dto.setLabel("cmbSystemAdmin");
    	dto.setData(new Long(ownerId));
    	valueIdDTOList.add(dto);

    	return valueIdDTOList;
    }

    private List<ValueIdDTO>addTeamIdToItemValueDTOList(Long teamId, List<ValueIdDTO> valueIdDTOList){
    	ValueIdDTO dto = new ValueIdDTO();
    	dto.setLabel("cmbSystemAdminTeam");
    	dto.setData(new Long(teamId));
    	valueIdDTOList.add(dto);
    	return valueIdDTOList;
    }

    private List<ValueIdDTO> createRackableItemValueIdDTOList(String itemName, long itemStatus){
    	List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
    	ValueIdDTO dto = new ValueIdDTO();

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbMake");
    	dto.setData(new Long(20)); //HP
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbModel");
    	dto.setData(new Long(1)); //Proliant DL320 G5p
    	valueIdDTOList.add(dto);

    	//Identity Panel Data
    	dto = new ValueIdDTO();
    	dto.setLabel("tiName");
    	dto.setData(itemName);
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("tiAlias");
    	dto.setData("BRH-TEST");
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbType");
    	dto.setData(new Long(1));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbFunction");
    	dto.setData(new Long(970));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbCustomer");
    	dto.setData(new Long(566));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbStatus");
    	dto.setData(itemStatus);
    	valueIdDTOList.add(dto);

    	//Placement Panel Data
    	dto = new ValueIdDTO();
    	dto.setLabel("cmbLocation");
    	dto.setData(new Long(1));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("radioRailsUsed");
    	dto.setData(new Long(8003));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbCabinet");
    	dto.setData(new Long(testCabinetId));
    	valueIdDTOList.add(dto);

    	if (itemStatus == SystemLookup.ItemStatus.INSTALLED) {
    		dto = new ValueIdDTO();
    		dto.setLabel("cmbUPosition");
    		dto.setData("4");
    		valueIdDTOList.add(dto);
    	}

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbOrientation");
    	dto.setData(new Long(7081));
    	valueIdDTOList.add(dto);

    	return valueIdDTOList;
    }

	private void printUiComponentDTOMap(Map<String, UiComponentDTO> xyzMap)
	{
		try {
			System.out.println("====== Map content: ");
			Set<String> keySet = xyzMap.keySet();
			for (String s1 : keySet) {
				UiComponentDTO componentDTO = xyzMap.get(s1);
					if (componentDTO != null) {
					UiValueIdField uiValueIdField = componentDTO
							.getUiValueIdField();
					if (uiValueIdField != null) {
						if (uiValueIdField.getValue() != null) {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId() + ", value="
									+ uiValueIdField.getValue().toString());
						} else {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId());
						}
					} else {
						System.out.println("key=" + s1 + "val=");
					}
				} else
					System.out.println("key=" + s1 + ", but val not exist");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	@BeforeMethod
	public void setUp() throws Throwable {
    	super.setUp();
    	testCabinetId = 10L;
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	protected UserInfo createGatekeeperUserInfo(){
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941",
				"", "1", "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}

	
	protected UserInfo createUser31UserInfo(Long userId, Long teamId){
		UserInfo userInfo = new UserInfo(userId, "31", "user31", "user35@raritan.com",
				"User31", "dcTrack", "942",
				"", "1", "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);
		//user31 belongs to teamid 556 (IT Software Development)
		Session mySession = sf.getCurrentSession();
		LkuData teamLookup = (LkuData)mySession.load(LkuData.class, teamId);
		//Important: Print something to avoid lazy loading that might mess up things when we
		//delete session
		System.out.println("teamLookup=" + teamLookup.getLkuValue());
		userInfo.setTeamLookup(teamLookup);

		return userInfo;
	}


	protected UserInfo createManagerUserInfo(){
		UserInfo userInfo = new UserInfo(6L, "20", "dctrack1", "user2@raritan.com",
				"User01", "dcTrack", "942",
				"", "", "en-US",
				"all_users", "ABC", 5256000, true);
		return userInfo;
	}

	protected UserInfo createMemberUserInfo(){
		UserInfo userInfo = new UserInfo(7L, "21", "dctrack2", "user3@raritan.com",
				"User02", "dcTrack", "943",
				"", "", "en-US",
				"all_users", "DEQ", 5256000, true);
		return userInfo;
	}

	protected UserInfo createViewerUserInfo(){
		UserInfo userInfo = new UserInfo(16L, "30", "user06", "user12@raritan.com",
				"User06", "dcTrack", "944",
				"", "", "en-US",
				"all_users", "DEQ", 5256000, true);
		return userInfo;
	}

	private long createOrEditItem(UserInfo userInfo, long state, long itemId, Long ownerId, Long teamId) throws BusinessValidationException, ClassNotFoundException, Throwable{
		List<ValueIdDTO> dto = createRackableItemValueIdDTOList("BOZANA9", state);
		if( ownerId > 0) addOwnerIdToItemValueDTOList(ownerId, dto);
		if(teamId > 0) addTeamIdToItemValueDTOList(teamId, dto);
		Map<String,UiComponentDTO> componentDTOMap = null;
    	//Create ValueDTO list
    	componentDTOMap = itemHome.saveItem(itemId, dto, userInfo);
    	AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    	System.out.println("===== New item moved to state: " + state);
    	printUiComponentDTOMap(componentDTOMap);

    	UiComponentDTO cmp = componentDTOMap.get("tiName");
    	AssertJUnit.assertTrue(cmp != null);
    	//get Item id
    	Long retItemId = (Long) cmp.getUiValueIdField().getValueId();
    	System.out.println("===== Item's id=" + retItemId);
    	AssertJUnit.assertTrue( retItemId.longValue() != -1L);

    	//check item's status
    	UiComponentDTO cmp2 = componentDTOMap.get("cmbStatus");
    	AssertJUnit.assertTrue(cmp2 != null);
    	long status = (Long) cmp2.getUiValueIdField().getValueId();
    	//verify it was indeed moved to the required state
    	AssertJUnit.assertTrue(status == state);

    	return retItemId;
	}

	private long createOrEditItemNoName(UserInfo userInfo, long state, long itemId, Long ownerId, Long teamId) throws BusinessValidationException, ClassNotFoundException, Throwable{
		List<ValueIdDTO> dto = createRackableItemValueIdDTOList("", state);

		if( ownerId > 0) addOwnerIdToItemValueDTOList(ownerId, dto);
		if(teamId > 0) addTeamIdToItemValueDTOList(teamId, dto);

		Map<String,UiComponentDTO> componentDTOMap = null;

    	//Create ValueDTO list
    	componentDTOMap = itemHome.saveItem(itemId, dto, userInfo);

    	AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    	System.out.println("===== New item moved to state: " + state);
    	printUiComponentDTOMap(componentDTOMap);

    	UiComponentDTO cmp = componentDTOMap.get("tiName");
    	AssertJUnit.assertTrue(cmp != null);
    	//get Item id
    	Long retItemId = (Long) cmp.getUiValueIdField().getValueId();
    	System.out.println("===== Item's id=" + retItemId);
    	AssertJUnit.assertTrue( retItemId.longValue() != -1L);

    	//check item's status
    	UiComponentDTO cmp2 = componentDTOMap.get("cmbStatus");
    	AssertJUnit.assertTrue(cmp2 != null);
    	long status = (Long) cmp2.getUiValueIdField().getValueId();
    	//verify it was indeed moved to the required state
    	AssertJUnit.assertTrue(status == state);

    	return retItemId;
	}

	private void moveRackableItemNotPermittedFromToState(long state1, long state2, UserInfo userInfo, Long owner, Long teamId) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long retItemId = -1L;
		boolean userPermitted = true;
		boolean creationSuccess = false;
		try{
			//create item
			long itemId = -1L;
System.out.println("########### BEFORE ITEM CRFEATED");
			retItemId = createOrEditItem(userInfo, state1, itemId, owner, teamId);
System.out.println("################# ITEM CREATED");
			itemId = retItemId;
			creationSuccess = true;
			cleanSession();
			//edit item -> should fail
			retItemId = createOrEditItem(userInfo, state2, itemId, owner, teamId);
			AssertJUnit.assertTrue(retItemId == itemId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
    		userPermitted = false;
    		AssertJUnit.assertTrue(creationSuccess == true);
    		System.out.println("=== test good: User: " + userInfo.getUserName() + " is not permitted" +
    				"to move item from " + state1 + " to state: " + state2);
    	}
    	finally {
    		//Delete the item
    		if( retItemId != -1L) cleanRackableItem(retItemId);
    	}
		if (state1 == state2) {
			AssertJUnit.assertTrue(userPermitted == false);
			return;
		}
		AssertJUnit.assertTrue(userPermitted == false);
	}


	
	private void moveRackableItemFromToState(long state1, long state2, UserInfo userInfo, Long owner, Long teamId) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long retItemId = -1L;
		try{
			//create item
			long itemId = -1L;
			retItemId = createOrEditItem(userInfo, state1, itemId, owner, teamId);
			itemId = retItemId;
			// cleanSession();
			
			//edit item
			System.out.println("===== Before edit");
			retItemId = createOrEditItem(userInfo, state2, itemId, owner, teamId);
			AssertJUnit.assertTrue(retItemId == itemId);
			System.out.println("===== After edit");
		}
    	finally {
    		//Delete the item
    		if( retItemId != -1L) cleanRackableItem(retItemId);
    	}
	}

	private void moveRackableItemFromToState3(long state1, long state2, long state3, UserInfo userInfo) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long retItemId = -1L;
		try{
			//create item and moveit into state1
			long itemId = -1L;
			retItemId = createOrEditItem(userInfo, state1, itemId, -1L, -1L);

			List<ValueIdDTO> dto2 = createRackableItemValueIdDTOList("BOZANA9", state2);
			itemId = retItemId;
			session.flush();
			// cleanSession();
			
			//edit item and move it to state 2
			retItemId = createOrEditItem(userInfo, state2, itemId, -1L, -1L);
			AssertJUnit.assertTrue(retItemId == itemId);
			// cleanSession();
			
			List<ValueIdDTO> dto3 = createRackableItemValueIdDTOList("BOZANA9", state3);
			retItemId = createOrEditItem(userInfo, state3, itemId, -1L, -1L);
			AssertJUnit.assertTrue(retItemId == itemId);
		}
    	finally {
    		//Delete the item
    		if( retItemId != -1L) cleanRackableItem(retItemId);
    	}
	}

	/**
	 * IMPORTANT!!!: After saving an item you have to close the session and open it again. Otherwise
	 * getAvailbleUPosition will show position as still available since cabinet will report that UPosition 
	 * as still available (and operation will fail when doing xor with the mask)
	 * although the data in the DB will show it as occupied. This is happening due to triggers:
	 * When item is saved initially, trigger will be generated and will update layout position 
	 * in cabinet. So DB will have correct data. However, hiberante will not know about this
	 * update since we did not update anything through hibernate, so data in memory will be wrong and
	 * will still show this position as available. The only way to handle this situation is to
	 * close the session after first save and open it again. Then hibernate will have to 
	 * read new cabinet data from the DB.
	 *  
	 * @throws Throwable
	 */
	private void cleanSession() throws Throwable{
		super.tearDown();
		super.setUp();
	}

	
	private void moveRackableItemFromToStateNoName(long state1, long state2, UserInfo userInfo, Long owner, Long teamId) throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long retItemId = -1L;
		Long itemId = -1L;
		try{
			//create item
			itemId = createOrEditItem(userInfo, state1, -1L, owner, teamId);
			// cleanSession();
			//edit item
			retItemId = createOrEditItemNoName(userInfo, state2, itemId, owner, teamId);
			AssertJUnit.assertTrue(retItemId.longValue() == itemId.longValue());
		}
    	finally {
    		//Delete the item
    		if( retItemId != -1L) cleanRackableItem(retItemId);
    		if( itemId != -1L && itemId != retItemId ) cleanRackableItem(itemId);
    	}
	}
	
	

	//Moving item from New to Storage state
	@Test
	public void testGKMoveFromNewToStorage() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE, userInfo, -1L, -1L);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	//Moving item from New to Archived state
	@Test
	public void testGKMoveFromNewToArchived() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED, userInfo, -1L, -1L);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testGKMoveFromNewToArchivedNoName() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		try{
			moveRackableItemFromToStateNoName(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED, userInfo, -1L, -1L);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			//throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void testNonTeamMemberMoveFromNewToArchivedNoNameNotPermitted() throws BusinessValidationException, Throwable{
		Long userCreator = 54L; //user31
		Long userCreatorTeamId = 556L; //IT Software Development
		Long owner = -1L;
		Long ownerTeamId = 557L;
		UserInfo userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );
		Long retItemId = -1L;
		try{
			//create item
			retItemId = createOrEditItem(userInfo, SystemLookup.ItemStatus.PLANNED, -1L, owner, ownerTeamId);
			cleanSession();
			
			//reacrete userInfo so that new session is propagated correctly
			userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );
			//edit item
			retItemId = createOrEditItemNoName(userInfo, SystemLookup.ItemStatus.ARCHIVED, retItemId, owner, ownerTeamId);
			//we are supposed to fail
			AssertJUnit.assertTrue(false);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			System.out.println("TEST PASSED");
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
    	finally {
    		//Delete the item
    		if( retItemId != -1L) cleanRackableItem(retItemId);
    	}
	}

	//Moving item from New to Archived then back to New state
	@Test
	public void testGKMoveFromArchivedToNew() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		try{
			moveRackableItemFromToState3(SystemLookup.ItemStatus.PLANNED,
					SystemLookup.ItemStatus.ARCHIVED,
					SystemLookup.ItemStatus.PLANNED, userInfo);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	//Moving item from New to Installed state
	@Test
	public void testGKMoveFromNewToInstall() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED, userInfo, -1L, -1L);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}


	private void moveRackableItemFromToStateNotPermitted(long state1, long state2, UserInfo userInfo) throws BusinessValidationException, ClassNotFoundException, Throwable{
		//create item
		long itemId = -1L;
		Long retItemId = -1L;
		boolean userPermitted = false;
		boolean creationSuccess=false;
		try{
			retItemId = createOrEditItem(userInfo, state1, itemId, -1L, -1L);
			creationSuccess = true;
			itemId = retItemId;
			cleanSession();

    		retItemId = createOrEditItem(userInfo, state2, itemId, -1L, -1L);
    		userPermitted = true;
    	}catch(BusinessValidationException e){
    		userPermitted = false;
    		AssertJUnit.assertTrue(creationSuccess == false);
    		AssertJUnit.assertTrue(userPermitted == false);
    		System.out.println("=== test good: User: " + userInfo.getUserName() + " is not permitted" +
    				"to move item from " + state1 + " to state: " + state2);
    	}
    	finally{
    		//Delete the item
    		if( retItemId != -1L ) cleanRackableItem(retItemId);
    	}
		AssertJUnit.assertTrue(userPermitted == false);
		AssertJUnit.assertTrue(creationSuccess == false);
	}

	//Moving item from New to Installed state
	@Test
	public void testNotPermittedUserMoveFromNewToInstall() throws BusinessValidationException, ClassNotFoundException, Throwable{
		UserInfo userInfo = createViewerUserInfo();
		try{
			moveRackableItemFromToStateNotPermitted(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED, userInfo);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}



	@Test
	public void testGetNewItemAllowableStatesForAdmin() throws BusinessValidationException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		Long itemId = 0L;
		try{
			itemId = createOrEditItem(userInfo, SystemLookup.ItemStatus.PLANNED, -1L, -1L, -1L);
			Long modelId = getItemModelId(itemId);
			List<SystemLookupDTO> list = itemHome.getStatusLookupForCurrentState(itemId, modelId, userInfo);
			AssertJUnit.assertTrue(list.size() > 0);
			for( SystemLookupDTO dto : list){
				System.out.println("label=" + dto.getLabel() + ", data=" + dto.getData());
			}
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		if( itemId > 0L) cleanRackableItem(itemId);
	}


	@Test
	public void testUserOwnerMovesItemFromNewToEdit() throws BusinessValidationException, Throwable{
		UserInfo userInfo = createUser31UserInfo(54L, 556L ); //user31 (id=54), teamId 556
		Long owner = 54L; //user31
		Long teamId = 556L; //IT Software Development

		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED, userInfo, owner, teamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testNonOwnerMovesItemFromNewToEdit() throws BusinessValidationException, Throwable{
		Long userCreator = 54L; //user31
		Long userCreatorTeamId = 556L; //IT Software Development
		Long owner = 61L; //user37
		Long ownerTeamId = 557L;
		UserInfo userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );

		try{
			moveRackableItemNotPermittedFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED, userInfo, owner, ownerTeamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testUserOwnerMovesItemFromNewToStorage() throws BusinessValidationException, Throwable{
		Long owner = 54L; //user31
		Long teamId = 556L; //IT Software Development
		UserInfo userInfo = createUser31UserInfo(owner, teamId );

		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.IN_STORAGE, userInfo, owner, teamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testNonOwnerMovesItemFromNewToStorage() throws BusinessValidationException, Throwable{
		Long userCreator = 54L; //user31
		Long userCreatorTeamId = 556L; //IT Software Development
		Long owner = 61L; //user37
		Long ownerTeamId = 557L;
		UserInfo userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );

		try{
			moveRackableItemNotPermittedFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.IN_STORAGE, userInfo, owner, ownerTeamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}

	}

	@Test
	public void testTeamMemberMovesItemFromNewToStorage() throws BusinessValidationException, Throwable{
		UserInfo userInfo = createUser31UserInfo(54L, 556L ); //user31 (id=54), teamId 556
		Long owner = -1L;
		Long teamId = 556L; //IT Software Development

		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.IN_STORAGE, userInfo, owner, teamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}


	}

	@Test
	public void testTeamMemberMovesItemFromNewToEdit() throws BusinessValidationException, Throwable{
		UserInfo userInfo = createUser31UserInfo(54L, 556L ); //user31 (id=54), teamId 556
		Long owner = -1L;
		Long teamId = 556L; //IT Software Development

		try{
			moveRackableItemFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED, userInfo, owner, teamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}

	}

	@Test
	public void testNonTeamMemberMovesItemFromNewToStorage() throws BusinessValidationException, Throwable{
		Long userCreator = 54L; //user31
		Long userCreatorTeamId = 556L; //IT Software Development
		Long owner = -1L;
		Long ownerTeamId = 557L;
		UserInfo userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );

		try{
			moveRackableItemNotPermittedFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.IN_STORAGE, userInfo, owner, ownerTeamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testNonTeamMemberMovesItemFromNewToEdit() throws BusinessValidationException, Throwable{
		Long userCreator = 54L; //user31
		Long userCreatorTeamId = 556L; //IT Software Development
		Long owner = -1L;
		Long ownerTeamId = 557L;
		UserInfo userInfo = createUser31UserInfo(userCreator, userCreatorTeamId );

		try{
			moveRackableItemNotPermittedFromToState(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED, userInfo, owner, ownerTeamId);
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}

	}
	
	private Long getItemModelId(Long itemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		UiComponentDTO itemModelField = item.get("cmbModel");
		assertNotNull(itemModelField);
		String statusStr = (String) itemModelField.getUiValueIdField().getValue();
		Long modelId = (Long) itemModelField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + modelId.toString() + "str = " + statusStr);
		
		return modelId;
	}


	@Test
	public void testGetArchivedItemAllowableStatesForAdmin() throws BusinessValidationException, Throwable{
		UserInfo userInfo = createGatekeeperUserInfo();
		Long retItemId = -1L;
		try{
			//create item and moveit into state1
			long itemId = -1L;
			retItemId = createOrEditItem(userInfo, SystemLookup.ItemStatus.PLANNED, itemId, -1L, -1L);
			itemId = retItemId;	
			cleanSession();

			//edit item and move it to state 2
			retItemId = createOrEditItem(userInfo, SystemLookup.ItemStatus.IN_STORAGE, itemId, -1L, -1L);
			AssertJUnit.assertTrue(retItemId == itemId);
			Long modelId = getItemModelId(itemId);
			List<SystemLookupDTO> list = itemHome.getStatusLookupForCurrentState(itemId, modelId, userInfo);
			AssertJUnit.assertTrue(list.size() > 0);
			for( SystemLookupDTO dto : list){
				System.out.println("label=" + dto.getLabel() + ", data=" + dto.getData());
			}
		}catch(BusinessValidationException e){
			List<String> errors = e.getValidationErrors();
			System.out.println("Errors:");
			for (String s : errors){
				System.out.println(s);
			}
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		if( retItemId > 0L) cleanRackableItem(retItemId);
	}



}
