package com.raritan.tdz.piq.integration;

 import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;

public class DataEnhancerImpl implements DataEnhancer {
	
	@Autowired(required = true)
	ItemDAO itemDAO;
	
	// this is gateway to get readings from PIQ
	PIQSyncPDUClient piqSyncPDUClient; 

	public PIQSyncPDUClient getPiqSyncPDUClient() {
		return piqSyncPDUClient;
	}

	public void setPiqSyncPDUClient(PIQSyncPDUClient piqSyncPDUClient) {
		this.piqSyncPDUClient = piqSyncPDUClient;
	}

	@Override
	public List<String> enhance(List<Item> items) throws DataAccessException, RemoteDataAccessException, Exception {
		
		List<String> result = new java.util.ArrayList<String>();
		
		// resolve which piq (ipaddress) is managing the items (Map<ipaddress, List<Item>>) 
		Map<String, List<Item>> itemsDestMap = 	resolveItemsPiqHost (items);

		// for each set of items managed by piq-host, call syncPduReadings
		for (Map.Entry<String, List<Item>> entry : itemsDestMap.entrySet()) {
			List<String> rVal = piqSyncPDUClient.syncPduReadings((List<Item>)entry.getValue());
			result.addAll(rVal);
		}
		
		return result;
	}
	
	private Map<String, List<Item>> resolveItemsPiqHost (List<Item> items) {
		Map<String, List<Item>> itemsDestMap = new HashMap<String, List<Item>>();

		//create a map for Map<IPAddress, List<Item>>

		for (Item item : items) {
			if (item != null) {
				String piqHost = itemDAO.getItemsPiqHost(item.getItemId());
				if (itemsDestMap.get(piqHost) != null) {
					itemsDestMap.get(piqHost).add(item);
				}
				else {
					List<Item> itemList = new ArrayList<Item>();
					item.setPiqHost(piqHost);
					itemList.add(item);
					itemsDestMap.put(piqHost, itemList); 
				}
			}
		}
		
		return itemsDestMap;
	}

}

