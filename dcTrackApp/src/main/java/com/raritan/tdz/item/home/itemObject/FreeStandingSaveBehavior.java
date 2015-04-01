/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.lang.Long;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.floormaps.home.CadHome;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Brian Wang
 *
 */
public class FreeStandingSaveBehavior implements ItemSaveBehavior {
	
	@Autowired
	private CadHome cadHome;
	
	@Autowired
	protected ItemDomainFactory itemDomainFactory;
	
	HashMap<Long,Long> locationMap = new HashMap<Long,Long>();
	
	private Logger log = Logger.getLogger(FreeStandingSaveBehavior.class);
	
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preValidateUpdate(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to do at this time.
		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preSave(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException {

		Item olditem = itemDomainFactory.findItem(new Long(item.getItemId()));
		DataCenterLocationDetails locationDetail = null;
		if (olditem != null)
			locationDetail = olditem.getDataCenterLocation();

		if (locationDetail != null) {
			log.info("Old locationid = " + locationDetail.getDataCenterLocationId());
			locationMap.put(new Long(item.getItemId()), locationDetail.getDataCenterLocationId());
		} else {
			log.info("Old locationid = null");
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.UserInfo, java.lang.Object[])
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		log.debug("postSave start");
		if (item != null && item.getClassLookup() != null) {
			
			Long key = new Long(item.getItemId());
			Long oldLocationId = locationMap.get(key);
			locationMap.remove(key);
			
			Long newLocationId = null;
			
			DataCenterLocationDetails locationDetail = item.getDataCenterLocation();
			if (locationDetail != null) {
				newLocationId = locationDetail.getDataCenterLocationId();
				log.info("New locationid = " + newLocationId);
			}
			
			Boolean isSiteChanged = false;

			if (oldLocationId != newLocationId) {
				log.info("Location is changed");
				isSiteChanged = true;
			}
			
			Boolean isSynced = true;
			if (item.getCadHandle() == null || item.getCadHandle().length() == 0) {
				isSynced = false;
				log.info("item is not synced");
			}
			
			if (isSiteChanged == true || isSynced == false) {
				// Call the sync only when the site is changed or this item is not synced.
				cadHome.syncCadHandleByItem(item);
			} else {
				log.info("No necessary to sync item");
			}
		}
		log.debug("postSave end");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#canSupportDomain(java.lang.String[])
	 */
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// Return true, so it will be processed	
		return true;
	}

}
