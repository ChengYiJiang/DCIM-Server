package com.raritan.tdz.dctimport.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.validation.Errors;

/**
 * import file for data connection
 * @author bunty/Santo Rosario
 *
 */
public class DataConnImport extends DCTImportBase {
	private Float circuitId;
	private Long statusLksValueCode;
	private String sharedCircuitTrace;
	
	@Header(value="startingitemlocation")
	private String startingItemLocation;
	
	@Header(value="startingitemname")
	private String startingItemName;
	
	@Header(value="startingportname")
	private String startingPortName;

	private Map<String,String> cordType;
	private Map<String,String> cordId;
	private Map<String,String> cordColor;
	private Map<String,String> cordLength;
	
	private Map<String,String> panelLocation;
	private Map<String,String> panelName;
	private Map<String,String> panelPortName;
	
	@Header(value="endingitemlocation")
	private String endingItemLocation;
	
	@Header(value="endingitemname")
	private String endingItemName;
	
	@Header(value="endingportname")
	private String endingPortName;

	public DataConnImport(List<String> specialHeaders){
		super(specialHeaders);
	}

	public DataConnImport() {
		super();
	}

	public String getStartingItemLocation() {
		return startingItemLocation;
	}

	public void setStartingItemLocation(String startingItemLocation) {
		this.startingItemLocation = startingItemLocation;
	}

	public String getStartingItemName() {
		return startingItemName;
	}

	public void setStartingItemName(String startingItemName) {
		this.startingItemName = startingItemName;
	}

	public String getStartingPortName() {
		return startingPortName;
	}

	public void setStartingPortName(String startingPortName) {
		this.startingPortName = startingPortName;
	}

	public Map<String, String> getCordType() {
		return cordType;
	}

	public void setCordType(Map<String, String> cordType) {
		this.cordType = cordType;
	}

	public Map<String, String> getCordId() {
		return cordId;
	}

	public void setCordId(Map<String, String> cordId) {
		this.cordId = cordId;
	}

	public Map<String, String> getCordColor() {
		return cordColor;
	}

	public void setCordColor(Map<String, String> cordColor) {
		this.cordColor = cordColor;
	}

	public Map<String, String> getCordLength() {
		return cordLength;
	}

	public void setCordLength(Map<String, String> cordLength) {
		this.cordLength = cordLength;
	}

	public Map<String, String> getPanelName() {
		return panelName;
	}

	public void setPanelName(Map<String, String> panelName) {
		this.panelName = panelName;
	}

	public Map<String, String> getPanelPortName() {
		return panelPortName;
	}

	public void setPanelPortName(Map<String, String> panelPortName) {
		this.panelPortName = panelPortName;
	}

	public String getEndingItemLocation() {
		return endingItemLocation;
	}

	public void setEndingItemLocation(String endingItemLocation) {
		this.endingItemLocation = endingItemLocation;
	}

	public String getEndingItemName() {
		return endingItemName;
	}

	public void setEndingItemName(String endingItemName) {
		this.endingItemName = endingItemName;
	}

	public String getEndingPortName() {
		return endingPortName;
	}

	public void setEndingPortName(String endingPortName) {
		this.endingPortName = endingPortName;
	}
	
	public Map<String, String> getPanelLocation() {
		return panelLocation;
	}

	public void setPanelLocation(Map<String, String> panelLocation) {
		this.panelLocation = panelLocation;
	}

	public Float getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(Float circuitId) {
		this.circuitId = circuitId;
	}
	
	public Long getStatusLksValueCode() {
		return statusLksValueCode;
	}

	public void setStatusLksValueCode(Long statusLksValueCode) {
		this.statusLksValueCode = statusLksValueCode;
	}

	public List<String> getCordTypeList() {
		return toArrayList(this.cordType);
	}
	
	public List<String> getCordIdList() {
		return toArrayList(this.cordId);
	}
	
	public List<String> getCordColorList() {
		return toArrayList(this.cordColor);
	}
	
	public List<String> getCordLengthList() {
		return toArrayList(this.cordLength);
	}
	
	public List<String> getPanelNameList() {
		return toArrayList(this.panelName);
	}
	
	public List<String> getPanelPortNameList() {
		return toArrayList(this.panelPortName);
	}

