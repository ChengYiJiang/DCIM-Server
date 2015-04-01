package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.ModelHome;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Santo Rosario
 *
 */
public class RackPduItemObject extends MeItemObject {
	protected CircuitPDHome circuitPDHome;
	protected ItemHome itemHome;
	protected ModelHome modelHome;
	protected NonRackableItemObject nonRackableItemObject;

	@Autowired
	private DataCircuitHome dataCircuitHome;

	@Autowired
	private PowerCircuitHome powerCircuitHome;

	public ModelHome getModelHome() {
		return modelHome;
	}

	public void setModelHome(ModelHome modelHome) {
		this.modelHome = modelHome;
	}

	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	public RackPduItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public CircuitPDHome getCircuitPDHome() {
		return circuitPDHome;
	}

	public void setCircuitPDHome(CircuitPDHome circuitPDHome) {
		this.circuitPDHome = circuitPDHome;
	}

	public NonRackableItemObject getNonRackableItemObject() {
		return nonRackableItemObject;
	}

	public void setNonRackableItemObject(NonRackableItemObject nonRackableItemObject) {
		this.nonRackableItemObject = nonRackableItemObject;
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.RACK_PDU);
		return Collections.unmodifiableSet( codes );
	}

	@Override
	protected void validateUPosition(Object target, Errors errors) {
		if (item.getModel() != null && item.getModel().getMounting() != null &&
				item.getModel().getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {
			nonRackableItemObject.validateUPosition(target, errors);
		}
		else {
			super.validateUPosition(target, errors);
		}
	}

	@Override
	public void validate(Object target, Errors errors){
		super.validate(target, errors);
		/* Validate the Non-Rackable item
			1. Validate if the shelf position is valid
			2. Validate U position */
		assert(target == item);
		if (item.getModel() != null && item.getModel().getMounting() != null &&
				item.getModel().getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {
			nonRackableItemObject.validateExtra(target, errors);
		}
	}

	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		//Delete Circuits for This Item
		List<Long> cirDataList = new ArrayList<Long>();
		List<Long> cirPowerList = new ArrayList<Long>();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setItemId(item.getItemId());

		for(CircuitViewData rec:circuitPDHome.viewCircuitPDList(cCriteria)){
			if(rec.isPowerCircuit()){
				cirPowerList.add(rec.getCircuitId());
			}

			if(rec.isDataCircuit()){
				cirDataList.add(rec.getCircuitId());
			}
		}

		if(cirPowerList.size() > 0){
			powerCircuitHome.deletePowerCircuitByIds(cirPowerList, false);
		}

		if(cirDataList.size() > 0){
			dataCircuitHome.deleteDataCircuitByIds(cirDataList, false);
		}

		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal input to outlet connections

		boolean deleted = super.deleteItem();

		/* FIXME:: HACK HACK HACK super ugly coding, I am hacking because I have no options now
		 * It should have called nonRackableItemObject.deleteItem() but
		 * nonRackableItemObject is derived from ItItem, we do not want ItItems delete to be called
		 * therefore the deleteItem() of nonRackableItemObject is duplicated her to avoid
		 * calling ItItem deleteItem(). Also, in future we will have a delete bean that will
		 * perform deletion independent of the item class and database table it belongs to. This
		 * delete bean will perform different kinda deletion that will be independent of
		 * Non-Rackable, Rackable, MeItem and ItItems */
		ModelDetails model = item.getModel();
		if (model != null && model.isMountingNonRackable()) {
			long cabinetId = (null != item.getParentItem()) ? item.getParentItem().getItemId() : -1;
			long uPosition = item.getuPosition();
			long railsLkpValueCode = item.getMountedRailLookup().getLkpValueCode();
			if (deleted) {
				itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, null, null);
			}
		}
		return deleted;

	}

	private Item loadItem(long itemId) {
		Session session = sessionFactory.getCurrentSession();

		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("itemServiceDetails", "itemServiceDetails");
		//We are doing an EAGER loading since we will be filling the data with what
		//client sends and none of them should be null;
		criteria.setFetchMode("model", FetchMode.JOIN);
		criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
		criteria.setFetchMode("parentItem", FetchMode.JOIN);
		criteria.setFetchMode("itemServiceDetails", FetchMode.JOIN);
		criteria.setFetchMode("itemServiceDetails.itemAdminUser", FetchMode.JOIN);
		criteria.setFetchMode("customFields", FetchMode.JOIN);
		criteria.setFetchMode("cracNwGrpItem", FetchMode.JOIN);
		criteria.setFetchMode("dataPorts", FetchMode.JOIN);
		criteria.setFetchMode("powerPorts", FetchMode.JOIN);
		criteria.setFetchMode("sensorPorts", FetchMode.JOIN);
		criteria.add(Restrictions.eq("itemId", itemId));
		Item item  = (Item) criteria.uniqueResult();
		return item;
	}

	@Override
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException {
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		if (item.getModel().isMountingNonRackable()) {
			nonRackableItemObject.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		}

		// TODO: this is for 3.0 for newly created item.
		// If user creates new power ports for existing item, it should be considered
		if (isUpdate) return;

		Session session = sessionFactory.getCurrentSession();
		Item item = loadItem(itemId);

		if( item.getPowerPorts() != null ){
			List<PowerPort> ppList = new ArrayList<PowerPort>(item.getPowerPorts());
			for (PowerPort pp: ppList) {
				if (null != pp.getInputCordPort()
						/* how else we will know that this is new port. Simple check the PowerConnection*/
						/*&& null == pp.getCreationDate()*/) {
					PowerConnection connection = new PowerConnection(pp, pp.getInputCordPort(),
							0, null,
							SystemLookup.getLksData(session, SystemLookup.LinkType.IMPLICIT), 2);
					connection.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED));
					connection.setCreatedBy(sessionUser.getUserName());
					connection.setCreationDate(pp.getCreationDate());
					connection.setUpdateDate(pp.getUpdateDate());
					session.save(connection);
				}
			}
		}
	}

}

