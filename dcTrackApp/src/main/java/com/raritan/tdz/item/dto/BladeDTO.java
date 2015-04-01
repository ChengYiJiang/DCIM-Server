package com.raritan.tdz.item.dto;
/**
 * @author Bunty
 *
 */
import java.util.Map;

public class BladeDTO {
	
	private long itemId;
	private String itemName;
	private String modelName;
	private long itemClass;
	private String itemClassName;
	private long itemSubClass;
	private String itemSubClassName;
	private String mounting;
	private double weight;
	private int ruHeight;
	private double dimH;
	private double dimW;
	private double dimD;
	private String serialNumber;
	private String raritanAssetTag;
	private String assetNumber;
	Map<Long, String> slotNumbers; 
	private long faceLkpValueCode;
	private String formFactor;
	private long itemStatus;
	private String itemStatusStr;
	private long anchorSlot;
	private long modelId;
	private long makeId;
	private String makeName;
	private long chassisId;
	private String chassisName;
	
	public BladeDTO(long itemId, String itemName, int itemClass,
			Map<Long, String> slotNumbers, long faceLkpValueCode, String formFactor,
			long itemStatus, String itemStatusStr) {
		super();
		this.itemId = itemId;
		this.itemName = itemName;
		this.itemClass = itemClass;
		this.slotNumbers = slotNumbers;
		this.faceLkpValueCode = faceLkpValueCode;
		this.formFactor = formFactor;
		this.itemStatus = itemStatus;
		this.setItemStatusStr(itemStatusStr);
	}
	public BladeDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public long getItemId() {
		return itemId;
	}
	public void setItemId(long itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public long getItemClass() {
		return itemClass;
	}
	public void setItemClass(long itemClass) {
		this.itemClass = itemClass;
	}
	public Map<Long, String> getSlotNumbers() {
		return slotNumbers;
	}
	public void setSlotNumbers(Map<Long, String> slotNumbers) {
		this.slotNumbers = slotNumbers;
	}
	public long getFaceLkpValueCode() {
		return faceLkpValueCode;
	}
	public void setFaceLkpValueCode(long faceLksId) {
		this.faceLkpValueCode = faceLksId;
	}
	public String getFormFactor() {
		return formFactor;
	}
	public void setFormFactor(String formFactor) {
		this.formFactor = formFactor;
	}
	public long getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(long itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getItemStatusStr() {
		return itemStatusStr;
	}
	public void setItemStatusStr(String itemStatusStr) {
		this.itemStatusStr = itemStatusStr;
	}
	public long getAnchorSlot() {
		return anchorSlot;
	}
	public void setAnchorSlot(long anchorSlot) {
		this.anchorSlot = anchorSlot;
	}
	public long getModelId() {
		return modelId;
	}
	public void setModelId(long modelId) {
		this.modelId = modelId;
	}
	public long getMakeId() {
		return makeId;
	}
	public long getChassisId() {
		return chassisId;
	}
	public void setChassisId(long chassisId) {
		this.chassisId = chassisId;
	}
	public String getChassisName() {
		return chassisName;
	}
	public void setChassisName(String chassisName) {
		this.chassisName = chassisName;
	}
	public void setMakeId(long makeId) {
		this.makeId = makeId;
	}
	public String getMakeName() {
		return makeName;
	}
	public void setMakeName(String makeName) {
		this.makeName = makeName;
	}
	public long getItemSubClass() {
		return itemSubClass;
	}
	public void setItemSubClass(long itemSubClass) {
		this.itemSubClass = itemSubClass;
	}
	public String getMounting() {
		return mounting;
	}
	public void setMounting(String mounting) {
		this.mounting = mounting;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public int getRuHeight() {
		return ruHeight;
	}
	public void setRuHeight(int ruHeight) {
		this.ruHeight = ruHeight;
	}
	public double getDimH() {
		return dimH;
	}
	public void setDimH(double dimH) {
		this.dimH = dimH;
	}
	public double getDimW() {
		return dimW;
	}
	public void setDimW(double dimW) {
		this.dimW = dimW;
	}
	public double getDimD() {
		return dimD;
	}
	public void setDimD(double dimD) {
		this.dimD = dimD;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getRaritanAssetTag() {
		return raritanAssetTag;
	}
	public void setRaritanAssetTag(String raritanAssetTag) {
		this.raritanAssetTag = raritanAssetTag;
	}
	public String getAssetNumber() {
		return assetNumber;
	}
	public void setAssetNumber(String assetNumber) {
		this.assetNumber = assetNumber;
	}
	public String getItemClassName() {
		return itemClassName;
	}
	public void setItemClassName(String itemClassName) {
		this.itemClassName = itemClassName;
	}
	public String getItemSubClassName() {
		return itemSubClassName;
	}
	public void setItemSubClassName(String itemSubClassName) {
		this.itemSubClassName = itemSubClassName;
	}
}