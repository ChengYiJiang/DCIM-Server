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
public class StandardFSToVM  extends StandardFSTOAny {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardFSToVM(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModel#change(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		// TODO Auto-generated method stub
		log.debug("StandardFS To VM");

		//2
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.CLEAR_UPOSITION);
		
		//4
		changeModelDAO.deletePowerPorts(itemToSave);
		
		//5
		changeModelDAO.changeDPSubclassToVirtual(itemToSave);
		
		//7
		changeModelDAO.deleteDataStore(itemToSave);
		
		//When calling this function from createVmItem(), itemToSave is a new clone of itemInDB, 
		//cannot delete the cabinet in this case
		if(itemInDB.getItemId() == itemToSave.getItemId()){
			//12
			changeModelDAO.deletePhantomCabinet(itemInDB);
			//15
			changeModelDAO.clearCabinetId(itemToSave);
		}
		
		//17
		changeModelDAO.clearModelId(itemToSave);		
	}
}
