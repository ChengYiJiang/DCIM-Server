package com.raritan.tdz.item.move;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.Query;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.IPortMoveInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.helper.ItemMoveHelper;
import com.raritan.tdz.item.home.SaveUtils;
import com.raritan.tdz.item.request.ItemRequestValidationAspect;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.RequestDTO;

/**
 * 
 * @author bunty
 *
 */

public class ItemMoveTestBase extends TestBase {

	protected UserInfo userInfo = null;
	protected ItemMoveHelper itemMoveHelper = null;
	protected ItemRequestValidationAspect validationAspect;
	protected PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	protected PortMoveDAO<DataPortMove> dataPortMoveDAO;
	protected DataCircuitDAO dataCircuitDAO;
	protected PowerCircuitDAO powerCircuitDAO;

	public static class TestOperations {
		Long movingItemId;
		Long powerPortMoveActionLkpValueCode;
		Long dataPortMoveActionLkpValueCode;
		Long newUPosition;
		List<String> errorCode;
		List<String> warningCode;
		public TestOperations(Long movingItemId,
				Long powerPortMoveActionLkpValueCode,
				Long dataPortMoveActionLkpValueCode, Long newUPosition,
				List<String> errorCode, List<String> warningCode) {
			super();
			this.movingItemId = movingItemId;
			this.powerPortMoveActionLkpValueCode = powerPortMoveActionLkpValueCode;
			this.dataPortMoveActionLkpValueCode = dataPortMoveActionLkpValueCode;
			this.newUPosition = newUPosition;
			this.errorCode = errorCode;
			this.warningCode = warningCode;
		}
		public Long getMovingItemId() {
			return movingItemId;
		}
		public void setMovingItemId(Long movingItemId) {
			this.movingItemId = movingItemId;
		}
		public Long getPowerPortMoveActionLkpValueCode() {
			return powerPortMoveActionLkpValueCode;
		}
		public void setPowerPortMoveActionLkpValueCode(
				Long powerPortMoveActionLkpValueCode) {
			this.powerPortMoveActionLkpValueCode = powerPortMoveActionLkpValueCode;
		}
		public Long getDataPortMoveActionLkpValueCode() {
			return dataPortMoveActionLkpValueCode;
		}
		public void setDataPortMoveActionLkpValueCode(
				Long dataPortMoveActionLkpValueCode) {
			this.dataPortMoveActionLkpValueCode = dataPortMoveActionLkpValueCode;
		}
		public Long getNewUPosition() {
			return newUPosition;
		}
		public void setNewUPosition(Long newUPosition) {
			this.newUPosition = newUPosition;
		}
		public List<String> getErrorCode() {
			return errorCode;
		}
		public void setErrorCode(List<String> errorCode) {
			this.errorCode = errorCode;
		}
		public List<String> getWarningCode() {
			return warningCode;
		}
		public void setWarningCode(List<String> warningCode) {
			this.warningCode = warningCode;
		}
		
	};

	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws Throwable {
		
		// This is required because of timeOut, if no timeout is used, this can be removed
		// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		
		super.setUp();

		powerPortMoveDAO = (PortMoveDAO<PowerPortMove>) ctx.getBean("powerPortMoveDAO");
		dataPortMoveDAO = (PortMoveDAO<DataPortMove>) ctx.getBean("dataPortMoveDAO");
		dataCircuitDAO = (DataCircuitDAO) ctx.getBean("dataCircuitDAO");
		powerCircuitDAO = (PowerCircuitDAO) ctx.getBean("powerCircuitDAO");

		validationAspect = (ItemRequestValidationAspect) ctx.getBean("itemRequestValidationAspect");
		validationAspect.setDisableValidate(true);
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
		
		// TransactionSynchronizationManager.unbindResource(sf);
	}

	protected final UserInfo getMoveTestAdminUser() {
		UserInfo user = new UserInfo();
		user.setUserName("unitTestAdmin");
		user.setUserId("100");
		user.setId(100);
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		return user;
	}

	
	private void testInit() {
		
		if (null == userInfo) userInfo = getMoveTestAdminUser();
		
		if (null == itemMoveHelper) itemMoveHelper = new ItemMoveHelper(itemHome, userInfo);

		// validationAspect = (ItemRequestValidationAspect) ctx.getBean("itemRequestValidationAspect");
		validationAspect.setDisableValidate(true);
	}

