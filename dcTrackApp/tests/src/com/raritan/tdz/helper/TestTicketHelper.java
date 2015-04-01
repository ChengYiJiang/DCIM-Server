package com.raritan.tdz.helper;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.home.ItemHome;

public class TestTicketHelper extends TestHelperBase {

	public TestTicketHelper(ItemHome itemHome, UserInfo userInfo) {

		super(itemHome, userInfo);
		
	}

	public List<ValueIdDTO> setItemState(ItemHome itemHome, UserInfo userInfo, Long itemId, Long itemStatus) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );
		
		return setItemState(item, itemStatus);
		
	}

	public List<ValueIdDTO> setItemTicket(ItemHome itemHome, UserInfo userInfo, Long itemId, Long ticketId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );
		
		return setItemTicket(item, ticketId);
		
	}

	/* ----------------------- */
	/* Private functions */
	/* ----------------------- */
	
	private List<ValueIdDTO> setItemState(Map<String, UiComponentDTO> item, Long itemStatus) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}
	
	public void editItemStatus(Map<String, UiComponentDTO> item, long itemStatus) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValueId(itemStatus);
	}


	private List<ValueIdDTO> setItemTicket(Map<String, UiComponentDTO> item, Long ticketId) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemTicket(item, ticketId);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}
	
	private void editItemTicket(Map<String, UiComponentDTO> item, long ticketId) {
		UiComponentDTO itemTicketField = item.get("tiTicketNumber");
		assertNotNull(itemTicketField);
		String ticketStr = (String) itemTicketField.getUiValueIdField().getValue();
		Long currentTicketId = (Long) itemTicketField.getUiValueIdField().getValueId();
		System.out.println("item ticket id = " + ((null != currentTicketId) ? currentTicketId.toString() : "(null)") + "str = " + ((null != ticketStr) ? ticketStr : "(null)"));
		itemTicketField.getUiValueIdField().setValueId(ticketId);
		itemTicketField.getUiValueIdField().setValue(ticketId);
		
	}





}