	public List<String> getPanelLocationList() {
		return toArrayList(this.panelLocation);
	}
	
	private List<String> toArrayList(Map<String, String> map) {
		TreeMap<String, String> treeMap = new TreeMap<String, String>(map);
			
		return new ArrayList(treeMap.values());
		/*List<String> list = new ArrayList<String>();
		
		for (String entry: treeMap.values()) {
			if(!entry.trim().isEmpty()) {
				list.add(entry);
			}
		}
		
		return list;*/
		
	}

	@Override
	public String toString() {
		return "DataConnImport [circuitId=" + circuitId
				+ ", statusLksValueCode=" + statusLksValueCode
				+ ", startingItemLocation=" + startingItemLocation
				+ ", startingItemName=" + startingItemName
				+ ", startingPortName=" + startingPortName + ", cordType="
				+ cordType + ", cordId=" + cordId + ", cordColor=" + cordColor
				+ ", cordLength=" + cordLength + ", panelLocation="
				+ panelLocation + ", panelName=" + panelName
				+ ", panelPortName=" + panelPortName + ", endingItemLocation="
				+ endingItemLocation + ", endingItemName=" + endingItemName
				+ ", endingPortName=" + endingPortName + "]";
	}

	public String getSharedCircuitTrace() {
		return sharedCircuitTrace;
	}

	public void setSharedCircuitTrace(String sharedCircuitTrace) {
		this.sharedCircuitTrace = sharedCircuitTrace;
	}
	
	public boolean isSharedConnection(Long connId) {
		if(sharedCircuitTrace != null && sharedCircuitTrace.isEmpty() == false) {
			return (sharedCircuitTrace.indexOf("," + connId + ",") > 0);
		}
		
		return false;
	}

	private void validate(String fieldName, String displayValue, Errors errors) {
		if(fieldName == null || fieldName.trim().isEmpty()) {
			Object[] errorArgs = { displayValue };
			errors.reject("ItemValidator.fieldRequired", errorArgs, "Cannot find cord type");
		}
	}

	public void checkRequiredFields(Errors errors) {
		validate(startingItemLocation, "Starting Item Location", errors);
		validate(startingItemName, "Starting Item Name", errors);
		validate(startingPortName, "Starting Port Name", errors);
		validate(endingItemLocation, "Ending Item Location", errors);
		validate(endingItemName, "Ending Item Name", errors);
		validate(endingPortName, "Ending Port Name", errors);
		
		checkPanels(errors);		
	}
	
	private void checkPanels(Errors errors) {
		List<String> panelLocList = getPanelLocationList();
		List<String> panelNameList = getPanelNameList();
		List<String> panelPortNameList = getPanelPortNameList();
		
		if(!(panelLocList.size() == panelNameList.size() && panelNameList.size() == panelPortNameList.size())) {
			Object[] errorArgs = {};
			errors.reject("Import.Circuit.InvalidCircuit", errorArgs, "Invalid circuit");
		}
		
		if(errors.hasErrors()) return;
		
		for(int i=0; i<panelLocList.size(); i++) {
			String temp = panelLocList.get(i);
			
			if(temp != null && temp.isEmpty() == false) { //then hop item and port must be provide
				validate(panelNameList.get(i), "Hop " + (i+1) + ":Near End Panel Name", errors);
				validate(panelPortNameList.get(i), "Hop " + (i+1) + ":Port Name", errors);
			}
		}

		if(errors.hasErrors()) return;
		
		for(int i=0; i<panelNameList.size(); i++) {
			String temp = panelNameList.get(i);
			
			if(temp != null && temp.isEmpty() == false) { //then hop item and port must be provide
				validate(panelLocList.get(i), "Hop " + (i+1) + ":Near End Location", errors);
				validate(panelPortNameList.get(i), "Hop " + (i+1) + ":Port Name", errors);
			}
		}

		if(errors.hasErrors()) return;
		
		for(int i=0; i<panelPortNameList.size(); i++) {
			String temp = panelPortNameList.get(i);
			
			if(temp != null && temp.isEmpty() == false) { //then hop item and port must be provide
				validate(panelLocList.get(i), "Hop " + (i+1) + ":Near End Location", errors);
				validate(panelNameList.get(i), "Hop " + (i+1) + ":Near End Panel Name", errors);
			}
		}		
	}	
}
