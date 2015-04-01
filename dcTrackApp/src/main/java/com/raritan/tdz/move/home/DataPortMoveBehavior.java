package com.raritan.tdz.move.home;

import java.lang.reflect.InvocationTargetException;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * 
 * @author bunty
 *
 */
public class DataPortMoveBehavior implements ItemSaveBehavior {

	private PortMoveBehaviorHelper<DataPort> portMoveBehaviorHelper;
	
	public PortMoveBehaviorHelper<DataPort> getPortMoveBehaviorHelper() {
		return portMoveBehaviorHelper;
	}

	public void setPortMoveBehaviorHelper(
			PortMoveBehaviorHelper<DataPort> portMoveBehaviorHelper) {
		this.portMoveBehaviorHelper = portMoveBehaviorHelper;
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

	@SuppressWarnings("deprecation")
	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		try {
			portMoveBehaviorHelper.postSave(item, sessionUser, additionalArgs);
			
		} catch (SecurityException | NoSuchMethodException
				| IllegalArgumentException | IllegalAccessException
				| InvocationTargetException | ClassNotFoundException
				| InstantiationException e) {
			
			BusinessValidationException bve =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			bve.addValidationError(e.getMessage());
			// bve.addValidationError(error.getCode(), e.getMessage());
			e.printStackTrace();
			
			throw bve;
		}

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return true;
	}

}
