/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import org.apache.log4j.Logger;


import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.modelchange.ChangeModelDAO.UPositionEnum;

/**
 * @author Santo Rosario
 *
 */
public class StackableToNetworkFS implements ChangeModel {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StackableToNetworkFS(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("Changing from Stackable To NetworkBlade");

		//1
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.KEEP_UPOSITION);

		// Clear Shelf Position
		changeModelDAO.clearShelfPosition(itemToSave, itemInDB);

		//11 -Create Phantom Cabinet
		changeModelDAO.createPhantomCabinet(itemToSave);
		
		//13
		changeModelDAO.clearSiblingId(itemToSave);
	}

	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
