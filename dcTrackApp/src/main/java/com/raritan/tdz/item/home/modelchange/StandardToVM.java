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
public class StandardToVM implements ChangeModel {

	ChangeModelDAO changeModelDAO;
	Logger log = Logger.getLogger(ChangeModel.class);
	
	public StandardToVM(ChangeModelDAO changeModelDAO) {
		this.changeModelDAO = changeModelDAO;
	}
	
	@Override
	public void change(Item itemInDB, Item itemToSave)
			throws BusinessValidationException {
		log.debug("Standard To VM");
		
		//2
		changeModelDAO.updateUPosition(itemInDB, itemToSave, UPositionEnum.CLEAR_UPOSITION);
		
		//4
		changeModelDAO.deletePowerPorts(itemToSave);
		
		//5
		changeModelDAO.changeDPSubclassToVirtual(itemToSave);
		
		//7
		changeModelDAO.deleteDataStore(itemToSave);
		
		//15
		changeModelDAO.clearCabinetId(itemToSave);
		
		//17
		changeModelDAO.clearModelId(itemToSave);
	}

	@Override
	public void init(Item itemInDB, Item itemToSave, Object additionalParams) {
		// TODO Auto-generated method stub
		
	}

}
