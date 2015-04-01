package com.raritan.tdz.dto;

public interface PortInterface {

	public abstract Long getItemId();
	public abstract void setItemId(Long itemId);

	public abstract Long getPortId();
	public abstract void setPortId(Long portId);

	public abstract boolean isUsed();
	public abstract void setUsed(boolean used);

	public abstract int getSortOrder();
	public abstract void setSortOrder(int sortOrder);

	public abstract String getPortName();
	public abstract void setPortName(String portName);

	public abstract String getItemName();
	public abstract void setItemName(String itemName);

	public abstract String getConnectorName();
	public abstract void setConnectorName( String connectorName );
	
	public abstract String getColorLkuDesc();
	public abstract void setColorLkuDesc(String colorLkuDesc);
	
	public abstract String getColorNumber();
	public abstract void setColorNumber(String colorNumber);
	
	public abstract Long getColorLkuId();
	public abstract void setColorLkuId(Long colorLkuId);
	
	public abstract Long getItemClassLksValueCode();
	public abstract void setItemClassLksValueCode(Long itemClassLksValueCode);
	
	public abstract Long getLocationId();
	public abstract void setLocationId(Long locationId);
	
	public abstract Long getPortSubClassLksValueCode();
	public abstract void setPortSubClassLksValueCode(
			Long portSubClassLksValueCode);
	
	public abstract Long getPortStatusLksValueCode();
	public abstract void setPortStatusLksValueCode(
			Long portSubClassLksValueCode);
	
	public abstract PortConnectorDTO getConnector();
	public abstract void setConnector(PortConnectorDTO connector);
	
	public abstract Long getConnectorLkuId();
	public abstract void setConnectorLkuId(Long connectorLkuId);
	
	public abstract Integer getPlacementX();
	public abstract void setPlacementX(Integer placementX);
	
	public abstract Integer getPlacementY();	
	public abstract void setPlacementY(Integer placementY);
	
	public abstract void setFaceLksValueCode(Long valueCode);
	public abstract Long getFaceLksValueCode();
	
	public abstract String getComments();
	public abstract void setComments(String comments);
	
	public abstract String getCableGradeLkuDesc();
	public abstract void setCableGradeLkuDesc(String cableGradeLkuDesc);
	
	public abstract Long getCableGradeLkuId();
	public abstract void setCableGradeLkuId(Long cableGradeLkuId);
	
	public abstract boolean isRedundant();
	public abstract void setRedundant(boolean redundant);
	
	public abstract Long getMoveActionLkpValueCode();
	public abstract void setMoveActionLkpValueCode(Long moveActionLkpValueCode);
	
	public abstract Long getNextNodeClassValueCode();
	public abstract void setNextNodeClassValueCode(Long nextNodeClassValueCode);
	
	public abstract PortInterface getAltData();
	public abstract void setAltData(PortInterface altData);
	
}