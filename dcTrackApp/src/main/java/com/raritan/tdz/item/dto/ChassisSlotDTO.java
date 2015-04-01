/**
 * 
 */
package com.raritan.tdz.item.dto;

/**
 * @author Basker
 *
 */
public class ChassisSlotDTO {

	private Long id;
	private int number;
	private String label;
	private Boolean isReservedSlot;
	private Boolean isAnchorSlot;
	private Long bladeId;
	private String bladeName;
	private Long Facing;
	
	/**
	 * @param id
	 * @param number
	 * @param label
	 * @param isReservedSlot
	 * @param isAnchorSlot
	 * @param bladeId
	 * @param bladeName
	 * @param facing
	 */
	public ChassisSlotDTO(Long id, int number, String label,
			Boolean isReservedSlot, Boolean isAnchorSlot, Long bladeId,
			String bladeName, Long facing) {
		super();
		this.id = id;
		this.number = number;
		this.label = label;
		this.isReservedSlot = isReservedSlot;
		this.isAnchorSlot = isAnchorSlot;
		this.bladeId = bladeId;
		this.bladeName = bladeName;
		Facing = facing;
	}
	
	public ChassisSlotDTO() {

	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getIsReservedSlot() {
		return isReservedSlot;
	}

	public void setIsReservedSlot(Boolean isReservedSlot) {
		this.isReservedSlot = isReservedSlot;
	}

	public Boolean getIsAnchorSlot() {
		return isAnchorSlot;
	}
	
	public void setIsAnchorSlot(Boolean isAnchorSlot) {
		this.isAnchorSlot = isAnchorSlot;
	}
	
	public Long getBladeId() {
		return bladeId;
	}
	
	public void setBladeId(Long bladeId) {
		this.bladeId = bladeId;
	}
	
	public String getBladeName() {
		return bladeName;
	}
	
	public void setBladeName(String bladeName) {
		this.bladeName = bladeName;
	}
	
	public Long getFacing() {
		return Facing;
	}
	
	public void setFacing(Long facing) {
		Facing = facing;
	}
	
}