  	public static long getItemId(Map<String, UiComponentDTO> itemFields) {
		long itemId = -1;
		UiComponentDTO componentDTO = itemFields.get("tiName");
		
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId = (Long)uiValueIdField.getValueId(); 
		}
		
		return itemId;
	}

	
  	
	private List<ValueIdDTO> getNewItemDTOWithMoveData(Item item, Long powerPortActionLkpValueCode, Long dataPortActionLkpValueCode, Long newUPosition, Boolean skipValidation) throws Throwable {
		
		
		Map<String, UiComponentDTO> itemMap = itemHome.getItemDetails( item.getItemId(), userInfo );
		
		// edit item name
		itemMoveHelper.editItemName(itemMap, item.getItemName() + "^^WHEN-MOVED");
		
		// set as a new item
		itemMoveHelper.editItemId(itemMap, -1L);
		
		// set item status
		itemMoveHelper.editItemStatus(itemMap, SystemLookup.ItemStatus.PLANNED);
		
		// set the item_ids to -1 and port_ids to -1
		long portId = -1L;
		long itemId = -1L;
		// for power ports
		itemMoveHelper.editPowerPortItemId(itemMap, itemId, portId);
		// for data ports
		itemMoveHelper.editDataPortItemId(itemMap, itemId, portId);
		// for sensor ports
		itemMoveHelper.editSensorPortItemId(itemMap, itemId, portId);
		
		// edit the action
		itemMoveHelper.editPowerPortAction(itemMap, powerPortActionLkpValueCode);
		itemMoveHelper.editDataPortAction(itemMap, dataPortActionLkpValueCode);
		
		// TODO:: edit item location
		
		// TODO:: edit item cabinet
		
		// TODO:: edit rails used
		
		// TODO:: edit U position
		itemMoveHelper.editItemUPosition(itemMap, newUPosition);
		
		// List<ValueIdDTO> itemDto = itemMoveHelper.getDto(item.getItemId());
		
		List<ValueIdDTO> itemDto = itemMoveHelper.prepareItemValueIdDTOList(itemMap);
		
		// itemDto = itemMoveHelper.setItemName(item.getItemId(), item.getItemName() + "^^WHEN-MOVED");
		
		itemDto.addAll(SaveUtils.addItemField("_itemToMoveId", item.getItemId()));
		
		// skip validation
		itemDto.addAll(SaveUtils.addItemField("_tiSkipValidation", skipValidation));

		return itemDto;
		
	}

	private IPortMoveInfo getPortRequest(List<IPortMoveInfo> moveList, Long portId) {
		for (IPortMoveInfo moveData: moveList) {
			IPortInfo port = moveData.getOrigPort();
			if (null != port && port.getPortId().equals(portId)) {
				return moveData;
			}
		}
		
		return null;
	}


	protected void validateItemMove(Long movingItemId, Long movedItemId, Long powerPortActionLkpValueCode, Long dataPortActionLkpValueCode) throws Throwable {
		
		Item movingItem = itemDAO.getItem(movingItemId);
		String movingItemName = movingItem.getItemName();
		
		Item movedItem = itemDAO.getItem(movedItemId);
		Set<PowerPort> movedPowerPorts = movedItem.getPowerPorts();
		Set<DataPort> movedDataPorts = movedItem.getDataPorts();
		
		// validate the power port move row count
		List<PowerPortMove> powerPortMoveList = powerPortMoveDAO.getPortMoveData(movedItemId);
		
		// -1 for the item entry in the port move data
		Assert.assertEquals(powerPortMoveList.size() - 1, movedPowerPorts.size(), movingItemName + ": incorrect power port move data count");
		
		// validate the data port move row count
		List<DataPortMove> dataPortMoveList = dataPortMoveDAO.getPortMoveData(movedItemId);
		// -1 for the item entry in the port move data
		Assert.assertEquals(dataPortMoveList.size() - 1, movedDataPorts.size(), movingItemName + ": incorrect data port move data count");
		
		
		
		Set<PowerPort> movingPowerPorts = movingItem.getPowerPorts();
		
		Set<DataPort> movingDataPorts = movingItem.getDataPorts();

		if (powerPortActionLkpValueCode.equals(SystemLookup.MoveActionLkpValue.DontConnect)) {
			// validate power port disconnect request count
			HashMap<Long, PortInterface> portMap = powerCircuitDAO.getDestinationItemsForItem(movingItemId);
			if( portMap != null ){
				
				for (PowerPort port: movingPowerPorts) {
	
					@SuppressWarnings("unchecked")
					IPortMoveInfo moveData = getPortRequest((List<IPortMoveInfo>)(List<?>) powerPortMoveList, port.getPortId());
	
					PowerPortDTO portIf = (PowerPortDTO)portMap.get(port.getPortId());
					if( portIf != null ) {
						
						Assert.assertNotNull(moveData.getRequest(), movingItemName + ": power port: no request generated");
						
					}
					
					// validate the action in the power port move
					Assert.assertNotNull(moveData.getAction(), movingItemName + ": unexpected (null) power port move action");
					Assert.assertEquals(moveData.getAction().getLkpValueCode(), powerPortActionLkpValueCode, movingItemName + ": unexpected power port move action");
				}
			}
		}

		if (dataPortActionLkpValueCode.equals(SystemLookup.MoveActionLkpValue.DontConnect)) {
			// validate data port disconnect request count		
			HashMap<Long, PortInterface> portMap = dataCircuitDAO.getDestinationItemsForItem(movingItemId);
			if( portMap != null ){
				
				for (DataPort port: movingDataPorts) {
	
					@SuppressWarnings("unchecked")
					IPortMoveInfo moveData = getPortRequest((List<IPortMoveInfo>)(List<?>)dataPortMoveList, port.getPortId());
	
					DataPortDTO portIf = (DataPortDTO)portMap.get(port.getPortId());
					if( portIf != null ) {
						
						Assert.assertNotNull(moveData.getRequest(), movingItemName + ": data port: no request generated");
					}
					
					// validate the action in the data port move
					Assert.assertNotNull(moveData.getAction(), movingItemName + ": unexpected (null) data port move action");
					Assert.assertEquals(moveData.getAction().getLkpValueCode(), dataPortActionLkpValueCode, movingItemName + ": unexpected data port move action");
				}
			}
		}

		Map<Long, String> actionMap = new HashMap<Long, String>();
		// actionMap.put(SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveActionLkpValue.DontConnect);
		actionMap.put(SystemLookup.MoveAction.DONT_CONNECT, null);
		actionMap.put(SystemLookup.MoveAction.KEEP_CONNECTED, SystemLookup.MoveActionLkpValue.KeepConnected);
		actionMap.put(SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, SystemLookup.MoveActionLkpValue.ReconnectToDataPanel);
		actionMap.put(SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, SystemLookup.MoveActionLkpValue.ReconnectToNetworkItem);
		actionMap.put(SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET, SystemLookup.MoveActionLkpValue.ReconnectToPowerOutlet);
		actionMap.put(SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveActionLkpValue.ReconnectToRackPdu);
		
		Map<String, UiComponentDTO> movedItemMap = itemHome.getItemDetails( movedItemId, userInfo );

		// validate the connected item data against the action for power port using the getItemDetails in the dto
		List<PowerPortDTO> powerPortDTOs = itemMoveHelper.getPowerPortDTOs(movedItemMap);
		for (PowerPortDTO dto: powerPortDTOs) {
			Assert.assertEquals(dto.getConnectedItemName(), actionMap.get(powerPortActionLkpValueCode), "Connected Item name do not contain the action against the power port");
		}
		
		// validate the connected item data against the action for data port using the getItemDetails in the dto
		List<DataPortDTO> dataPortDTOs = itemMoveHelper.getDataPortDTOs(movedItemMap);
		for (DataPortDTO dto: dataPortDTOs) {
			Assert.assertEquals(dto.getConnectedItemName(), actionMap.get(dataPortActionLkpValueCode), "Connected Item name do not contain the action against the data port");
		}
		
		// validate the status string for the when moved item using the getItemDetails in the dto
		String statusStr = itemMoveHelper.getItemStatusStr(movedItemMap);
		Assert.assertTrue(statusStr.contains("Request Issued") || statusStr.contains("Request Updated"), "Status description not updated to have the request stage from original item [" + movingItemName + " ]");

		// validate that all power port fields are propagated to the moved item
		@SuppressWarnings("unchecked")
		Map<IPortInfo, IPortInfo> powerPortMap = createPortMap((Set<IPortInfo>)(Set<?>)movedPowerPorts, (Set<IPortInfo>)(Set<?>)movingPowerPorts);
		for (Map.Entry<IPortInfo, IPortInfo> entry: powerPortMap.entrySet()) {
			PowerPort movedPort = (PowerPort)entry.getKey();
			PowerPort movingPort = (PowerPort) entry.getValue();
			Assert.assertTrue(equalsPowerPort(movedPort, movingPort), "Power Port " + ((PowerPort)entry.getKey()).getPortName() + " is not equal");
		}

		// validate that all power port fields are propagated to the moved item
		@SuppressWarnings("unchecked")
		Map<IPortInfo, IPortInfo> dataPortMap = createPortMap((Set<IPortInfo>)(Set<?>)movedDataPorts, (Set<IPortInfo>)(Set<?>)movingDataPorts);
		for (Map.Entry<IPortInfo, IPortInfo> entry: dataPortMap.entrySet()) {
			DataPort movedPort = (DataPort)entry.getKey();
			DataPort movingPort = (DataPort) entry.getValue();
			Assert.assertTrue(equalsDataPort(movedPort, movingPort), "Data Port " + ((DataPort)entry.getKey()).getPortName() + " is not equal");
		}

		// validate moved item id against the moving item id
		Map<String, UiComponentDTO> movingItemMap = itemHome.getItemDetails( movingItemId, userInfo );
		
		Long whenMovedItemId = itemMoveHelper.getWhenMovedId(movingItemMap);
		Assert.assertEquals(movedItemId, whenMovedItemId, "Did not updated the moved item id: _whenMoveItemId");
		
		Long movingItemIdFromMap = itemMoveHelper.getMovingItemId(movedItemMap);
		Assert.assertEquals(movingItemId, movingItemIdFromMap, "Did not updated the moving item id: _itemToMoveId");
		
		// validate the request against the moving item id
		List<RequestDTO> movingItemRequests = itemMoveHelper.getRequestDTOs(movingItemMap);
		Assert.assertTrue(movingItemRequests.size() > 0, "no request provided in the _itemRequest for moving item");
		
		// validate the request against the moved item id
		List<RequestDTO> movedItemRequests = itemMoveHelper.getRequestDTOs(movedItemMap);
		Assert.assertTrue(movedItemRequests.size() > 0, "no request provided in the _itemRequest for moved item");
		
		// validate available U positions are the same for both items
		Collection<Long> movedItemAvailUPos = null;
		
		Long railsUsed = (null == movedItem.getMountedRailLookup()) ? SystemLookup.RailsUsed.BOTH : movedItem.getMountedRailLookup().getLkpValueCode();
		Integer itemRUHeight = new Integer(movedItem.getModel().getRuHeight());
		Long cabinetId = null; 
		if (! movedItem.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)) {
			cabinetId = (null == movedItem || null == movedItem.getParentItem()) ? null : movedItem.getParentItem().getItemId();
			if (null != cabinetId) {
				movedItemAvailUPos = itemHome.getAvailableUPositions(cabinetId, itemRUHeight, movedItemId, railsUsed, null);
			}
	
			Collection<Long> movingItemAvailUPos = null;
			Long movingRailsUsed = (null == movedItem.getMountedRailLookup()) ? SystemLookup.RailsUsed.BOTH : movedItem.getMountedRailLookup().getLkpValueCode();
			Integer movingItemRUHeight = new Integer(movedItem.getModel().getRuHeight());
			Long movingCabinetId = (null == movedItem || null == movedItem.getParentItem()) ? null : movedItem.getParentItem().getItemId();
			if (null != cabinetId) {
				movingItemAvailUPos = itemHome.getAvailableUPositions(movingCabinetId, movingItemRUHeight, movingItemId, movingRailsUsed, null);
			}
			
			Assert.assertTrue(movedItemAvailUPos.containsAll(movingItemAvailUPos), "Not the same u positions provided for moved and moving items");
		}
		
	}
	
	private Map<IPortInfo, IPortInfo> createPortMap(Set<IPortInfo> movedPorts, Set<IPortInfo> movingPorts) {
		Map<IPortInfo, IPortInfo> map = new HashMap<IPortInfo, IPortInfo>();
		
		for (IPortInfo movedPort: movedPorts) {
			for (IPortInfo movingPort: movingPorts) {
				if (movedPort.getPortName().equals(movingPort.getPortName())) {
					map.put(movedPort, movingPort);
					break;
				}
			}
		}
		
		return map;
		
	}
	
	private boolean equalsPowerPort(PowerPort lhs, PowerPort rhs) {
		if (null == lhs && null == rhs) {
			return true;
		}
		if ((!(lhs instanceof PowerPort)) || (!(rhs instanceof PowerPort))) {
			return false;
		}
		return new EqualsBuilder()
				.append(lhs.getPortName(), rhs.getPortName())
				.append(lhs.getColorLookup(), rhs.getColorLookup())
				.append(lhs.getConnectorLookup(), rhs.getConnectorLookup())
				.append(lhs.getPhaseLookup(), rhs.getPhaseLookup())
				.append(lhs.getVoltsLookup(), rhs.getVoltsLookup())
				.append(lhs.getPowerFactor(), rhs.getPowerFactor())
				.append(lhs.getWattsNameplate(), rhs.getWattsNameplate())
				.append(lhs.getWattsBudget(), rhs.getWattsBudget())
				.append(lhs.getComments(), rhs.getComments())
				.isEquals();
	}

	public boolean equalsDataPort(DataPort lhs, DataPort rhs) {
		if (null == lhs && null == rhs) {
			return true;
		}
		if ((! (lhs instanceof DataPort)) || (! (rhs instanceof DataPort)) ) {
			return false;
		}
		
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(lhs.getPortName(), rhs.getPortName());
		eb.append(lhs.getColorLookup(), rhs.getColorLookup());
		// eb.append(lhs.getSortOrder(), rhs.getSortOrder());
		eb.append(lhs.getGroupingVlanTag(), rhs.getGroupingVlanTag());
		eb.append(lhs.getMacAddress(), rhs.getMacAddress());
		eb.append(lhs.getIpAddress(), rhs.getIpAddress());
		eb.append(lhs.getCommunityString(), rhs.getCommunityString());
		eb.append(lhs.getConnectorLookup(), rhs.getConnectorLookup());
		eb.append(lhs.getMediaId(), rhs.getMediaId());
		eb.append(lhs.getProtocolID(), rhs.getProtocolID());
		eb.append(lhs.getSpeedId(), rhs.getSpeedId());
		eb.append(lhs.getComments(), rhs.getComments());
		boolean equal = eb.isEquals();
		
		return equal;
		
		/*return new EqualsBuilder()
				.append(lhs.getPortName(), rhs.getPortName())
				.append(lhs.getColorLookup(), rhs.getColorLookup())
				.append(lhs.getSortOrder(), rhs.getSortOrder())
				.append(lhs.getGroupingVlanTag(), rhs.getGroupingVlanTag())
				.append(lhs.getMacAddress(), rhs.getMacAddress())
				.append(lhs.getIpAddress(), rhs.getIpAddress())
				.append(lhs.getCommunityString(), rhs.getCommunityString())
				.append(lhs.getConnectorLookup(), rhs.getConnectorLookup())
				.append(lhs.getMediaId(), rhs.getMediaId())
				.append(lhs.getProtocolID(), rhs.getProtocolID())
				.append(lhs.getSpeedId(), rhs.getSpeedId())
				.append(lhs.getComments(), rhs.getComments())
				.isEquals();*/
	}
	
	protected Long testNewItemWithMove(Item item, List<String> errorCodes, Long powerPortActionLkpValueCode, Long dataPortActionLkpValueCode, Long newUPosition, Boolean skipValidation, List<String> warningCodes) throws Throwable {
		
		this.testInit();
		
		List<ValueIdDTO> itemDto = getNewItemDTOWithMoveData(item, powerPortActionLkpValueCode, dataPortActionLkpValueCode, newUPosition, skipValidation);
		
		// itemMoveHelper.saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodesOriginal);
		Long newItemId = itemMoveHelper.saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes, warningCodes);
		
		// itemHome.deleteItem(newItemId, false, userInfo);

		session.flush();
		session.clear();
		
		/*validateOriginField(itemId);
		
		// Validate Placement
		Long ticketId = ticketsDAO.getTicketId(itemId);
		
		validatePlacement(ticketId);
		// validateTicketItemLocation(ticketId); // location
		
		// Validate Hardware Info
		validateTicketItemHardwareInfo(ticketId);
		
		// Validate Identity Info
		validateTicketItemIdentityInfo(ticketId);*/
		
		// TODO:: set the item ids for the ports (data, power and sensor)
		
		return newItemId;

	}
	
	
	private List<ValueIdDTO> getEditItemDTOWithCabinetChanged(Long itemId, Boolean skipValidation) throws Throwable {
		
		
		Map<String, UiComponentDTO> itemMap = itemHome.getItemDetails( itemId, userInfo );
		
		// TODO:: edit item location
		
		// TODO:: edit item cabinet
		
		// TODO:: edit rails used
		
		// edit the location
		itemMoveHelper.editItemPlacedLocation(itemMap, "SITE A", 1);
		
		// edit the cabinet
		itemMoveHelper.editItemPlacedCabinet(itemMap, "1C", 5);
		
		// edit the chassis
		itemMoveHelper.editItemPlacedChassis(itemMap, "BLADE-CHASSIS121", 4966);
		
		// edit slot position
		itemMoveHelper.editItemPlacedSlotPosition(itemMap, "1", 1);
		
		// edit U position
		itemMoveHelper.editItemUPosition(itemMap, -1);
		
		List<ValueIdDTO> itemDto = itemMoveHelper.prepareItemValueIdDTOList(itemMap);
		
		// itemDto.addAll(SaveUtils.addItemField("_itemToMoveId", item.getItemId()));
		
		// skip validation
		itemDto.addAll(SaveUtils.addItemField("_tiSkipValidation", skipValidation));

		return itemDto;
		
	}

	
	protected Long testEditMovedItemCabinetChanged(Long itemId, Boolean skipValidation, List<String> errorCodes, List<String> warningCodes) throws Throwable {
		
		this.testInit();
		
		List<ValueIdDTO> itemDto = getEditItemDTOWithCabinetChanged(itemId, skipValidation);
		
		// itemMoveHelper.saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodesOriginal);
		Long newItemId = itemMoveHelper.saveNewItemWithExpectedErrorCode(itemId, itemDto, errorCodes, warningCodes);
		
		// itemHome.deleteItem(newItemId, false, userInfo);

		session.flush();
		session.clear();
		
		/*validateOriginField(itemId);
		
		// Validate Placement
		Long ticketId = ticketsDAO.getTicketId(itemId);
		
		validatePlacement(ticketId);
		// validateTicketItemLocation(ticketId); // location
		
		// Validate Hardware Info
		validateTicketItemHardwareInfo(ticketId);
		
		// Validate Identity Info
		validateTicketItemIdentityInfo(ticketId);*/
		
		// TODO:: set the item ids for the ports (data, power and sensor)
		
		return newItemId;

	}
	
	protected ItItem createChassisInState(Long itemState) throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-Move-Test", itemState);
		
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Move-Test", itemState, cabinetItem.getItemId());
		
		devChassisItem.setParentItem(cabinetItem);
		
		cabinetItem.addChildItem(devChassisItem);
		
		return devChassisItem;
		
	}

	protected void deleteRequest(Long itemId)	 {
		
		Query q1 = session.createSQLQuery("delete from tblrequesthistory where requestid in (select id from tblrequest where itemid = :itemId)");
		q1.setLong("itemId", itemId);
		q1.executeUpdate();
		
		Query q2 = session.createSQLQuery("delete from tblrequestpointer where requestid in (select id from tblrequest where itemid = :itemId)");
		q2.setLong("itemId", itemId);
		q2.executeUpdate();
		
		Query q3 = session.createSQLQuery("delete from tblrequest where itemid = :itemId");
		q3.setLong("itemId", itemId);
		q3.executeUpdate();

		session.flush();
		
	}	


}
