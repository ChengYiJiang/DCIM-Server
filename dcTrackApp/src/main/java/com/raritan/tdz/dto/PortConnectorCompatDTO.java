package com.raritan.tdz.dto;


public class PortConnectorCompatDTO {
	private Long connectorCompatId;
	private PortConnectorDTO connectorLookup;
	private PortConnectorDTO connector2Lookup;
	private LkuDataDTO connectionTypeLookup;
	
	public PortConnectorCompatDTO(){
		
	}
	
	public Long getConnectorCompatId() {
		return connectorCompatId;
	}
	public void setConnectorCompatId(Long connectorCompatId) {
		this.connectorCompatId = connectorCompatId;
	}
	public PortConnectorDTO getConnectorLookup() {
		return connectorLookup;
	}
	public void setConnectorLookup(PortConnectorDTO connectorLookup) {
		this.connectorLookup = connectorLookup;
	}
	public PortConnectorDTO getConnector2Lookup() {
		return connector2Lookup;
	}
	public void setConnector2Lookup(PortConnectorDTO connector2Lookup) {
		this.connector2Lookup = connector2Lookup;
	}
	public LkuDataDTO getConnectionTypeLookup() {
		return connectionTypeLookup;
	}
	public void setConnectionTypeLookup(LkuDataDTO connectionTypeLookup) {
		this.connectionTypeLookup = connectionTypeLookup;
	}

	
}
