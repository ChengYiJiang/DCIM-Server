package com.raritan.tdz.views;

import javax.persistence.Column;

import com.raritan.tdz.lookup.SystemLookup;

public class ItemObject {
    private Long itemId;
    private String itemName;
    private String statusLksDesc;
    private Long statusLksCode;
    private String classLksDesc;
    private Long classLksCode;
    private String subClassLksDesc;
    private Long subClassLksCode;
    private Long parentItemId;
    private String parentItemName;
    private Long locationId;
    private String locationName;
    private String locationCode;
    private String make;
    private String modelName;
    private Long modelId;
    private Integer ruHeight;
    private String mountedRailsPosDesc;
    private Long uPosition;
    private Long slotPosition;
    private Long chassisId;
    private String chassisItemName;
    private Long vmClusterId;
    private String vmClusterName;
    private Long dataPortCount;
    private Long dataPortCountFree;
    private Long powerPortCountFree;
    private String itemClassFilter;
    private boolean frontImage;
    private boolean backImage;
    private String psRedundancy;
    private String redundancy;
    
	private Integer polesQty;
	private Long ratingKva;
	private Long ratingV;
	private Long ratingKW;
	private Long lineVolts;
	private Long phaseVolts;
	private String phaseLksDesc;
    private Integer unitsInUpsBank;
    private String itemNodeType;
	private Long ampsMax;
	private Long ampsRated;
	private String poleDesc;
	private String outputWiring;
	private Long inputCordCountFree;
	private Long originLkpValueCode;
	
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getStatusLksDesc() {
		return statusLksDesc;
	}
	public void setStatusLksDesc(String statusLksDesc) {
		this.statusLksDesc = statusLksDesc;
	}
	public Long getStatusLksCode() {
		return statusLksCode;
	}
	public void setStatusLksCode(Long statusLksCode) {
		this.statusLksCode = statusLksCode;
	}
	public String getClassLksDesc() {
		return classLksDesc;
	}
	public void setClassLksDesc(String classLksDesc) {
		this.classLksDesc = classLksDesc;
	}
	public Long getClassLksCode() {
		return classLksCode;
	}
	public void setClassLksCode(Long classLksCode) {
		this.classLksCode = classLksCode;
	}
	public String getSubClassLksDesc() {
		return subClassLksDesc;
	}
	public void setSubClassLksDesc(String subClassLksDesc) {
		this.subClassLksDesc = subClassLksDesc;
	}
	public Long getSubClassLksCode() {
		return subClassLksCode;
	}
	public void setSubClassLksCode(Long subClassLksCode) {
		this.subClassLksCode = subClassLksCode;
	}
	public Long getParentItemId() {
		return parentItemId;
	}
	public void setParentItemId(Long parentItemId) {
		this.parentItemId = parentItemId;
	}
	public String getParentItemName() {
		return parentItemName;
	}
	public void setParentItemName(String parentItemName) {
		this.parentItemName = parentItemName;
	}
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public String getMake() {
		return make;
	}
	public void setMake(String make) {
		this.make = make;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public Long getModelId() {
		return modelId;
	}
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}
	public Integer getRuHeight() {
		return ruHeight;
	}
	public void setRuHeight(Integer ruHeight) {
		if(ruHeight != null){
			this.ruHeight = ruHeight;
		}
	}
	public String getMountedRailsPosDesc() {
		return mountedRailsPosDesc;
	}
	public void setMountedRailsPosDesc(String mountedRailsPosDesc) {
		this.mountedRailsPosDesc = mountedRailsPosDesc;
	}
	public Long getUPosition() {
		return uPosition;
	}
	public void setUPosition(Long uPosition) {
		this.uPosition = uPosition;
	}
	public Long getSlotPosition() {
		return slotPosition;
	}
	public void setSlotPosition(Long slotPosition) {
		this.slotPosition = slotPosition;
	}
	public Long getChassisId() {
		return chassisId;
	}
	public void setChassisId(Long chassisId) {
		this.chassisId = chassisId;
	}
	public String getChassisItemName() {
		return chassisItemName;
	}
	public void setChassisItemName(String chassisItemName) {
		this.chassisItemName = chassisItemName;
	}
	public Long getVmClusterId() {
		return vmClusterId;
	}
	public void setVmClusterId(Long vmClusterId) {
		this.vmClusterId = vmClusterId;
	}
	public String getVmClusterName() {
		return vmClusterName;
	}
	public void setVmClusterName(String vmClusterName) {
		this.vmClusterName = vmClusterName;
	}
	public Long getDataPortCount() {
		return dataPortCount;
	}
	public void setDataPortCount(Long dataPortCount) {
		if(dataPortCount == null){
			this.dataPortCount = 0L;
		}
		else{
			this.dataPortCount = dataPortCount;
		}
	}
	
