package com.raritan.tdz.circuit.dto;

public class StructureCableDTO extends WireNodeDTO {
	private Long mediaLksId;
	private String mediaLksDesc;
	private Long cableGradeLkuId;
	private String cableGradeLkuDesc;
	
	public StructureCableDTO(){
		
	}
	
	public Long getMediaLksId() {
		return mediaLksId;
	}
	public void setMediaLksId(Long mediaLksId) {
		this.mediaLksId = mediaLksId;
	}
	public String getMediaLksDesc() {
		return mediaLksDesc;
	}
	public void setMediaLksDesc(String mediaLksDesc) {
		this.mediaLksDesc = mediaLksDesc;
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
