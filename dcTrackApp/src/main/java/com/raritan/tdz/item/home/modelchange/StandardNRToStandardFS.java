package com.raritan.tdz.item.home.modelchange;

import org.apache.log4j.Logger;


import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author bunty
 *
 */

public class StandardNRToStandardFS implements ChangeModel {
	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardNRToStandardFS(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave) throws BusinessValidationException {
		log.debug("Changing Standard To StandardFS");

		//3 -Clear Shelf position
		changeModelDAO.clearShelfPosition(itemToSave, itemInDB);
		
		//11 -Create Phantom Cabinet
		changeModelDAO.createPhantomCabinet(itemToSave);
	}

	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
