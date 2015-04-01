package com.raritan.tdz.move.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class ClearWhenMovedItemMoveBehavior implements ItemSaveBehavior {

	@Autowired(required=true)
	protected PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	private ItemObjectFactory itemObjectTemplateFactory;
	
	public ItemObjectFactory getItemObjectTemplateFactory() {
		return itemObjectTemplateFactory;
	}

	public void setItemObjectTemplateFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectTemplateFactory = itemObjectFactory;
	}

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		
		if (null == item.getDeleteWhenMovedItem() || false == item.getDeleteWhenMovedItem()) return;

		Long whenMovedItemId = powerPortMoveDAO.getWhenMovedItemId(item.getItemId());
		if (null == whenMovedItemId) return;
		
		ItemObjectTemplate itemObject = null; 
		itemObject = itemObjectTemplateFactory.getItemObjectFromItemId(whenMovedItemId);
		if (itemObject != null) {
			try {
				itemObject.deleteItem(whenMovedItemId, false, sessionUser);
			} catch (BusinessValidationException be) {
	 			//This is to handle any warning and put the data into the exception
				throw be;
			} catch (Throwable e) {
				BusinessValidationException bve =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
				bve.addValidationError("Cannot delete the when moved item");
				throw bve;
			}
		}
		
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}

}
