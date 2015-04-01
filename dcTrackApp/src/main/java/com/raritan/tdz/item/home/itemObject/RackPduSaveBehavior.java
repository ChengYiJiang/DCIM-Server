/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

/**
 * @author prasanna
 *
 */
public class RackPduSaveBehavior implements ItemSaveBehavior {
	
	@Autowired
	private SystemLookupFinderDAO systemLookupDao;
	
	@Autowired
	private PowerConnDAO powerConnDao;
	
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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		// boolean isUpdate = item != null && item.getItemId() > 0;
		
		// TODO: this is for 3.0 for newly created item.
		// If user creates new power ports for existing item, it should be considered
//		if (isUpdate) return;
//
//		if( item.getPowerPorts() != null ){
//			List<PowerPort> ppList = new ArrayList<PowerPort>(item.getPowerPorts());
//			for (PowerPort pp: ppList) {
//				if (null != pp.getInputCordPort()
//						/* how else we will know that this is new port. Simple check the PowerConnection*/
//						/*&& null == pp.getCreationDate()*/) {
//					PowerConnection connection = new PowerConnection(pp, pp.getInputCordPort(),
//							0, null, systemLookupDao.findByLkpValueCode(SystemLookup.LinkType.IMPLICIT).get(0)
//							, 2);
//					connection.setStatusLookup(systemLookupDao.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0));
//					connection.setCreatedBy(sessionUser.getUserName());
//					connection.setCreationDate(pp.getCreationDate());
//					connection.setUpdateDate(pp.getUpdateDate());
//					powerConnDao.merge(connection);
//				}
//			}
//		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#canSupportDomain(java.lang.String[])
	 */
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(ItItem.class.getName()) || name.equals(MeItem.class.getName())) {
				canSupport = true;
			}
		}
		return canSupport;
	}

}
