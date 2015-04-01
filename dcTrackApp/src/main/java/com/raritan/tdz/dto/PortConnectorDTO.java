package com.raritan.tdz.dto;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class PortConnectorDTO {
	private Long connectorId;
	private String attribute;
	private String typeName;
	private String connectorName;
	private String description;
	private String imagePath;
	private List<PortConnectorCompatListDTO> connCompatList;
	
	public PortConnectorDTO(){
		
	}
	
	public Long getConnectorId() {
		return connectorId;
	}
	public void setConnectorId(Long connectorId) {
		this.connectorId = connectorId;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	@JsonProperty("connector")
	public String getConnectorName() {
		return connectorName;
	}

	@JsonProperty("connector")
	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public List<PortConnectorCompatListDTO> getConnCompatList() {
		return connCompatList;
	}
	public void setConnCompatList(List<PortConnectorCompatListDTO> connCompatList) {
		this.connCompatList = connCompatList;
	}
}

