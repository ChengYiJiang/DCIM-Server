/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHandlerDelegate;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQAssetStripClient;
import com.raritan.tdz.piq.home.PIQProbeLookup;
import com.raritan.tdz.piq.home.PIQRestClient;
import com.raritan.tdz.piq.json.AssetStrip;


/**
 * Default service implementation for handling "Asset Strip" connect and  disconnect events.
 * 
 * @author Andrew Cohen
 */
public class AssetStripAutoAssociationImpl extends AssetAutoAssociationBase implements AssetStripAutoAssociation {
	
	private static final String ASSET_STRIP_PORT_NAME = "Asset Strip";
	private static final String RACK_UNITS = "Rack Units";
	
	private Logger log = Logger.getLogger( this.getClass() );
	private PIQAssetStripClient piqAssetStripClient;
	private EventHome eventHome;
	
	public AssetStripAutoAssociationImpl(SessionFactory sessionFactory,
			ItemHome itemHome,
			com.raritan.tdz.port.home.PortHome portHome,
			EventHandlerDelegate eventHandlerDelegate,
			PIQProbeLookup probeLookup,
			PIQAssetStripClient piqAssetStripClient,
			EventHome eventHome) {
		super( sessionFactory, itemHome, portHome, probeLookup );
		eventHandlerDelegate.registerEventHandler( this );
		this.piqAssetStripClient = piqAssetStripClient;
		this.eventHome = eventHome;
	}
	
	/*
	 * Handle the event.
	 * @see com.raritan.tdz.events.home.EventHandler#handleEvent(com.raritan.tdz.events.domain.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		long eventType = event.getType().getLkpValueCode();
		if (eventType == SystemLookup.EventType.ASSET_STRIP_CONNECTED) {
			addAssociation( event );
		}
		else if (eventType == SystemLookup.EventType.ASSET_STRIP_REMOVED) {
			removeAssociation( event );
		}
	}
	
	/*
	 * Add the asset strip association.
	 * @see com.raritan.tdz.assetstrip.home.AssetStripAutoAssociation#addAssociation(com.raritan.tdz.events.domain.Event)
	 */
	@Override
	public Item addAssociation(Event event) {
		dctEvent.set( event );
		
		// Locate the item connected with the asset strip
		Item item = findItemForAssetStrip();
		
		// Locate the cabinet associated with the asset strip
		CabinetItem cabinet = findCabinetForAssetStrip(); 
		
		// Associate the item with the asset strip
		associateItemWithStrip(cabinet, item);

		// Build the event summary for the asset strip event
		buildAssetStripEventSummary(cabinet, item);
		
		// Attach event details
		setEventDetails(cabinet, item);
		
		dctEvent.set( null );
		
		return item;
	}

	/*
	 * Remove the asset strip association.
	 * @see com.raritan.tdz.assetstrip.home.AssetStripAutoAssociation#removeAssociation(com.raritan.tdz.events.domain.Event)
	 */
	@Override
	public Item removeAssociation(Event event) {
		dctEvent.set( event );
		
		// Locate the item connected with the asset strip
		Item item = findItemForAssetStrip();
		
		// Locate the cabinet associated with the asset strip
		CabinetItem cabinet = findCabinetForAssetStrip();
		
		// Build the event summary for the asset strip event
		buildAssetStripEventSummary(cabinet, item);

		// Attach event details
		setEventDetails(cabinet, item);
		
		dctEvent.set( null );
		
		return item;
	}
	
	//
	// EventHandler Implementation
	//
	@Override
	public String getEventHandlerName() {
		return "AssetStripAutoAssociation";
	}
	@Override
	public int getPriority() {
		return Priority.HIGH;
	}

	/*
	 * Return the logger.
	 * @see com.raritan.tdz.assetstrip.home.AssetAutoAssociationBase#getLogger()
	 */
	@Override
	Logger getLogger() {
		return log;
	}
	
	/*
	 * Handle an asset strip event where we can't find the associated cabinet or PDU item.
	 * @see com.raritan.tdz.assetstrip.home.AssetAutoAssociationBase#logUnknown(com.raritan.tdz.domain.CabinetItem, com.raritan.tdz.domain.Item)
	 */
	@Override
	void handleUnknownAssociation(CabinetItem cabinet, Item item) {
		// Build the summary
		buildAssetStripEventSummary(cabinet, item);
		// Make this event a WARNING
		dctEvent.get().setSeverity(sessionFactory.getCurrentSession(), EventSeverity.WARNING);
	}

	@Override
	public List<Long> createAssetStripEvents(Item item) throws RemoteDataAccessException {
		return createAssetStripEvents(item, true, true);
	}
	
	@Override
	public List<Long> createAssetStripConnectedEvents(Item item) throws RemoteDataAccessException {
		return createAssetStripEvents(item, true, false);
	}
	
