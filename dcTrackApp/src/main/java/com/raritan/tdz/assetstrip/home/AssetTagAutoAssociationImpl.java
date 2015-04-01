/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.home.EventHandlerDelegate;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQProbeLookup;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


/**
 * @author prasanna
 *
 */
public class AssetTagAutoAssociationImpl extends AssetAutoAssociationBase implements AssetTagAutoAssociation {

	private Logger log = Logger.getLogger( this.getClass() );
	
	private AssetStripLEDControl ledControl = null;
	private Boolean assetTagRemovedConflict = false;
	
	/**
	 * The set of valid tag lookups.
	 */
	public enum AssetTagStatus  {
		ASSET_AUTHORIZED {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_AUTHORIZED; }
		},
		ASSET_UNKNOWN {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_UNKNOWN; }
		},
		ASSET_UNAUTHORIZED {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_UNAUTHORIZED; }
		},
		ASSET_CONFLICT_ASSET_TAG {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_CONFLICT; }
		},
		ASSET_CONFLICT_UPOSITION {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_CONFLICT; }
		},
		ASSET_REMOVED {
			public long valueCode() { return SystemLookup.AssetTagStatus.ASSET_REMOVED; }
		};
		public abstract long valueCode();
	}
	
	//-- public methods/constructor.
	public AssetTagAutoAssociationImpl(SessionFactory sessionFactory, 
			ItemHome itemHome, 
			PortHome portHome,
			AssetStripLEDControl ledControl,
			EventHandlerDelegate eventHandlerDelegate,
			PIQProbeLookup probeLookup) {
		super( sessionFactory, itemHome, portHome, probeLookup );
		this.ledControl = ledControl;
		eventHandlerDelegate.registerEventHandler( this );
	}

	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	
	//-- Override methods --
	
	public AssetStripLEDControl getLedControl() {
		return ledControl;
	}

	public void setLedControl(AssetStripLEDControl ledControl) {
		this.ledControl = ledControl;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.getType().getLkpValueCode() == SystemLookup.EventType.ASSET_TAG_CONNECTED ) {
			addAssociation( event );
		}
		else if (event.getType().getLkpValueCode() == SystemLookup.EventType.ASSET_TAG_REMOVED)
		{
			removeAssociation( event );
		}
	}

	@Override
	public Item addAssociation(Event event) {
		dctEvent.set( event );
		
		//First look for the cabinet associated with the item.  Event result is updated in event
		CabinetItem cabinet = findCabinetForAssetStrip();
		
		//If we found one, find the item at the u-position provided in event.  Event result is updated in event
		Item assetItem = findItemAtUPosition(cabinet);
		
		//Once we find the item, go ahead and associate. Event result is updated in event
		associateItemWithTag(assetItem, cabinet);
		
		dctEvent.set( null );
		
		return assetItem;
	}

	@Override
	public Item removeAssociation(Event event) {
		dctEvent.set( event );
		
		CabinetItem cabinet = null;
		Item assetItem = null;
		
		//First look for the cabinet associated with the item.  Event result is updated in event
		cabinet = findCabinetForAssetStrip();
		
		//If we found one, find the item at the u-position provided in event.  Event result is updated in event
		assetItem = findItemAtUPosition(cabinet);
		
		//Once we find the item, go ahead and un-associate. Event result is updated in event
		removeAssociationWithItem(assetItem, cabinet);
		
		dctEvent.set( null );
		
		return assetItem;
	}
	
	//
	// EventHandler Implementation
	//
	@Override
	public String getEventHandlerName() {
		return "AssetTagAutoAssociation";
	}
	@Override
	public int getPriority() {
		return Priority.HIGH;
	}

	@Override
	Logger getLogger() {
		return log;
	}
	
	/*
	 * Handles an asset tag event where we can't find the associated cabinet or item.
	 * @see com.raritan.tdz.assetstrip.home.AssetAutoAssociationBase#handleUnknownAssociation(com.raritan.tdz.domain.CabinetItem, com.raritan.tdz.domain.Item)
	 */
	@Override
	void handleUnknownAssociation(CabinetItem cabinet, Item item) throws HibernateException {
		if (isAttachEvent())
			logAssetEvent(item, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_UNKNOWN);
		else
			logAssetEvent(item, cabinet, EventSeverity.INFORMATIONAL, AssetTagStatus.ASSET_REMOVED);
	}

	
	//=== All private methods go here ===
	
	/**
	 * Returns an item found in the u-position on the given cabinet.
	 * @param cabinet
	 * @return
	 * @throws HibernateException
	 */
	private Item findItemAtUPosition(CabinetItem cabinet) throws HibernateException {
		
		String uPosStr = getAssetParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER);
		Item assetItem = null;
		if (uPosStr != null)
		{
			try {
				assetItem = itemHome.findItemAtUPosition(cabinet, new Integer(uPosStr));
			} 
			catch (NumberFormatException e) {
				handleUnknownAssociation(cabinet, null);
				e.printStackTrace();
			} 
			catch (DataAccessException e) {
				ApplicationCodesEnum applCode = (ApplicationCodesEnum) e.getExceptionContext().getExceptionItem(ExceptionContext.APPLICATIONCODEENUM);
				
				//We handle the application codes returned by FindItemAtUPosition
				switch (applCode){
				case UPOSITION_OCCUPIED_NOT_BOTTOM_OF_ITEM:
					if (isAttachEvent())
						logAssetEvent(null, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_CONFLICT_UPOSITION);
					else{
						assetTagRemovedConflict = true;
						logAssetEvent(null, cabinet, EventSeverity.INFORMATIONAL, AssetTagStatus.ASSET_REMOVED);
					}
					break;
				case ITEM_DOES_NOT_EXIST_FOR_UPOSITION:
					handleUnknownAssociation(cabinet, null);
					break;
				default:
					handleUnknownAssociation(cabinet, null);
					break;
				}
				
				e.printStackTrace();
			}
			catch (IllegalArgumentException e) {
				handleUnknownAssociation(cabinet, null);
				e.printStackTrace();
			}
		}
		return assetItem;
	}

	
	/**
	 * Associate the tag with the assetItem found at the U-Position. 
	 * @param assetItem
	 * @param cabinet 
	 * @param session
	 * @throws HibernateException
	 */
	private void associateItemWithTag(Item assetItem, CabinetItem cabinet)
			throws HibernateException {

		if (assetItem != null && sessionFactory != null){
			
			String assetTagId = getAssetParam(AssetEventParam.ASSET_TAG_ID);
			
			//If no asset tag is attached, assign the assetNumber
			if (assetItem.getRaritanAssetTag() == null){
					assignAssetTag(assetItem, assetTagId);
			}
			else {  //Item has tag 
					if (assetItem.getRaritanAssetTag().equals(assetTagId)){ // If there is asset tag and matches with what was provided in the event
						//
						// Set the is asset tag verified to true. Since the asset tag in item and asset tag we received via event are
						// the same, it does not harm to assign the asset tag as well. Also, note that, even if asset tag is the same,
						// we still need to check for the isInstalled, else it is unauthorized.
						//
						assignAssetTag(assetItem, assetTagId);
						
					}else { // If there is asset tag and is not the same provided by event, then flag as conflict.
						logAssetEvent(assetItem, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_CONFLICT_ASSET_TAG);
					}
			}
		}
	}
	
	

	/**
	 * Copy the asset tag into the item.
	 * @param assetItem
	 * @param assetTag
	 */
	private void assignAssetTag(Item assetItem, String assetTag)
			throws HibernateException {
		
		CabinetItem cabinet = itemHome.getCabinet(assetItem);
		
		//Make sure the item is installed, then copy the asset tag
		if (itemHome.isItemInstalled(assetItem) && sessionFactory != null){
			assetItem.setRaritanAssetTag(assetTag);
			assetItem.setIsAssetTagVerified(true);
			Session session = sessionFactory.getCurrentSession();
			session.merge(assetItem);
			logAssetEvent(assetItem, cabinet, EventSeverity.INFORMATIONAL, AssetTagStatus.ASSET_AUTHORIZED);
		} else { //Else log unauthorized.
			logAssetEvent(assetItem, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_UNAUTHORIZED);
		}
	}
	
	
	/**
	 * Remove the tag with the assetItem found at the U-Position. 
	 * @param assetItem
	 * @param cabinet 
	 * @param session
	 * @throws HibernateException
	 */
	private void removeAssociationWithItem(Item assetItem, CabinetItem cabinet)
			throws HibernateException {

		if (assetItem != null && sessionFactory != null){
			
			String assetNumber = getAssetParam(AssetEventParam.ASSET_TAG_ID);
			
			//If no asset tag is attached, assign the assetNumber
			if (assetItem.getRaritanAssetTag() == null && assetItem.getIsAssetTagVerified() == false){
				logAssetEvent(assetItem, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_REMOVED);
			}
			else {  //Item has tag 
					if (assetItem.getRaritanAssetTag().equals(assetNumber)){ // If there is asset tag and matches with what was provided in the event
						//
						// Set the is asset tag verified to true. Since the asset tag in item and asset tag we received via event are
						// the same, it does not harm to assign the asset tag as well. Also, note that, even if asset tag is the same,
						// we still need to check for the isInstalled, else it is unauthorized.
						//
						clearAssetTag(assetItem);
						
					}else { // If there is asset tag and is not the same provided by event, then flag as conflict.
						logAssetEvent(assetItem, cabinet, EventSeverity.WARNING, AssetTagStatus.ASSET_REMOVED);
					}
			}
		}
	}

	/**
	 * Copy the asset tag into the item.
	 * @param assetItem
	 */
	private void clearAssetTag(Item assetItem)
			throws HibernateException {
		
		CabinetItem cabinet = itemHome.getCabinet(assetItem);

		if (sessionFactory != null){
			// According to Email from Victor Bartash: Jul 29, 2011, at 11:24 AM, we should not clear the asset tag since
			// they are glued to the item and when the item is discarded, the tag will go with it.
			//Clear the tag.
			//assetItem.setRaritanAssetTag(null);
			//assetItem.setIsAssetTagVerified(false);
			//Session session = sessionFactory.getCurrentSession();
			//session.merge(assetItem);
			logAssetEvent(assetItem, cabinet, EventSeverity.INFORMATIONAL, AssetTagStatus.ASSET_REMOVED);
		}
		
	}

	
	

	/**
	 * Log the Asset Event and also generate the appropriate summary.
	 * @param associatedItem
	 * @param cabinet
	 * @param severity
	 * @param statusLks
	 */
	private void logAssetEvent(Item associatedItem, CabinetItem cabinet, EventSeverity severity, AssetTagStatus statusLks)
		throws HibernateException {
		StringBuffer summary = new StringBuffer();
		
		
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			
			//Log Severity
			setSeverity( severity );
			
			//Log Summary

			switch (statusLks){
			case ASSET_AUTHORIZED:
				initSummary(cabinet, summary);
				getItemNameSummary(associatedItem, summary);
				getItemStatusSummary(associatedItem, summary);
				break;
			case ASSET_UNAUTHORIZED:
				initSummary(cabinet, summary);
				summary.append(" attached to Item " + getItemName(associatedItem, ""));
				summary.append(" in state  " + getItemStatus(associatedItem));
				summary.append(" was not authorized");
				break;
			case ASSET_CONFLICT_ASSET_TAG:
				initSummary(cabinet, summary);
				getItemNameSummary(associatedItem, summary);
				getItemStatusSummary(associatedItem, summary);
				summary.append(" conflicts with existing tag ");
				summary.append(getRaritanAssetTag(associatedItem));
				break;
			case ASSET_CONFLICT_UPOSITION:
				initSummary(cabinet, summary);
				summary.append(" conflicts with an Item");
				break;
			case ASSET_UNKNOWN:
				initSummary(cabinet, summary);
				summary.append(" does not match to an item");
				break;
			case ASSET_REMOVED:
				summary.append("Asset Tag ");
				summary.append( getAssetParam(AssetEventParam.ASSET_TAG_ID) );
				summary.append(" removed at RU ");
				summary.append( getAssetParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER) );
				getCabinetNameSummary(cabinet, summary);
				getLocationCodeSummary(cabinet, summary);
				if (!assetTagRemovedConflict){
					getItemNameSummary(associatedItem, summary);
					getItemStatusSummary(associatedItem, summary);
				}else {
					summary.append(" attached to item but not in the proper RU");
				}
			default:
				break;
			}
			
			Event event = dctEvent.get();
			event.setSummary(summary.toString());
			
			//Include Cabinet Name (if any), Associated item (if any) and dcLocation (if any) to event
			event.setCabinet(cabinet);
			event.setItem(associatedItem);
			event.setLocation(cabinet != null ? cabinet.getDataCenterLocation() : null);
			
			//Add event details
			// FIXME: Use EventHome.addItemEventParams()
			event.addParam( CABINET_PARAM, cabinet != null ? cabinet.getItemName(): UNKNOWN_VALUE );
			event.addParam( ITEM_NAME_PARAM, associatedItem != null ? associatedItem.getItemName() : UNKNOWN_VALUE );
			event.addParam( ITEM_CLASS_PARAM, associatedItem != null ? associatedItem.getClassLookup().getLkpValue() : UNKNOWN_VALUE );
			event.addParam( ITEM_STATUS_PARAM, associatedItem != null ? associatedItem.getStatusLookup().getLkpValue() : UNKNOWN_VALUE );
			DataCenterLocationDetails location = event.getLocation();
			event.addParam( LOCATION_PARAM, location != null ? location.getDcName() : UNKNOWN_VALUE);
			
			//Log Asset Tag Status
			event.setProcessedStatus(SystemLookup.getLksData(session, statusLks.valueCode()));
			
			//Save the event
			session.merge(event);
			
			//Set LED
			setLED(statusLks);
		}
		
	}

	



	
	
	//-- Helper methods. --
	
	/**
	 * @param cabinet
	 * @param summary
	 */
	private void initSummary(CabinetItem cabinet, StringBuffer summary) {
		summary.append("Asset Tag ");
		summary.append( getAssetParam(AssetEventParam.ASSET_TAG_ID) );
		summary.append(" connected at RU ");
		summary.append( getAssetParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER) );
		
		getCabinetNameSummary(cabinet, summary);

		getLocationCodeSummary(cabinet, summary);
	}

	/**
	 * @param associatedItem
	 * @param summary
	 */
	private void getItemStatusSummary(Item associatedItem, StringBuffer summary) {
		if (associatedItem != null){
			summary.append(" in state ");
			summary.append(getItemStatus(associatedItem));
		}
	}

	/**
	 * @param associatedItem
	 * @param summary
	 */
	private void getItemNameSummary(Item associatedItem, StringBuffer summary) {
		if (associatedItem == null 
				&& dctEvent.get().getType().getLkpValueCode() == SystemLookup.EventType.ASSET_TAG_REMOVED){
			
			summary.append(" does not match to an item");
			
		} else if ( associatedItem != null ) {
			summary.append(" attached to Item ");
			summary.append(getItemName(associatedItem, ""));			
		}
	}
	
	private String getItemStatus(Item associatedItem) {
		return associatedItem != null ? associatedItem.getStatusLookup().getLkpValue() : "";
	}

	private String getRaritanAssetTag(Item associatedItem){
		return associatedItem != null ? associatedItem.getRaritanAssetTag() : "";
	}
	
	/**
	 * @param cabinet
	 * @param summary
	 */
	private void getCabinetNameSummary(CabinetItem cabinet, StringBuffer summary) {
		if (cabinet != null){
			summary.append(" cabinet ");
			summary.append(getItemName(cabinet, ""));
		}
	}
	
	/**
	 * @param cabinet
	 * @param summary
	 */
	private void getLocationCodeSummary(CabinetItem cabinet,
			StringBuffer summary) {
		if (cabinet != null)
		{
			summary.append(" site ");
			summary.append(getLocationCode(cabinet, ""));
		}
	}

	private Boolean isAttachEvent(){
		Boolean isAttach = false;
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			LksData attachEvent = SystemLookup.getLksData(session, Event.EventType.ASSET_TAG_CONNECTED.valueCode());
			isAttach = (dctEvent.get().getType().equals(attachEvent));
		}
		return isAttach;
	}
	
	/**
	 * @param statusLks
	 */
	private void setLED(AssetTagStatus statusLks) {
		//LED Control
		String rackUnitId = getAssetParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER);
		if (isAttachEvent())
			ledControl.setLED(rackUnitId, statusLks);
		else
			ledControl.turnOffLED( rackUnitId );
	}
}
