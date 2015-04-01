package com.raritan.tdz.dctimport.integration.transformers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.DataPortDTOHelper;
import com.raritan.tdz.item.dao.ItemDAO;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * transforms data related to snmp credentials
 * one such transformation is getting the data port dto and set to the item's map
 * so that the community string can be applied to the data port
 * @author bunty
 *
 */
public class SNMPCredentialsTransformer implements ImportTransformer {

	private ImportErrorHandler importErrorHandler;
	
	@Autowired
	private DataPortDTOHelper dataPortDTOHelper;
	
	@Autowired
	private ItemDAO itemDAO;
	
	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}

	@Override
	public Message<?> transform(Message<?> message) throws Exception {

		List<?> payload = (List<?>) message.getPayload();
		
		@SuppressWarnings("unchecked")
		Map<String,Object> objectAsMap = (Map<String, Object>) payload.get(0);

		updatePortList(objectAsMap);
		
		Object[] newPayLoadArray = {objectAsMap, payload.get(1)};
		
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
		
	}
	
	private void updatePortList(Map<String,Object> objectAsMap) {
		
		boolean snmpCommStrHeaderProvided = objectAsMap.containsKey("tiSnmpWriteCommString");
		
		if (snmpCommStrHeaderProvided) {
			
			Item item = itemDAO.getItem(objectAsMap.get("tiName").toString(), objectAsMap.get("cmbLocation").toString());
			
			if (null == item) return;
			
			List<DataPortDTO> dataPortDTOList = dataPortDTOHelper.getPortDTOList(item);
			
			objectAsMap.put("tabDataPorts", dataPortDTOList);
			
		}
		
	}

}
