package com.raritan.tdz.item.dto;

import java.util.Date;

/**
 * @author prasanna
 * This is an DataTransferObject interface which gives you the Item Search result
 * It will be sent via a List interface.
 */
public interface ItemSearchResultDTO {

	/**
	 * @return the item id - Item Id that can be used for asking further details
	 */
	public abstract Long getItemId();
	
	/**
	 * @return the code - Location
	 */
	public abstract String getCode();

	/**
	 * @return the classLkpValue
	 */
	public abstract String getClassLkpValue();
	
	/**
	 * @return the classLkpValueCode
	 */
	public Long getClassLkpValueCode();
	
	/**
	 * @return the statusLkpValueCode
	 */
	public Long getStatusLkpValueCode();

	/**
	 * @return the statusLkpValue
	 */
	public abstract String getStatusLkpValue();

	/**
	 * @return the itemName 
	 */
	public abstract String getItemName();

	/**
	 * @return the itemAlias
	 */
	public abstract String getItemAlias();

	/**
	 * @return the cabinetName
	 */
	public abstract String getCabinetName();

	/**
	 * @return the uPosition
	 */
	public abstract Long getUPosition();

	/**
	 * @return the mountedRailLkpValue - Rails/Side
	 */
	public abstract String getMountedRailLkpValue();

	/**
	 * @return the ruHeight - In case of cabinet - Rack Units
	 */
	public abstract Integer getRuHeight();

	/**
	 * @return the chassisName 
	 */
	public abstract String getChassisName();

	/**
	 * @return the slotPosition
	 */
	public abstract Long getSlotPosition();

	/**
	 * @return the mfrName
	 */
	public abstract String getMfrName();

	/**
	 * @return the modelName
	 */
	public abstract String getModelName();

	/**
	 * @return the dimH
	 */
	public abstract Double getDimH();

	/**
	 * @return the dimW
	 */
	public abstract Double getDimW();

	/**
	 * @return the dimD
	 */
	public abstract Double getDimD();

	/**
	 * @return the mounting
	 */
	public abstract String getMounting();

	/**
	 * @return the formFactor
	 */
	public abstract String getFormFactor();

	/**
	 * @return the purposeLkuValue - Type
	 */
	public abstract String getPurposeLkuValue();

	/**
	 * @return the functionLkuValue
	 */
	public abstract String getFunctionLkuValue();

	/**
	 * @return the itemAdminUser
	 */
	public abstract String getItemAdminUser();

	/**
	 * @return the itemAdminTeamLkuValue
	 */
	public abstract String getItemAdminTeamLkuValue();

	/**
	 * @return the departmentLkuValue - Customer
	 */
	public abstract String getDepartmentLkuValue();

	/**
	 * @return the serialNumber
	 */
	public abstract String getSerialNumber();

	/**
	 * @return the assetNumber
	 */
	public abstract String getAssetNumber();

	/**
	 * @return the raritanAssetTag - Elec. Asset Tag
	 */
	public abstract String getRaritanAssetTag();

	/**
	 * @return the purchasePrice
	 */
	public abstract Double getPurchasePrice();

	/**
	 * @return the purchaseDate
	 */
	public abstract Date getPurchaseDate();

	/**
	 * @return the installDate
	 */
	public abstract Date getInstallDate();

	/**
	 * @return the slaProfileLkpValue
	 */
	public abstract String getSlaProfileLkuValue();

	/**
	 * @return the contractNumber
	 */
	public abstract String getContractNumber();

	/**
	 * @return the contractAmount
	 */
	public abstract Double getContractAmount();

	/**
	 * @return the contractBeginDate
	 */
	public abstract Date getContractBeginDate();

	/**
	 * @return the contractExpireDate
	 */
	public abstract Date getContractExpireDate();

	/**
	 * @return the freeDataPorts
	 */
	public abstract Integer getFreeDataPortCount();

	/**
	 * @return the freePowerPorts
	 */
	public abstract Integer getFreePowerPortCount();

	/**
	 * @return the sysCreatedBy
	 */
	public abstract String getSysCreatedBy();

	/**
	 * @return the sysCreationDate
	 */
	public abstract Date getSysCreationDate();

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @return the gridLocation
	 */
	public abstract String getGridLocation();

	/**
	 * @return the rowLabel
	 */
	public abstract String getRowLabel();

	/**
	 * @return the positionInRow
	 */
	public abstract Integer getPositionInRow();

	/**
	 * @return the ratingV
	 */
	public abstract Double getRatingV();

	/**
	 * @return the ratingKW
	 */
	public abstract Double getRatingKW();

	/**
	 * @return the ratingTons
	 */
	public abstract Double getRatingTons();

	/**
	 * @return the ratingKva
	 */
	public abstract Double getRatingKva();

	/**
	 * @return the inputBreakerAmps
	 */
	public abstract Double getRatingAmps();

	/**
	 * @return the powerFactor
	 */
	public abstract Double getPowerFactor();

	/**
	 * @return the throwDist
	 */
	public abstract Double getThrowDist();
	

	/**
	 * @return the numPorts - Number of Poles
	 */
	public abstract Integer getNumPorts();
	
	/**
	 * @return the weight 
	 */
	public abstract Double getWeight();
	
	
}