	@Override
	public List<Long> createAssetStripDisconnectedEvents(Item item) throws RemoteDataAccessException {
		return createAssetStripEvents(item, false, true);
	}
	
	//
	// Private methods
	//
	
	private List<Long> createAssetStripEvents(Item item, boolean connectEvents, boolean disconnectEvents) throws RemoteDataAccessException {
		if (!connectEvents && !disconnectEvents) {
			return Collections.emptyList();
		}
		
		if (item.getPiqId() == null) {
			log.info("createAssetStripAttachedEvent: PIQ ID is null");
			return Collections.emptyList();
		}
		
		List<Long> eventIds = new LinkedList<Long>();
		
		String piqId = item.getPiqId().toString();
		List<AssetStrip> assetStrips = piqAssetStripClient.getAssetStrips( piqId );
		
		if (assetStrips != null) {
			
			// If this is a dummy Rack PDU collecting for probe, get the real probe item
			Item probeItem = probeLookup.getProbeItemForDummyRackPDU( item.getItemId() );
			if (probeItem != null) {
				item = probeItem;
			}
			
			for (AssetStrip assetStrip : assetStrips) {
				
				if (assetStrip != null) {
					final String state = assetStrip.getState();
					Event ev = null;
					
					try {
						if (state != null && state.equals(AssetStrip.AVAILABLE_STATE)) {
							if (connectEvents) {
								ev = createAssetStripEvent(EventType.ASSET_STRIP_CONNECTED, item, assetStrip, piqId);
							}
						}
						else if (disconnectEvents) {
							ev = createAssetStripEvent(EventType.ASSET_STRIP_REMOVED, item, assetStrip, piqId);
						}
						
						if (ev != null) {
							eventHome.saveEvent( ev );
							eventIds.add( ev.getId() );
						}
					}
					catch (DataAccessException e) {
						log.error("", e);
					}
				}
			}
		}
		
		return eventIds;
	}
	
	
	/**
	 * Associate the specified item with an asset strip.
	 * @param cabinet the cabinet associated with the asset strip
	 * @param item the item connected to the asset strip (PDU or EMX)
	 * @return true if a new sensor port was created, false if an asset strip sensor port already exists for the PDU.
	 */
	private boolean associateItemWithStrip(CabinetItem cabinet, Item item) {
		boolean createdNewPort = false;
		
		if (item != null && sessionFactory != null) {
			// Search for an existing sensor port on the PDU item
			SensorPort assetStripSensor = findAssetStripSensorPort( item );
			if (assetStripSensor == null) {
				createAssetStripSensorPort( cabinet, item );
				createdNewPort = true;
			}
			else {
				if (log.isInfoEnabled()) {
					log.info("Detected an existing asset strip sensor port on " + item.getItemName());
				}
				populateAssetStripSensorPort(assetStripSensor, cabinet, item);
				try {
					portHome.editSensorPort( assetStripSensor );
				} 
				catch (DataAccessException e) {
					log.error("", e);
				}
				createdNewPort = false;
			}
		}
		
		return createdNewPort;
	}
	
	
	/**
	 * Builds the event summary string for the asset strip event.
	 * @param dctEvent the event
	 * @param cabinet cabinet associated with the asset strip
	 * @param item the PDU or EMX item associated with the asset strip
	 */
	private void buildAssetStripEventSummary(CabinetItem cabinet, Item item) {
		Event event = dctEvent.get();
		MessageSource messageSource = eventHome.getMessageSource();
		
		event.setSummary( messageSource.getMessage( 
				event.getType().getLkpValueCode() == SystemLookup.EventType.ASSET_STRIP_CONNECTED ?
						"assetEvent.stripConnected" : "assetEvent.stripDisconnected",
				new Object[] { 
						getAssetParam(AssetEventParam.ASSET_STRIP_NUMBER),
						getItemName(item, messageSource.getMessage("assetEvent.unknownInfo", null, null)),
						getItemName(cabinet, messageSource.getMessage("assetEvent.unknownInfo", null, null)),
						getLocationCode(cabinet, messageSource.getMessage("assetEvent.unknownInfo", null, null))
					},
				null
			)
		);
		
		Session sess = sessionFactory.getCurrentSession();
		sess.merge( event );
	}
	
