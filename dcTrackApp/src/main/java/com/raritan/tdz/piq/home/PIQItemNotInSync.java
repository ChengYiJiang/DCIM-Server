/**
 * 
 */
package com.raritan.tdz.piq.home;



import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.domain.Item;

/**
 * This object holds all the item ids that are not in sync with PIQ
 * This is populated by the respective PIQSync clients (Device/Rack)
 * @author prasanna
 *
 */
public class PIQItemNotInSync {
	private Map<Long,PIQItemNotInSyncParams> itemsNotInSync = new HashMap<Long, PIQItemNotInSyncParams>();
	
	/**
	 * Add an item that is *not* in sync
	 * @param item that is not in sync
	 * @param isFound TODO
	 */
	public void addItem(Item item, boolean isFound){
		if (item != null ){
			PIQItemNotInSyncParams params = new PIQItemNotInSyncParams();
			params.setFound(isFound);
			itemsNotInSync.put(item.getItemId(), params);
		}
	}
	
	/**
	 * Add an item that is *not* in sync
	 * @param item that is not in sync
	 * @param isFound is the item found on PIQ
	 * @param isReplace do we need to replace what is on power IQ
	 */
	public void addItem(Item item, boolean isFound, boolean isReplace){
		if (item != null ){
			PIQItemNotInSyncParams params = new PIQItemNotInSyncParams();
			params.setFound(isFound);
			params.setReplace(isReplace);
			itemsNotInSync.put(item.getItemId(), params);
		}
	}
	
	/**
	 * Add an item that is *not* in syncisNotInSync
	 * @param item that is not in sync
	 * @param isFound is the item found on PIQ
	 * @param isReplace do we need to replace what is on power IQ
	 * @param oldIPAddress - ip address change detected update set param oldIP and new IP
	 */
	public void addItem(Item item, boolean isFound, String oldIpAddress, String newIpAddress) {
		if (item != null ){
			PIQItemNotInSyncParams params = new PIQItemNotInSyncParams();
			params.setFound(isFound);
			params.setIpChanged(true);
			params.setOldIpAddress(oldIpAddress);
			params.setNewIpAddress(newIpAddress);
			itemsNotInSync.put(item.getItemId(), params);
		}
	}
	
	
	/**
	 * Remove an item that is *not* in sync
	 * @param item that is not in sync
	 * @param isFound TODO
	 */
	public void removeItem(Item item, boolean isFound){
		if (item != null){
			itemsNotInSync.remove(item.getItemId());
		}
	}
	
	/**
	 * Clear all the items in the opdu.getIpAddress()bject
	 */
	public void clear(){
		itemsNotInSync.clear();
	}
	
	/**
	 * Returns true if the item is not in sync with PowerIQ
	 * @param item that is not in sync
	 */
	public boolean isNotInSync(Item item){
		return (item.getPiqId() == null ||  (itemsNotInSync != null && itemsNotInSync.containsKey(item.getItemId())));
	}
	
	public boolean isFound(Item item){
		if (item != null && itemsNotInSync != null && itemsNotInSync.get(item.getItemId()) != null){
			boolean found = itemsNotInSync.get(item.getItemId()).isFound();
			return found;
		}else {
			return false;
		}
	}
	
	public boolean isReplace(Item item){
		if (item != null  && itemsNotInSync != null&& itemsNotInSync.get(item.getItemId()) != null){
			boolean replace = itemsNotInSync.get(item.getItemId()).isReplace();
			return replace;
		}else {
			return false;
		}
	}
	
	public boolean isIpChanged(Item item){
		if (item == null  && itemsNotInSync != null) return false;
		if (itemsNotInSync.get(item.getItemId()) != null)
			return itemsNotInSync.get(item.getItemId()).isIpChanged();
		return false;
	}
	
	public String getoldIP(Item item) {
		if (item == null  && itemsNotInSync != null) return null;
		if (itemsNotInSync.get(item.getItemId()) != null)
			return  itemsNotInSync.get(item.getItemId()).getOldIpAddress();
		return null;
	}

	public String getnewIP(Item item) {
		if (item == null  && itemsNotInSync != null) return null;
		if (itemsNotInSync.get(item.getItemId()) != null)
			return  itemsNotInSync.get(item.getItemId()).getNewIpAddress();
		
		return null;
	}
	
	private class PIQItemNotInSyncParams {
		private boolean isFound = false;
		private boolean isReplace = false;
		private boolean ipChanged = false;
		private String oldIpAddress = "";
		private String newIpAddress = "";
		
		public boolean isFound() {
			return isFound;
		}
		
		public void setFound(boolean isFound) {
			this.isFound = isFound;
		}

		public boolean isReplace() {
			return isReplace;
		}

		public void setReplace(boolean isReplace) {
			this.isReplace = isReplace;
		}

		public boolean isIpChanged() {
			return ipChanged;
		}

		public void setIpChanged(boolean ipChanged) {
			this.ipChanged = ipChanged;
		}

		public String getOldIpAddress() {
			return oldIpAddress;
		}

		public void setOldIpAddress(String oldIpAddress) {
			this.oldIpAddress = oldIpAddress;
		}

		public String getNewIpAddress() {
			return newIpAddress;
		}
		public void setNewIpAddress(String newIpAddress) {
			this.newIpAddress = newIpAddress;
		}
	}
}
