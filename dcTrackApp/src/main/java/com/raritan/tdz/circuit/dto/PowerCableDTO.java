package com.raritan.tdz.circuit.dto;

import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.PowerPort;

public class PowerCableDTO extends WireNodeDTO {
	private Long cableGradeLkuId;
	private String cableGradeLkuDesc;
	
	public PowerCableDTO() {
		
	}
	
	public PowerCableDTO(PowerPort source, ConnectionCord connCord) {
		if (connCord != null) {
			if (connCord.getColorLookup() != null) {
				setCordColor( connCord.getColorLookup().getLkuId() );
			}
			setCordId( connCord.getCordId() );
			setCordLabel( connCord.getCordLabel() );
			setCordLength( connCord.getCordLength() );
			if (connCord.getCordLookup() != null) {
				setCordLkuId( connCord.getCordLookup().getLkuId() );
				setCordLkuDesc( connCord.getCordLookup().getLkuValue() );
			}
		}
		if(source != null && source.getCableGradeLookup() != null){
			setCableGradeLkuId( source.getCableGradeLookup().getLkuId() );
			setCableGradeLkuDesc( source.getCableGradeLookup().getLkuValue() );
		}
	}
	
	public Long getCableGradeLkuId() {
		return cableGradeLkuId;
	}
	public void setCableGradeLkuId(Long cableGradeLkuId) {
		this.cableGradeLkuId = cableGradeLkuId;
	}
	public String getCableGradeLkuDesc() {
		return cableGradeLkuDesc;
	}
	public void setCableGradeLkuDesc(String cableGradeLkuDesc) {
		this.cableGradeLkuDesc = cableGradeLkuDesc;
	}		
}
