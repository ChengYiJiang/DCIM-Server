/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.util.List;
import java.util.Map;

/**
 * @author prasanna
 *
 */
public class ItemImport extends DCTImportBase {

	@Header(value="make")
	private String cmbMake;
	
	@Header(value="model")
    private String cmbModel;
	
	@Header(value="serialnumber")
    private String tiSerialNumber;
	
	@Header(value="assettag")
    private String tiAssetTag;
	
	@Header(value="eAssetTag")
    private String tieAssetTag;

    @Header(value="name")
    private String tiName;
    
    @Header(value="newname")
    private String newName;
    
    @Header(value="alias")
    private String tiAlias;
    
    @Header(value="type")
    private String cmbType;
    
    @Header(value="function")
    private String cmbFunction;

    @Header(value="sysadmin")
    private String cmbSystemAdmin;
    
    @Header(value="sysadminteam")
    private String cmbSystemAdminTeam;
    
    @Header(value="customer")
    private String cmbCustomer;

    @Header(value="status")
    private String cmbStatus;

    @Header(value="location")
    private String cmbLocation;
    
    @Header(value="newlocation")
    private String newLocation;
    
    @Header(value="locationreference")
    private String tiLocationRef;
    
    @Header(value="frontfaces")
    private String radioFrontFaces;
    
    @Header(value="cabinetside")
    private String radioCabinetSide;
    
    @Header(value="positioninrow")
    private String cmbRowPosition;
    
    @Header(value="rowlabel")
    private String cmbRowLabel;
    
    @Header(value="cabinet")
    private String cmbCabinet;
    
    @Header(value="railsused")
    private String radioRailsUsed;
    
    @Header(value="uposition")
    private String cmbUPosition;

    @Header(value="slotposition")
    private String cmbSlotPosition;
    
    @Header(value="orientation")
    private String cmbOrientation;
    
    @Header(value="chassis")
    private String cmbChassis;
    
    @Header(value="chassisface")
    private String radioChassisFace;
    
    @Header(value="depthposition")
    private String radioDepthPosition;
    
    @Header(value="order(l-r)")
    private Integer cmbOrder;
    
    @Header(value="ponumber")
    private String tiPONumber;
    
    @Header(value="purchaseprice")
    private Double tiPurchasePrice;
    
    @Header(value="purchasedate")
    private String dtPurchaseDate;
    
    @Header(value="installationdate")
    private String dtInstallationDate;
    
    @Header(value="slaprofile")
    private String cmbSLAProfile;
    
    @Header(value="contractnumber")
    private String cmbContractNumber;
    
    @Header(value="contractamount")
    private String tiContractAmount;
    
    @Header(value="contractstartdate")
    private String dtContractStartDate;
    
    @Header(value="contractenddate")
    private String dtContractEndDate;
    
    @Header(value="notes")
    private String tiNotes;

    //@Header(value="customfield")
    private Map<String,String> tiCustomField;

    @Header(value="operatingsystem")
    private String cmbOperatingSystem;
    
    @Header(value="domain")
    private String cmbDomain;
    
    @Header(value="cputype")
    private String tiCpuType;
    
    @Header(value="cpuquantity")
    private String tiCpuQuantity;
    
    @Header(value="ram")
    private String tiRAM;
    
    @Header(value="users")
    private String tiUsers;
    
    @Header(value="processes")
    private String tiProcesses;
    
    @Header(value="services")
    private String tiServices;
    
    @Header(value="vmcluster")
    private String cmbVMCluster;
    
    @Header(value="poweriqexternalkey")
    private String tiPiqExternalKey;
     
    @Header(value="ipaddress")
    private String ipAddress;
    
    @Header(value="proxyindex")
    private String proxyIndex;

    private String tiSubclass;


    @Header(value="ipaddressportname")
    private String ipAddressPortName;

	@Header(value="pxusername")
	private String tiPXUsername;

	@Header(value="pxpassword")
	private String tiPXPassword;

    @Header(value="snmp3enabled")
    private Boolean cmbSnmpV3Enabled;
    
    @Header(value="snmp3username")
    private String tiSnmpV3Username;
    
    @Header(value="snmp3authorizationlevel")
    private String cmbSnmpV3AuthLevel;
    
    @Header(value="snmp3authorizationprotocol")
    private String cmbSnmpV3AuthProtocol;
    
    @Header(value="snmp3authorizationpasskey")
    private String tiSnmpV3AuthPasskey;
    
