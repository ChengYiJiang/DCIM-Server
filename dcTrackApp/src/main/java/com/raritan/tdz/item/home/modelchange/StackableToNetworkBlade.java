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
public class StackableToNetworkBlade implements ChangeModel {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StackableToNetworkBlade(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("Changing from Stackable To NetworkBlade");

		//2
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.CLEAR_UPOSITION);
		
		// Clear Shelf Position
		changeModelDAO.clearShelfPosition(itemToSave, itemInDB);

		//4
		changeModelDAO.deletePowerPorts(itemToSave);
		
		//13
		changeModelDAO.clearSiblingId(itemToSave);
	}

	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
