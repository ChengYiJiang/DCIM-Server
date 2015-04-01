/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;


/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class PduJSON {
	private Pdu pdu;
	private List<Pdu> pdus = null;
	
	public static String externalKeyPrefix = "DCT Rack PDU -- "; 

	public PduJSON(){
		super();
	}
	
	public PduJSON(Item item, DataPort dataPort, String ipAddress, boolean update){
		pdu = new Pdu();
		
		if (update)
			pdu.setId(null);
		else
		{
			if (item.getPiqId() != null)
				pdu.setId(item.getPiqId().toString());
			
			pdu.setExternalKey(externalKeyPrefix + item.getItemId());
			pdu.setIpAddress(ipAddress);
			//Set up proxy number
			if (item.getGroupingNumber() != null && !item.getGroupingNumber().isEmpty()){
				pdu.setProxyIndex(Integer.parseInt(item.getGroupingNumber()));
			}
		}
		
		
		if (dataPort != null && dataPort.getCommunityString() != null && !dataPort.getCommunityString().isEmpty())
			pdu.setSnmpCommunityString(dataPort.getCommunityString());
		
		ItemSNMP snmpData = item.getItemSnmp();
		if (snmpData != null){
			if (snmpData.getPxUserName() != null && !snmpData.getPxUserName().isEmpty()
					&& snmpData.getPxPassword() != null && !snmpData.getPxPassword().isEmpty()){
				pdu.setIpmiUserName(snmpData.getPxUserName());
				pdu.setIpmiPassword(snmpData.getPxPassword());
			}
			
			if (null != snmpData.getSnmp3Enabled() && snmpData.getSnmp3Enabled()){
				pdu.setSnmp3Enabled(true);
				pdu.setSnmp3User(snmpData.getSnmp3User());
				pdu.setSnmp3AuthLevel(snmpData.getSnmp3AuthLevel());
				pdu.setSnmp3AuthProtocol(snmpData.getSnmp3AuthProtocol());
				pdu.setSnmp3AuthPasskey(snmpData.getSnmp3AuthPasskey());
				pdu.setSnmp3PrivProtocol(snmpData.getSnmp3PrivProtocol());
				pdu.setSnmp3PrivPassKey(snmpData.getSnmp3PrivPasskey());
			}
			else {
				pdu.setSnmp3Enabled(false);
			}
		}
		else {
			pdu.setSnmp3Enabled(false);
		}
	}
	
	public void setExternalKey(Item item, String ipAddress, boolean reset, String piqIdForReset){
		if (item != null){
			pdu = new Pdu();
			Integer rackId = item.getPiqId();
			if (rackId != null)
				pdu.setId(item.getPiqId().toString());
			
			if (reset == true && piqIdForReset != null){
				pdu.setId(piqIdForReset);
				pdu.setExternalKey(ipAddress);
			} else if (reset == false){
				pdu.setExternalKey(externalKeyPrefix + item.getItemId());
			}
		}
	}
	
	/**
	 * @return the pdu
	 */
	@JsonProperty(value="pdu")
	public Pdu getPdu() {
		return pdu;
	}

	/**
	 * @param pdu the pdu to set
	 */
	@JsonSetter(value="pdu")
	public void setPdu(Pdu pdu) {
		this.pdu = pdu;
	}

	@JsonProperty(value="pdus")
	public List<Pdu> getPdus() {
		return pdus;
	}

	@JsonSetter(value="pdus")
	public void setPdus(List<Pdu> pdus) {
		this.pdus = pdus;
	}
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Pdu{
		private Logger log = Logger.getLogger(this.getClass());
		
		private String id;
		private Boolean snmp3Enabled;
		private String snmp3User;
		private String snmp3AuthLevel;
		private String snmp3AuthProtocol;
		private String snmp3AuthPasskey;
		private String snmp3PrivProtocol;
		private String snmp3PrivPassKey;
		
		private String snmpCommunityString;
		
		private String ipmiUserName;
	

		private String ipmiPassword;

	
		private String caption;
		private String description;
		private String contact;
		private Integer proxyIndex;
		private Boolean requiresManualVoltage;
		private String configuredInletVoltage;
		private String configuredOutletVoltage;
		
		private Boolean singleSignOn;
		private Boolean supportsFirmwareUpgrades;
		private Boolean supportsBulkConfiguration;
		private Boolean supportsOutletPowerControl;
		private Boolean supportsOutletRenaming;
		
		private String name;
		private String model;
		private String location;
		private String serialNumber;
		private String manufacturer;
		private String firmwareVersion;
		private String pollerPlugin;
		
		private String ratedVolts;
		private String ratedAmps;
		private String ratedVA;
		
		private String ipAddress;
		private String inlineMeter;
		
		private Boolean supportsReadingOnlyPoll;
		private Boolean supportsDataLogging;
		private Boolean supportsSensorRenaming;
		
		private String defaultConnectedLEDColor;
		private String defaultDisconnectedLEDColor;
		private String dynamicPluginName;
		
		private String phase;
		private Boolean userDefinedPhase;
		
		private String customField1;
		private String customField2;
		private String externalKey;
		
		
		private Health health;
		private Reading reading;
		
		private Parent parent;
		
		
		
		public Pdu() {
			super();
		}

		//Check to see if we need to update the PDU on PowerIQ
		public Boolean isUpdate(Item item, String ipAddress){
			Boolean result = false;
			
			//TODO: When we require this method, we will update it.
			//TODO: Other than checking if we need to replace the PDU, 
			//TODO: we cannot check anything else since PowerIQ does not 
			//TODO: give us anything for a Rack PDU to check against!
			return result;
		}
		
		//Check to see if we need to replace the PDU on PowerIQ
		public Boolean isReplace(Item item, String ipAddress){
			Boolean result = false;
			Boolean idInSync = item.getPiqId() != null ? isIdInSync(item.getPiqId().toString()):false;
			Boolean proxyIdxInSync = item.getGroupingNumber() != null ? isProxyIndexInSync(Integer.parseInt(item.getGroupingNumber()))
					: isProxyIndexInSync(null);
			Boolean ipAddressInSync = isIpAddressInSync(ipAddress);
			
			if (
					!(
					   (idInSync)
					&& (ipAddressInSync)
					&& (proxyIdxInSync)
					)
				)
					result = true;
			
			return result;
		}

		/**
		 * This function verifies whether PDU IPAddress has changed and proxyId && device Id is intact.
		 * @param item
		 * @param ipAddress
		 * @return true if IP address of PDU is changed otherwise false
		 */
		//Check to see if we need to replace the PDU on PowerIQ
		public Boolean hasIPAddressChanged(Item item, String ipAddress){
			Boolean result = false;
			Boolean idInSync = item.getPiqId() != null ? isIdInSync(item.getPiqId().toString()):false;
			String itemGroupingNumber =  item.getGroupingNumber();
			Boolean proxyIdxInSync = false;
			
			try{
				proxyIdxInSync =  isProxyIndexInSync(Integer.parseInt(itemGroupingNumber));
			} catch (NumberFormatException e){
				log.error("item grouping number is not valid, it must be of numeric type in database, verify.. ");
				proxyIdxInSync = isProxyIndexInSync(null);
			}
			
			Boolean ipAddressInSync = isIpAddressInSync(ipAddress);
			if (idInSync && proxyIdxInSync && !(ipAddressInSync)) return true;
			return result;
		}

		@JsonProperty(value="parent")
		public Parent getParent() {
			return parent;
		}
		
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
		 * @return the snmp3Enabled
		 */
		@JsonProperty(value="snmp3_enabled")
		public Boolean getSnmp3Enabled() {
			return snmp3Enabled;
		}

		/**
		 * @param snmp3Enabled the snmp3Enabled to set
		 */
		@JsonSetter(value="snmp3_enabled")
		public void setSnmp3Enabled(Boolean snmp3Enabled) {
			this.snmp3Enabled = snmp3Enabled;
		}

		/**
		 * @return the snmp3User
		 */
		@JsonProperty(value="snmp3_user")
		public String getSnmp3User() {
			return snmp3User;
		}

		/**
		 * @param snmp3User the snmp3User to set
		 */
		@JsonSetter(value="snmp3_user")
		public void setSnmp3User(String snmp3User) {
			this.snmp3User = snmp3User;
		}

		/**
		 * @return the snmp3AuthLevel
		 */
		@JsonProperty(value="snmp3_auth_level")
		public String getSnmp3AuthLevel() {
			return snmp3AuthLevel;
		}

		/**
		 * @param snmp3AuthLevel the snmp3AuthLevel to set
		 */
		@JsonSetter(value="snmp3_auth_level")
		public void setSnmp3AuthLevel(String snmp3AuthLevel) {
			this.snmp3AuthLevel = snmp3AuthLevel;
		}
		
		/**
		 * @return the snmp3AuthProtocol
		 */
		@JsonProperty(value="snmp3_auth_protocol")
		public String getSnmp3AuthProtocol() {
			return snmp3AuthProtocol;
		}

		/**
		 * @param snmp3PrivProtocol the snmp3AuthProtocol to set
		 */
		@JsonSetter(value="snmp3_auth_protocol")
		public void setSnmp3AuthProtocol(String snmp3AuthProtocol) {
			this.snmp3AuthProtocol = snmp3AuthProtocol;
		}

		/**
		 * @return the snmp3PrivProtocol
		 */
		@JsonProperty(value="snmp3_priv_protocol")
		public String getSnmp3PrivProtocol() {
			return snmp3PrivProtocol;
		}

		/**
		 * @param snmp3PrivProtocol the snmp3PrivProtocol to set
		 */
		@JsonSetter(value="snmp3_priv_protocol")
		public void setSnmp3PrivProtocol(String snmp3PrivProtocol) {
			this.snmp3PrivProtocol = snmp3PrivProtocol;
		}

		/**
		 * @return the snmp3AuthPasskey
		 */
		@JsonProperty(value="snmp3_auth_passkey")
		public String getSnmp3AuthPasskey() {
			return snmp3AuthPasskey;
		}

		/**
		 * @param snmp3AuthPasskey the snmp3AuthPasskey to set
		 */
		@JsonSetter(value="snmp3_auth_passkey")
		public void setSnmp3AuthPasskey(String snmp3AuthPasskey) {
			this.snmp3AuthPasskey = snmp3AuthPasskey;
		}

		/**
		 * @return the snmp3PrivPassKey
		 */
		@JsonProperty(value="snmp3_priv_passkey")
		public String getSnmp3PrivPassKey() {
			return snmp3PrivPassKey;
		}

		/**
		 * @param snmp3PrivPassKey the snmp3PrivPassKey to set
		 */
		@JsonSetter(value="snmp3_priv_passkey")
		public void setSnmp3PrivPassKey(String snmp3PrivPassKey) {
			this.snmp3PrivPassKey = snmp3PrivPassKey;
		}

		/**
		 * @return the snmpCommunityString
		 */
		@JsonProperty(value="snmp_community_string")
		public final String getSnmpCommunityString() {
			return snmpCommunityString;
		}

		/**
		 * @param snmpCommunityString the snmpCommunityString to set
		 */
		@JsonSetter(value="snmp_community_string")
		public final void setSnmpCommunityString(String snmpCommunityString) {
			this.snmpCommunityString = snmpCommunityString;
		}
		
		@JsonProperty(value="ipmi_username")
		public final String getIpmiUserName() {
			return ipmiUserName;
		}

		@JsonSetter(value="ipmi_username")
		public final void setIpmiUserName(String ipmiUserName) {
			this.ipmiUserName = ipmiUserName;
		}

		@JsonProperty(value="ipmi_password")
		public String getIpmiPassword() {
			return ipmiPassword;
		}

		@JsonSetter(value="ipmi_password")
		public void setIpmiPassword(String ipmiPassword) {
			this.ipmiPassword = ipmiPassword;
		}

		/**
		 * @return the caption
		 */
		@JsonProperty(value="caption")
		public String getCaption() {
			return caption;
		}

		/**
		 * @param caption the caption to set
		 */
		@JsonSetter(value="caption")
		public void setCaption(String caption) {
			this.caption = caption;
		}

		/**
		 * @return the description
		 */
		@JsonProperty(value="description")
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		@JsonSetter(value="description")
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return the contact
		 */
		@JsonSetter(value="contact")
		public String getContact() {
			return contact;
		}

		/**
		 * @param contact the contact to set
		 */
		@JsonSetter(value="contact")
		public void setContact(String contact) {
			this.contact = contact;
		}

		/**
		 * @return the proxyIndex
		 */
		@JsonProperty(value="proxy_index")
		public Integer getProxyIndex() {
			return proxyIndex;
		}



		/**
		 * @param proxyIndex the proxyIndex to set
		 */
		@JsonSetter(value="proxy_index")
		public void setProxyIndex(Integer proxyIndex) {
			this.proxyIndex = proxyIndex;
		}


		public boolean isProxyIndexInSync(Integer proxyIndex){
			return ( this.proxyIndex == null && proxyIndex == null) || (this.proxyIndex != null && proxyIndex != null && this.proxyIndex.equals((proxyIndex)));
		}

		/**
		 * @return the requiresManualVoltage
		 */
		@JsonProperty(value="requires_manual_voltage")
		public Boolean getRequiresManualVoltage() {
			return requiresManualVoltage;
		}

		/**
		 * @param requiresManualVoltage the requiresManualVoltage to set
		 */
		@JsonSetter(value="requires_manual_voltage")
		public void setRequiresManualVoltage(Boolean requiresManualVoltage) {
			this.requiresManualVoltage = requiresManualVoltage;
		}

		/**
		 * @return the configuredInletVoltage
		 */
		@JsonProperty(value="configured_inlet_voltage")
		public String getConfiguredInletVoltage() {
			return configuredInletVoltage;
		}

		/**
		 * @param configuredInletVoltage the configuredInletVoltage to set
		 */
		@JsonSetter(value="configured_inlet_voltage")
		public void setConfiguredInletVoltage(String configuredInletVoltage) {
			this.configuredInletVoltage = configuredInletVoltage;
		}

		/**
		 * @return the configuredOutletVoltage
		 */
		@JsonProperty(value="configured_outlet_voltage")
		public String getConfiguredOutletVoltage() {
			return configuredOutletVoltage;
		}

		/**
		 * @param configuredOutletVoltage the configuredOutletVoltage to set
		 */
		@JsonSetter(value="configured_outlet_voltage")
		public void setConfiguredOutletVoltage(String configuredOutletVoltage) {
			this.configuredOutletVoltage = configuredOutletVoltage;
		}

		/**
		 * @return the singleSignOn
		 */
		@JsonProperty(value="supports_single_sign_on")
		public Boolean getSingleSignOn() {
			return singleSignOn;
		}

		/**
		 * @param singleSignOn the singleSignOn to set
		 */
		@JsonSetter(value="supports_single_sign_on")
		public void setSingleSignOn(Boolean singleSignOn) {
			this.singleSignOn = singleSignOn;
		}

		/**
		 * @return the supportsFirmwareUpgrades
		 */
		@JsonProperty(value="supports_firmware_upgrades")
		public Boolean getSupportsFirmwareUpgrades() {
			return supportsFirmwareUpgrades;
		}

		/**
		 * @param supportsFirmwareUpgrades the supportsFirmwareUpgrades to set
		 */
		@JsonSetter(value="supports_firmware_upgrades")
		public void setSupportsFirmwareUpgrades(Boolean supportsFirmwareUpgrades) {
			this.supportsFirmwareUpgrades = supportsFirmwareUpgrades;
		}

		/**
		 * @return the supportsBulkConfiguration
		 */
		@JsonProperty(value="supports_bulk_configuration")
		public Boolean getSupportsBulkConfiguration() {
			return supportsBulkConfiguration;
		}

		/**
		 * @param supportsBulkConfiguration the supportsBulkConfiguration to set
		 */
		@JsonSetter(value="supports_bulk_configuration")
		public void setSupportsBulkConfiguration(Boolean supportsBulkConfiguration) {
			this.supportsBulkConfiguration = supportsBulkConfiguration;
		}

		/**
		 * @return the supportsOutletPowerControl
		 */
		@JsonProperty(value="supports_outlet_power_control")
		public Boolean getSupportsOutletPowerControl() {
			return supportsOutletPowerControl;
		}

		/**
		 * @param supportsOutletPowerControl the supportsOutletPowerControl to set
		 */
		@JsonSetter(value="supports_outlet_power_control")
		public void setSupportsOutletPowerControl(Boolean supportsOutletPowerControl) {
			this.supportsOutletPowerControl = supportsOutletPowerControl;
		}

		/**
		 * @return the supportsOutletRenaming
		 */
		@JsonProperty(value="supports_outlet_renaming")
		public Boolean getSupportsOutletRenaming() {
			return supportsOutletRenaming;
		}

		/**
		 * @param supportsOutletRenaming the supportsOutletRenaming to set
		 */
		@JsonSetter(value="supports_outlet_renaming")
		public void setSupportsOutletRenaming(Boolean supportsOutletRenaming) {
			this.supportsOutletRenaming = supportsOutletRenaming;
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

		/**
		 * @return the model
		 */
		@JsonProperty(value="model")
		public String getModel() {
			return model;
		}

		/**
		 * @param model the model to set
		 */
		@JsonSetter(value="model")
		public void setModel(String model) {
			this.model = model;
		}

		/**
		 * @return the location
		 */
		@JsonProperty(value="location")
		public String getLocation() {
			return location;
		}

		/**
		 * @param location the location to set
		 */
		@JsonSetter(value="location")
		public void setLocation(String location) {
			this.location = location;
		}

		/**
		 * @return the serialNumber
		 */
		@JsonProperty(value="serial_number")
		public String getSerialNumber() {
			return serialNumber;
		}

		/**
		 * @param serialNumber the serialNumber to set
		 */
		@JsonSetter(value="serial_number")
		public void setSerialNumber(String serialNumber) {
			this.serialNumber = serialNumber;
		}

		/**
		 * @return the manufacturer
		 */
		@JsonProperty(value="manufacturer")
		public String getManufacturer() {
			return manufacturer;
		}

		/**
		 * @param manufacturer the manufacturer to set
		 */
		@JsonSetter(value="manufacturer")
		public void setManufacturer(String manufacturer) {
			this.manufacturer = manufacturer;
		}

		/**
		 * @return the firmwareVersion
		 */
		@JsonProperty(value="firmware_version")
		public String getFirmwareVersion() {
			return firmwareVersion;
		}

		/**
		 * @param firmwareVersion the firmwareVersion to set
		 */
		@JsonSetter(value="firmware_version")
		public void setFirmwareVersion(String firmwareVersion) {
			this.firmwareVersion = firmwareVersion;
		}

		/**
		 * @return the pollerPlugin
		 */
		@JsonProperty(value="poller_plugin")
		public String getPollerPlugin() {
			return pollerPlugin;
		}

		/**
		 * @param pollerPlugin the pollerPlugin to set
		 */
		@JsonSetter(value="poller_plugin")
		public void setPollerPlugin(String pollerPlugin) {
			this.pollerPlugin = pollerPlugin;
		}

		/**
		 * @return the ratedVolts
		 */
		@JsonProperty(value="rated_volts")
		public String getRatedVolts() {
			return ratedVolts;
		}

		/**
		 * @param ratedVolts the ratedVolts to set
		 */
		@JsonSetter(value="rated_volts")
		public void setRatedVolts(String ratedVolts) {
			this.ratedVolts = ratedVolts;
		}

		/**
		 * @return the ratedAmps
		 */
		@JsonProperty(value="rated_amps")
		public String getRatedAmps() {
			return ratedAmps;
		}

		/**
		 * @param ratedAmps the ratedAmps to set
		 */
		@JsonSetter(value="rated_amps")
		public void setRatedAmps(String ratedAmps) {
			this.ratedAmps = ratedAmps;
		}

		/**
		 * @return the ratedVA
		 */
		@JsonProperty(value="rated_va")
		public String getRatedVA() {
			return ratedVA;
		}

		/**
		 * @param ratedVA the ratedVA to set
		 */
		@JsonSetter(value="rated_va")
		public void setRatedVA(String ratedVA) {
			this.ratedVA = ratedVA;
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
		 * @return the inlineMeter
		 */
		@JsonProperty(value="inline_meter")
		public String getInlineMeter() {
			return inlineMeter;
		}

		/**
		 * @param inlineMeter the inlineMeter to set
		 */
		@JsonSetter(value="inline_meter")
		public void setInlineMeter(String inlineMeter) {
			this.inlineMeter = inlineMeter;
		}

		/**
		 * @return the supportsReadingOnlyPoll
		 */
		@JsonProperty(value="supports_readingsonly_poll")
		public Boolean getSupportsReadingOnlyPoll() {
			return supportsReadingOnlyPoll;
		}

		/**
		 * @param supportsReadingOnlyPoll the supportsReadingOnlyPoll to set
		 */
		@JsonSetter(value="supports_readingsonly_poll")
		public void setSupportsReadingOnlyPoll(Boolean supportsReadingOnlyPoll) {
			this.supportsReadingOnlyPoll = supportsReadingOnlyPoll;
		}

		/**
		 * @return the supportsDataLogging
		 */
		@JsonProperty(value="supports_data_logging")
		public Boolean getSupportsDataLogging() {
			return supportsDataLogging;
		}

		/**
		 * @param supportsDataLogging the supportsDataLogging to set
		 */
		@JsonSetter(value="supports_data_logging")
		public void setSupportsDataLogging(Boolean supportsDataLogging) {
			this.supportsDataLogging = supportsDataLogging;
		}

		/**
		 * @return the supportsSensorRenaming
		 */
		@JsonProperty(value="supports_sensor_renaming")
		public Boolean getSupportsSensorRenaming() {
			return supportsSensorRenaming;
		}

		/**
		 * @param supportsSensorRenaming the supportsSensorRenaming to set
		 */
		@JsonSetter(value="supports_sensor_renaming")
		public void setSupportsSensorRenaming(Boolean supportsSensorRenaming) {
			this.supportsSensorRenaming = supportsSensorRenaming;
		}

		/**
		 * @return the defaultConnectedLEDColor
		 */
		@JsonProperty(value="default_connected_led_color")
		public String getDefaultConnectedLEDColor() {
			return defaultConnectedLEDColor;
		}

		/**
		 * @param defaultConnectedLEDColor the defaultConnectedLEDColor to set
		 */
		@JsonSetter(value="default_connected_led_color")
		public void setDefaultConnectedLEDColor(String defaultConnectedLEDColor) {
			this.defaultConnectedLEDColor = defaultConnectedLEDColor;
		}

		/**
		 * @return the defaultDisconnectedLEDColor
		 */
		@JsonProperty(value="default_disconnected_led_color")
		public String getDefaultDisconnectedLEDColor() {
			return defaultDisconnectedLEDColor;
		}

		/**
		 * @param defaultDisconnectedLEDColor the defaultDisconnectedLEDColor to set
		 */
		@JsonSetter(value="default_disconnected_led_color")
		public void setDefaultDisconnectedLEDColor(String defaultDisconnectedLEDColor) {
			this.defaultDisconnectedLEDColor = defaultDisconnectedLEDColor;
		}

		/**
		 * @return the dynamicPluginName
		 */
		@JsonProperty(value="dynamic_plugin_name")
		public String getDynamicPluginName() {
			return dynamicPluginName;
		}

		/**
		 * @param dynamicPluginName the dynamicPluginName to set
		 */
		@JsonSetter(value="dynamic_plugin_name")
		public void setDynamicPluginName(String dynamicPluginName) {
			this.dynamicPluginName = dynamicPluginName;
		}

		/**
		 * @return the phase
		 */
		@JsonProperty(value="phase")
		public String getPhase() {
			return phase;
		}

		/**
		 * @param phase the phase to set
		 */
		@JsonSetter(value="phase")
		public void setPhase(String phase) {
			this.phase = phase;
		}

		/**
		 * @return the userDefinedPhase
		 */
		@JsonProperty(value="user_defined_phase")
		public Boolean getUserDefinedPhase() {
			return userDefinedPhase;
		}

		/**
		 * @param userDefinedPhase the userDefinedPhase to set
		 */
		@JsonSetter(value="user_defined_phase")
		public void setUserDefinedPhase(Boolean userDefinedPhase) {
			this.userDefinedPhase = userDefinedPhase;
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
		 * @return the health
		 */
		@JsonProperty(value="health")
		public Health getHealth() {
			return health;
		}

		/**
		 * @param health the health to set
		 */
		@JsonSetter(value="health")
		public void setHealth(Health health) {
			this.health = health;
		}

		/**
		 * @return the reading
		 */
		@JsonIgnore
		public final Reading getReading() {
			return reading;
		}

		/**
		 * @param reading the reading to set
		 */
		@JsonSetter(value="reading")
		public final void setReading(Reading reading) {
			this.reading = reading;
		}

		@JsonIgnoreProperties(ignoreUnknown=true)
		@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
		public static class Reading {

			public Reading() {
				super();
			}
			
		}

		@JsonIgnoreProperties(ignoreUnknown=true)
		@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
		public static class Health {
			private String overall;
			private String connectivity;
			private String connectivity_explanation;
			private String events;
			private Integer activeEventsCount;
			
			public Health() {
				super();
			}
			/**
			 * @return the overall
			 */
			@JsonProperty(value="overall")
			public String getOverall() {
				return overall;
			}
			/**
			 * @param overall the overall to set
			 */
			@JsonSetter(value="overall")
			public void setOverall(String overall) {
				this.overall = overall;
			}
			/**
			 * @return the connectivity
			 */
			@JsonProperty(value="connectivity")
			public String getConnectivity() {
				return connectivity;
			}
			/**
			 * @param connectivity the connectivity to set
			 */
			@JsonSetter(value="connectivity")
			public void setConnectivity(String connectivity) {
				this.connectivity = connectivity;
			}
			/**
			 * @return the connectivity_explanation
			 */
			@JsonProperty(value="connectivity_explanation")
			public String getConnectivity_explanation() {
				return connectivity_explanation;
			}
			/**
			 * @param connectivity_explanation the connectivity_explanation to set
			 */
			@JsonSetter(value="connectivity_explanation")
			public void setConnectivity_explanation(String connectivity_explanation) {
				this.connectivity_explanation = connectivity_explanation;
			}
			/**
			 * @return the events
			 */
			@JsonProperty(value="events")
			public String getEvents() {
				return events;
			}
			/**
			 * @param events the events to set
			 */
			@JsonSetter(value="events")
			public void setEvents(String events) {
				this.events = events;
			}
			/**
			 * @return the activeEventsCount
			 */
			@JsonProperty(value="active_events_count")
			public Integer getActiveEventsCount() {
				return activeEventsCount;
			}
			/**
			 * @param activeEventsCount the activeEventsCount to set
			 */
			@JsonSetter(value="active_events_count")
			public void setActiveEventsCount(Integer activeEventsCount) {
				this.activeEventsCount = activeEventsCount;
			}
		}
		
		@JsonIgnoreProperties(ignoreUnknown=true)
		@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
		public static class Parent{
			
			public Parent() {
				super();
			}

			private Rack rack;
			@JsonIgnoreProperties(ignoreUnknown=true)
			public static class Rack{
				private String id;
				
				public Rack() {
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
	}
}
