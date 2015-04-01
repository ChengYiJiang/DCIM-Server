package com.raritan.tdz.dto;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

import flex.messaging.log.Log;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public abstract class PortDTOBase implements PortInterface {

	private Long itemId;
	private Long portId;
	private boolean used;
	private int sortOrder;
	private String portName;
	private String itemName;
	private String colorLkuDesc;
	
	private Long itemClassLksValueCode;
	private Long itemSubClassLksValueCode;
	private Long locationId;
	private Long portSubClassLksValueCode;
	private PortConnectorDTO connector;
	private Integer placementX;
	private Integer placementY;
	private Long connectorLkuId;
	private Long colorLkuId;
	private String colorNumber;
	private Long portStatusLksValueCode;
	private Long faceLksValueCode;
	
	private String comments;
	private String cableGradeLkuDesc;
	private Long cableGradeLkuId;
	private boolean isRedundant;
	private boolean isSharedConnection;
	private int partialCircuitLength = 0;
	private Long connectedItemId;
	

	private String connectedItemName;
	private Long circuitStatusLksValueCode;
	private String circuitStatusLksValue;
	private Long connectedPortId;
	private String connectedPortName;
	private Float connectedCircuitId;
	private String portSubClassLksDesc;
	private Float proposedCircuitId;

	private Long moveActionLkpValueCode;
	private Long nextNodeClassValueCode;
	
	private PortInterface altData;
	

	/*
	 * This is specifically used by the REST-API to setup a dirty flag when user
	 * sets a value via the JSON object into the DTO. The setting up of the flag
	 * to true is done in the PortDTOJSONSetterInterceptor aspect
	 * @See PortDTOJSONSetterInterceptor
	 * 
	 * This is especially used during the update port data operation via REST-API
	 */
	protected Map<String,Boolean> dirtyFlagMap = new HashMap<String,Boolean>();
	
	public PortDTOBase(){
		initDirtyFlagMap(this.getClass());
	}
	
	@Override
	public Long getItemId() {
		return itemId;
	}

	@Override
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Override
	@JsonProperty("portId")
	public Long getPortId() {
		return portId;
	}

	@Override
	@JsonProperty("portId")
	public void setPortId(Long portId) {
		this.portId = portId;
	}

	@Override
	public boolean isUsed() {
		return used;
	}

	@Override
	public void setUsed(boolean used) {
		this.used = used;
	}

	@Override
	@JsonProperty("index")
	public int getSortOrder() {
		return sortOrder;
	}
	
	@Override
	@JsonProperty("index")
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	@Override
	@JsonProperty("name")
	public String getPortName() {
		return portName;
	}
	
	@Override
	@JsonProperty("name")
	public void setPortName(String portName) {
		this.portName = portName;
	}
	
	@Override
	public String getItemName() {
		return itemName;
	}
	
	@Override
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	@Override
	@JsonProperty("connector")
	public String getConnectorName() {
		if(connector != null){
			return connector.getConnectorName();
		}
		
		return null;
	}
	
	@Override
	@JsonProperty("connector")
	public void setConnectorName(String connectorName) {
		if( connector == null ){
			connector = new PortConnectorDTO();
		}
		connector.setConnectorName(connectorName);
		
	}
	
	@Override
	@JsonProperty("colorCode")
	public String getColorLkuDesc() {
		return colorLkuDesc;
	}
	
	@Override
	@JsonProperty("colorCode")
	public void setColorLkuDesc(String colorLkuDesc) {
		this.colorLkuDesc = colorLkuDesc;
	}
	
	@Override
	public Long getItemClassLksValueCode() {
		return itemClassLksValueCode;
	}
	
	@Override
	public void setItemClassLksValueCode(Long itemClassLksValueCode) {
		this.itemClassLksValueCode = itemClassLksValueCode;
	}
	
	@Override
	public Long getLocationId() {
		return locationId;
	}
	
	@Override
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	
	@Override
	public Long getPortSubClassLksValueCode() {
		return portSubClassLksValueCode;
	}
	
	@Override
	public void setPortSubClassLksValueCode(Long portSubClassLksValueCode) {
		this.portSubClassLksValueCode = portSubClassLksValueCode;
	}

	@Override
	public PortConnectorDTO getConnector() {
		return connector;
	}

	@Override
	public void setConnector(PortConnectorDTO connector) {
		this.connector = connector;		
	}

	@Override
	public Long getColorLkuId() {
		return colorLkuId;
	}

	@Override
	public void setColorLkuId(Long colorLkuId) {
		this.colorLkuId = colorLkuId;
	}

	@Override
	public Long getPortStatusLksValueCode() {
		return portStatusLksValueCode;
	}

	@Override
	public void setPortStatusLksValueCode(Long portStatusLookupValueCode) {
		this.portStatusLksValueCode = portStatusLookupValueCode;
	}

	@Override
	public Long getConnectorLkuId() {
		return connectorLkuId;
	}

	@Override
	public void setConnectorLkuId(Long connectorLkuId) {
		this.connectorLkuId = connectorLkuId;
	}

	@Override
	public Integer getPlacementX() {
		return placementX;
	}

	@Override
	public void setPlacementX(Integer placementX) {
		this.placementX = placementX;
	}

	@Override
	public Integer getPlacementY() {
		return placementY;
	}

	@Override
	public void setPlacementY(Integer placementY) {
		this.placementY = placementY;
	}

	@Override
	public void setFaceLksValueCode(Long faceLksValueCode) {
		this.faceLksValueCode = faceLksValueCode;
	}

	@Override
	public Long getFaceLksValueCode() {
		return faceLksValueCode;
	}

	@Override
	@JsonProperty("comment")
	public String getComments() {
		return comments;
	}

	@Override
	@JsonProperty("comment")
	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String getCableGradeLkuDesc() {
		return cableGradeLkuDesc;
	}

	@Override
	public void setCableGradeLkuDesc(String cableGradeLkuDesc) {
		this.cableGradeLkuDesc = cableGradeLkuDesc;
	}

	@Override
	public Long getCableGradeLkuId() {
		return cableGradeLkuId;
	}

	@Override
	public void setCableGradeLkuId(Long cableGradeLkuId) {
		this.cableGradeLkuId = cableGradeLkuId;
	}
	
	@Override
	public boolean isRedundant() {
		return isRedundant;
	}

	@Override
	public void setRedundant(boolean isRedundant) {
		this.isRedundant = isRedundant;
	}

	public String getColorNumber() {
		return colorNumber;
	}

	public void setColorNumber(String colorNumber) {
		this.colorNumber = colorNumber;
	}
	
	public boolean isSharedConnection() {
		return isSharedConnection;
	}

	public void setSharedConnection(boolean isSharedConnection) {
		this.isSharedConnection = isSharedConnection;
	}

	public int getPartialCircuitLength() {
		return partialCircuitLength;
	}

	public void setPartialCircuitLength(int partialCircuitLength) {
		this.partialCircuitLength = partialCircuitLength;
	}

	@JsonProperty("connectedItemId")
	public Long getConnectedItemId() {
		return connectedItemId;
	}

	@JsonProperty("connectedItemId")
	public void setConnectedItemId(Long connectedItemId) {
		this.connectedItemId = connectedItemId;
	}

	@JsonProperty("connectedPortId")
	public Long getConnectedPortId() {
		return connectedPortId;
	}
	@JsonProperty("connectedPortId")
	public void setConnectedPortId(Long connectedPortId) {
		this.connectedPortId = connectedPortId;
	}

	@JsonProperty("connectedItemName")
	public String getConnectedItemName() {
		return connectedItemName;
	}

	@JsonProperty("connectedItemName")
	public void setConnectedItemName(String connectedItemName) {
		this.connectedItemName = connectedItemName;
	}

	public Long getCircuitStatusLksValueCode(){
		return circuitStatusLksValueCode;
	}
	
	public void setCircuitStatusLksValueCode(Long circuitStatusLksValueCode){
		this.circuitStatusLksValueCode = circuitStatusLksValueCode;
	}
	
	public String getCircuitStatusLksValue(){
		return circuitStatusLksValue;
	}
	
	public void setCircuitStatusLksValue(String circuitStatusLksValue){
		this.circuitStatusLksValue = circuitStatusLksValue;
	}
	
	@JsonProperty("connectedPortName")
	public String getConnectedPortName() {
		return connectedPortName;
	}

	@JsonProperty("connectedPortName")
	public void setConnectedPortName(String connectedPortName) {
		this.connectedPortName = connectedPortName;
	}

	public Float getConnectedCircuitId() {
		return connectedCircuitId;
	}

	public void setConnectedCircuitId(Float connectedCircuitId) {
		this.connectedCircuitId = connectedCircuitId;
	}

	@JsonProperty("type")
	public String getPortSubClassLksDesc() {
		return portSubClassLksDesc;
	}

	@JsonProperty("type")
	public void setPortSubClassLksDesc(String portSubClassLksDesc) {
		this.portSubClassLksDesc = portSubClassLksDesc;
	}

	public Long getItemSubClassLksValueCode() {
		return itemSubClassLksValueCode;
	}

	public void setItemSubClassLksValueCode(Long itemSubClassLksValueCode) {
		this.itemSubClassLksValueCode = itemSubClassLksValueCode;
	}
	
	public Map<String, Boolean> getDirtyFlagMap() {
		return dirtyFlagMap;
	}

	protected void initDirtyFlagMap(Class<?> classname){
		//Get the setter methods that have JsonProperty annotation
		Method[] methods = classname.getMethods();
		for (Method method:methods){
			try {
				Annotation[] annotations =  method.getAnnotations();
				for (Annotation annotation: annotations){
					if (annotation instanceof JsonProperty && method.getName().contains("set")){
						//initialize the dirty flag.
						dirtyFlagMap.put(method.getName(), false);
					}
				}
			} catch (SecurityException e) {
				if (Log.isDebug())
					e.printStackTrace();
			}
		}
	}
	
	public Float getProposedCircuitId() {
		return proposedCircuitId;
	}

	public void setProposedCircuitId(Float proposedCircuitId) {
		this.proposedCircuitId = proposedCircuitId;
	}

	public Long getMoveActionLkpValueCode() {
		return moveActionLkpValueCode;
	}

	public void setMoveActionLkpValueCode(Long moveActionLkpValueCode) {
		this.moveActionLkpValueCode = moveActionLkpValueCode;
	}

	public Long getNextNodeClassValueCode() {
		return nextNodeClassValueCode;
	}

	public void setNextNodeClassValueCode(Long nextNodeClassValueCode) {
		this.nextNodeClassValueCode = nextNodeClassValueCode;
	}


	@Override
	public PortInterface getAltData() {
		return altData;
	}

	@Override
	public void setAltData(PortInterface altData) {
		this.altData = altData;
	}
	
}
