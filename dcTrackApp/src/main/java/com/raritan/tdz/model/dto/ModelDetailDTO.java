package com.raritan.tdz.model.dto;

public class ModelDetailDTO {
	
	private long modelId;
	private String modelName;
	private int ruHeight;
	private long classLksId;
	private String mounting;
	private String formFactor;
    private boolean isFrontImage;
    private boolean isRearImage;
	
	public ModelDetailDTO(long modelId, String modelName, int ruHeight,
			long classLksId, String mounting, String formFactor,
			boolean isFrontImage, boolean isRearImage) {
		super();
		this.modelId = modelId;
		this.modelName = modelName;
		this.ruHeight = ruHeight;
		this.classLksId = classLksId;
		this.mounting = mounting;
		this.formFactor = formFactor;
        this.isFrontImage = isFrontImage;
        this.isRearImage = isRearImage;
	}

	public ModelDetailDTO() {
		super();
		// TODO Auto-generated constructor stub
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

	public int getRuHeight() {
		return ruHeight;
	}

	public void setRuHeight(int ruHeight) {
		this.ruHeight = ruHeight;
	}

	public long getClassLksId() {
		return classLksId;
	}

	public void setClassLksId(long classLksId) {
		this.classLksId = classLksId;
	}

	public String getMounting() {
		return mounting;
	}

	public void setMounting(String mounting) {
		this.mounting = mounting;
	}

	public String getFormFactor() {
		return formFactor;
	}

	public void setFormFactor(String formFactor) {
		this.formFactor = formFactor;
	}

    public boolean isFrontImage() {
        return isFrontImage;
    }

    public void setFrontImage(boolean isFrontImage) {
        this.isFrontImage = isFrontImage;
    }

    public boolean isRearImage() {
        return isRearImage;
    }

    public void setRearImage(boolean isRearImage) {
        this.isRearImage = isRearImage;
    }

}