    @Header(value="snmp3privacyprotocol")
    private String cmbSnmpV3PrivacyProtocol;
    
    @Header(value="snmp3privacypasskey")
    private String tiSnmpV3PrivacyPasskey;
    
    @Header(value="snmpwritecommunitystring")
    private String tiSnmpWriteCommString;
    
	private Boolean _tiSkipValidation = true;
    
    public ItemImport(){
    	super();
    }
	
	public ItemImport(Map<String, String> headerMap) {
		super(headerMap);
	}
	
	public ItemImport(List<String> specialHeaders){
		super(specialHeaders);
	}
	
	public ItemImport(ImportHeaderMapCache headerMapCache){
		super(headerMapCache);
	}

	public String getCmbMake() {
		return cmbMake;
	}

	public void setCmbMake(String cmbMake) {
		this.cmbMake = cmbMake;
	}

	public String getCmbModel() {
		return cmbModel;
	}

	public void setCmbModel(String cmbModel) {
		this.cmbModel = cmbModel;
	}

	public String getTiSerialNumber() {
		return tiSerialNumber;
	}

	public void setTiSerialNumber(String tiSerialNumber) {
		this.tiSerialNumber = tiSerialNumber;
	}

	public String getTiAssetTag() {
		return tiAssetTag;
	}

	public void setTiAssetTag(String tiAssetTag) {
		this.tiAssetTag = tiAssetTag;
	}

	public String getTieAssetTag() {
		return tieAssetTag;
	}

	public void setTieAssetTag(String tieAssetTag) {
		this.tieAssetTag = tieAssetTag;
	}

	public String getTiName() {
		return tiName;
	}

