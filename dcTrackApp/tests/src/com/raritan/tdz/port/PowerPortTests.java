package com.raritan.tdz.port;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.lookup.SystemLookup;

public abstract class PowerPortTests extends PortTests {
	
	protected List<ValueIdDTO> addPowerPorts(long itemId, long modelId) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		List<PowerPortDTO> powerPortDTOListCopy = new ArrayList<PowerPortDTO>();
		powerPortDTOListCopy.addAll(powerPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		List<PowerPortDTO> ppModelDTOs = modelHome.getAllPowerPort(modelId);
		for (PowerPortDTO ppdto: ppModelDTOs) {
			ppdto.setItemId(itemId);
			ppdto.setWattsBudget(100);
			ppdto.setWattsNameplate(120);
			ppdto.setAmpsRated(2);
			ppdto.setPortName(ppdto.getPortName() + date.toString() + "-");
			powerPortDTOListCopy.add(ppdto);
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, powerPortDTOListCopy);
		return valueIdDTOList;
	}

	private ValueIdDTO getDtoUsingUiId(String uiId, List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals(uiId) && dto.getData() != null) {
				return dto;
			}
		}
		return null;
	}
	
	protected void correctSortOrder(List<ValueIdDTO> valueIdDTOList, long itemStatus ) throws Throwable {
		// correct the sort order
		ValueIdDTO ppdto  = getDtoUsingUiId("tabPowerPorts", valueIdDTOList);
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) ppdto.getData();

		// edit the power ports
		int sortOrder = 1;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setSortOrder(sortOrder);
			sortOrder++;
		}
	}

	
	protected long getPowerPortCount( Map<String, UiComponentDTO> item ) {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		if (null == itemPowerPortsField) {
			return 0;
		}
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		return powerPortDTOList.size();
	}

	protected void setPowerPortUsed(Long itemId) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);
		Set<PowerPort> pps = item.getPowerPorts();
		for (PowerPort pp: pps) {
			pp.setUsed(true);
		}
		session.update(item);
		session.flush();
	}

	protected void setPowerPortNotUsed(Long itemId) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);
		Set<PowerPort> pps = item.getPowerPorts();
		for (PowerPort pp: pps) {
			pp.setUsed(false);
		}
		session.update(item);
		session.flush();
	}

	protected List<ValueIdDTO> addPowerPortsWithSortOrderAndConnected(long itemId, long modelId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		List<PowerPortDTO> powerPortDTOListCopy = createPowerPortsWithSortOrder(item, itemId, modelId);
		for (PowerPortDTO ppdto: powerPortDTOListCopy) {
			ppdto.setUsed(true);
		}
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, powerPortDTOListCopy);
		return valueIdDTOList;
	}

	private List<PowerPortDTO> createPowerPortsWithDuplicateSortOrder(Map<String, UiComponentDTO> item, long itemId, long modelId) throws Throwable {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		List<PowerPortDTO> powerPortDTOListCopy = new ArrayList<PowerPortDTO>();
		powerPortDTOListCopy.addAll(powerPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		int sortOrder = 1;
		List<PowerPortDTO> ppModelDTOs = modelHome.getAllPowerPort(modelId);
		for (PowerPortDTO ppdto: ppModelDTOs) {
			ppdto.setItemId(itemId);
			ppdto.setWattsBudget(100);
			ppdto.setWattsNameplate(120);
			ppdto.setAmpsRated(2);
			ppdto.setPortName(ppdto.getPortName() + date.toString() + "-");
			ppdto.setSortOrder(sortOrder); if ((sortOrder % 2) != 0) sortOrder++;
			powerPortDTOListCopy.add(ppdto);
		}
		return powerPortDTOListCopy;
	}
	
	private List<PowerPortDTO> createPowerPortsWithInvalidSortOrder(Map<String, UiComponentDTO> item, long itemId, long modelId) throws Throwable {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		List<PowerPortDTO> powerPortDTOListCopy = new ArrayList<PowerPortDTO>();
		powerPortDTOListCopy.addAll(powerPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		int sortOrder = -5;
		List<PowerPortDTO> ppModelDTOs = modelHome.getAllPowerPort(modelId);
		for (PowerPortDTO ppdto: ppModelDTOs) {
			ppdto.setItemId(itemId);
			ppdto.setWattsBudget(100);
			ppdto.setWattsNameplate(120);
			ppdto.setAmpsRated(2);
			ppdto.setPortName(ppdto.getPortName() + date.toString() + "-");
			ppdto.setSortOrder(sortOrder); sortOrder++;
			powerPortDTOListCopy.add(ppdto);
		}
		return powerPortDTOListCopy;
	}
	
	private List<PowerPortDTO> createPowerPortsWithSortOrder(Map<String, UiComponentDTO> item, long itemId, long modelId) throws Throwable {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		List<PowerPortDTO> powerPortDTOListCopy = new ArrayList<PowerPortDTO>();
		powerPortDTOListCopy.addAll(powerPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		int sortOrder = 1;
		List<PowerPortDTO> ppModelDTOs = modelHome.getAllPowerPort(modelId);
		for (PowerPortDTO ppdto: ppModelDTOs) {
			ppdto.setItemId(itemId);
			ppdto.setWattsBudget(100);
			ppdto.setWattsNameplate(120);
			ppdto.setAmpsRated(2);
			ppdto.setPortName(ppdto.getPortName() + date.toString() + "-");
			ppdto.setSortOrder(sortOrder); sortOrder++;
			powerPortDTOListCopy.add(ppdto);
		}
		return powerPortDTOListCopy;
	}
	
	protected List<ValueIdDTO> addPowerPortsWithSortOrder(long itemId, long modelId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		List<PowerPortDTO> powerPortDTOListCopy = createPowerPortsWithSortOrder(item, itemId, modelId);
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, powerPortDTOListCopy);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> addPowerPortsWithInvalidSortOrder(long itemId, long modelId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		List<PowerPortDTO> powerPortDTOListCopy = createPowerPortsWithInvalidSortOrder(item, itemId, modelId);
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, powerPortDTOListCopy);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> addPowerPortsWithDuplicateSortOrder(long itemId, long modelId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		List<PowerPortDTO> powerPortDTOListCopy = createPowerPortsWithDuplicateSortOrder(item, itemId, modelId);
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, powerPortDTOListCopy);
		return valueIdDTOList;
	}

	private List<PowerPortDTO> setDupPPName(Map<String, UiComponentDTO> item, String namePrefix) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPortName(namePrefix);
		}

		return powerPortDTOList;
	}
 
	private void editItemStatus(Map<String, UiComponentDTO> item, long itemStatus) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValueId(itemStatus);
	}

	private void editItemSkipValidation(Map<String, UiComponentDTO> item, boolean skipValidation) {
		UiComponentDTO itemSkipValidationField = item.get("_tiSkipValidation");
		assertNotNull(itemSkipValidationField);
		itemSkipValidationField.getUiValueIdField().setValueId(skipValidation);
		itemSkipValidationField.getUiValueIdField().setValue(skipValidation);
	}

	protected List<ValueIdDTO> editItemSkipValidation( long itemId, boolean skipValidation ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemSkipValidation(item, skipValidation);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = getPP(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}



	private void editItemRedundancy(Map<String, UiComponentDTO> item, String redundancy) {
		UiComponentDTO itemRedundancyField = item.get("cmbPSRedundancy");
		assertNotNull(itemRedundancyField);
		itemRedundancyField.getUiValueIdField().setValueId(redundancy);
		itemRedundancyField.getUiValueIdField().setValue(redundancy);
	}
	
	protected List<PowerPortDTO> getPP(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPComments(Map<String, UiComponentDTO> item, String appendComments) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			if (null == appendComments) {
				dto.setComments(dto.getComments() + "edit comments");
			}
			else {
				dto.setComments(appendComments);
			}
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> deleteNonInputPP( Map<String, UiComponentDTO> item, long numPorts ) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		PowerPortDTO nonInputDto = null;

		// delete the power ports
		Iterator<PowerPortDTO> itr1 = powerPortDTOList.iterator();
		while (itr1.hasNext()) {
			nonInputDto = itr1.next();
			if (!nonInputDto.isInputCord() && nonInputDto.getInputCordPortId() == null) {
				itr1.remove();
				if (--numPorts == 0) {
					break;
				}
			}
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> deleteInputAndAllOutputPP(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		PowerPortDTO inputDto = null;

		// delete the power ports
		Iterator<PowerPortDTO> itr = powerPortDTOList.iterator();
		while (itr.hasNext()) {
			inputDto = itr.next();
			if (inputDto.isInputCord()) {
				itr.remove();
				break;
			}
		}
		itr = powerPortDTOList.iterator();
		while (itr.hasNext()) {
			if (itr.next().getInputCordPortId() == inputDto.getPortId()) {
				itr.remove();
				// break; // TODO:: remove to check delete
			}
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> deleteInputAndNotAllOutputPP(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		PowerPortDTO inputDto = null;

		// delete the power ports
		Iterator<PowerPortDTO> itr = powerPortDTOList.iterator();
		while (itr.hasNext()) {
			inputDto = itr.next();
			if (inputDto.isInputCord()) {
				itr.remove();
				break;
			}
		}
		itr = powerPortDTOList.iterator();
		while (itr.hasNext()) {
			if (itr.next().getInputCordPortId() == inputDto.getPortId()) {
				itr.remove();
				break;
			}
		}

		return powerPortDTOList;
	}

	
	protected List<ValueIdDTO> editVoltPPAttributedWhenConnected(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPVolt(item, 9L); // 277 Volt
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPWithDuplicateNames(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = setDupPPName(item, "Duplicates");
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editItemRedundancy(long itemId, String redundancy ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item redundancy
		editItemRedundancy(item, redundancy);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = getPP(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPhasePPAttributedWhenConnected(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Phase type
		List<PowerPortDTO> ppDtoList = editPPPhase(item, 7020L); // "Single Phase (2-Wire)"
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editConnectorPPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPConnectorDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPNameMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPNameMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPIndex(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editSortOrder(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPColorCode(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPColorCode(item, 941L);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	


	protected List<ValueIdDTO> editPPName(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPName(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editPPNameLengthInvalid(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPNameInvalidLength(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPNameNotUnique(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPNameNotUnique(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	protected List<ValueIdDTO> editPPConnectorMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPConnectorMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPWattsBudgetMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Watts Budget
		List<PowerPortDTO> ppDtoList = editPPBudgetMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPPWattsNameplateMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Watts Nameplate
		List<PowerPortDTO> ppDtoList = editPPNameplateMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	protected List<ValueIdDTO> editPPPFMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP PF
		List<PowerPortDTO> ppDtoList = editPPPFMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editPPVoltMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Volt
		List<PowerPortDTO> ppDtoList = editPPVoltMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	protected List<ValueIdDTO> editPPPhaseTypeMissing(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPhaseTypeMissing(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	protected List<ValueIdDTO> editConnectorPPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPConnector(item, 126L);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	private List<PowerPortDTO> editPPConnectorDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		int[] connectorList = { 7, 13, 14, 126, 128 };

		// edit the power ports
		int connIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setConnectorLkuId(new Long(connectorList[connIdx++]));
			if (connIdx > 4) {
				connIdx = 0;
			}
		}

		return powerPortDTOList;
	}


	
	private List<PowerPortDTO> editPPNameNotUnique(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		String oddPortName = "PortNameOdd";
		String evenPortName = "PortNameEven";

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			
			if ((count % 2) == 0)  dto.setPortName(evenPortName);
			dto.setPortName(((count % 2) == 0) ? evenPortName : oddPortName);
			count++;
		}

		return powerPortDTOList;
	}

	protected List<PowerPortDTO>  editSortOrder(Map<String, UiComponentDTO> item ) throws Throwable {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports sort order (index)
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setSortOrder(dto.getSortOrder() + 1);
		}

		return powerPortDTOList;
	}

	
	private List<PowerPortDTO> editPPName(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		String newPortNamePrefix = "PortName";

		// edit the power ports
		Integer count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPortName(newPortNamePrefix + count.toString());
			count++;
		}

		return powerPortDTOList;
	}

	
	private List<PowerPortDTO> editPPNameInvalidLength(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		String invalidPortNameLength = "PortName012345678901234567890123456789012345678901234567890123456789";

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count) dto.setPortName(invalidPortNameLength);
			if (1 == count) dto.setPortName(new String());
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPNameMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count) dto.setPortName(null);
			if (1 == count) dto.setPortName(new String());
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPConnectorMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count) dto.setConnectorLkuId(null);
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPVoltMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count || 1 == count) {
				dto.setVoltsLksValueCode(null);
				dto.setVoltsLksDesc(null);
			}
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPPFMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count || 1 == count) {
				dto.setPowerFactor(-1);
			}
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPNameplateMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count || 1 == count) {
				dto.setWattsNameplate(-1);
			}
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}


	private List<PowerPortDTO> editPPBudgetMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count || 1 == count) {
				dto.setWattsBudget(-1);
			}
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPPhaseTypeMissing(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		int count = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			if (0 == count || 1 == count) {
				dto.setPhaseLksValueCode(null);
				dto.setPhaseLksDesc(null);
			}
			if (2 == count) break;
			count++;
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPPFDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		double[] powerFactors = { 0.3, 0.5, 0.77, 0.87 };

		// edit the power ports
		int pfIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPowerFactor(powerFactors[pfIdx++]);
		}

		return powerPortDTOList;
	}
	
	protected List<ValueIdDTO> editPFPPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPFDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	private List<PowerPortDTO> editPPWattsBudgetDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		Long[] wattsBudgetList = { 110L, 111L, 112L, 113L };

		// edit the power ports
		int wattsIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setWattsBudget(wattsBudgetList[wattsIdx++]);
		}

		return powerPortDTOList;
	}
	
	protected List<ValueIdDTO> editWattsBudgetPPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPWattsBudgetDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	
	private List<PowerPortDTO> editPPWattsNameplateDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		Long[] wattsNameplateList = { 100L, 101L, 102L, 103L };

		// edit the power ports
		int wattsIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setWattsNameplate(wattsNameplateList[wattsIdx++]);
		}

		return powerPortDTOList;
	}
	
	protected List<ValueIdDTO> editWattsNameplatePPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPWattsNameplateDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	private List<PowerPortDTO> editPPVoltDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		Long[] voltList = { 1L, 1L, 2L, 9L };

		// edit the power ports
		int voltIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			Session session = sf.getCurrentSession();
			LksData voltLksData = SystemLookup.getLksData(session, voltList[voltIdx++].longValue(), "VOLTS");
			dto.setVoltsLksValueCode(voltLksData.getLkpValueCode());
			dto.setPhaseLksDesc(voltLksData.getLkpValue());
			if (voltIdx > 3) {
				voltIdx = 0;
			}
		}

		return powerPortDTOList;
	}

	
	
	protected List<ValueIdDTO> editVoltPPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPVoltDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	private List<PowerPortDTO> editPPPhaseDifferent(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		Long[] phaseList = { 7021L, 7020L, 7021L, 	7020L }; // Single phases

		// edit the power ports
		int phaseIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			Session session = sf.getCurrentSession();
			LksData phaseLksData = SystemLookup.getLksData(session, phaseList[phaseIdx++], "PHASE_ID");
			dto.setPhaseLksValueCode(phaseLksData.getLkpValueCode());
			dto.setPhaseLksDesc(phaseLksData.getLkpValue());
			if (phaseIdx > 3) {
				phaseIdx = 0;
			}
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPPhase(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);
		Long[] phaseList = { 7021L, 7020L, 7021L, 	7020L }; // Single phases

		// edit the power ports
		// int phaseIdx = 0;
		for (PowerPortDTO dto: powerPortDTOList) {
			Session session = sf.getCurrentSession();
			LksData phaseLksData = SystemLookup.getLksData(session, phaseList[3], "PHASE_ID");
			dto.setPhaseLksValueCode(phaseLksData.getLkpValueCode());
			dto.setPhaseLksDesc(phaseLksData.getLkpValue());
		}

		return powerPortDTOList;
	}

	
	private List<PowerPortDTO> editPPLegs(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPhaseLegsLksValueCode(SystemLookup.PhaseLegClass.AB);
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPAmpsR(Map<String, UiComponentDTO> item) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setAmpsRated(3);
			dto.setAmpsNameplate(3);
		}

		return powerPortDTOList;
	}

	protected List<ValueIdDTO> editPhasePPAttributedDifferent(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPhaseDifferent(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editPhasePPAttributed(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPhase(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editLegsPPAttributed(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPLegs(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	
	protected List<ValueIdDTO> editAmpsRPPAttributed(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPAmpsR(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	protected List<ValueIdDTO> editPFPPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPowerFactor(item, 0.71);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPFPPAttributedNegative(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPowerFactor(item, -0.21);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editPFPPAttributedOne(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPowerFactor(item, 1.00);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}


	protected List<ValueIdDTO> editPFPPAttributedZero(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPowerFactor(item, 0.00);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> editPFPPAttributedGTOne(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPowerFactor(item, 1.01);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	protected List<ValueIdDTO> editWattsBudgetPPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPWattsBudget(item, 134);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editWattsNameplatePPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPWattsNameplate(item, 121);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editVoltPPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPVolt(item, 2L);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> editPhasePPAttributedSame(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPPhase(item, 7021L);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	protected List<ValueIdDTO> editConnectorPPAttributedWhenConnected(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP Connector type
		List<PowerPortDTO> ppDtoList = editPPConnector(item, 13L); // RJ11
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	private List<PowerPortDTO> editPPConnector(Map<String, UiComponentDTO> item, Long connectorLkuId) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setConnectorLkuId(connectorLkuId);
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPPhase(Map<String, UiComponentDTO> item, Long phaseLksValueCode) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			Session session = sf.getCurrentSession();
			LksData phaseLksData = SystemLookup.getLksData(session, phaseLksValueCode, "PHASE_ID");
			dto.setPhaseLksValueCode(phaseLksValueCode);
			dto.setPhaseLksDesc(phaseLksData.getLkpValue());
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPVolt(Map<String, UiComponentDTO> item, Long voltLksValueCode) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			Session session = sf.getCurrentSession();
			LksData voltLksData = SystemLookup.getLksData(session, voltLksValueCode, "VOLTS");
			dto.setVoltsLksValueCode(voltLksData.getLkpValueCode()/* voltLksValueCode*/);
		}

		return powerPortDTOList;
	}
	
	protected List<ValueIdDTO> editAllowedPPAttributedWhenConnected(long itemId, long itemStatus ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit PP name
		List<PowerPortDTO> ppDtoList = editPPName(item, "-");
		
		// edit PP color
		ppDtoList = editPPColorCode(item, 941); // Change all ports to Black
		
		// edit Watts Name plate
		ppDtoList = editPPWattsNameplate(item, 113);
		
		// edit Watts budget
		ppDtoList = editPPWattsBudget(item, 171);
		
		// edit power factor
		ppDtoList = editPPPowerFactor(item, 0.78);
		
		// edit comment
		ppDtoList = editPPComments(item, "allowed");
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	private List<PowerPortDTO> editPPPowerFactor(Map<String, UiComponentDTO> item, double powerFactor) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPowerFactor(powerFactor);
		}

		return powerPortDTOList;
	}

	private List<PowerPortDTO> editPPWattsBudget(Map<String, UiComponentDTO> item, long wattsBudget) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setWattsNameplate(wattsBudget);
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPWattsNameplate(Map<String, UiComponentDTO> item, long wattsNameplate) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setWattsNameplate(wattsNameplate);
		}

		return powerPortDTOList;
	}
	
	private List<PowerPortDTO> editPPName(Map<String, UiComponentDTO> item, String appendToName) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPortName(dto.getPortName() + appendToName);
		}

		return powerPortDTOList;
	}

	private static LkuData getLkuData(long lkuId) {
		Session session = sf.getCurrentSession();
		Criteria c = session.createCriteria(LkuData.class);
		c.add(Restrictions.eq("lkuId", lkuId));
		return ((LkuData)c.uniqueResult());
	}
	
	private List<PowerPortDTO> editPPColorCode(Map<String, UiComponentDTO> item, long colorCodeLkuId) {
		List<PowerPortDTO> powerPortDTOList = getPP(item);

		// edit the power ports
		for (PowerPortDTO dto: powerPortDTOList) {
			LkuData lkuColorData = getLkuData(colorCodeLkuId);  // Black
			dto.setColorLkuId(lkuColorData.getLkuId());
			dto.setColorLkuDesc(lkuColorData.getLkuValue());
			dto.setColorNumber(lkuColorData.getLkuAttribute());
		}

		return powerPortDTOList;
	}

	protected List<ValueIdDTO> editPPCommentsMoreThanMaxLength(long itemId, long itemStatus ) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		String newComment = StringUtils.rightPad("Invalid Comment Length", 501, '-');
		
		// edit the power ports comments
		List<PowerPortDTO> ppDtoList = editPPComments(item, newComment);

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	

	
	protected List<ValueIdDTO> editPPCommentsAndItemStatusWithSortOrder(long itemId, long itemStatus ) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit the power ports comments
		List<PowerPortDTO> ppDtoList = editPPComments(item, "editing comments");

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}
	
	protected List<ValueIdDTO> deletePPAndEditItemStatusWithSortOrder( long itemId, long itemStatus, long numPorts ) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit the power ports comments
		List<PowerPortDTO> ppDtoList = deleteNonInputPP( item, numPorts );

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> deletePPInputCordAndAllOutputWithSortOrder( long itemId, long itemStatus) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit the power ports comments
		List<PowerPortDTO> ppDtoList = deleteInputAndAllOutputPP(item);

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	protected List<ValueIdDTO> deletePPInputCordAndNotAllOutputWithSortOrder( long itemId, long itemStatus) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		// edit the power ports comments
		List<PowerPortDTO> ppDtoList = deleteInputAndNotAllOutputPP(item);

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemPowerPortValueIdDTOList(item, ppDtoList);
		return valueIdDTOList;
	}

	
	protected List<ValueIdDTO> prepareItemPowerPortValueIdDTOList(Map<String, UiComponentDTO> item, List<PowerPortDTO> powerPortDTOListCopy) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
	    @SuppressWarnings("rawtypes")
		Iterator it = item.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue();
			assertNotNull( field );
			/* these have no object trace and is not expected by the server */
			if (((String)pairs.getKey()).equals("tiRailWidth") || 
					((String)pairs.getKey()).equals("tiRearClearance") ||
					((String)pairs.getKey()).equals("tiFrontRailOffset") ||
					((String)pairs.getKey()).equals("tiRearRailOffset") || 
					((String)pairs.getKey()).equals("tiLeftClearance") || 
					((String)pairs.getKey()).equals("tiRightClearance") || 
					((String)pairs.getKey()).equals("tiCustomField") ||
					((String)pairs.getKey()).equals("tiRearDoorPerforation") ||
					((String)pairs.getKey()).equals("tiFrontDoorPerforation") ||
					((String)pairs.getKey()).equals("cmbRowLabel") ||
					((String)pairs.getKey()).equals("tiLoadCapacity") ||
					((String)pairs.getKey()).equals("cmbCabinetGrouping") ||
					((String)pairs.getKey()).equals("cmbRowPosition") ||
					((String)pairs.getKey()).equals("tiFrontClearance") ||
					// ((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			else if (((String)pairs.getKey()).equals("tabPowerPorts")) {
				dto.setData(powerPortDTOListCopy);
			}
			/*else if (((String)pairs.getKey()).equals("tabDataPorts")) {
				dto.setData(dataPortDTOListCopy);
			}*/
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
	    return valueIdDTOList;
	}

}
