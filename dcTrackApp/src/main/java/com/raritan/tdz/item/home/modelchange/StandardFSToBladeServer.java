/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import org.apache.log4j.Logger;


import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.modelchange.ChangeModelDAO.UPositionEnum;

/**
 * @author prasanna
 *
 */
public class StandardFSToBladeServer  extends StandardFSTOAny {
	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardFSToBladeServer(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("Changing StandardFS To BladeServer");

		//2
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.CLEAR_UPOSITION);
		
		//4
		changeModelDAO.deletePowerPorts(itemToSave);
		
		//12
		changeModelDAO.deletePhantomCabinet(itemInDB);		
	}
}
