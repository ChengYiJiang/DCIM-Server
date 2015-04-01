package com.raritan.tdz.model.dto;

public class ModelDTO {

    private long modelId;
    private String modelName;
    private int ruHeight;
    private long classLksId;
    private String mounting;
    private String formFactor;
    private boolean isStandard;
    private String psredundancy;

    // For passive models
    private long makeId;
    private String makeName;
    private String className;
    private long classLkpValueCode;
    private String subClassName;
    private long subClassLksId;
    private long subClassLkpValueCode;

    public ModelDTO(long modelId, String modelName, int ruHeight,
            long classLksId, String mounting, String formFactor,
            boolean isStandard, String psredundancy,
            long makeId, String makeName,
            String className, long classLkpValueCode,
            String subClassName, long subClassLksId, long subClassLkpValueCode) {

        super();
        this.modelId = modelId;
        this.modelName = modelName;
        this.ruHeight = ruHeight;
        this.classLksId = classLksId;
        this.mounting = mounting;
        this.formFactor = formFactor;
        this.isStandard = isStandard;
        this.psredundancy = psredundancy;
        this.makeId = makeId;
        this.makeName = makeName;
        this.className = className;
        this.classLkpValueCode = classLkpValueCode;
        this.subClassName = subClassName;
        this.subClassLksId = subClassLksId;
        this.subClassLkpValueCode = subClassLkpValueCode;
    }

    public ModelDTO() {
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

    public boolean isStandard() {
        return isStandard;
    }

    public void setStandard(boolean isStandard) {
        this.isStandard = isStandard;
    }

    public String getPsredundancy() {
        return psredundancy;
    }

    public void setPsredundancy(String psredundancy) {
        this.psredundancy = psredundancy;
    }

    public long getMakeId() {
        return makeId;
    }

    public void setMakeId(long makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getClassLkpValueCode() {
        return classLkpValueCode;
    }

    public void setClassLkpValueCode(long classLkpValueCode) {
        this.classLkpValueCode = classLkpValueCode;
    }

    public String getSubClassName() {
        return subClassName;
    }

    public void setSubClassName(String subClassName) {
        this.subClassName = subClassName;
    }

    public long getSubClassLksId() {
        return subClassLksId;
    }

    public void setSubClassLksId(long subClassLksId) {
        this.subClassLksId = subClassLksId;
    }

    public long getSubClassLkpValueCode() {
        return subClassLkpValueCode;
    }

    public void setSubClassLkpValueCode(long subClassLkpValueCode) {
        this.subClassLkpValueCode = subClassLkpValueCode;
    }
}