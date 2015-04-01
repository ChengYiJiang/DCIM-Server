package com.raritan.tdz.item.json;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.chassis.home.BladeItem;
import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.chassis.home.ChassisItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ContainerItemHome;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.json.MobileSearchItemInfo.PlacementGroup;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Chengyi Based on bozana's BasicItemInfo and make some modification
 *         JSON body: Status Item Name Location Make/Model Position tiPosition
 *         HasChildren ==> cbHasChildren
 */

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MobileSearchItemInfo implements Comparable<MobileSearchItemInfo> {

	@Autowired
	ChassisHome chassisHome;
	
	@Autowired
	ItemHome itemHome;
	
	@Autowired
	ContainerItemHome containerItemHome;

	private Long id;

	// Hardware Panel Data
	private String tiMounting;
	private String tiClass;
	private String cmbMake;
	private String cmbModel;
	private Long modelId;
	private int tiRackUnits;
	private String placementGroup;
	private String tiPosition;

	protected final Logger log = Logger.getLogger(getClass());

	/*
	 * Placement groups: For cabinet: in cabinet, above, below, zeroU and
	 * unassigned For chassis: front, back
	 */
	protected enum PlacementGroup {
		/*
		 * IMPORTANT: Order defined here is the order in which we send items to
		 * the client So, do not change it unless we have to send them in
		 * different order.
		 */
		UNASSIGNED("Unassigned"), IN_CABINET("In cabinet"), ZERO_U("Zero U"), ABOVE_CABINET(
				"Above cabinet"), BELOW_CABINET("Below cabinet"),

		FRONT_BLADE("Front"), BACK_BLADE("Back"), UNSLOTTED("Unslotted");

		private String text;

		private PlacementGroup(final String str) {
			this.text = str;
		}

		@Override
		public String toString() {
			return text;
		}

		public static PlacementGroup fromString(String enumTxt) {
			if (enumTxt.equals(IN_CABINET.toString()))
				return IN_CABINET;
			if (enumTxt.equals(ZERO_U.toString()))
				return ZERO_U;
			if (enumTxt.equals(UNASSIGNED.toString()))
				return UNASSIGNED;
			if (enumTxt.equals(ABOVE_CABINET.toString()))
				return ABOVE_CABINET;
			if (enumTxt.equals(BELOW_CABINET.toString()))
				return BELOW_CABINET;
			if (enumTxt.equals(FRONT_BLADE.toString()))
				return FRONT_BLADE;
			if (enumTxt.equals(BACK_BLADE.toString()))
				return BACK_BLADE;
			if (enumTxt.equals(UNSLOTTED.toString()))
				return UNSLOTTED;

			return UNASSIGNED;
		}
	}

	// Identity Panel Data
	private String tiName;

	// Placement Panel Data
	// private String cmbRowLabel;
	// private int cmbRowPosition;
	private String tiLocationRef;
	private String radioRailsUsed;
	private String cmbCabinet;
	private long cmbUPosition;
	// private String radioDepthPosition;
	// private String radioCabinetSide;
	private String cmbChassis;
	private String radioChassisFace;
	private String cmbSlotPosition;
	// private int cmbOrder;

	// Status
	private String cmbStatus;

	// hasChildren for Cabinet/Chassis
	private boolean cbHasChildren;

	@JsonProperty("cbHasChildren")
	public boolean cbHasChildren() {
		return cbHasChildren;
	}

	public void setCbHasChildren(boolean hasChildren) {
		this.cbHasChildren = hasChildren;
	}

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

	// @JsonProperty("tiRackUnits")
	public int getTiRackUnits() {
		return tiRackUnits;
	}

	public void setTiRackUnits(int tiRackUnits) {
		this.tiRackUnits = tiRackUnits;
	}

	@JsonProperty("tiName")
	public String getTiName() {
		return tiName;
	}

	public void setTiName(String tiName) {
		this.tiName = tiName;
	}

	@JsonProperty("tiLocationRef")
	public String getTiLocationRef() {
		return tiLocationRef;
	}

	public void setTiLocationRef(String tiLocationRef) {
		this.tiLocationRef = tiLocationRef;
	}

	// @JsonProperty("radioRailsUsed")
	public String getRadioRailsUsed() {
		return radioRailsUsed;
	}

	public void setRadioRailsUsed(String radioRailsUsed) {
		this.radioRailsUsed = radioRailsUsed;
	}

	// @JsonProperty("cmbCabinet")
	public String getCmbCabinet() {
		return cmbCabinet;
	}

	public void setCmbCabinet(String cmbCabinet) {
		this.cmbCabinet = cmbCabinet;
	}

	// @JsonProperty("tiMounting")
	public String getTiMounting() {
		return tiMounting;
	}

	public void setTiMounting(String tiMounting) {
		this.tiMounting = tiMounting;
	}

	// @JsonProperty("cmbChassis")
	public String getCmbChassis() {
		return cmbChassis;
	}

	public void setCmbChassis(String cmbChassis) {
		this.cmbChassis = cmbChassis;
	}

	// @JsonProperty("radioChassisFace")
	public String getRadioChassisFace() {
		return radioChassisFace;
	}

	public void setRadioChassisFace(String radioChassisFace) {
		this.radioChassisFace = radioChassisFace;
	}

	@JsonProperty("tiClass")
	public String getTiClass() {
		return tiClass;
	}

	public void setTiClass(String tiClass) {
		this.tiClass = tiClass;
	}

	@JsonProperty("cmbStatus")
	public String getCmbStatus() {
		return cmbStatus;
	}

	public void setCmbStatus(String cmbStatus) {
		this.cmbStatus = cmbStatus;
	}

	// @JsonProperty("cmbSlotPosition")
	public String getCmbSlotPosition() {
		return cmbSlotPosition;
	}

	public void setCmbSlotPosition(String cmbSlotPosition) {
		this.cmbSlotPosition = cmbSlotPosition;
	}

	@JsonProperty("modelId")
	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public String getPlacementGroup() {
		return placementGroup;
	}

	public void setPlacementGroup(String placementGroup) {
		this.placementGroup = placementGroup;
	}

	@JsonProperty("tiPosition")
	public String getTiPosition() {
		return tiPosition;
	}

	public void setTiPosition(String tiPosition) {
		this.tiPosition = tiPosition;
	}

	public MobileSearchItemInfo() {

	}

	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).toHashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		BasicItemInfo otherItem = (BasicItemInfo) obj;
		if ((id == otherItem.getId()) && (id > 0))
			return true;

		return false;
	}

	@Override
	public int compareTo(MobileSearchItemInfo bi) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		int retval = EQUAL;

		if (this == bi)
			return EQUAL;

		if (this.getPlacementGroup() != null && bi.getPlacementGroup() != null) {
			if (this.getPlacementGroup().equals(bi.getPlacementGroup())) {
				// if "this" is a blade then "bi" must be blade too inside the
				// same group,
				// compare their slot labels to determine which one goes first
				if (this.getCmbSlotPosition() != null
						&& bi.getCmbSlotPosition() != null) {
					retval = this.getCmbSlotPosition().compareTo(
							bi.getCmbSlotPosition());
				} else { // not blade, same group, "this" and "bi" are treated
							// as equal
					retval = EQUAL;
				}
			} else { // "this" and "bi" do not have the same placement group,
						// compare their groups (enums)
						// to determine which one goes first
				retval = PlacementGroup.fromString(this.getPlacementGroup())
						.compareTo(
								PlacementGroup.fromString(bi
										.getPlacementGroup()));
			}
		} else {
			// This statement should never be reachable, placementGroup must be
			// set before this method is invoked
			log.error("### Something is wrong?! Check placementGroup in: "
					+ this.getTiName() + " and " + bi.getTiName());
			throw new RuntimeException("placementGroup is missing");
		}
		if (log.isDebugEnabled()) {
			log.debug("###### Sorting: " + this.getTiName() + " ("
					+ this.getPlacementGroup() + "), " + bi.getTiName() + "("
					+ bi.getPlacementGroup() + ")");
		}
		return retval;
	}

	protected void setPosition(Item item) throws DataAccessException {

		tiPosition = "";
		if (item.getSlotPosition() > 0
				&& item.getModel().getMounting().equals(SystemLookup.Mounting.BLADE)) // blade
			tiPosition = getSlotLabel(item);

		if (item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET) )
			tiPosition = "0/" + item.getModel().getRuHeight();
		
		if (item.getModel().getMounting().equals(SystemLookup.Mounting.FREE_STANDING)) {
			tiPosition = "" + item.getModel().getRuHeight();
		} 
		else {			
			String uPosition = "";
			if (item.getUPosition() == -2)
				uPosition += "Above";
			else if (item.getUPosition() == -1)
				uPosition += "Below";
			else
				uPosition += item.getUPosition();

			if (item.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U)) {
				tiPosition = item.getMountedRailLookup().getLkpValue() + ","
						+ item.getFacingLookup().getLkpValue() + ","
						+ uPosition + "/" + item.getModel().getRuHeight();
			} else if (item.getModel().getMounting().equals(SystemLookup.Mounting.RACKABLE)) {
				tiPosition = radioRailsUsed + "," + uPosition + "/"
						+ item.getModel().getRuHeight();
			} else if (item.getModel().getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {
				// TODO
				if (item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.WHIP_OUTLET))
					tiPosition = uPosition;
				else {
					tiPosition = radioRailsUsed + "," + item.getShelfPosition() + "," + uPosition + "/"
							+ item.getModel().getRuHeight();
				}
			} 
		}
		
	}

	protected void setHasChildren(Item item) throws DataAccessException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		if (item.getChildItems().size() > 0) {
			cbHasChildren = true;
		} else if (item.getModel().isItItemModel()) {	
			ItItem itItem = (ItItem) item;
			if (itItem.getSubclassLookup() != null && itItem.getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.BLADE_CHASSIS) {
				//if (containerItemHome.getAllItemsInContainer(item, false, false).size() > 0)
					//cbHasChildren = true;
				long chassisId = itItem.getItemId();
				if (chassisHome.getAllBladesForChassis(chassisId).size() > 0)
					cbHasChildren = true;
			}
		} else if (item.isClassFloorPDU() && item.getSubclassLookup() == null) {
			if (containerItemHome.getAllItemsInContainer(item, false, false).size() > 0)
				cbHasChildren = true;			
		}

	}

	protected void setPlacementGroup(Item item) {
		if (item.getuPosition() > 0
				&& item.getModel() != null
				&& item.getModel().getMounting() != null
				&& !item.getModel().getMounting()
						.equals(SystemLookup.Mounting.ZERO_U)
				&& item.getFacingLookup() != null) {// in cabinet
			setPlacementGroup(PlacementGroup.IN_CABINET.toString());
		} else if (item.getUPosition() == -1) {// below
			setPlacementGroup(PlacementGroup.BELOW_CABINET.toString());
		} else if (item.getUPosition() == -2) { // Above
			setPlacementGroup(PlacementGroup.ABOVE_CABINET.toString());
		} else if (item.getModel() != null
				&& item.getModel().getMounting() != null
				&& item.getModel().getMounting()
						.equals(SystemLookup.Mounting.ZERO_U)) {
			setPlacementGroup(PlacementGroup.ZERO_U.toString());
		} else {
			setPlacementGroup(PlacementGroup.UNASSIGNED.toString());
		}
	}

	public void collectItemInfo(Item item) throws DataAccessException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		id = item.getItemId();
		modelId = item.getModel() != null ? item.getModel().getModelDetailId()
				: null;

		if (item.getModel() != null) {
			cmbModel = item.getModel().getModelName();
			tiRackUnits = item.getModel().getRuHeight();
			if (item.getModel().getModelMfrDetails() != null) {
				cmbMake = item.getModel().getModelMfrDetails().getMfrName();

			}
		}
		tiName = item.getItemName();

		if( item.getClassLookup() != null ) tiClass = item.getClassLookup().getLkpValue();
		
		if (item.getStatusLookup() != null)
			cmbStatus = item.getStatusLookup().getLkpValue();

		if (item.getDataCenterLocation() != null)
			tiLocationRef = item.getDataCenterLocation().getCode();

		if (item.getMountedRailLookup() != null)
			radioRailsUsed = item.getMountedRailLookup().getLkpValue();
		if (item.getParentItem() != null)
			cmbCabinet = item.getParentItem().getItemName();

		if (item.getMountedRailLookup() != null)
			radioRailsUsed = item.getMountedRailLookup().getLkpValue();

		if (item.getSlotPosition() > 0)
			cmbSlotPosition = String.valueOf(item.getSlotPosition());

		if (item.getModel() != null) {
			StringBuilder mounting = new StringBuilder();
			mounting.append(item.getModel().getMounting());
			// mounting.append(" / ");
			// mounting.append(item.getModel().getFormFactor());
			tiMounting = mounting.toString();
		}
		// setPlacementGroup( item );
		setPosition(item);
		setHasChildren(item);
	}

	//

	// From bozana's BladeItemInfo and make some modification
	// for blade
	private String getSlotLabel(Item item) throws DataAccessException {
		StringBuilder str = new StringBuilder();
		String retval = null;
		String face = item.getFacingLookup().getLkpValue();
		ItItem blade = (ItItem) item;

		if (null == blade.getBladeChassis()
				|| (blade.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE && blade
						.getSubclassLookup().getLkpValueCode() != SystemLookup.SubClass.BLADE_SERVER)) {
			return null;
		}

		long facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
		if (null != blade.getFacingLookup()) {
			facingLkpValueCode = blade.getFacingLookup().getLkpValueCode();
		} else {
			long bladeRailsUsedLkpValueCode = -1;
			if (null != blade.getMountedRailLookup()) {
				bladeRailsUsedLkpValueCode = blade.getMountedRailLookup()
						.getLkpValueCode();
			}
			/*
			 * The facing information for the blade is not updated in the
			 * database. If updated use the
			 * blade.getFacingLookup().getLkpValueCode()
			 */
			if (SystemLookup.RailsUsed.FRONT == bladeRailsUsedLkpValueCode
					|| SystemLookup.RailsUsed.BOTH == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
			} else if (SystemLookup.RailsUsed.REAR == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.REAR;
			}
		}

		int maxSlot = -1;
		if (blade != null && blade.getModel() != null) {
			ItItem chassisItem = chassisHome.getItItemDomainObject(blade
					.getBladeChassis().getItemId());
			ModelChassis modelChassis = chassisHome.getBladeChassis(chassisItem
					.getModel().getModelDetailId(), facingLkpValueCode);
			maxSlot = modelChassis.getSlotMax();
			Map<Long, String> sortedSlotNumbers = chassisHome
					.getSortedSlotNumber(modelChassis, blade);
			if (sortedSlotNumbers.size() > 0) {
				for (String value : sortedSlotNumbers.values()) {
					str.append(value);
					str.append(",");
				}
				// take out last comma
				retval = str.toString().substring(0, str.length() - 1);
			}
		}
		String result = face + "-" + retval + "/" + maxSlot;
		return result;

	}

}
