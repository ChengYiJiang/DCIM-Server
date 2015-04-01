package com.raritan.tdz.item.home.itemObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.dao.LNEventDAO;

public class PiqIntegratedItemsLocationChangeSaveBehavior implements
		ItemSaveBehavior {
	
	@Autowired
	LNEventDAO lnEventDao;
	
	@Autowired
	LocationDAO locationDao;
	
	@Autowired
	LksCache lksCache;

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {

		boolean isUpdate = (item != null && item.getItemId() > 0);
		
		if (isUpdate) {
			Item origItem = (isUpdate && SavedItemData.getCurrentItem() != null) ? 
								SavedItemData.getCurrentItem().getSavedItem() : null;
			
			String origPiqHost = getPiqHostByLocationId(origItem);
			String newPiqHost = getPiqHostByLocationId(item);

			// return when the item is not integrated with PIQ ( no piqId )
			if ( (origPiqHost == null && newPiqHost == null)) {
				return; 
			}
			
			if (origPiqHost == null && newPiqHost != null) {
				// moving item to a site that is integrated with PIQ
				
				// add item to new site (in piq)
				LksData opInsert = lksCache.getLksDataUsingLkpCode(SystemLookup.VBJavaBridgeOperations.INSERT);
				if (opInsert != null) { 
					lnEventDao.setLnEvent(opInsert.getLksId(), item.getItemId(), null, null, newPiqHost);
				}
				// reset piq id
				item.setPiqId(null);

			} else if (origPiqHost != null && newPiqHost != null && 
					!origPiqHost.equals(newPiqHost) ) {
				// moving item from one site to another which is integrated with piq
				
				// delete item from one site and add to another. (in piq)
				
				LksData opDelete = lksCache.getLksDataUsingLkpCode(SystemLookup.VBJavaBridgeOperations.DELETE);
				LksData opInsert = lksCache.getLksDataUsingLkpCode(SystemLookup.VBJavaBridgeOperations.INSERT);
				if (opDelete != null  && opInsert != null) {
					Long itemClassLksId = item.getClassLookup() != null ?  item.getClassLookup().getLksId() : null;
					if (itemClassLksId != null && origItem.getPiqId() != null) {
						// delete item from orig piqHost
						lnEventDao.setLnEvent(opDelete.getLksId(), origItem.getItemId(), itemClassLksId.toString(), origItem.getPiqId().toString(), origPiqHost);
					}
					// insert item into new piqHost
					lnEventDao.setLnEvent(opInsert.getLksId(), item.getItemId(), null, null, newPiqHost);
				}
				
				// reset piq id
				item.setPiqId(null);
			}
		}
	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private String getPiqHostByLocationId(Item item) {
		if (item != null ) {
			DataCenterLocationDetails loc = item.getDataCenterLocation();
			if (loc != null) {
				return locationDao.getPiqHostByLocationId(loc.getDataCenterLocationId());
			}
		}
		return null;
	}

}
