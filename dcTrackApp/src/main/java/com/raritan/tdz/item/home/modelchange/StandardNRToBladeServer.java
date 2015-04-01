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
public class StandardNRToBladeServer implements ChangeModel {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardNRToBladeServer(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("Changing Standard To BladeServer");
		
		//2
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.CLEAR_UPOSITION);
		
		//3 -Clear Shelf position
		changeModelDAO.clearShelfPosition(itemToSave, itemInDB);

		//4
		changeModelDAO.deletePowerPorts(itemToSave);
	}
	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
