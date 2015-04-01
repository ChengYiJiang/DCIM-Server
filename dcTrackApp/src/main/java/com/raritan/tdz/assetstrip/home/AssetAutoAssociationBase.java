package com.raritan.tdz.assetstrip.home;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQProbeLookup;
import com.raritan.tdz.port.home.PortHome;

/**
 * Base class supporting auto association logic for asset tag and asset strip events.
 * @author Andrew Cohen
 */
@Transactional
public abstract class AssetAutoAssociationBase {
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	// Asset event parameters
	protected static final String CABINET_PARAM = "Cabinet";
	protected static final String LOCATION_PARAM = "Location";
	protected static final String ITEM_NAME_PARAM = "Item Name";
	protected static final String ITEM_CLASS_PARAM = "Item Class";
	protected static final String ITEM_STATUS_PARAM = "Item Status";
	protected static final String UNKNOWN_VALUE = "Unknown";
	protected static final String EMPTY_VALUE = "";
	
	// This ThreadLocal tracks the event that the current thread is processing.
	static final ThreadLocal<Event> dctEvent = new ThreadLocal<Event>();
	
	SessionFactory sessionFactory;
	ItemHome itemHome;
	PortHome portHome;
	PIQProbeLookup probeLookup;
	
	public AssetAutoAssociationBase(SessionFactory sessionFactory, ItemHome itemHome, PortHome portHome, PIQProbeLookup probeLookup) {
		this.sessionFactory = sessionFactory;
		this.itemHome = itemHome;
		this.portHome = portHome;
		this.probeLookup = probeLookup;
	}
	
	/**
	 * This method will handle an asset event in which we could not find
	 * either the associated cabinet or item.
	 * @param cabinet
	 * @param item
	 */
	abstract void handleUnknownAssociation(CabinetItem cabinet, Item item);
	
	/**
	 * @return the log4j logger
	 */
	abstract Logger getLogger();
	
	
	final CabinetItem findCabinetItemForAssetStripSensorPort( SensorPort assetStrip, Item item ) {
		CabinetItem cabinet = null;

		if (assetStrip != null) {
			cabinet = itemHome.getCabinet( assetStrip.getCabinetItem() );
		}
		else {
			// If PDU or EMX-111 has no sensor port exists, check to see if the containing cabinet has an asset strip attached.
			// If not, we can automatically assign this as the cabinet for the asset strip
			boolean assignDefaultCabinet = ((item.getClassLookup() != null) &&
					item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU);
			if (!assignDefaultCabinet) {
				if ((item.getModel() != null) && (item.getModel().getModelName().toUpperCase().equals("EMX-111"))) {
					assignDefaultCabinet = true;
				}
			}
			
			if (assignDefaultCabinet) {
				cabinet = itemHome.getCabinet( item );
				if (cabinet != null) {
					SensorPort cabinetAssetStrip = getAssetStripForCabinet( cabinet );
					if (cabinetAssetStrip != null) {
						cabinet = null; // This cabinet already has an asset strip attached
					}
				}
			}
		}
		
		return cabinet;
	}
	
	/**
	 * Find the cabinet where the asset strip is placed. 
	 * @return
	 * @throws HibernateException
	 */
	final CabinetItem findCabinetForAssetStrip() throws HibernateException {
		CabinetItem cabinet = null;
		Item item = findItemForAssetStrip();
		
		if (item != null) {
			try {
				SensorPort assetStrip = findAssetStripSensorPort( item );
				cabinet = findCabinetItemForAssetStripSensorPort(assetStrip, item);
			}
			catch (HibernateException e) {
				handleUnknownAssociation(null, null);
				getLogger().error( e );
				cabinet = null;
			}
			catch (IllegalArgumentException e) {
				handleUnknownAssociation(null, null);
				getLogger().error( e );
				cabinet = null;
			}
		}
		
		return cabinet;
	}
	
