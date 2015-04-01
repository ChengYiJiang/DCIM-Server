package com.raritan.tdz.reports.imageprovider;

import java.math.BigInteger;

import com.raritan.tdz.domain.Item;


public class CabinetElevationData implements java.io.Serializable {
	
    private static final long serialVersionUID = 2327839503418352071L;

    private Long itemid;
    private String itemname;
    private Long modelid;
    private Long uposition;
    private Integer ruheight;
    private Long classlksid;
    private Long parentitemid;
    private Long statuslksid;
    private String mounting;
    private Long facinglksid;
    private String classname;
    private Long classvaluecode;
    private Long statusvaluecode;
    private String modelname;
    private String make;
    private Long facingvaluecode;
    private Long mountedrailsposvaluecode;
    private boolean frontimage;
    private boolean rearimage;
    private Long shelfposition;

    public CabinetElevationData() {
    }

	public CabinetElevationData(Long itemid, String itemname, Long modelid,
			Long uposition, Integer ruheight, Long classlksid,
			Long parentitemid, Long statuslksid, String mounting,
			Long facinglksid, String classname, Long classvaluecode,
			Long statusvaluecode, String modelname, String make,
			Long facingvaluecode, Long mountedrailsposvaluecode,
			boolean frontimage, boolean rearimage, Long shelfposition) {
		super();
		this.itemid = itemid;
		this.itemname = itemname;
		this.modelid = modelid;
		this.uposition = uposition;
		this.ruheight = ruheight;
		this.classlksid = classlksid;
		this.parentitemid = parentitemid;
		this.statuslksid = statuslksid;
		this.mounting = mounting;
		this.facinglksid = facinglksid;
		this.classname = classname;
		this.classvaluecode = classvaluecode;
		this.statusvaluecode = statusvaluecode;
		this.modelname = modelname;
		this.make = make;
		this.facingvaluecode = facingvaluecode;
		this.mountedrailsposvaluecode = mountedrailsposvaluecode;
		this.frontimage = frontimage;
		this.rearimage = rearimage;
		this.shelfposition = shelfposition;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(BigInteger itemid) {
		if (itemid != null) this.itemid = itemid.longValue();
	}

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public Long getModelid() {
		return modelid;
	}

	public void setModelid(BigInteger modelid) {
		if (modelid != null) this.modelid = modelid.longValue();
	}

	public Long getUposition() {
		return uposition;
	}

	public void setUposition(BigInteger uposition) {
		this.uposition = uposition.longValue();
	}

	public Integer getRuheight() {
		return ruheight;
	}

	public void setRuheight(Integer ruheight) {
		if (ruheight != null) this.ruheight = ruheight.intValue();
	}

	public Long getClasslksid() {
		return classlksid;
	}

	public void setClasslksid(BigInteger classlksid) {
		if (classlksid != null)this.classlksid = classlksid.longValue();
	}

	public Long getParentitemid() {
		return parentitemid;
	}

	public void setParentitemid(BigInteger parentitemid) {
		if (parentitemid != null) this.parentitemid = parentitemid.longValue();
	}

	public Long getStatuslksid() {
		return statuslksid;
	}

	public void setStatuslksid(BigInteger statuslksid) {
		if (statuslksid != null) this.statuslksid = statuslksid.longValue();
	}

	public String getMounting() {
		return mounting;
	}

	public void setMounting(String mounting) {
		this.mounting = mounting;
	}



	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public Long getClassvaluecode() {
		return classvaluecode;
	}

	public void setClassvaluecode(BigInteger classvaluecode) {
		if (classvaluecode != null) this.classvaluecode = classvaluecode.longValue();
	}

	public Long getStatusvaluecode() {
		return statusvaluecode;
	}

	public void setStatusvaluecode(BigInteger statusvaluecode) {
		if (statusvaluecode != null) this.statusvaluecode = statusvaluecode.longValue();
	}

	public String getModelname() {
		return modelname;
	}

	public void setModelname(String modelname) {
		this.modelname = modelname;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public Long getFacingvaluecode() {
		return facingvaluecode;
	}

	public void setFacingvaluecode(BigInteger facingvaluecode) {
		if (facingvaluecode != null) this.facingvaluecode = facingvaluecode.longValue();
	}

	public Long getMountedrailsposvaluecode() {
		return mountedrailsposvaluecode;
	}

	public void setMountedrailsposvaluecode(BigInteger mountedrailsposvaluecode) {
		if (mountedrailsposvaluecode != null) this.mountedrailsposvaluecode = mountedrailsposvaluecode.longValue();
	}

	public boolean isFrontimage() {
		return frontimage;
	}

	public void setFrontimage(boolean frontimage) {
		this.frontimage = frontimage;
	}

	public boolean isRearimage() {
		return rearimage;
	}

	public void setRearimage(boolean rearimage) {
		this.rearimage = rearimage;
	}

	public Long getShelfposition() {
		return shelfposition;
	}

	public void setShelfposition(Integer shelfposition) {
		if (shelfposition != null) this.shelfposition = shelfposition.longValue();
	}

	public Long getFacinglksid() {
		return facinglksid;
	}

	public void setFacinglksid(BigInteger facinglksid) {
		if (facinglksid != null) { 
			this.facinglksid = facinglksid.longValue();
		}
	}
	
	public void setItemData(Item item) {
		this.itemid = item.getItemId();
		this.itemname = item.getItemName() != null ? item.getItemName() : "";
		this.modelid = item.getModel().getModelDetailId();
		this.mounting = item.getModel() != null ? item.getModel().getMounting() : "";
		this.frontimage = item.getModel() != null ? item.getModel().getFrontImage(): false;
/*		this.rearimage = item.getModel() != null ? item.getModel().getRearImage(): false;
		this.facinglksid = item.getFacingLookup() != null ? item.getFacingLookup().getLksId() : -1;
		this.classname = item.getClassLookup() != null ? item.getClassLookup().getLkpTypeName(): "";
		this.classvaluecode = item.getClassLookup() != null ? item.getClassLookup().getLkpValueCode() : -1;
		this.statusvaluecode = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValueCode() : -1;
		this.modelname = item.getModel() != null ? item.getModel().getModelName(): "";
		this.make = item.getModel() != null ? item.getModel().getModelMfrDetails().getMfrName(): "";
		this.facingvaluecode = item.getFacingLookup() != null ? item.getFacingLookup().getLkpValueCode(): -1;
		this.mountedrailsposvaluecode = item.getMountedRailLookup() != null ? item.getMountedRailLookup().getLkpValueCode(): -1;
		this.shelfposition = item.getShelfPosition().longValue();
		this.uposition = item.getuPosition();
		this.ruheight = item.getModel() != null ? item.getModel().getRuHeight(): 0;
		this.classlksid = item.getClassLookup() != null ? item.getClassLookup().getLksId() : -1;
		this.parentitemid = item.getParentItem() != null ? item.getParentItem().getItemId(): -1;
		this.statuslksid = item.getStatusLookup() != null ? item.getStatusLookup().getLksId() : -1;
*/
	}
	
}

