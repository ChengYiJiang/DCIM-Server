package com.raritan.tdz.item.dto;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.raritan.tdz.lookup.SystemLookup;

public class CloneItemDTO {
	private long itemIdToClone;
	private String itemName;
	private long clonedItemlocationId;
	private String newItemName;
	private boolean includeChildren;
	private boolean includeCustomFieldData;
	private boolean includeFarEndDataPanel;
	private int quantity;                 //number of items to create
	private boolean includePowerPort;
	private boolean includeDataPort;
	private boolean includeSensorPort;
	private boolean keepParentChildAssoc;
	private long statusValueCode;
	private boolean filterItemList;
	
	private Timestamp creationDate;
	
	
	public CloneItemDTO(){
		includePowerPort = true;
		includeDataPort = true;
		includeSensorPort = true;
		quantity = 1;
		statusValueCode = SystemLookup.ItemStatus.PLANNED;
		creationDate = getCurrentDate();
	}

	public CloneItemDTO(long itemId){
		this();
		
		itemIdToClone = itemId;
	}
	
	public long getItemIdToClone() {
		return itemIdToClone;
	}
	public void setItemIdToClone(long itemIdToClone) {
		this.itemIdToClone = itemIdToClone;
	}
	public long getClonedItemlocationId() {
		return clonedItemlocationId;
	}
	public void setClonedItemlocationId(long clonedItemlocationId) {
		this.clonedItemlocationId = clonedItemlocationId;
	}
	public String getNewItemName() {
		return newItemName;
	}
	public void setNewItemName(String newItemName) {
		this.newItemName = newItemName;
	}
	public boolean isIncludeChildren() {
		return includeChildren;
	}
	public void setIncludeChildren(boolean includeChildren) {
		this.includeChildren = includeChildren;
	}
	public boolean isIncludeCustomFieldData() {
		return includeCustomFieldData;
	}
	public void setIncludeCustomFieldData(boolean includeCustomFieldData) {
		this.includeCustomFieldData = includeCustomFieldData;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public boolean isIncludePowerPort() {
		return includePowerPort;
	}
	public void setIncludePowerPort(boolean includePowerPort) {
		this.includePowerPort = includePowerPort;
	}
	public boolean isIncludeDataPort() {
		return includeDataPort;
	}
	public void setIncludeDataPort(boolean includeDataPort) {
		this.includeDataPort = includeDataPort;
	}
	public boolean isIncludeSensorPort() {
		return includeSensorPort;
	}
	public void setIncludeSensorPort(boolean includeSensorPort) {
		this.includeSensorPort = includeSensorPort;
	}

	public boolean isKeepParentChildAssoc() {
		return keepParentChildAssoc;
	}

	public void setKeepParentChildAssoc(boolean keepParentChildAssoc) {
		this.keepParentChildAssoc = keepParentChildAssoc;
	}

	
	public long getStatusValueCode() {
		return statusValueCode;
	}

	public void setStatusValueCode(long statusValueCode) {
		this.statusValueCode = statusValueCode;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public boolean isFilterItemList() {
		return filterItemList;
	}

	public void setFilterItemList(boolean filterItemList) {
		this.filterItemList = filterItemList;
	}

	
	public boolean isIncludeFarEndDataPanel() {
		return includeFarEndDataPanel;
	}

	public void setIncludeFarEndDataPanel(boolean includeFarEndDataPanel) {
		this.includeFarEndDataPanel = includeFarEndDataPanel;
	}

	@Override
	public String toString() {
		return "CloneItemDTO [itemIdToClone=" + itemIdToClone + ", itemName="
				+ itemName + ", clonedItemlocationId=" + clonedItemlocationId
				+ ", newItemName=" + newItemName + ", includeChildren="
				+ includeChildren + ", includeCustomFieldData="
				+ includeCustomFieldData + ", includeFarEndDataPanel="
				+ includeFarEndDataPanel + ", quantity=" + quantity
				+ ", includePowerPort=" + includePowerPort
				+ ", includeDataPort=" + includeDataPort
				+ ", includeSensorPort=" + includeSensorPort
				+ ", keepParentChildAssoc=" + keepParentChildAssoc
				+ ", statusValueCode=" + statusValueCode + ", filterItemList="
				+ filterItemList + ", creationDate=" + creationDate + "]";
	}
	
	public Timestamp getCurrentDate(){
		Timestamp ts = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(ts.toString());
			
			ts = new Timestamp(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ts;
	}

}