	/**
	 * Finds an asset strip sensor port associated with an item.
	 * @param item the item connected to the asset strip (PDU or EMX)
	 * @param the asset strip event
	 * @return
	 */
	final SensorPort findAssetStripSensorPort(Item item) {
		SensorPort assetStrip = null;
		Collection<SensorPort> ports = null;
		List<Integer> sortOrders = new LinkedList<Integer>();
		
		Integer assetStripNum = getAssetStripNumber();
		if (assetStripNum != null) {
			sortOrders.add( assetStripNum );
		}
		
		// Search for an existing sensor port on the item
		try {
			ports = portHome.viewSensorPortsByCriteria(
						item.getItemId(), // the rack PDU item
						-1, // Ignore item class code
						null, // We aren't looking for ports by Id
						SystemLookup.PortSubClass.ASSET_STRIP,
						sortOrders,
						-1, // Ignore cabinet
						true,
						null); // Ignore sensor name
		}
		catch (DataAccessException e) {
			log.error("Error searching for asset strip sensor port ", e);
		}
		
		if (ports != null && !ports.isEmpty()) { 
			assetStrip = ports.iterator().next();
		}
		
		return assetStrip;
	}
	
	/**
	 * Get the asset strip number for the current event.
	 * @return
	 */
	final Integer getAssetStripNumber() {
		String assetStripNum = getAssetParam(AssetEventParam.ASSET_STRIP_NUMBER);
		try {
			return Integer.parseInt( assetStripNum );
		}
		catch (NumberFormatException e) {
			log.error("Invalid asset strip number: " + assetStripNum, e);
		}
		
		return null;
	}
	
	/**
	 * Finds the item in which the asset strip is plugged into based on the event parameters.
	 * This is currently either an PDU or EMX probe.
	 * @return
	 */
	final Item findItemForAssetStrip() {
		String piq_id = getAssetParam(AssetEventParam.PDU_ID);
		Item item = null;
		
		if (piq_id != null) {
			try {
				Integer piqId = Integer.parseInt( piq_id );
				item = itemHome.getPDUItem( piqId );
				
				// See if the PDU is linked to a probe
				Item probeItem = probeLookup.getProbeItemForDummyRackPDU( item.getItemId() );
				if (probeItem != null) {
					item = probeItem;
				}
			} 
			catch (NumberFormatException e) {
				handleUnknownAssociation(null, null);
				e.printStackTrace();
				item = null;
			} 
			catch (HibernateException e) {
				handleUnknownAssociation(null, null);
				e.printStackTrace();
				item = null;
			} 
			catch (DataAccessException e) {
				handleUnknownAssociation(null, null);
				e.printStackTrace();
				item = null;
			} 
			catch (IllegalArgumentException e) {
				handleUnknownAssociation(null, null);
				e.printStackTrace();
				item = null;
			}
		}
		return item;
	}
	
	/**
	 * 
	 * @param dctEvent
	 * @param paramName
	 * @return
	 */
	final String getAssetParam(AssetEventParam paramName) {
		Event event = dctEvent.get();
		if (event == null) return "";
		EventParam param = event.getEventParams().get( paramName.toString() );
		return param != null ? param.getValue() : "";
	}
	
	/**
	 * 
	 * @param cabinet
	 * @return
	 */
	final String getLocationCode(Item cabinet, String defaultLocation){
		return cabinet != null ? cabinet.getDataCenterLocation().getCode() : defaultLocation;
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 */
	final String getItemName(Item item, String defaultName) {
		return item != null ? item.getItemName() : defaultName;
	}
	
	/**
	 * 
	 * @param severity
	 */
	final void setSeverity(EventSeverity severity) {
		dctEvent.get().setSeverity(sessionFactory.getCurrentSession(), severity);
	}
	
	//
	// Private methods
	//
	
	@Transactional(readOnly = true)
	private SensorPort getAssetStripForCabinet(CabinetItem cabinet) {
		Session session = sessionFactory.getCurrentSession();
		
		Query q = session.createQuery("from SensorPort where cabinet_item_id = :cabinetItemId");
		q.setLong("cabinetItemId", cabinet.getItemId());
		SensorPort assetStrip = (SensorPort)q.uniqueResult();
		
		if (assetStrip != null) {
			if ((assetStrip.getPortSubClassLookup() == null) || 
					(assetStrip.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.ASSET_STRIP)) {
				return null;
			}
		}
		return assetStrip;
	}
}