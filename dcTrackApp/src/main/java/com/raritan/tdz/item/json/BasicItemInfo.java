package com.raritan.tdz.item.json;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class BasicItemInfo implements Comparable<BasicItemInfo>{

	private Long id;
	
    //Hardware Panel Data
	private String cmbMake;
	private String cmbModel;
	private Long modelId;
	private String tiClass;
    private String tiMounting;
	private int tiRackUnits;
	private String tiSerialNumber;
	private String tiAssetTag;
	private String tieAssetTag;
	private String placementGroup;
	
	protected final Logger log = Logger.getLogger(getClass());
	/*
	 * Placement groups:
	 * For cabinet: in cabinet, above, below, zeroU and unassigned
	 * For chassis: front, back
	 */
	protected enum PlacementGroup {
		/*
		 * IMPORTANT: Order defined here is the order in which we send items to the client
		 * So, do not change it unless we have to send them in different order.
		 */
		UNASSIGNED( "Unassigned" ),
		IN_CABINET("In cabinet"),
		ZERO_U("Zero U"),
		ABOVE_CABINET("Above cabinet"),
		BELOW_CABINET("Below cabinet"),
		
		FRONT_BLADE("Front"),
		BACK_BLADE("Back"),
		UNSLOTTED("Unslotted");
		
		private String text;
		
		private PlacementGroup( final String str){
			this.text = str;
		}
		
		@Override
		public String toString(){
			return text;
		}

		public static PlacementGroup fromString( String enumTxt){
			if( enumTxt.equals(IN_CABINET.toString())) return IN_CABINET;
			if( enumTxt.equals(ZERO_U.toString())) return ZERO_U;
			if( enumTxt.equals(UNASSIGNED.toString())) return UNASSIGNED;
			if( enumTxt.equals(ABOVE_CABINET.toString())) return ABOVE_CABINET;
			if( enumTxt.equals(BELOW_CABINET.toString())) return BELOW_CABINET;
			if( enumTxt.equals(FRONT_BLADE.toString())) return FRONT_BLADE;
			if( enumTxt.equals(BACK_BLADE.toString())) return BACK_BLADE;
			if( enumTxt.equals(UNSLOTTED.toString())) return UNSLOTTED;
			
			return UNASSIGNED;
		}
	}

    //Identity Panel Data
	private String tiName;
	private String cmbCustomer; 

    //Placement Panel Data
	private String cmbRowLabel; 
	private int cmbRowPosition;
	private String tiLocationRef;
	private String radioRailsUsed;
	private String cmbCabinet; 
	private long cmbUPosition;
	private String radioDepthPosition;
	private String radioCabinetSide;
	private String cmbChassis; 
	private String radioChassisFace;
	private String cmbSlotPosition;
	private int cmbOrder;

    //Status
   	private String cmbStatus;
	
	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonProperty("cmbMake")
	public String getCmbMake() {
		return cmbMake;
	}


	public void setCmbMake(String cmbMake) {
		this.cmbMake = cmbMake;
	}

	@JsonProperty("cmbModel")
	public String getCmbModel() {
		return cmbModel;
	}


	public void setCmbModel(String cmbModel) {
		this.cmbModel = cmbModel;
	}

	@JsonProperty("tiRackUnits")
	public int getTiRackUnits() {
		return tiRackUnits;
	}


	public void setTiRackUnits(int tiRackUnits) {
		this.tiRackUnits = tiRackUnits;
	}


	@JsonProperty("tiSerialNumber")
	public String getTiSerialNumber() {
		return tiSerialNumber;
	}


	public void setTiSerialNumber(String tiSerialNumber) {
		this.tiSerialNumber = tiSerialNumber;
	}

	@JsonProperty("tiAssetTag")
	public String getTiAssetTag() {
		return tiAssetTag;
	}


	public void setTiAssetTag(String tiAssetTag) {
		this.tiAssetTag = tiAssetTag;
	}

	@JsonProperty("tieAssetTag")
	public String getTieAssetTag() {
		return tieAssetTag;
	}


	public void setTieAssetTag(String tieAssetTag) {
		this.tieAssetTag = tieAssetTag;
	}

	@JsonProperty("tiName")
	public String getTiName() {
		return tiName;
	}


	public void setTiName(String tiName) {
		this.tiName = tiName;
	}

	@JsonProperty("cmbCustomer")
	public String getCmbCustomer() {
		return cmbCustomer;
	}


	public void setCmbCustomer(String cmbCustomer) {
		this.cmbCustomer = cmbCustomer;
	}

	@JsonProperty("cmbRowLabel")
	public String getCmbRowLabel() {
		return cmbRowLabel;
	}


	public void setCmbRowLabel(String cmbRowLabel) {
		this.cmbRowLabel = cmbRowLabel;
	}

	@JsonProperty("cmbRowPosition")
	public int getCmbRowPosition() {
		return cmbRowPosition;
	}


	public void setCmbRowPosition(int cmbRowPosition) {
		this.cmbRowPosition = cmbRowPosition;
	}

	@JsonProperty("tiLocationRef")
	public String getTiLocationRef() {
		return tiLocationRef;
	}


	public void setTiLocationRef(String tiLocationRef) {
		this.tiLocationRef = tiLocationRef;
	}

	@JsonProperty("radioRailsUsed")
	public String getRadioRailsUsed() {
		return radioRailsUsed;
	}


	public void setRadioRailsUsed(String radioRailsUsed) {
		this.radioRailsUsed = radioRailsUsed;
	}

	@JsonProperty("cmbCabinet")
	public String getCmbCabinet() {
		return cmbCabinet;
	}


	public void setCmbCabinet(String cmbCabinet) {
		this.cmbCabinet = cmbCabinet;
	}

	@JsonProperty("cmbUPosition")
	public long getCmbUPosition() {
		return cmbUPosition;
	}


	public void setCmbUPosition(long cmbUPosition) {
		this.cmbUPosition = cmbUPosition;
	}

	@JsonProperty("radioDepthPosition")
	public String getRadioDepthPosition() {
		return radioDepthPosition;
	}


	public void setRadioDepthPosition(String radioDepthPosition) {
		this.radioDepthPosition = radioDepthPosition;
	}

	@JsonProperty("radioCabinetSide")
	public String getRadioCabinetSide() {
		return radioCabinetSide;
	}


	public void setRadioCabinetSide(String radioCabinetSide) {
		this.radioCabinetSide = radioCabinetSide;
	}

	@JsonProperty("cmbChassis")
	public String getCmbChassis() {
		return cmbChassis;
	}


	public void setCmbChassis(String cmbChassis) {
		this.cmbChassis = cmbChassis;
	}

	@JsonProperty("radioChassisFace")
	public String getRadioChassisFace() {
		return radioChassisFace;
	}


	public void setRadioChassisFace(String radioChassisFace) {
		this.radioChassisFace = radioChassisFace;
	}

	@JsonProperty("cmbOrder")
	public int getCmbOrder() {
		return cmbOrder;
	}


	public void setCmbOrder(int cmbOrder) {
		this.cmbOrder = cmbOrder;
	}

	@JsonProperty("cmbStatus")
	public String getCmbStatus() {
		return cmbStatus;
	}


	public void setCmbStatus(String cmbStatus) {
		this.cmbStatus = cmbStatus;
	}

	@JsonProperty("cmbSlotPosition")
	public String getCmbSlotPosition() {
		return cmbSlotPosition;
	}


	public void setCmbSlotPosition(String cmbSlotPosition) {
		this.cmbSlotPosition = cmbSlotPosition;
	}
	
	@JsonProperty("tiClass")
	public String getTiClass() {
		return tiClass;
	}


	public void setTiClass(String tiClass) {
		this.tiClass = tiClass;
	}

	@JsonProperty("tiMounting")
	public String getTiMounting() {
		return tiMounting;
	}

	public void setTiMounting(String tiMounting) {
		this.tiMounting = tiMounting;
	}
	
	@JsonProperty("modelId")
	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	@JsonProperty("placementGroup")
	public String getPlacementGroup() {
		return placementGroup;
	}

	public void setPlacementGroup(String placementGroup) {
		this.placementGroup = placementGroup;
	}

	public BasicItemInfo() {
	}

	public void collectItemInfo(Item item) {
		
		id = item.getItemId();
		modelId = item.getModel() != null ? item.getModel().getModelDetailId() : null;
		
		if( item.getModel() != null ){
			cmbModel = item.getModel().getModelName();
			tiRackUnits = item.getModel().getRuHeight();
			if(item.getModel().getModelMfrDetails() != null ){
				cmbMake = item.getModel().getModelMfrDetails().getMfrName();
				
			}
		}
		if( item.getItemServiceDetails() != null ){
			tiSerialNumber = item.getItemServiceDetails().getSerialNumber();
			tiAssetTag = item.getItemServiceDetails().getAssetNumber();
			if( item.getItemServiceDetails().getDepartmentLookup() != null ){
				cmbCustomer = item.getItemServiceDetails().getDepartmentLookup().getLkuValue();		
			}
		}

		if( item.getClassLookup() != null ) tiClass = item.getClassLookup().getLkpValue();
		
		tieAssetTag = item.getRaritanAssetTag();
		tiName = item.getItemName();
		
		cmbRowPosition = -1;
		
		if( item.getStatusLookup() != null ) cmbStatus = item.getStatusLookup().getLkpValue();	
		
		if( item.getDataCenterLocation() != null ) tiLocationRef = item.getDataCenterLocation().getCode();
		
		if( item.getMountedRailLookup() != null ) radioRailsUsed = item.getMountedRailLookup().getLkpValue();
		if( item.getParentItem() != null ) cmbCabinet = item.getParentItem().getItemName();
	
	
		if( item.getModel() != null ){
			StringBuilder mounting = new StringBuilder();
			mounting.append(item.getModel().getMounting());
			mounting.append(" / ");
			mounting.append(item.getModel().getFormFactor());
			tiMounting = mounting.toString();
		}
		setPlacementGroup( item );
	}
	
	protected void setPlacementGroup(Item item) {
		if( item.getuPosition() > 0 && item.getModel() != null &&
				item.getModel().getMounting() != null &&
				!item.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U) &&
				item.getFacingLookup() != null ){//in cabinet
			setPlacementGroup(PlacementGroup.IN_CABINET.toString());
		}else if( item.getUPosition() == -1 ){//below
			setPlacementGroup(PlacementGroup.BELOW_CABINET.toString());
		}else if( item.getUPosition() == -2 ){ //Above
			setPlacementGroup(PlacementGroup.ABOVE_CABINET.toString());
		}else if( item.getModel() != null && item.getModel().getMounting() != null && item.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U)){
			setPlacementGroup(PlacementGroup.ZERO_U.toString());
		}else{
			setPlacementGroup(PlacementGroup.UNASSIGNED.toString());
		}
		
	}

	public int hashCode(){
		return new HashCodeBuilder(17, 31).append( id ).toHashCode();
	}
	
	public boolean equals(Object obj){
		if( obj == null ) return false;
		if( this == obj ) return true;
		if( getClass() != obj.getClass()) return false;
		BasicItemInfo otherItem = (BasicItemInfo)obj;
		if( (id == otherItem.getId()) && (id > 0 )) return true;
		
		return false;
	}

	/**
	 * Compare this element and bi. If "this" should go before "bi" return BEFORE,
	 * If "this" should go after bi, return AFTER.
	 * If "this" and "bi" order does not matter, return EQUAL
	 * 
	 * This method is invoked by java when calling Collections.sort(). See: ContainerItem.java
	 */
	@Override
	public int compareTo(BasicItemInfo bi) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		int retval = EQUAL;
		
		if ( this == bi ) return EQUAL;
			
		if( this.getPlacementGroup() != null && bi.getPlacementGroup() != null){
			if( this.getPlacementGroup().equals(bi.getPlacementGroup())){
				//if "this" is a blade then "bi" must be blade too inside the same group, 
				//compare their slot labels to determine which one goes first
				if( this.getCmbSlotPosition() != null && bi.getCmbSlotPosition() != null ){
					retval = this.getCmbSlotPosition().compareTo(bi.getCmbSlotPosition());	
				}else{ //not blade, same group, "this" and "bi" are treated as equal
					retval= EQUAL;
				}
			}else{ //"this" and "bi" do not have the same placement group, compare their groups (enums)
				//to determine which one goes first
				retval = PlacementGroup.fromString(this.getPlacementGroup()).compareTo(PlacementGroup.fromString(bi.getPlacementGroup()));
			}
		}else{
			//This statement should never be reachable, placementGroup must be set before this method is invoked
			log.error("### Something is wrong?! Check placementGroup in: " + this.getTiName() + " and " + bi.getTiName());
			throw new RuntimeException("placementGroup is missing");
		}
		if (log.isDebugEnabled()) {
			log.debug("###### Sorting: " + this.getTiName() + " (" + this.getPlacementGroup() + "), " + bi.getTiName() +
				"(" + bi.getPlacementGroup() + ")");
		}
		return retval;
	}

}