	/**
	 * Sets details of the asset strip event.
	 * @param cabinet the cabinet associated with the asset strip
	 * @param item the PDU connected to the asset strip
	 */
	private void setEventDetails(CabinetItem cabinet, Item item) {
		Event event = dctEvent.get();
		event.setCabinet( cabinet );
		event.setItem( item );
		event.setLocation(cabinet != null ? cabinet.getDataCenterLocation() : null);
		
		// Add event details
		// event.addParam( ITEM_NAME_PARAM, item != null ? item.getItemName() : UNKNOWN_VALUE );
		
		if (cabinet != null) {
			ModelDetails model = cabinet.getModel();
			if (model != null) {
				event.addParam( RACK_UNITS, Integer.toString( model.getRuHeight() ) );
			}
		}
		
		if ((item != null) && (item.getClassLookup() != null) && (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)) {
			event.addParam( ITEM_CLASS_PARAM, item != null ? item.getClassLookup().getLkpValue() : UNKNOWN_VALUE );
			event.addParam( ITEM_STATUS_PARAM, item != null ? item.getStatusLookup().getLkpValue() : UNKNOWN_VALUE );
		}
		else {
			// Blank item class and no item status
			event.addParam( ITEM_CLASS_PARAM, EMPTY_VALUE );
		}
		
		Session sess = sessionFactory.getCurrentSession();
		sess.merge( event );
	}
	
	
	
	/**
	 * Creates a new sensor port on the item representing the asset strip.
	 * @param item
	 * @param event
	 */
	private void createAssetStripSensorPort(CabinetItem cabinet, Item item) {
		Event event = dctEvent.get();
		
		SensorPort sensor = new SensorPort();
		sensor.setCreationDate( event.getCreatedAt() );
		populateAssetStripSensorPort(sensor, cabinet, item);
	
		// Save the new sensor port
		try {
			portHome.saveSensorPort( sensor );
		}
		catch (DataAccessException e) {
			log.error("Error saving asset strip sensor port", e);
		}
	}
	
	private void populateAssetStripSensorPort(SensorPort sensor, CabinetItem cabinet, Item item) {
		Session sess = sessionFactory.getCurrentSession();
		
		// automatically set to installed state
		sensor.setPortStatusLookup( SystemLookup.getLksData(sess, SystemLookup.ItemStatus.INSTALLED) );
		// automatically set to asset strip
		sensor.setPortSubClassLookup( SystemLookup.getLksData(sess, SystemLookup.PortSubClass.ASSET_STRIP) );
		
		// Only update port name if it has not already been specified
		if (!StringUtils.hasText(sensor.getPortName())) {
			sensor.setPortName( getSensorPortName() );
		}
		
		if (sensor.getItem() == null) {
			sensor.setItem( item );
		}
		if (sensor.getCabinetItem() == null) {
			sensor.setCabinetItem( cabinet );
		}

/* CR 50810: do not set Cabinet Location, default to null since we don't get this information via Power IQ
		if (sensor.getCabLocationLookup() == null) {
			// Default sensor location is 'Top Front'
			sensor.setCabLocationLookup( SystemLookup.getLksData(sess, SystemLookup.SensorLocation.TOP_FRONT) );
		}
*/
		// Default sensor value is RU height of associated cabinet
		if (cabinet != null) {
			ModelDetails model = cabinet.getModel();
			if (model != null) {
				sensor.setValueActual( model.getRuHeight() );
				sensor.setValueActualUnit( RACK_UNITS );
			}
		}
		
		// Use the asset strip number as the sort order - we use this to uniquely
		// identify a particular asset strip sensor port associated with a PDU or EMX.
		Integer assetStripNum = getAssetStripNumber();
		if (assetStripNum != null) {
			sensor.setSortOrder( assetStripNum );
		}
	}
	
	/**
	 * Creates a name for the new sensor port which is "<Asset Strip> <Asset Strip Number".
	 * @param event
	 * @return
	 */
	private String getSensorPortName() {
		StringBuffer b = new StringBuffer( ASSET_STRIP_PORT_NAME );
		b.append(" ");
		b.append( getAssetParam(AssetEventParam.ASSET_STRIP_NUMBER) );
		return b.toString();
	}
	
	/**
	 * 
	 * @param item
	 * @param assetStrip
	 * @param piqId
	 * @return
	 * @throws DataAccessException
	 */
	private Event createAssetStripEvent(EventType eventType, Item item, AssetStrip assetStrip, String piqId) throws DataAccessException {
		Event ev = eventHome.createEvent(eventType, EventSeverity.INFORMATIONAL, piqAssetStripClient.getEventSource());
		AssetEventParam.ASSET_STRIP_ID.addToEvent(ev, Long.toString(assetStrip.getId()) );
		AssetEventParam.PDU_ID.addToEvent(ev, piqId);
		AssetEventParam.ASSET_STRIP_NAME.addToEvent(ev, assetStrip.getName());
		AssetEventParam.ASSET_STRIP_NUMBER.addToEvent(ev, Integer.toString(assetStrip.getOrdinal()));
		AssetEventParam.PDU_NAME.addToEvent(ev, item.getItemName());
		return ev;
	}
}
