/**
 * 
 */
package com.raritan.tdz.item.dto;

import java.util.Date;

import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class ItemSearchResultDTOImpl implements ItemSearchResultDTO {
	
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @param classLkpValue the classLkpValue to set
	 */
	public void setClassLkpValue(String classLkpValue) {
		this.classLkpValue = classLkpValue;
	}
	/**
	 * @param statusLkpValue the statusLkpValue to set
	 */
	public void setStatusLkpValue(String statusLkpValue) {
		this.statusLkpValue = statusLkpValue;
	}
	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	/**
	 * @param itemAlias the itemAlias to set
	 */
	public void setItemAlias(String itemAlias) {
		this.itemAlias = itemAlias;
	}
	/**
	 * @param cabinetName the cabinetName to set
	 */
	public void setCabinetName(String cabinetName) {
		this.cabinetName = cabinetName;
	}
	/**
	 * @param uPosition the uPosition to set
	 */
	public void setUPosition(Long uPosition) {
		if (uPosition != null)
			this.uPosition = uPosition;
	}
	/**
	 * @param mountedRailLkpValue the mountedRailLkpValue to set
	 */
	public void setMountedRailLkpValue(String mountedRailLkpValue) {
		this.mountedRailLkpValue = mountedRailLkpValue;
	}
	/**
	 * @param ruHeight the ruHeight to set
	 */
	public void setRuHeight(Integer ruHeight) {
		if (ruHeight != null)
			this.ruHeight = ruHeight;
		else
			this.ruHeight = -1;
	}
	/**
	 * @param chassisName the chassisName to set
	 */
	public void setChassisName(String chassisName) {
		this.chassisName = chassisName;
	}
	/**
	 * @param slotPosition the slotPosition to set
	 */
	public void setSlotPosition(Long slotPosition) {
		if (slotPosition != null)
			this.slotPosition = slotPosition;
	}
	/**
	 * @param mfrName the mfrName to set
	 */
	public void setMfrName(String mfrName) {
		this.mfrName = mfrName;
	}
	/**
	 * @param modelName the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	/**
	 * @param dimH the dimH to set
	 */
	public void setDimH(Double dimH) {
		if (dimH != null)
			this.dimH = dimH;
	}
	/**
	 * @param dimW the dimW to set
	 */
	public void setDimW(Double dimW) {
		if (dimW != null)
			this.dimW = dimW;
	}
	/**
	 * @param dimD the dimD to set
	 */
	public void setDimD(Double dimD) {
		if (dimD != null)
			this.dimD = dimD;
	}
	/**
	 * @param mounting the mounting to set
	 */
	public void setMounting(String mounting) {
		this.mounting = mounting;
	}
	/**
	 * @param formFactor the formFactor to set
	 */
	public void setFormFactor(String formFactor) {
		this.formFactor = formFactor;
	}
	/**
	 * @param purposeLkpValue the purposeLkpValue to set
	 */
	public void setPurposeLkuValue(String purposeLkpValue) {
		this.purposeLkuValue = purposeLkpValue;
	}
	/**
	 * @param functionLkpValue the functionLkpValue to set
	 */
	public void setFunctionLkuValue(String functionLkpValue) {
		this.functionLkuValue = functionLkpValue;
	}
	/**
	 * @param itemAdminUser the itemAdminUser to set
	 */
	public void setItemAdminUser(String itemAdminUser) {
		this.itemAdminUser = itemAdminUser;
	}
	/**
	 * @param itemAdminTeamLkpValue the itemAdminTeamLkpValue to set
	 */
	public void setItemAdminTeamLkuValue(String itemAdminTeamLkpValue) {
		this.itemAdminTeamLkuValue = itemAdminTeamLkpValue;
	}
	/**
	 * @param departmentLkpValue the departmentLkpValue to set
	 */
	public void setDepartmentLkuValue(String departmentLkpValue) {
		this.departmentLkuValue = departmentLkpValue;
	}
	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	/**
	 * @param assetNumber the assetNumber to set
	 */
	public void setAssetNumber(String assetNumber) {
		this.assetNumber = assetNumber;
	}
	/**
	 * @param raritanAssetTag the raritanAssetTag to set
	 */
	public void setRaritanAssetTag(String raritanAssetTag) {
		this.raritanAssetTag = raritanAssetTag;
	}
	/**
	 * @param purchasePrice the purchasePrice to set
	 */
	public void setPurchasePrice(Double purchasePrice) {
		if (purchasePrice != null)
			this.purchasePrice = purchasePrice;
	}
	/**
	 * @param purchaseDate the purchaseDate to set
	 */
	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	/**
	 * @param installDate the installDate to set
	 */
	public void setInstallDate(Date installDate) {
		this.installDate = installDate;
	}
	/**
	 * @param slaProfileLkpValue the slaProfileLkpValue to set
	 */
	public void setSlaProfileLkuValue(String slaProfileLkpValue) {
		this.slaProfileLkuValue = slaProfileLkpValue;
	}
	/**
	 * @param contractNumber the contractNumber to set
	 */
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}
	/**
	 * @param contractAmount the contractAmount to set
	 */
	public void setContractAmount(Double contractAmount) {
		if (contractAmount != null)
			this.contractAmount = contractAmount;
	}
	/**
	 * @param contractBeginDate the contractBeginDate to set
	 */
	public void setContractBeginDate(Date contractBeginDate) {
		this.contractBeginDate = contractBeginDate;
	}
	/**
	 * @param contractExpireDate the contractExpireDate to set
	 */
	public void setContractExpireDate(Date contractExpireDate) {
		this.contractExpireDate = contractExpireDate;
	}
	/**
	 * @param freeDataPorts the freeDataPorts to set
	 */
	public void setFreeDataPortCount(Integer freeDataPortCount) {
		this.freeDataPortCount = freeDataPortCount;
	}
	/**
	 * @param freePowerPorts the freePowerPorts to set
	 */
	public void setFreePowerPortCount(Integer freePowerPortCount) {
		this.freePowerPortCount = freeDataPortCount;
	}
	/**
	 * @param sysCreatedBy the sysCreatedBy to set
	 */
	public void setSysCreatedBy(String sysCreatedBy) {
		this.sysCreatedBy = sysCreatedBy;
	}
	/**
	 * @param sysCreationDate the sysCreationDate to set
	 */
	public void setSysCreationDate(Date sysCreationDate) {
		this.sysCreationDate = sysCreationDate;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @param gridLocation the gridLocation to set
	 */
	public void setGridLocation(String gridLocation) {
		this.gridLocation = gridLocation;
	}
	/**
	 * @param rowLabel the rowLabel to set
	 */
	public void setRowLabel(String rowLabel) {
		this.rowLabel = rowLabel;
	}
	/**
	 * @param positionInRow the positionInRow to set
	 */
	public void setPositionInRow(Integer positionInRow) {
		if (positionInRow != null)
			this.positionInRow = positionInRow;
	}
	/**
	 * @param ratingV the ratingV to set
	 */
	public void setRatingV(Double ratingV) {
		if (ratingV != null)
			this.ratingV = ratingV;
	}
	/**
	 * @param ratingKW the ratingKW to set
	 */
	public void setRatingKW(Double ratingKW) {
		if (ratingKW != null)
			this.ratingKW = ratingKW;
	}
	/**
	 * @param ratingTons the ratingTons to set
	 */
	public void setRatingTons(Double ratingTons) {
		if (ratingTons != null)
			this.ratingTons = ratingTons;
	}
	/**
	 * @param ratingKva the ratingKva to set
	 */
	public void setRatingKva(Double ratingKva) {
		if (ratingKva != null)
			this.ratingKva = ratingKva;
	}
	/**
	 * @param ratingAmps the ratingAmps to set
	 */
	public void setRatingAmps(Double ratingAmps) {
		if (ratingAmps != null)
			this.ratingAmps = ratingAmps;
	}
	/**
	 * @param powerFactor the powerFactor to set
	 */
	public void setPowerFactor(Double powerFactor) {
		if (powerFactor != null)
			this.powerFactor = powerFactor;
	}
	/**
	 * @param throwDist the throwDist to set
	 */
	public void setThrowDist(Double throwDist) {
		if (throwDist != null)
			this.throwDist = throwDist;
	}
	/**
	 * @param throwDist the throwDist to set
	 */
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	
	/**
	 * @param throwDist the throwDist to set
	 */
	public void setWeight(Double weight) {
		if (weight != null)
			this.weight = weight;
		else 
			this.weight = (double) -1;
	}
	
	/**
	 * @param numPorts the numPorts to set
	 */
	public void setNumPorts(Integer numPorts){
		if (numPorts != null)
			this.numPorts = numPorts;
	}
	
	/**
	 * @param classLkpValueCode the classLkpValueCode to set
	 */
	public void setClassLkpValueCode(Long classLkpValueCode) {
		this.classLkpValueCode = classLkpValueCode;
	}
	
	/**
	 * @param statusLkpValueCode the statusLkpValueCode to set
	 */
	public void setStatusLkpValueCode(Long statusLkpValueCode) {
		this.statusLkpValueCode = statusLkpValueCode;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getCode()
	 */
	@Override
	public String getCode() {
		return code != null ? code : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getClassLkpValue()
	 */
	@Override
	public String getClassLkpValue() {
		return classLkpValue != null ? classLkpValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getStatusLkpValue()
	 */
	@Override
	public String getStatusLkpValue() {
		return statusLkpValue != null ? statusLkpValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getItemName()
	 */
	@Override
	public String getItemName() {
		return itemName != null ? itemName : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getItemAlias()
	 */
	@Override
	public String getItemAlias() {
		return itemAlias != null ? itemAlias : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getCabinetName()
	 */
	@Override
	public String getCabinetName() {
		return cabinetName != null ? cabinetName : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getuPosition()
	 */
	@Override
	public Long getUPosition() {
		if (subclassLkpValueCode != null && (subclassLkpValueCode == SystemLookup.SubClass.BLADE || subclassLkpValueCode == SystemLookup.SubClass.BLADE_SERVER))
			return chassisUPosition;
		return uPosition;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getMountedRailLkpValue()
	 */
	@Override
	public String getMountedRailLkpValue() {
		return mountedRailLkpValue != null ? mountedRailLkpValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRuHeight()
	 */
	@Override
	public Integer getRuHeight() {
		return ruHeight;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getChassisName()
	 */
	@Override
	public String getChassisName() {
		return chassisName != null ? chassisName : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getSlotPosition()
	 */
	@Override
	public Long getSlotPosition() {
			return slotPosition;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getMfrName()
	 */
	@Override
	public String getMfrName() {
		return mfrName != null ? mfrName : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getModelName()
	 */
	@Override
	public String getModelName() {
		return modelName != null ? modelName : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getDimH()
	 */
	@Override
	public Double getDimH() {
		return dimH;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getDimW()
	 */
	@Override
	public Double getDimW() {
		return dimW;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getDimD()
	 */
	@Override
	public Double getDimD() {
		return dimD;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getMounting()
	 */
	@Override
	public String getMounting() {
		return mounting != null ? mounting : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getFormFactor()
	 */
	@Override
	public String getFormFactor() {
		return formFactor != null ? formFactor : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getPurposeLkpValue()
	 */
	@Override
	public String getPurposeLkuValue() {
		return purposeLkuValue != null ? purposeLkuValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getFunctionLkpValue()
	 */
	@Override
	public String getFunctionLkuValue() {
		return functionLkuValue != null ? functionLkuValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getItemAdminUser()
	 */
	@Override
	public String getItemAdminUser() {
		return itemAdminUser != null ? itemAdminUser : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getItemAdminTeamLkpValue()
	 */
	@Override
	public String getItemAdminTeamLkuValue() {
		return itemAdminTeamLkuValue != null ? itemAdminTeamLkuValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getDepartmentLkpValue()
	 */
	@Override
	public String getDepartmentLkuValue() {
		return departmentLkuValue != null ? departmentLkuValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getSerialNumber()
	 */
	@Override
	public String getSerialNumber() {
		return serialNumber != null ? serialNumber : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getAssetNumber()
	 */
	@Override
	public String getAssetNumber() {
		return assetNumber != null ? assetNumber : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRaritanAssetTag()
	 */
	@Override
	public String getRaritanAssetTag() {
		return raritanAssetTag != null ? raritanAssetTag : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getPurchasePrice()
	 */
	@Override
	public Double getPurchasePrice() {
		if (purchasePrice != null)
			return purchasePrice;
		else
			return new Double(0);
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getPurchaseDate()
	 */
	@Override
	public Date getPurchaseDate() {
		return purchaseDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getInstallDate()
	 */
	@Override
	public Date getInstallDate() {
		return installDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getSlaProfileLkpValue()
	 */
	@Override
	public String getSlaProfileLkuValue() {
		return slaProfileLkuValue != null ? slaProfileLkuValue : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getContractNumber()
	 */
	@Override
	public String getContractNumber() {
		return contractNumber != null ? contractNumber : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getContractAmount()
	 */
	@Override
	public Double getContractAmount() {
		return contractAmount;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getContractBeginDate()
	 */
	@Override
	public Date getContractBeginDate() {
		return contractBeginDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getContractExpireDate()
	 */
	@Override
	public Date getContractExpireDate() {
		return contractExpireDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getFreeDataPorts()
	 */
	@Override
	public Integer getFreeDataPortCount() {
		if (freeDataPortCount != null)
			return freeDataPortCount;
		else
			return 0;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getFreePowerPorts()
	 */
	@Override
	public Integer getFreePowerPortCount() {
		if (freePowerPortCount != null)
			return freePowerPortCount;
		else
			return 0;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getSysCreatedBy()
	 */
	@Override
	public String getSysCreatedBy() {
		return sysCreatedBy != null ? sysCreatedBy : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getSysCreationDate()
	 */
	@Override
	public Date getSysCreationDate() {
		return sysCreationDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getDescription()
	 */
	@Override
	public String getDescription() {
		return description != null ? description : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getGridLocation()
	 */
	@Override
	public String getGridLocation() {
		return gridLocation != null ? gridLocation : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRowLabel()
	 */
	@Override
	public String getRowLabel() {
		return rowLabel != null ? rowLabel : "";
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getPositionInRow()
	 */
	@Override
	public Integer getPositionInRow() {
		return positionInRow;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRatingV()
	 */
	@Override
	public Double getRatingV() {
		if (classLkpValueCode != null && classLkpValueCode.equals(SystemLookup.Class.FLOOR_PDU)){
			return lineVolts;
		}
		return ratingV;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRatingKW()
	 */
	@Override
	public Double getRatingKW() {
		return ratingKW;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRatingTons()
	 */
	@Override
	public Double getRatingTons() {
		return ratingTons;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getRatingKva()
	 */
	@Override
	public Double getRatingKva() {
		return ratingKva;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getInputBreakerAmps()
	 */
	@Override
	public Double getRatingAmps() {
		return ratingAmps;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getPowerFactor()
	 */
	@Override
	public Double getPowerFactor() {
		return getRatingKW()/getRatingKva();
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.dto.ItemSearchResultDTO#getThrowDist()
	 */
	@Override
	public Double getThrowDist() {
		return throwDist;
	}
	
	/**
	 * @return the numPorts
	 */
	@Override
	public Integer getNumPorts() {
		return numPorts;
	}
	
	@Override
	public Long getItemId() {
		return itemId;
	}
	
	@Override
	public Double getWeight() {
		return weight;
	}
	
	
	@Override
	public Long getClassLkpValueCode() {
		return classLkpValueCode;
	}
	
	
	
	@Override
	public Long getStatusLkpValueCode() {
		return statusLkpValueCode;
	}
	
	/**
	 * Special setter for itemName when we are getting it from hibernate.
	 * @param _itemName
	 */

	public void set_itemName(String _itemName) {
		this.itemName = _itemName;
	}


	/**
	 * Special setter for itemId when we are getting it from hibernate.
	 * @param _itemName
	 */

	public void set_itemId(Long _itemId) {
		this.itemId = _itemId;
	}

	/**
	 * Special setter for itemAlias when we are getting it from hibernate.
	 * @param _itemAlias
	 */

	public void set_itemAlias(String _itemAlias) {
		this.itemAlias = _itemAlias;
	}
	
	/**
	 * Special setter for uPosition when we are getting it from hibernate.
	 * @param _uPosition
	 */

	public void set_uPosition(long _uPosition) {
		this.uPosition = _uPosition;
	}
	
	/**
	 * Special setter for uPosition when we are getting it from hibernate.
	 * @param _uPosition
	 */

	public void set_slotPosition(Long _slotPosition) {
		this.slotPosition = _slotPosition;
	}
	
	/**
	 * Special setter for raritanAssetTag when we are getting it from hibernate.
	 * @param _raritanAssetTag
	 */

	public void set_raritanAssetTag(String _raritanAssetTag) {
		this.raritanAssetTag = _raritanAssetTag;
	}
	
	/**
	 * Special setter for freeDataPortCount when we are getting it from hibernate.
	 * @param _freeDataPortCount
	 */

	public void set_freeDataPortCount(Integer _freeDataPortCount) {
		this.freeDataPortCount = _freeDataPortCount;
	}
	
	/**
	 * Special setter for freePowerPortCount when we are getting it from hibernate.
	 * @param _freeDataPortCount
	 */

	public void set_freePowerPortCount(Integer _freePowerPortCount) {
		this.freePowerPortCount = _freePowerPortCount;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_numPorts(Integer _numPorts) {
		this.numPorts = _numPorts;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_gridLocation(String _gridLocation) {
		this.gridLocation = _gridLocation;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_rowLabel(String _rowLabel) {
		this.rowLabel = _rowLabel;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_positionInRow(Integer _positionInRow) {
		if (_positionInRow != null)
			this.positionInRow = _positionInRow;
	}
	
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_ratingV(Double _ratingV) {
		if (_ratingV != null)
			this.ratingV = _ratingV;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_ratingKW(Double _ratingKW) {
		if (_ratingKW != null)
			this.ratingKW = _ratingKW;
	}
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_ratingTons(Double _ratingTons) {
		if (_ratingTons != null)
			this.ratingTons = _ratingTons;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_throwDist(Double _throwDist) {
		if (_throwDist != null)
			this.throwDist = _throwDist;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_ratingAmps(Double _ratingAmps) {
		if (_ratingAmps != null)
			this.ratingAmps = _ratingAmps;
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_lineVolts(Double _lineVolts) {
		if (_lineVolts != null)
			this.lineVolts = _lineVolts; 
	}
	
	/**
	 * Special setter for numPorts when we are getting it from hibernate.
	 * @param _numPorts
	 */

	public void set_ratingKva(Double _ratingKva) {
		if (_ratingKva != null)
			this.ratingKva = _ratingKva;
	}
	/**
	 * Special setter for subclassLkpValueCode when we are getting it from hibernate.
	 * @param subclassLkpValueCode
	 */
	public void setSubclassLkpValueCode(Long subclassLkpValueCode){
		this.subclassLkpValueCode = subclassLkpValueCode;
	}
	
	public void setChassisUPosition(Long chassisUPosition){
		this.chassisUPosition = chassisUPosition;
	}
	
	
	private Long itemId;
	private String code;
	private String classLkpValue;
	private String statusLkpValue;
	private String itemName;
	private String itemAlias;
	private String cabinetName;
	private Long uPosition;
	private String mountedRailLkpValue;
	private Integer ruHeight;
	private String chassisName;
	private Long slotPosition;
	private String mfrName;
	private String modelName;
	private Double dimH;
	private Double dimW;
	private Double dimD;
	private Double weight;
	private String mounting;
	private String formFactor;
	private String purposeLkuValue;
	private String functionLkuValue;
	private String itemAdminUser;
	private String itemAdminTeamLkuValue;
	private String departmentLkuValue;
	private String serialNumber;
	private String assetNumber;
	private String raritanAssetTag;
	private Double purchasePrice;
	private Date purchaseDate;
	private Date installDate;
	private String slaProfileLkuValue;
	private String contractNumber;
	private Double contractAmount;
	private Date contractBeginDate;
	private Date contractExpireDate;
	private Integer freeDataPortCount;
	private Integer freePowerPortCount;
	private String sysCreatedBy;
	private Date sysCreationDate;
	private String description;
	private String gridLocation;
	private String rowLabel;
	private Integer positionInRow;
	private Double ratingV;
	private Double ratingKW;
	private Double ratingTons;
	private Double ratingKva;
	private Double ratingAmps;
	private Double powerFactor;
	private Double throwDist;
	private Integer numPorts;
	private Double lineVolts;
	private Long classLkpValueCode;
	private Long statusLkpValueCode;
	private Long subclassLkpValueCode;
	private Long chassisUPosition;
}
