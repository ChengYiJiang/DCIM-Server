package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class NetworkItems extends TestBase {
	private Long mChassisId;
	private boolean mPropagateFields;
	private Map<Long, Long> mBladeIds;
	private final Long mMaxNumBlades = 2L;
	public NetworkItems(){
		mBladeIds = new HashMap();
	}
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		mPropagateFields = false;
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

    private List<ValueIdDTO> createValueIdDTOList(String newName, long make, long model, 
    		String mounting, long railsUsed, String myClass, Long myCabinet, long myChassis, 
    		long uPosition, String slotPosition, boolean skipValidation){
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();

		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.PLANNED);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(make)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(model));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiClass");
		dto.setData(myClass);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiMounting");
		dto.setData(mounting);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("radioRailsUsed");
		dto.setData(railsUsed);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData(newName);
		valueIdDTOList.add(dto);

		//Placement Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(new Long(1));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbCabinet");
		dto.setData(myCabinet);
		valueIdDTOList.add(dto);
		
    	dto = new ValueIdDTO();
    	dto.setLabel("cmbUPosition");
    	dto.setData(uPosition);
    	valueIdDTOList.add(dto);

		//If this is blade, add chassis info
		if( myChassis != -1L){
			dto = new ValueIdDTO();
			dto.setLabel("cmbChassis");
			dto.setData(myChassis);
			valueIdDTOList.add(dto);
			
			dto = new ValueIdDTO();
			dto.setLabel("cmbSlotPosition");
			dto.setData(slotPosition);
			valueIdDTOList.add(dto);
			
		}
		if( skipValidation){//skip blades validation
			dto = new ValueIdDTO();
			dto.setLabel("tiNotes");
			dto.setData("");
			valueIdDTOList.add(dto);
			
			dto = new ValueIdDTO();
			dto.setLabel("_tiSkipValidation");
			dto.setData(true);
			valueIdDTOList.add(dto);			
		}
		return valueIdDTOList;
    }

    private Map<String,UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> valueIdDTOList) throws BusinessValidationException, ClassNotFoundException, Throwable{
    	Map<String,UiComponentDTO> componentDTOMap = null;
    		
		UserInfo userInfo = createGatekeeperUserInfo();
    	componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, userInfo);
		return componentDTOMap;
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
	
	@Test(enabled=false)
    public void saveChassis() throws BusinessValidationException, ClassNotFoundException, Throwable{
    	String itemName = "00BRH_Chassis";
    	Long itemId = new Long(-1L);
    	long make = 17; //Cisco Systems
    	long model = 45; //Catalyst C6509-E
    	String mounting = "Rackable / Chassis";
    	String myClass = "Network / Chassis";
    	long myCabinet = 10;
    	long myChassis = -1L;
    	long uPosition = -2L;
    	boolean skipValidation = false;
    	String slotPosition = String.valueOf(-1L);
    	long railsUsed = 8001; // Mounting Rails = Front 
    	
    	
    	
    	try{
    		//Create ValueDTO list
    		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName, make, model, mounting, railsUsed,
    				myClass, myCabinet, myChassis, uPosition, slotPosition, skipValidation);
    		Map<String,UiComponentDTO> componentDTOMap = saveItem(itemId, valueIdDTOList);
    		AssertJUnit.assertNotNull(componentDTOMap);
    		System.out.println("== list size=" + componentDTOMap.size());
    		AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    		System.out.println("--testSaveDevice():");
    		printUiComponentDTOMap(componentDTOMap);

    		mChassisId = -1L;
    		Item item = getItemFromDTOMap(componentDTOMap);
    		mChassisId = item.getItemId();
    	}catch(BusinessValidationException e){
    		printBusinessExceptionMsg(e);
    		throw e;
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }
	
	
	@Test(enabled=false)
	public void saveBlades() throws BusinessValidationException, ClassNotFoundException, Throwable{
		for( Long bladeId =1L; bladeId <= mMaxNumBlades; bladeId++){
			saveBlade(bladeId);
		}
	}
	
	@Test
	public void testSave() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			saveChassis();
			saveBlades();
		}finally{
			cleanNetworkItems();
		}
	}
	
	@Test
	public void testPropagateChassisPropsToBlades() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			saveChassis();
			saveBlades();
			mPropagateFields = true;
			modifyChassis();
			verifyFieldsPropagated();
		}finally{
			cleanNetworkItems();
		}
	}
	
	@Test
	public void testPropagateBladePropsToAll() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			saveChassis();
			saveBlades();
			Long bladeId = 1L;
			mPropagateFields = true;
			modifyBlade(bladeId);
			verifyFieldsPropagated();
		}finally{
			cleanNetworkItems();
		}		
	}
	
	@Test
	public void testDoNotPropagateChassisPropsToBlades() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			saveChassis();
			saveBlades();
			mPropagateFields = false;
			modifyChassis();
			verifyFieldsNotPropagated();
		}finally{
			cleanNetworkItems();
		}
	}
	
	@Test
	public void testDoNotPropagateBladePropsToAll() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			saveChassis();
			saveBlades();
			Long bladeId = 1L;
			mPropagateFields = false;
			modifyBlade(bladeId);
			verifyFieldsNotPropagated();
		}finally{
			cleanNetworkItems();
		}		
	}
	

	private void verifyFieldsPropagated() {
		ItItem chassis = (ItItem) session.load(ItItem.class, mChassisId);
		ItemServiceDetails chassisISD = chassis.getItemServiceDetails();
		for (Map.Entry<Long, Long> bladeId : mBladeIds.entrySet()) {
			ItItem blade = (ItItem) session.load(ItItem.class, bladeId.getValue());
			ItemServiceDetails bladeISD = blade.getItemServiceDetails();
			AssertJUnit.assertTrue(blade.getItemAlias().equals(chassis.getItemAlias()));
			AssertJUnit.assertTrue(bladeISD.getFunctionLookup().equals(chassisISD.getFunctionLookup()));
			AssertJUnit.assertTrue(bladeISD.getItemAdminTeamLookup().equals(chassisISD.getItemAdminTeamLookup()));
			AssertJUnit.assertTrue(bladeISD.getItemAdminUser().equals(chassisISD.getItemAdminUser()));
			AssertJUnit.assertTrue(bladeISD.getPurposeLookup().equals(chassisISD.getPurposeLookup()));
			AssertJUnit.assertTrue(bladeISD.getDepartmentLookup().equals(chassisISD.getDepartmentLookup()));
			AssertJUnit.assertTrue(blade.getOsiLayerLookup() == chassis.getOsiLayerLookup());
			AssertJUnit.assertTrue(blade.getOsLookup().equals(chassis.getOsLookup()));
		}
	}

	private void verifyFieldsNotPropagated() {
		ItItem chassis = (ItItem) session.load(ItItem.class, mChassisId);
		ItemServiceDetails chassisISD = chassis.getItemServiceDetails();
		for (Map.Entry<Long, Long> bladeId : mBladeIds.entrySet()) {
			ItItem blade = (ItItem) session.load(ItItem.class, bladeId.getValue());
			ItemServiceDetails bladeISD = blade.getItemServiceDetails();
			if( blade.getItemAlias() != null && chassis.getItemAlias() != null ){
				AssertJUnit.assertFalse(blade.getItemAlias().equals(chassis.getItemAlias()));
			}
			if(bladeISD.getFunctionLookup() != null && chassisISD.getFunctionLookup() != null){
				AssertJUnit.assertFalse(bladeISD.getFunctionLookup().equals(chassisISD.getFunctionLookup()));
			}
			if(bladeISD.getItemAdminTeamLookup() != null && chassisISD.getItemAdminTeamLookup() != null){
				AssertJUnit.assertFalse(bladeISD.getItemAdminTeamLookup().equals(chassisISD.getItemAdminTeamLookup()));
			}
			if(bladeISD.getItemAdminUser() != null && chassisISD.getItemAdminUser() != null){
				AssertJUnit.assertFalse(bladeISD.getItemAdminUser().equals(chassisISD.getItemAdminUser()));
			}
			if(bladeISD.getPurposeLookup() != null && chassisISD.getPurposeLookup() != null){
				AssertJUnit.assertFalse(bladeISD.getPurposeLookup().equals(chassisISD.getPurposeLookup()));
			}
			if(bladeISD.getDepartmentLookup() != null &&  chassisISD.getDepartmentLookup() != null){
				AssertJUnit.assertFalse(bladeISD.getDepartmentLookup().equals(chassisISD.getDepartmentLookup()));
			}
/*			if(blade.getOsiLayerLookup() != null &&  chassis.getOsiLayerLookup() != null){
				AssertJUnit.assertFalse(blade.getOsiLayerLookup().equals(chassis.getOsiLayerLookup()));
			}
*/			if(blade.getOsLookup() != null && chassis.getOsLookup() != null ){
				AssertJUnit.assertFalse(blade.getOsLookup().equals(chassis.getOsLookup()));
			}
		}
	}

	private void modifyChassis() throws Throwable {
    	String itemName = "00BRH_Chassis";
    	Long itemId = mChassisId;
    	long make = 17; //Cisco Systems
    	long model = 45; //Catalyst C6509-E
    	String mounting = "Rackable / Chassis";
    	String myClass = "Network / Chassis";
    	long myCabinet = 10;
    	long myChassis = -1L;
    	long uPosition = -2L;
    	boolean skipValidation = false;
    	String slotPosition = String.valueOf(-1L);
    	long railsUsed = 8001;
    	
    	try{
    		//Create ValueDTO list
    		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName, make, model, mounting, railsUsed, 
    				myClass, myCabinet, myChassis, uPosition, slotPosition, skipValidation);
    		valueIdDTOList = modifyValueIdDTOList(valueIdDTOList);	
    		Map<String,UiComponentDTO> componentDTOMap = saveItem(itemId, valueIdDTOList);
    		AssertJUnit.assertNotNull(componentDTOMap);
    		System.out.println("== list size=" + componentDTOMap.size());
    		AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    		System.out.println("--testSaveDevice():");
    		printUiComponentDTOMap(componentDTOMap);

    		mChassisId = -1L;
    		Item item = getItemFromDTOMap(componentDTOMap);
    		mChassisId = item.getItemId();
    	}catch(BusinessValidationException e){
    		printBusinessExceptionMsg(e);
    		throw e;
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}

	}

	private List<ValueIdDTO> modifyValueIdDTOList(
			List<ValueIdDTO> valueIdDTOList){
		
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel("cmbOSILayer");
		dto.setData(new Long(6043));
		valueIdDTOList.add(dto); //Layer 3
		
    	dto = new ValueIdDTO();
    	dto.setLabel("cmbOperatingSystem");
    	dto.setData(new Long(1011)); //Linux 
    	valueIdDTOList.add(dto);
    	
    	dto = new ValueIdDTO();
    	dto.setLabel("tiAlias");
    	dto.setData("BozanaAlias");
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbType"); //Network Switch
    	dto.setData(new Long(8));
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbFunction");
    	dto.setData(new Long(984)); //Access Network
    	valueIdDTOList.add(dto);
    	
    	dto = new ValueIdDTO();
    	dto.setLabel("cmbSystemAdmin");
    	dto.setData("20"); //dcTrack, User01
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbSystemAdminTeam");
    	dto.setData(new Long(561)); //Raritan Software Dev
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
    	dto.setLabel("cmbCustomer");
    	dto.setData(new Long(605)); //Biology
    	valueIdDTOList.add(dto);

    	dto = new ValueIdDTO();
        dto.setLabel("cbPropagate");
    	if( mPropagateFields){
    		dto.setData(true);
    	}else{
    		dto.setData(false);
    	}
        valueIdDTOList.add(dto);	
    	
    	return valueIdDTOList;
	}

	private void printBusinessExceptionMsg( BusinessValidationException e)
	{
		List<String> errors = e.getValidationErrors();
		System.out.println("Errors: ");
		for (String s : errors){
			System.out.println(s);
		}
		List<String> warnings = e.getValidationWarnings();
		System.out.println("Warnings: ");
		for (String s : warnings){
			System.out.println(s);
		}
		e.printStackTrace();		
	}
	
	/* 
	//removed from test case list since stackable items are not complete yet
	 private void saveStackableItem( long stackableId ){
    	Long itemId = new Long(-1L);	
    	long make = 43; //Juniper Network
    	long model = 69; //NetScreen 204
    	String mounting = "Rackable / Fixed";
    	String myClass = "Network / NetworkStack"; 
    	long myCabinet = 10;
    	long myChassis = mChassisId;
    	long uPosition = -1L;
    	String slotPosition = String.valueOf(bladeId);
    	StringBuffer itemName = new StringBuffer();
    	itemName.append("00BRH_Stack_");
    	itemName.append(bladeId);
    	boolean skipValidation = false;
    	Map<String,UiComponentDTO> componentDTOMap;
    	Long railsUsed = 8001;
    	int numRetries = 2;
    	
    	for ( int i=0; i< numRetries; i++){
    		try{
    			if ( i > 0 ) skipValidation = true;

        		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName.toString(), make, model, mounting, railsUsed,
        				myClass, myCabinet, myChassis, uPosition, slotPosition, skipValidation);
      
    			componentDTOMap = saveItem(itemId, valueIdDTOList);
    	    	AssertJUnit.assertNotNull(componentDTOMap);
    			System.out.println("== list size=" + componentDTOMap.size());
    			AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    			System.out.println("--testSaveDevice():");
    			printUiComponentDTOMap(componentDTOMap);

    			Item item = getItemFromDTOMap(componentDTOMap);
    			mBladeIds.put(bladeId, item.getItemId());
    		}catch(BusinessValidationException e){
    			printBusinessExceptionMsg(e);
    			if( i == 2) throw e;
    		}catch(Exception e){
    			e.printStackTrace();
    			throw e;
    		}
    	}		
	}
	*/
	
	//try to save blade that does not belong to any chassis and force field propagation
	@Test
	public void testSaveBladeAndPropagate() throws BusinessValidationException, ClassNotFoundException, Throwable{
		try{
			mPropagateFields = true;
			mChassisId = -1L;
			saveBlade( 5L );
		}finally{
			cleanNetworkItems();
		}
	}
	
	//IMPORTANT NOTE: saveBlade does not set (left blank) the filelds that are propogated.  
	private void saveBlade( long bladeId ) throws BusinessValidationException, ClassNotFoundException, Throwable{
    	Long itemId = new Long(-1L);	
    	long make = 17; //Cisco Systems
    	long model = 5788; //WS-X6748-GE-TX
    	String mounting = "Blade / Full";
    	String myClass = "Network / Blade"; 
    	long myCabinet = 10;
    	long myChassis = mChassisId;
    	long uPosition = -1L;
    	mPropagateFields = true;
    	String slotPosition = String.valueOf(bladeId);
    	StringBuffer itemName = new StringBuffer();
    	itemName.append("00BRH_Blade_");
    	itemName.append(bladeId);
    	boolean skipValidation = false;
    	long railsUsed = 8001;
    	Map<String,UiComponentDTO> componentDTOMap;
    	int numRetries = 2;
    	for ( int i=0; i< numRetries; i++){
    		try{
    			if ( i > 0 ) skipValidation = true;

        		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName.toString(), make, model, mounting, railsUsed,
        				myClass, myCabinet, myChassis, uPosition, slotPosition, skipValidation);
      
    			componentDTOMap = saveItem(itemId, valueIdDTOList);
    	    	AssertJUnit.assertNotNull(componentDTOMap);
    			System.out.println("== list size=" + componentDTOMap.size());
    			AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    			System.out.println("--testSaveDevice():");
    			printUiComponentDTOMap(componentDTOMap);

    			Item item = getItemFromDTOMap(componentDTOMap);
    			mBladeIds.put(bladeId, item.getItemId());
    		}catch(BusinessValidationException e){
    			printBusinessExceptionMsg(e);
    			if( i == 2) throw e;
    		}catch(Exception e){
    			e.printStackTrace();
    			throw e;
    		}
    	}
	}
	
	private void modifyBlade( long bladeId ) throws BusinessValidationException, ClassNotFoundException, Throwable{
    	Long itemId = mBladeIds.get(bladeId);	
    	long make = 17; //Cisco Systems
    	long model = 5788; //WS-X6748-GE-TX
    	String mounting = "Blade / Full";
    	String myClass = "Network / Blade"; 
    	long myCabinet = 10;
    	long myChassis = mChassisId;
    	long uPosition = -1L;
    	String slotPosition = String.valueOf(bladeId);
    	StringBuffer itemName = new StringBuffer();
    	itemName.append("00BRH_Blade_");
    	itemName.append(bladeId);
    	long railsUsed = 8001;
    	boolean skipValidation = false;
    	Map<String,UiComponentDTO> componentDTOMap;
    	int numRetries = 2;
    	for ( int i=0; i< numRetries; i++){
    		try{
    			if ( i > 0 ) skipValidation = true;

        		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName.toString(), make, model, mounting, railsUsed, 
        				myClass, myCabinet, myChassis, uPosition, slotPosition, skipValidation);
        		valueIdDTOList = modifyValueIdDTOList(valueIdDTOList);
        		
    			componentDTOMap = saveItem(itemId, valueIdDTOList);
    	    	AssertJUnit.assertNotNull(componentDTOMap);
    			System.out.println("== list size=" + componentDTOMap.size());
    			AssertJUnit.assertTrue(componentDTOMap.size() > 0);
    			System.out.println("--testSaveDevice():");
    			printUiComponentDTOMap(componentDTOMap);

    			Item item = getItemFromDTOMap(componentDTOMap);
    			mBladeIds.put(bladeId, item.getItemId());
    		}catch(BusinessValidationException e){
    			printBusinessExceptionMsg(e);
    			if( i == 2) throw e;
    		}catch(Exception e){
    			e.printStackTrace();
    			throw e;
    		}
    	}
	}
    private void cleanNetworkItems(){
    	System.out.println("$$$$$$ cleanup");
 
		try{
			itemHome.deleteItem(mChassisId, false, null);
			mChassisId = -1L;
		}
		catch(Exception ex){
			ex.printStackTrace();
		} catch (BusinessValidationException e) {
			e.printStackTrace();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		//JB: deleteItem() call above with chassisId is deleting all blades in that chassis	
		// No need to cll delete() seperately for each bladeId
/*		
		for (Map.Entry<Long, Long> entry : mBladeIds.entrySet()) {
			try{
				itemHome.deleteItem(entry.getValue().longValue(), false, null);
			}catch(Exception ex){
				ex.printStackTrace();
			} catch (DataAccessException e) {
				e.printStackTrace();
			} catch (BusinessValidationException e) {
				e.printStackTrace();
			}
		}
	*/
		mBladeIds.clear();
    }
	
	private Item getItemFromDTOMap(Map<String, UiComponentDTO> cabinetItemMap) {
		long itemId = -1;
		Item retval = null;
		UiComponentDTO componentDTO = cabinetItemMap.get("tiName");
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId =(Long) uiValueIdField.getValueId(); 
		}
		if( itemId > 0){
			Session session = sf.getCurrentSession();
			retval = (Item) session.load(Item.class, itemId);
		}
		return retval;
	}
	
	@Test
	public final void proba(){
		System.out.println("Hello world!");
		
	}
}
