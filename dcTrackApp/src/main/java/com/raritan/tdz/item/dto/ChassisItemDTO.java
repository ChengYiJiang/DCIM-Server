package com.raritan.tdz.item.dto;

/**
 * @author Bunty
 *
 */

public class ChassisItemDTO {
	
	private long itemId;
	private String itemName;
    private long modelId;
    private String modelName;
    private long itemClass;
    private String itemClassName;
    private long itemSubClass;
    private String itemSubClassName;
    private String mounting;
	private int numOfBays;
	private int maxSlots;
	private boolean isAllowDouble;
	private boolean isAllowSpanning;
    private boolean isSlotVertical;
	
	public ChassisItemDTO(long itemId, String itemName, int numOfBays,
			int maxSlots, boolean isAllowDouble, boolean isAllowSpanning, boolean isSlotVertical) {
		super();
		this.itemId = itemId;
		this.itemName = itemName;
		this.numOfBays = numOfBays;
		this.maxSlots = maxSlots;
		this.isAllowDouble = isAllowDouble;
		this.isAllowSpanning = isAllowSpanning;
        this.isSlotVertical = isSlotVertical;
	}
	
	public ChassisItemDTO() {
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

	public int getNumOfBays() {
		return numOfBays;
	}

	public void setNumOfBays(int numOfBays) {
		this.numOfBays = numOfBays;
	}

	public int getMaxSlots() {
		return maxSlots;
	}

	public void setMaxSlots(int maxSlots) {
		this.maxSlots = maxSlots;
	}

	public boolean isAllowDouble() {
		return isAllowDouble;
	}

	public void setAllowDouble(boolean isAllowDouble) {
		this.isAllowDouble = isAllowDouble;
	}

	public boolean isAllowSpanning() {
		return isAllowSpanning;
	}

	public void setAllowSpanning(boolean isAllowSpanning) {
		this.isAllowSpanning = isAllowSpanning;
	}
   
    public boolean isSlotVertical() {
        return isSlotVertical;
    }

    public void setSlotVertical(boolean isSlotVertical) {
        this.isSlotVertical = isSlotVertical;
    }
 
    public long getModelId() {
        return modelId;
    }

    public void setModelId(long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public long getItemClass() {
        return itemClass;
    }
    
    public void setItemClass(long itemClass) {
        this.itemClass = itemClass;
    }

    public String getItemClassName() {
        return itemClassName;
    }

    public void setItemClassName(String itemClassName) {
        this.itemClassName = itemClassName;
    }

    public long getItemSubClass() {
        return itemSubClass;
    }

    public void setItemSubClass(long itemSubClass) {
        this.itemSubClass = itemSubClass;
    }

    public String getItemSubClassName() {
        return itemSubClassName;
    }

    public void setItemSubClassName(String itemSubClassName) {
        this.itemSubClassName = itemSubClassName;
    }

    public String getMounting() {
        return mounting;
    }

    public void setMounting(String mounting) {
        this.mounting = mounting;
    }
	
}