	public void setTiName(String tiName) {
		this.tiName = tiName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getTiAlias() {
		return tiAlias;
	}

	public void setTiAlias(String tiAlias) {
		this.tiAlias = tiAlias;
	}

	public String getCmbType() {
		return cmbType;
	}

	public void setCmbType(String cmbType) {
		this.cmbType = cmbType;
	}

	public String getCmbFunction() {
		return cmbFunction;
	}

	public void setCmbFunction(String cmbFunction) {
		this.cmbFunction = cmbFunction;
	}

	public String getCmbSystemAdmin() {
		return cmbSystemAdmin;
	}

	public void setCmbSystemAdmin(String cmbSystemAdmin) {
		this.cmbSystemAdmin = cmbSystemAdmin;
	}

	public String getCmbSystemAdminTeam() {
		return cmbSystemAdminTeam;
	}

	public void setCmbSystemAdminTeam(String cmbSystemAdminTeam) {
		this.cmbSystemAdminTeam = cmbSystemAdminTeam;
	}

	public String getCmbCustomer() {
		return cmbCustomer;
	}

	public void setCmbCustomer(String cmbCustomer) {
		this.cmbCustomer = cmbCustomer;
	}

	public String getCmbStatus() {
		return cmbStatus;
	}

	public void setCmbStatus(String cmbStatus) {
		this.cmbStatus = cmbStatus;
	}

	public String getCmbLocation() {
		return cmbLocation;
	}

	public void setCmbLocation(String cmbLocation) {
		this.cmbLocation = cmbLocation;
	}

	public String getTiLocationRef() {
		return tiLocationRef;
	}

	public void setTiLocationRef(String tiLocationRef) {
		this.tiLocationRef = tiLocationRef;
	}

	public String getRadioFrontFaces() {
		return radioFrontFaces;
	}

	public void setRadioFrontFaces(String radioFrontFaces) {
		this.radioFrontFaces = radioFrontFaces;
	}

	public String getRadioCabinetSide() {
		return radioCabinetSide;
	}

	public void setRadioCabinetSide(String radioCabinetSide) {
		this.radioCabinetSide = radioCabinetSide;
	}

	public String getCmbRowPosition() {
		return cmbRowPosition;
	}

	public void setCmbRowPosition(String cmbRowPosition) {
		this.cmbRowPosition = cmbRowPosition;
	}

	public String getCmbRowLabel() {
		return cmbRowLabel;
	}

	public void setCmbRowLabel(String cmbRowLabel) { 
		this.cmbRowLabel = cmbRowLabel;
	}

	public String getCmbCabinet() {
		return cmbCabinet;
	}

	public void setCmbCabinet(String cmbCabinet) {
		this.cmbCabinet = cmbCabinet;
	}

	public String getRadioRailsUsed() {
		return radioRailsUsed;
	}

	public void setRadioRailsUsed(String radioRailsUsed) {
		this.radioRailsUsed = radioRailsUsed;
	}

	public String getCmbUPosition() {
		return cmbUPosition;
	}

	public void setCmbUPosition(String cmbUPosition) {
		this.cmbUPosition = cmbUPosition;
	}

	public String getCmbSlotPosition() {
		return cmbSlotPosition;
	}

	public void setCmbSlotPosition(String cmbSlotPosition) {
		this.cmbSlotPosition = cmbSlotPosition;
	}

	public String getCmbOrientation() {
		return cmbOrientation;
	}

	public void setCmbOrientation(String cmbOrientation) {
		this.cmbOrientation = cmbOrientation;
	}

	public String getCmbChassis() {
		return cmbChassis;
	}

	public void setCmbChassis(String cmbChassis) {
		this.cmbChassis = cmbChassis;
	}

	public String getRadioChassisFace() {
		return radioChassisFace;
	}

	public void setRadioChassisFace(String radioChassisFace) {
		this.radioChassisFace = radioChassisFace;
	}

	public String getRadioDepthPosition() {
		return radioDepthPosition;
	}

	public void setRadioDepthPosition(String radioDepthPosition) {
		this.radioDepthPosition = radioDepthPosition;
	}

	public Integer getCmbOrder() {
		return cmbOrder;
	}

	public void setCmbOrder(Integer cmbOrder) {
		this.cmbOrder = cmbOrder;
	}

	public String getTiPONumber() {
		return tiPONumber;
	}

	public void setTiPONumber(String tiPONumber) {
		this.tiPONumber = tiPONumber;
	}

	public Double getTiPurchasePrice() {
		return tiPurchasePrice;
	}

	public void setTiPurchasePrice(Double tiPurchasePrice) {
		this.tiPurchasePrice = tiPurchasePrice;
	}

	public String getDtPurchaseDate() {
		return dtPurchaseDate;
	}

	public void setDtPurchaseDate(String dtPurchaseDate) {
		this.dtPurchaseDate = dtPurchaseDate;
	}

	public String getDtInstallationDate() {
		return dtInstallationDate;
	}

	public void setDtInstallationDate(String dtInstallationDate) {
		this.dtInstallationDate = dtInstallationDate;
	}

	public String getCmbSLAProfile() {
		return cmbSLAProfile;
	}

	public void setCmbSLAProfile(String cmbSLAProfile) {
		this.cmbSLAProfile = cmbSLAProfile;
	}

	public String getCmbContractNumber() {
		return cmbContractNumber;
	}

	public void setCmbContractNumber(String cmbContractNumber) {
		this.cmbContractNumber = cmbContractNumber;
	}

	public String getTiContractAmount() {
		return tiContractAmount;
	}

	public void setTiContractAmount(String tiContractAmount) {
		this.tiContractAmount = tiContractAmount;
	}

	public String getDtContractStartDate() {
		return dtContractStartDate;
	}

	public void setDtContractStartDate(String dtContractStartDate) {
		this.dtContractStartDate = dtContractStartDate;
	}

	public String getDtContractEndDate() {
		return dtContractEndDate;
	}

	public void setDtContractEndDate(String dtContractEndDate) {
		this.dtContractEndDate = dtContractEndDate;
	}

	public String getTiNotes() {
		return tiNotes;
	}

	public void setTiNotes(String tiNotes) {
		this.tiNotes = tiNotes;
	}

	public Map<String,String> getTiCustomField() {
		return tiCustomField;
	}

	public void setTiCustomField(Map<String,String> tiCustomField) {
		this.tiCustomField = tiCustomField;
	}

	public String getCmbOperatingSystem() {
		return cmbOperatingSystem;
	}

	public void setCmbOperatingSystem(String cmbOperatingSystem) {
		this.cmbOperatingSystem = cmbOperatingSystem;
	}

	public String getCmbDomain() {
		return cmbDomain;
	}

	public void setCmbDomain(String cmbDomain) {
		this.cmbDomain = cmbDomain;
	}

	public String getTiCpuType() {
		return tiCpuType;
	}

	public void setTiCpuType(String tiCpuType) {
		this.tiCpuType = tiCpuType;
	}

	public String getTiCpuQuantity() {
		return tiCpuQuantity;
	}

	public void setTiCpuQuantity(String tiCpuQuantity) {
		this.tiCpuQuantity = tiCpuQuantity;
	}

	public String getTiRAM() {
		return tiRAM;
	}

	public void setTiRAM(String tiRAM) {
		this.tiRAM = tiRAM;
	}

	public String getTiUsers() {
		return tiUsers;
	}

	public void setTiUsers(String tiUsers) {
		this.tiUsers = tiUsers;
	}

	public String getTiProcesses() {
		return tiProcesses;
	}

	public void setTiProcesses(String tiProcesses) {
		this.tiProcesses = tiProcesses;
	}

	public String getTiServices() {
		return tiServices;
	}

	public void setTiServices(String tiServices) {
		this.tiServices = tiServices;
	}

	public String getCmbVMCluster() {
		return cmbVMCluster;
	}

	public void setCmbVMCluster(String cmbVMCluster) {
		this.cmbVMCluster = cmbVMCluster;
	}

	/**
	 * @return the tiPiqExternalKey
	 */
	public String getTiPiqExternalKey() {
		return tiPiqExternalKey;
	}

	/**
	 * @param tiPiqExternalKey the tiPiqExternalKey to set
	 */
	public void setTiPiqExternalKey(String tiPiqExternalKey) {
		this.tiPiqExternalKey = tiPiqExternalKey;
	}
		
	public Boolean get_tiSkipValidation() {
		return _tiSkipValidation;
	}

	public void set_tiSkipValidation(Boolean _tiSkipValidation) {
		this._tiSkipValidation = _tiSkipValidation;
	}

	public String getNewLocation() {
		return newLocation;
	}

	public void setNewLocation(String newLocation) {
		this.newLocation = newLocation;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getProxyIndex() {
		return proxyIndex;
	}

	public void setProxyIndex(String proxyIndex) {
		this.proxyIndex = proxyIndex;
	}

	public String getIpAddressPortName() {
		return ipAddressPortName;
	}

	public void setIpAddressPortName(String ipAddressPortName) {
		this.ipAddressPortName = ipAddressPortName;
	}

	public String getTiSubclass() {
		return tiSubclass;
	}

	public void setTiSubclass(String subClass) {
		this.tiSubclass = subClass;
	}
	
	
	public String getTiPXUsername() {
		return tiPXUsername;
	}

	public void setTiPXUsername(String tiPXUsername) {
		this.tiPXUsername = tiPXUsername;
	}

	public String getTiPXPassword() {
		return tiPXPassword;
	}

	public void setTiPXPassword(String tiPXPassword) {
		this.tiPXPassword = tiPXPassword;
	}

	public Boolean getCmbSnmpV3Enabled() {
		return cmbSnmpV3Enabled;
	}

	public void setCmbSnmpV3Enabled(Boolean cmbSnmpV3Enabled) {
		this.cmbSnmpV3Enabled = cmbSnmpV3Enabled;
	}

	public String getTiSnmpV3Username() {
		return tiSnmpV3Username;
	}

	public void setTiSnmpV3Username(String tiSnmpV3Username) {
		this.tiSnmpV3Username = tiSnmpV3Username;
	}

	public String getCmbSnmpV3AuthLevel() {
		return cmbSnmpV3AuthLevel;
	}

	public void setCmbSnmpV3AuthLevel(String cmbSnmpV3AuthLevel) {
		this.cmbSnmpV3AuthLevel = cmbSnmpV3AuthLevel;
	}

	public String getCmbSnmpV3AuthProtocol() {
		return cmbSnmpV3AuthProtocol;
	}

	public void setCmbSnmpV3AuthProtocol(String cmbSnmpV3AuthProtocol) {
		this.cmbSnmpV3AuthProtocol = cmbSnmpV3AuthProtocol;
	}

	public String getTiSnmpV3AuthPasskey() {
		return tiSnmpV3AuthPasskey;
	}

	public void setTiSnmpV3AuthPasskey(String tiSnmpV3AuthPasskey) {
		this.tiSnmpV3AuthPasskey = tiSnmpV3AuthPasskey;
	}

	public String getCmbSnmpV3PrivacyProtocol() {
		return cmbSnmpV3PrivacyProtocol;
	}

	public void setCmbSnmpV3PrivacyProtocol(String cmbSnmpV3PrivacyProtocol) {
		this.cmbSnmpV3PrivacyProtocol = cmbSnmpV3PrivacyProtocol;
	}

	public String getTiSnmpV3PrivacyPasskey() {
		return tiSnmpV3PrivacyPasskey;
	}

	public void setTiSnmpV3PrivacyPasskey(String tiSnmpV3PrivacyPasskey) {
		this.tiSnmpV3PrivacyPasskey = tiSnmpV3PrivacyPasskey;
	}

	public String getTiSnmpWriteCommString() {
		return tiSnmpWriteCommString;
	}

	public void setTiSnmpWriteCommString(String tiSnmpWriteCommString) {
		this.tiSnmpWriteCommString = tiSnmpWriteCommString;
	}

	@Override
	public String toString() {
		return "ItemImport [cmbMake=" + cmbMake + ", cmbModel=" + cmbModel
				+ ", tiSerialNumber=" + tiSerialNumber + ", tiAssetTag="
				+ tiAssetTag + ", tieAssetTag=" + tieAssetTag + ", tiName="
				+ tiName + ", newName=" + newName + ", tiAlias=" + tiAlias
				+ ", cmbType=" + cmbType + ", cmbFunction=" + cmbFunction
				+ ", cmbSystemAdmin=" + cmbSystemAdmin
				+ ", cmbSystemAdminTeam=" + cmbSystemAdminTeam
				+ ", cmbCustomer=" + cmbCustomer + ", cmbStatus=" + cmbStatus
				+ ", cmbLocation=" + cmbLocation + ", newLocation="
				+ newLocation + ", tiLocationRef=" + tiLocationRef
				+ ", radioFrontFaces=" + radioFrontFaces
				+ ", radioCabinetSide=" + radioCabinetSide
				+ ", cmbRowPosition=" + cmbRowPosition + ", cmbRowLabel="
				+ cmbRowLabel + ", cmbCabinet=" + cmbCabinet
				+ ", radioRailsUsed=" + radioRailsUsed + ", cmbUPosition="
				+ cmbUPosition + ", cmbSlotPosition=" + cmbSlotPosition
				+ ", cmbOrientation=" + cmbOrientation + ", cmbChassis="
				+ cmbChassis + ", radioChassisFace=" + radioChassisFace
				+ ", radioDepthPosition=" + radioDepthPosition + ", cmbOrder="
				+ cmbOrder + ", tiPONumber=" + tiPONumber
				+ ", tiPurchasePrice=" + tiPurchasePrice + ", dtPurchaseDate="
				+ dtPurchaseDate + ", dtInstallationDate=" + dtInstallationDate
				+ ", cmbSLAProfile=" + cmbSLAProfile + ", cmbContractNumber="
				+ cmbContractNumber + ", tiContractAmount=" + tiContractAmount
				+ ", dtContractStartDate=" + dtContractStartDate
				+ ", dtContractEndDate=" + dtContractEndDate + ", tiNotes="
				+ tiNotes + ", tiCustomField=" + tiCustomField
				+ ", cmbOperatingSystem=" + cmbOperatingSystem + ", cmbDomain="
				+ cmbDomain + ", tiCpuType=" + tiCpuType + ", tiCpuQuantity="
				+ tiCpuQuantity + ", tiRAM=" + tiRAM + ", tiUsers=" + tiUsers
				+ ", tiProcesses=" + tiProcesses + ", tiServices=" + tiServices
				+ ", cmbVMCluster=" + cmbVMCluster + ", tiPiqExternalKey="
				+ tiPiqExternalKey + ", subClass=" + tiSubclass
				+ tiPiqExternalKey + ", tiPXUsername=" + tiPXUsername
				+ ", tiPXPassword=" + tiPXPassword + ", cmbSnmpV3Enabled="
				+ cmbSnmpV3Enabled + ", tiSnmpV3Username=" + tiSnmpV3Username
				+ ", cmbSnmpV3AuthLevel=" + cmbSnmpV3AuthLevel
				+ ", cmbSnmpV3AuthProtocol=" + cmbSnmpV3AuthProtocol
				+ ", tiSnmpV3AuthPasskey=" + tiSnmpV3AuthPasskey
				+ ", cmbSnmpV3PrivacyProtocol=" + cmbSnmpV3PrivacyProtocol
				+ ", tiSnmpV3PrivacyPasskey=" + tiSnmpV3PrivacyPasskey
				+ ", tiSnmpWriteCommString=" + tiSnmpWriteCommString
				+ ", _tiSkipValidation=" + _tiSkipValidation + "]";
	}

}
