package com.raritan.tdz.item.snmp.home;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.item.snmp.dao.ItemSNMPDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * item's snmp behavior 
 * @author bunty
 *
 */
public class ItemSNMPSaveBehavior implements ItemSaveBehavior {

	@Autowired
	private ItemSNMPDAO itemSNMPDAO; 
	
	// Class that supports snmp
	final List<Long> supportedClass = Arrays.asList( 
			SystemLookup.Class.CRAC, 
			SystemLookup.Class.CRAC_GROUP, 
			SystemLookup.Class.DEVICE, 
			SystemLookup.Class.FLOOR_PDU, 
			SystemLookup.Class.NETWORK, 
			SystemLookup.Class.PROBE, 
			SystemLookup.Class.RACK_PDU,
			SystemLookup.Class.UPS, 
			SystemLookup.Class.UPS_BANK);

	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		
		if (item == null) return;
		
		ItemSNMP itemSnmp = item.getItemSnmp();
		if (null == itemSnmp) return;

		// check supported class
		Long itemClass = item.getClassLookup().getLkpValueCode();
		if (!supportedClass.contains(itemClass)) {
			item.setItemSnmp(null);
			return;
		}

		
		// V3 is disabled, clear all V3 related data
		if (null == itemSnmp.getSnmp3Enabled() || !itemSnmp.getSnmp3Enabled()) {
			itemSnmp.setSnmp3AuthLevel(null);
			itemSnmp.setSnmp3AuthPasskey(null);
			itemSnmp.setSnmp3AuthProtocol(null);
			itemSnmp.setSnmp3PrivPasskey(null);
			itemSnmp.setSnmp3PrivProtocol(null);
			itemSnmp.setSnmp3User(null);
			return;
		}
		
		// V3 is enabled, auth level 'noAuthNoPriv': clear all auth and privacy data
		if (null == itemSnmp.getSnmp3AuthLevel() || itemSnmp.getSnmp3AuthLevel().equals("noAuthNoPriv")) {
			itemSnmp.setSnmp3AuthPasskey(null);
			itemSnmp.setSnmp3AuthProtocol(null);
			itemSnmp.setSnmp3PrivPasskey(null);
			itemSnmp.setSnmp3PrivProtocol(null);
			return;
		}

		// V3 is enabled, auth level 'authNoPriv': clear all provacy data
		if (null == itemSnmp.getSnmp3AuthLevel() || itemSnmp.getSnmp3AuthLevel().equals("authNoPriv")) {
			itemSnmp.setSnmp3PrivPasskey(null);
			itemSnmp.setSnmp3PrivProtocol(null);
			return;
		}
		
	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {

		if (item.getItemId() > 0 && null != item.getItemSnmp()) {
			
			// this is the case when item exist and new snmp data is been added
			// make sure to save the snmp data before item save. The foreign key
			// mapping uses the existing item set
			if (null == item.getItemSnmp().getItemSNMPId()) {
				item.getItemSnmp().setItem(item);


				itemSNMPDAO.getSession().save(item.getItemSnmp());
				
			}
			// this is the case when item exist and snmp data also exist
			// the update on the item is good enough to save snmp data
			else {
				// nothing
			}
		}
		
		// for the third case when both item and snmp is new, save shall be called
		// before merge to save the snmp data with the item to update the foreign
		// key on the snmp. This case is been taken care during item save in itemDAO

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
		return true;
	}

}
