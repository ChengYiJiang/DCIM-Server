/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import org.apache.log4j.Logger;


import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.modelchange.ChangeModelDAO.UPositionEnum;

/**
 * @author bunty
 *
 */
public class StandardNRToBladeChassis implements ChangeModel {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardNRToBladeChassis(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("StandardNR To BladeChassis");
		
		//1
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.KEEP_UPOSITION);
		
		//3 -Clear Shelf position
		changeModelDAO.clearShelfPosition(itemToSave, itemInDB);
		
		//7
		changeModelDAO.deleteDataStore(itemToSave);
		
		//14
		changeModelDAO.clearDeviceConfigurationFields(itemToSave);
	}

	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
