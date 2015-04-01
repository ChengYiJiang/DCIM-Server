/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DeviceJSON {
	
	private Device device = null;
	private List<Device> devices = null;
	public static String externalKeyPrefix = "DCT IT Item -- ";
	public static String externalKeyPrefixPIQ = "IT Device ";
	public DeviceJSON() {
		super();
	}
	
	public DeviceJSON(Item item, String ipAddress, Integer powerRating, boolean update){
		
		//Since we cannot send the parent during a PUT
		//we create a DeviceForUpdate class which will ignore this param
		//This resolves the issue of not having to send parent=null and have all the other optional params null (CR 40174)
		//Not an elegant solution, however this works!
		
		if (update)
			device = new DeviceForUpdate();
		else
			device = new Device();
		
		if (update)
			device.setId(null);
		else
		{
			if (item.getPiqId() != null)
				device.setId(item.getPiqId().toString());
		}
		
		device.setName(item.getItemName());
		
		if (item.getItemServiceDetails() != null && item.getItemServiceDetails().getDepartmentLookup() != null)
			device.setCustomer(item.getItemServiceDetails().getDepartmentLookup().getLkuValue());
		
		device.setDeviceType(device.getDeviceType(item));

		device.setPowerRating(powerRating);
		
		// if item is archived then it means it is decommissioned.
		device.setDecommissioned(item.isStatusArchived());
		
		device.setCustomField1(null);
		device.setCustomField2(null);
		device.setExternalKey(externalKeyPrefix + item.getItemId());
		device.setIpAddress(!isProbe(item) ? ipAddress : null);
		device.setAssetTagId(item.getRaritanAssetTag());
		
		if (!update && item.getParentItem() != null && item.getParentItem().getPiqId() != null)
			device.setParent((item.getParentItem().getPiqId().toString()));
	}
	
	public Boolean isDeviceInSync(Item item, String ipAddress, Integer powerRating ){
		return device.isDeviceInSync(item, ipAddress, powerRating);
	}
	
	public void setExternalKey(Item item, boolean reset, String piqIdForReset){
		if (item != null){
			device = new DeviceForUpdate();
			Integer deviceId = item.getPiqId();
			if (deviceId != null)
				device.setId(item.getPiqId().toString());
			if (reset == true && piqIdForReset != null){
				device.setId(piqIdForReset);
				device.setExternalKey(externalKeyPrefixPIQ + "-- " + device.getId());
			} else if (reset == false){
				device.setExternalKey(externalKeyPrefix + item.getItemId());
			}
		}
	}

	
	@JsonProperty(value="device")
	public Device getDevice() {
		return device;
	}

	@JsonSetter(value="device")
	public void setDevice(Device device) {
		this.device = device;
	}
	
	@JsonProperty(value="devices")
	public List<Device> getDevices() {
		return devices;
	}

	@JsonSetter(value="devices")
	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Device {
	private String id;
	private String name;
	private String customer;
	private String deviceType;
	private Integer powerRating;
	private Boolean decommissioned;
	private String customField1;
	private String customField2;
	private String externalKey;
	private String ipAddress;
	private String assetTagId;
	protected Parent parent;
	
	public Device(){
		super();
	}
	
	public Boolean isDeviceInSync(Item item, String ipAddress, Integer powerRating ){
		Boolean result = false;
		
		Boolean idInSync = item.getPiqId() != null ? isIdInSync(item.getPiqId().toString()):false;
		Boolean nameInSync = isNameInSync(item);
		Boolean customerInSync = (item.getItemServiceDetails() != null 
				&& item.getItemServiceDetails().getDepartmentLookup() != null)
				? isCustomerInSync(item.getItemServiceDetails().getDepartmentLookup().getLkuValue()):true; 
		Boolean deviceTypeInSync = isDeviceTypeInSync(getDeviceType(item));
		Boolean powerRatingInSync = isPowerRatingInSync(powerRating);
		Boolean ipAddressInSync = ((item.getClassLookup() != null) && (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)) || isIpAddressInSync(ipAddress);
		Boolean assetTagInSync = isAssetTagIdInSync(item.getRaritanAssetTag());
		Boolean itemDecommissioned = item.isStatusArchived(); // if item is archived then it means it is decommissioned.
		Boolean decommissionedInSync = isDecommissionedInSync(itemDecommissioned);
		Boolean externalKeyInSync = (externalKeyPrefix + item.getItemId()).equals(getExternalKey());
		
		Boolean parentIdInSync = true;
		//There may be no parent associated with the item. This means the item is in sync.
		if (item.getParentItem() != null && getParent() != null)
			parentIdInSync = item.getParentItem().getPiqId() != null && getParent().getRack() != null 
							? getParent().getRack().isIdInSync(item.getParentItem().getPiqId().toString()):false;
		
	
		if (
			   (idInSync)
			&& (nameInSync)
			&& (customerInSync)
			&& (deviceTypeInSync)
			&& (powerRatingInSync)
			&& (ipAddressInSync)
			&& (assetTagInSync) 
			&& (decommissionedInSync)
			&& (parentIdInSync)
//			&& (externalKeyInSync)
			)
			result = true;
		
		return result;
	}
	
	
	public String getDeviceType(Item item){
		String deviceType = null;
		if (item.getSubclassLookup() != null){
			deviceType = item.getSubclassLookup().getLkpValue();
		} else if (item.getClassLookup() != null){
			deviceType = item.getClassLookup().getLkpValue();
		}
		return deviceType;
	}
	
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Parent{
		
		private Rack rack;
		@JsonIgnoreProperties(ignoreUnknown=true)
		public static class Rack{
			private String id;
			
			public Rack(){
				super();
			}
			@JsonProperty(value="id")
			public String getId() {
				return id;
			}

			@JsonSetter(value="id")
			public void setId(String id) {
				this.id = id;
			}
			
			public Boolean isIdInSync(String id){
				return ((this.id == null && id == null) ||  (this.id != null && id != null && this.id.equals(id)));
			}
			
		}
		
		public Parent(){
			super();
		}
		
		@JsonProperty(value="rack")
		public Rack getRack() {
			return rack;
		}

		@JsonSetter(value="rack")
		public void setRack(Rack rack) {
			this.rack = rack;
		}

		public Parent(String id){
			setRack(new Rack());
			getRack().setId(id);
		}
	}
	
	@JsonProperty(value="parent")
	public Parent getParent() {
		return parent;
	}
	
	//@JsonIgnore
	@JsonSetter(value="parent")
	public void setParent(Parent parent) {
		this.parent = parent;
	}
	
	public void setParent(String id){
		setParent( new Parent(id) );
	}

	
	
	/**
	 * @return the id
	 */
	@JsonProperty(value="id")
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	@JsonSetter(value="id")
	public void setId(String id) {
		this.id = id;
	}
	
	public Boolean isIdInSync(String id){
		return ((this.id == null && id == null) || (this.id != null && id != null && this.id.equals(id)));
	}
	
	/**
	 * @return the name
	 */
	@JsonProperty(value="name")
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	@JsonSetter(value="name")
	public void setName(String name) {
		this.name = name;
	}
	
	public Boolean isNameInSync(String name){
		return ((this.name == null && name == null) || (this.name != null && name != null && this.name.equalsIgnoreCase(name)));
	}
	
	public Boolean isNameInSync(Item item){
		Boolean result = false;
		String piqName = getName() != null ? getName().toLowerCase() : null;
		String dcTrackName = item.getItemName() != null ? item.getItemName().toLowerCase() : null;
		
//		//There may be situation where Windows client truncates names and puts (<number>) at the end
//		//We need to handle this situiation.
//		//The best way is to look if we have (<number>) in the dcTrack item's name
//		//Then we take the substring that does not contain (<number>) and compare this
//		//against the PIQName. If it matches, we are good and do not need to perform any updates
//		//If it does not match then we will update the PIQ name
//		//Please note that we have a big assumption here. The item names (<number>) is not something
//		//that user sets in, but Windows client sets during PIQ import (wizard)
//		//If we do not have a (<number>), it just does a "contains" comparision
//		if (getName() != null && item.getItemName() != null){
//			piqName = getName().toLowerCase();
//			dcTrackName = item.getItemName().toLowerCase();
//			
//			String regex1 = ".*\\(\\d+\\)";
//			if (dcTrackName.matches(regex1)){
//				String regex2 = "\\(";
//				String[] tokens = dcTrackName.split(regex2);
//				dcTrackName = tokens[0];
//			}
//		}
		
		if (piqName != null && dcTrackName != null && piqName.equalsIgnoreCase(dcTrackName)){
			result = true;
		}
		
		return result;
	}
	
	/**
	 * @return the customer
	 */
	@JsonProperty(value="customer")
	public String getCustomer() {
		return customer;
	}
	/**
	 * @param customer the customer to set
	 */
	@JsonSetter(value="customer")
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	
	public Boolean isCustomerInSync(String customer){
		return ((this.customer == null && customer == null) || (this.customer != null && customer != null && this.customer.equals(customer)));
	}
	
	/**
	 * @return the deviceType
	 */
	@JsonProperty(value="device_type")
	public String getDeviceType() {
		return deviceType;
	}
	/**
	 * @param deviceType the deviceType to set
	 */
	@JsonSetter(value="device_type")
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public Boolean isDeviceTypeInSync(String deviceType){
		return ((this.deviceType == null && deviceType == null) || (this.deviceType != null && deviceType != null && this.deviceType.equals(deviceType)));
	}
	
	/**
	 * @return the powerRating
	 */
	@JsonProperty(value="power_rating")
	public Integer getPowerRating() {
		return powerRating;
	}
	/**
	 * @param powerRating the powerRating to set
	 */
	@JsonSetter(value="power_rating")
	public void setPowerRating(Integer powerRating) {
		this.powerRating = powerRating;
	}
	
	public Boolean isPowerRatingInSync(Integer powerRating){
		return (this.powerRating == null && powerRating == null) 
				|| (this.powerRating != null && this.powerRating.equals(powerRating));
	}
	
	/**
	 * @return the decommissioned
	 */
	@JsonProperty(value="decommissioned")
	public Boolean getDecommissioned() {
		return decommissioned;
	}
	/**
	 * @param decommissioned the decommissioned to set
	 */
	@JsonSetter(value="decommissioned")
	public void setDecommissioned(Boolean decommissioned) {
		this.decommissioned = decommissioned;
	}
	
	public Boolean isDecommissionedInSync(Boolean decommissioned){
		return (((this.decommissioned == null && decommissioned == null) 
				|| (this.decommissioned != null && this.decommissioned.equals(decommissioned))));
	}
	
	/**
	 * @return the customField1
	 */
	@JsonProperty(value="custom_field_1")
	public String getCustomField1() {
		return customField1;
	}
	/**
	 * @param customField1 the customField1 to set
	 */
	@JsonSetter(value="custom_field_1")
	public void setCustomField1(String customField1) {
		this.customField1 = customField1;
	}
	
	public Boolean isCustomField1InSync(String customField1){
		return (((this.customField1 == null && customField1 == null)) || (this.customField1 != null && customField1 != null && this.customField1.equals(customField1)));
	}
	
	/**
	 * @return the customField2
	 */
	@JsonProperty(value="custom_field_2")
	public String getCustomField2() {
		return customField2;
	}
	/**
	 * @param customField2 the customField2 to set
	 */
	@JsonSetter(value="custom_field_2")
	public void setCustomField2(String customField2) {
		this.customField2 = customField2;
	}
	
	public Boolean isCustomField2InSync(String customField2){
		return (((this.customField2 == null && customField2 == null)) || (this.customField2 != null && customField2 != null && this.customField2.equals(customField2)));
	}
	
	/**
	 * @return the externalKey
	 */
	@JsonProperty(value="external_key")
	public String getExternalKey() {
		return externalKey;
	}
	/**
	 * @param externalKey the externalKey to set
	 */
	@JsonSetter(value="external_key")
	public void setExternalKey(String externalKey) {
		this.externalKey = externalKey;
	}
	
	public Boolean isExternalKeyInSync(String externalKey){
		return (((this.externalKey == null && externalKey == null)) || (this.externalKey != null && externalKey != null && this.externalKey.equals(externalKey)));
	}
	
	/**
	 * @return the ipAddress
	 */
	@JsonProperty(value="ip_address")
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress the ipAddress to set
	 */
	@JsonSetter(value="ip_address")
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public Boolean isIpAddressInSync(String ipAddress){
		return ((this.ipAddress == null && ipAddress == null) || (this.ipAddress != null && ipAddress != null && this.ipAddress.equals(ipAddress)));
	}
	
	/**
	 * @return the assetTagId
	 */
	@JsonProperty(value="asset_tag_id")
	public String getAssetTagId() {
		return assetTagId;
	}
	/**
	 * @param assetTagId the assetTagId to set
	 */
	@JsonSetter(value="asset_tag_id")
	public void setAssetTagId(String assetTagId) {
		this.assetTagId = assetTagId;
	}
	
	public Boolean isAssetTagIdInSync(String assetTagId){
		return ((this.assetTagId == null && assetTagId == null) || (this.assetTagId != null && assetTagId != null && this.assetTagId.equals(assetTagId)));
	}
	

	
	}
	@JsonIgnoreProperties(ignoreUnknown=true)
	public class DeviceForUpdate extends Device {
		public DeviceForUpdate(){
			super();
		}
		@JsonIgnore
		public Parent getParent() {
			return parent;
		}
	}
	
	private boolean isProbe(Item item) {
		if (item == null) return false;
		if (item.getClassLookup() != null && item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE) {
			return true;
		}
		return false;
	}
}