	public Long getDataPortCountFree() {
		return dataPortCountFree;
	}
	public void setDataPortCountFree(Long dataPortCountFree) {
		if(dataPortCountFree == null){
			this.dataPortCountFree = 0L;
		}
		else{
			this.dataPortCountFree = dataPortCountFree;
		}
	}
	
	public Long getPowerPortCountFree() {
		return powerPortCountFree;
	}
	public void setPowerPortCountFree(Long powerPortCountFree) {
		if(powerPortCountFree == null){
			this.powerPortCountFree = 0L;
		}
		else{
			this.powerPortCountFree = powerPortCountFree;
		}
	}
	
	public String getItemClassFilter() {
		return itemClassFilter;
	}
	
	public void setItemClassFilter(String itemClassFilter) {
		this.itemClassFilter = itemClassFilter;
	}
	
	public boolean getFrontImage() {
		return frontImage;
	}
	public void setFrontImage(boolean frontImage) {
		this.frontImage = frontImage;
	}
	public boolean getBackImage() {
		return backImage;
	}
	public void setBackImage(boolean backImage) {
		this.backImage = backImage;
	}
	public String getPsRedundancy() {
		return psRedundancy;
	}
	public void setPsRedundancy(String psRedundancy) {
		this.psRedundancy = psRedundancy;
	}
	public Integer getPolesQty() {
		return polesQty;
	}
	public void setPolesQty(Integer polesQty) {
		this.polesQty = polesQty;
	}
	public Long getRatingKva() {
		return ratingKva;
	}
	public void setRatingKva(Long ratingKva) {
		this.ratingKva = ratingKva;
	}
	public Long getRatingV() {
		return ratingV;
	}
	public void setRatingV(Long ratingV) {
		this.ratingV = ratingV;
	}
	public Long getRatingKW() {
		return ratingKW;
	}
	public void setRatingKW(Long ratingKW) {
		this.ratingKW = ratingKW;
	}
	public Long getLineVolts() {
		return lineVolts;
	}
	public void setLineVolts(Long lineVolts) {
		this.lineVolts = lineVolts;
	}
	public Long getPhaseVolts() {
		return phaseVolts;
	}
	public void setPhaseVolts(Long phaseVolts) {
		this.phaseVolts = phaseVolts;
	}
	public String getPhaseLksDesc() {
		return phaseLksDesc;
	}
	public void setPhaseLksDesc(String phaseLksDesc) {
		this.phaseLksDesc = phaseLksDesc;
	}
	public Integer getUnitsInUpsBank() {
		return unitsInUpsBank;
	}
	public void setUnitsInUpsBank(Integer unitsInUpsBank) {
		this.unitsInUpsBank = unitsInUpsBank;
	}
	public String getItemNodeType() {
		return itemNodeType;
	}
	public void setItemNodeType(String itemNodeType) {
		this.itemNodeType = itemNodeType;
	}
	public Long getAmpsMax() {
		return ampsMax;
	}
	public void setAmpsMax(Long ampsMax) {
		this.ampsMax = ampsMax;
	}
	public Long getAmpsRated() {
		return ampsRated;
	}
	public void setAmpsRated(Long ampsRated) {
		this.ampsRated = ampsRated;
	}
	public String getPoleDesc() {
		return poleDesc;
	}
	public void setPoleDesc(String poleDesc) {
		this.poleDesc = poleDesc;
	}
	public String getOutputWiring() {
		return outputWiring;
	}
	public void setOutputWiring(String outputWiring) {
		this.outputWiring = outputWiring;
	}
	public Long getInputCordCountFree() {
		return inputCordCountFree;
	}
	public void setInputCordCountFree(Long inputCordCountFree) {
		this.inputCordCountFree = inputCordCountFree;
	}
	public String getRedundancy() {
		return redundancy;
	}
	public void setRedundancy(String redundancy) {
		this.redundancy = redundancy;
	}
	public Long getOriginLkpValueCode() {
		return originLkpValueCode;
	}
	public void setOriginLkpValueCode(Long originLkpValueCode) {
		this.originLkpValueCode = originLkpValueCode;
	}
	
	public boolean isVpcPowerOutlet() {
		return (originLkpValueCode.equals(SystemLookup.ItemOrigen.VPC) && classLksCode.equals(SystemLookup.Class.FLOOR_OUTLET));
	}
	
	
}


