package com.raritan.tdz.chassis.home;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import com.raritan.tdz.chassis.service.ChassisService;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dto.BladeDTO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class ChassisTest extends TestBase {
	
	ChassisHome chassisHome;
	
	ItemService itemService;

	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemService = (ItemService)ctx.getBean("itemService");
	    chassisHome = (ChassisHome)ctx.getBean("chassisHome");
	}
	
	@AfterMethod
	public void afterMethod() {
	}
  
	//@Test
	public void updateChassisLayoutTest() throws DataAccessException, Throwable {
		long chassisId = 3L;
		chassisHome.updateChassisLayout(chassisId);
	}

//	@Test
//	public void updateChassisDefinitionTest() throws DataAccessException, Throwable {
//		long chassisModelId = 15L;
//		chassisHome.updateChassisDefinition(chassisModelId);
//
//	}
	
	@Test
	public void updateChassisLayout_FirmwareUpdateTestCase() throws DataAccessException, Throwable {
		long UPDATE_ALL_MODEL_CHASSIS_LAYOUT_IDENTIFIER = -20120823;
		chassisHome.updateChassisLayout(UPDATE_ALL_MODEL_CHASSIS_LAYOUT_IDENTIFIER);
	}
	
	@Test
	public void updateAllChassisLayoutTest() throws DataAccessException, Throwable {
		chassisHome.updateAllChassisLayout();
	}

	@Test
	public void updateAllChassisGroupNameTest() throws DataAccessException, Throwable {
		chassisHome.updateAllChassisGroupName();
	}

	//@Test
	public void getAllBladesForItemTest() throws Throwable {
		long itemId = 3L;
		Collection<ItItem> itItems = chassisHome.getAllBladesForItem(itemId);
		System.out.println("dto list size = " + itItems.size());
		
		itemId = 6L;
		itItems = chassisHome.getAllBladesForItem(itemId);
		System.out.println("dto list size = " + itItems.size());
	}
	
	@Test
	public void getAllBladeItemTest() throws Throwable {
		long itemId = 5081L; // chassis
		List<BladeDTO> dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);
		
		itemId = 5077L; // blade
		dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);

		itemId = 5116L; // cabinet
		dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);
		
		itemId = 5170L; // chassis
		dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);
		
		itemId = 5176L;
		dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);

		itemId = 5164L; // blade no slot position assigned
		dtos = itemService.getAllBladeItem(itemId);
		printBladeDTOs(itemId, dtos);
	}
	
	@Test
	public void getAllChassisInCabinetTest() {
		long cabinetItemId = 3L;
		List<ValueIdDTO> dtos = itemService.getAllChassisInCabinet(cabinetItemId, SystemLookup.Class.NETWORK);
		for (ValueIdDTO dto: dtos) {
			System.out.println("chassis id = " + (Long) (dto.getData()) + " chassis label = " + dto.getLabel());
		}
		
		cabinetItemId = 14L;
		dtos = itemService.getAllChassisInCabinet(cabinetItemId, SystemLookup.Class.NETWORK);
		for (ValueIdDTO dto: dtos) {
			System.out.println("chassis id = " + (Long) (dto.getData()) + " chassis label = " + dto.getLabel());
		}
		
		cabinetItemId = 3L;
		dtos = itemService.getAllChassisInCabinet(cabinetItemId, SystemLookup.Class.DEVICE);
		for (ValueIdDTO dto: dtos) {
			System.out.println("chassis id = " + (Long) (dto.getData()) + " chassis label = " + dto.getLabel());
		}
		
		cabinetItemId = 14L;
		dtos = itemService.getAllChassisInCabinet(cabinetItemId, SystemLookup.Class.DEVICE);
		for (ValueIdDTO dto: dtos) {
			System.out.println("chassis id = " + (Long) (dto.getData()) + " chassis label = " + dto.getLabel());
		}
	}
	
	//@Test
	public void getChassisInfoTest() throws Throwable {
		long chassisItemId = 3L;
		long faceLksValueCode = SystemLookup.ChassisFace.FRONT;
		ChassisItemDTO dto = itemService.getChassisInfo(chassisItemId, faceLksValueCode);
		System.out.println("chassis dto: id=" + dto.getItemId() + "name = " + dto.getItemName() + "max slot = " + dto.getMaxSlots() + "bays = " + dto.getNumOfBays() + "double allowed = " + dto.isAllowDouble() + "spanning allowed = " + dto.isAllowSpanning());
		faceLksValueCode = SystemLookup.ChassisFace.REAR;
		dto = itemService.getChassisInfo(chassisItemId, faceLksValueCode);
		System.out.println("chassis dto: id=" + dto.getItemId() + "name = " + dto.getItemName() + "max slot = " + dto.getMaxSlots() + "bays = " + dto.getNumOfBays() + "double allowed = " + dto.isAllowDouble() + "spanning allowed = " + dto.isAllowSpanning());
	}

	@Test
	public void getChassisSlotsInfo() throws Throwable {
		
		long chassisItemId = 5067;
		List<ChassisSlotDTO> dtoList = null;
		
		try {
			// Get Front facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.FRONT);
			System.out.println ("\n############ Front facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println( "Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}

	}


	
	
	@Test
	public void getChassisSlotsInfoTest() throws Throwable {
		
		long chassisItemId = 5067;
		List<ChassisSlotDTO> dtoList = null;
		
		try {
			// Get Front facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.FRONT);
			System.out.println ("\n############ Front facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println( "Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}
	
			// Get Rear facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.REAR);
			System.out.println ("\n############ Rear facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println("Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id   = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}

			chassisItemId = 3269L;
			// Get Front facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.FRONT);
			System.out.println ("\n############ Front facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println( "Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}
	
			// Get Rear facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.REAR);
			System.out.println ("\n############ Rear facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println("Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id   = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}

			
			chassisItemId = 3510L;
			// Get Front facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.FRONT);
			System.out.println ("\n############ Front facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println( "Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}
	
			// Get Rear facing chassis slots
			dtoList = itemService.getChassisSlotDetails(chassisItemId, SystemLookup.ChassisFace.REAR);
			System.out.println ("\n############ Rear facing chassis slots############### \n");
			for (ChassisSlotDTO dto: dtoList) {
				System.out.println("Slot Number = " + dto.getNumber() +
						", Slot Label = " + dto.getLabel() +
						", ChassisSlotDto: id = " + dto.getId() +
						", Blade Id   = " + dto.getBladeId() +
						", AnchorSlot = " + dto.getIsAnchorSlot() +
						", ReserveSlot = " + dto.getIsReservedSlot());
			}

		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}

	}

	// @Test192192
	public void getAllBladesForChassisTest() throws Throwable {
		Collection<ItItem> itItems = chassisHome.getAllBladesForChassis(3L, SystemLookup.ChassisFace.FRONT);
		System.out.println("ititems = " + itItems.toString());
	}
	
	@Test
	public void getAllBladesForChassis() throws Throwable {
		Collection<ItItem> itItems = chassisHome.getAllBladesForChassis(5356L);
		System.out.println("ititems = " + itItems.toString());
	}

	@Test
	public void getAllAvailableCabinetForBladeModelTest() throws Throwable {
		long locationId = 1;
		long bladeModelId = 21541;
		int bladeId = -1;
		System.out.println("..... Start test .... ");
		List<ValueIdDTO> dtos = itemService.getAllAvailableCabinetForBladeModel(locationId, bladeModelId, bladeId);
		for (ValueIdDTO dto: dtos) {
			long id = ((Long) (dto.getData())).longValue();
			System.out.println("cabinet info: cabinet id = " + id + " cabinet name = " + dto.getLabel());
		}
	}
	
	@Test
	public void modelDefinitionTestOnChassisModelChange() {
		long modelId = 21504;
		try {
			chassisHome.updateModelDefinition(modelId);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void modelDefinitionTestOnBladeModelChange() {
		long modelId = 21516;
		try {
			chassisHome.updateModelDefinition(modelId);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void updateModelDefinitionTest() throws Throwable {
		long modelId = 16923;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16916;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16917;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16918;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16919;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16920;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16921;
		chassisHome.updateModelDefinition(modelId);
		modelId = 16922;
		chassisHome.updateModelDefinition(modelId);
	}
	
	@Test
	public final void testBladeItemSaveInNew() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();
		
		//Hardware Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(1)); //3Com
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(12)); //Blade 3C13884
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		//Identity Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData("Blade C13884 01");
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData("HELLO THERE!"); 
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
		dto.setLabel("cmbSystemAdmin");
		dto.setData(new Long(20)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdminTeam");
		dto.setData(new Long(555)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCustomer");
		dto.setData(new Long(566)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.IN_STORAGE);
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
		dto.setData(new Long(2));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbChassis");
		dto.setData(new Long(24));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbSlotPosition");
		dto.setData("1");
		valueIdDTOList.add(dto);

		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(null, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		} catch (BusinessValidationException e){
			e.printValidationErrors();
		}
	}


	public String bladeDTOtoString(BladeDTO dto) {
		return "BladeDTO [itemId=" + dto.getItemId() + ", itemName=" + dto.getItemName()
				+ ", modelName=" + dto.getModelName() + ", itemClass=" + dto.getItemClass()
				+ ", itemSubClass=" + dto.getItemSubClass() + ", mounting=" + dto.getMounting()
				+ ", weight=" + dto.getWeight() + ", ruHeight=" + dto.getRuHeight() + ", dimH="
				+ dto.getDimH() + ", dimW=" + dto.getDimW() + ", dimD=" + dto.getDimD()
				+ ", serialNumber=" + dto.getSerialNumber() + ", raritanAssetTag="
				+ dto.getRaritanAssetTag() + ", assetNumber=" + dto.getAssetNumber()
				+ ", slotNumbers=" + dto.getSlotNumbers() + ", faceLkpValueCode="
				+ dto.getFaceLkpValueCode() + ", formFactor=" + dto.getFormFactor()
				+ ", itemStatus=" + dto.getItemStatus() + ", itemStatusStr="
				+ dto.getItemStatusStr() + ", anchorSlot=" + dto.getAnchorSlot() + ", modelId="
				+ dto.getModelId() + ", makeId=" + dto.getMakeId() + ", makeName=" + dto.getMakeName()
				+ "]";
	}
	
	private void printBladeDTO(BladeDTO dto) {
		System.out.println(bladeDTOtoString(dto));
	}
	
	private void printBladeDTOs(long itemId, List<BladeDTO> dtos) {
		System.out.println("Blade item id = " + itemId + " dto list size = " + dtos.size());
		for (BladeDTO dto: dtos) {
			printBladeDTO(dto);
		}
	}
	
	// test for handling blade form factor change
	// The blade model exists on unit test m/c
	@Test
	public void modelDefinitionTestOnBladeModelChange2() {
		long modelId = 21624;
		try {
			chassisHome.updateModelDefinition(modelId);
		} catch (Throwable e) {
			fail();
		}
	}
	
	// test for handling chassis property change.
	// The chassis model exists on unit test m/c
	@Test
	public void modelDefinitionTestOnChassisModelChange2() {
		long modelId = 21622;
		try {
			chassisHome.updateModelDefinition(modelId);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void getFirstBladeModelForMakeTest() throws Throwable {
		long makeId = 3;
		BladeDTO dto = itemService.getFirstBladeModelForMake(makeId);
		printBladeDTO(dto);
		
		makeId = 20;
		dto = itemService.getFirstBladeModelForMake(makeId);
		printBladeDTO(dto);
	}

	@Test
	public void getFirstBladeModelForMakeAndClassTest() throws Throwable {
		long makeId = 3;
		long classLkpValueCode = SystemLookup.Class.DEVICE;
		BladeDTO dto = itemService.getFirstBladeModelForMakeAndClass(makeId, classLkpValueCode);
		printBladeDTO(dto);
		
		classLkpValueCode = SystemLookup.Class.NETWORK;
		dto = itemService.getFirstBladeModelForMakeAndClass(makeId, classLkpValueCode);
		printBladeDTO(dto);
		
		makeId = 20;
		classLkpValueCode = SystemLookup.Class.DEVICE;
		dto = itemService.getFirstBladeModelForMakeAndClass(makeId, classLkpValueCode);
		printBladeDTO(dto);
		
		classLkpValueCode = SystemLookup.Class.NETWORK;
		dto = itemService.getFirstBladeModelForMakeAndClass(makeId, classLkpValueCode);
		printBladeDTO(dto);
	}

	//@Test
	public void updateCabinetAndLocationForBladesInChassisTest() throws Throwable {
		long chassisItemId = 5406;
		chassisHome.updateCabinetAndLocationForBladesInChassis(chassisItemId);
	}
}
