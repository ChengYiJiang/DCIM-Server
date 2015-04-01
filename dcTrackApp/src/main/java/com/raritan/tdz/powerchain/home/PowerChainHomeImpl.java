package com.raritan.tdz.powerchain.home;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * power chain home implementation to support handling LN events and operations initiated by clients (RESTAPI and WEB GUI)
 * @author bunty
 *
 */
public class PowerChainHomeImpl implements PowerChainHome {

	/* LN event action and the corresponding handler */
	private Map<String, PowerChainActionHandler> powerChainActionHandler;
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;

	@Autowired
	private PowerChainUpdateAllPortsAndConnectionsPowerChainActionHandler powerChainUpdateAllActionHandler;
	
	private Logger log = Logger.getLogger(getClass());
	
	public PowerChainHomeImpl(
			Map<String, PowerChainActionHandler> powerChainActionHandler) {
		super();
		this.powerChainActionHandler = powerChainActionHandler;
	}

	/*public void setPowerChainActionHandler(
			Map<String, PowerChainActionHandler> powerChainActionHandler) {
		this.powerChainActionHandler = powerChainActionHandler;
	}*/

	@Override
	public void createPowerChainForExistingItems() throws BusinessValidationException {
		MapBindingResult errors = getErrorObject();
		boolean validateConn = false;
		boolean migrationInProgress = true;
		powerChainUpdateAllActionHandler.process(0, null, null, errors, validateConn, migrationInProgress);
		
	}
	
	@Override
	public void processLNEvent(LNEvent event) throws BusinessValidationException {
		MapBindingResult errors = getErrorObject();
		
		if (null == event || null == event.getOperationLks() || null == event.getOperationLks().getLkpValueCode() || null == event.getAction()) {
			noActionHandlerException(event, errors);
		}
		
		PowerChainActionHandler handler = powerChainActionHandler.get(event.getOperationLks().getLkpValueCode().toString() + ":" + event.getAction());
		if (null != handler) {
			boolean validateConn = false;
			boolean migrationInProgress = false;
			handler.process(event.getTableRowId(), event.getCustomField1(), event.getCustomField2(), errors, validateConn, migrationInProgress);
		}
		else {
			noActionHandlerException(event, errors);
		}
		// throwBusinessValidationException(errors);
	}
	
	private void noActionHandlerException(LNEvent event, Errors errors) throws BusinessValidationException {
		Object[] errorArgs = { event.toString() };
		errors.rejectValue("PowerChain", "PowerChain.noActionHandler", errorArgs, "No action handler defined for event " + event.toString());
	}
	
	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, MeItem.class.getName() );
		return errors;
		
	}
	
	@SuppressWarnings({ "unused", "deprecation" })
	private void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				log.error(msg);
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}

		//If we have validation errors for any of the DTO arguments, then throw that.
		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			// throw e;
		} else if (e.getValidationWarnings().size() > 0){
			// throw e;
		}
	}


}
