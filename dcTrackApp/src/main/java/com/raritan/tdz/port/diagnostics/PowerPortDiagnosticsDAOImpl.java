package com.raritan.tdz.port.diagnostics;

import java.util.List;
import java.util.Locale;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.diagnostics.domain.PortDiagnostics;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;

@Transactional
public class PowerPortDiagnosticsDAOImpl extends DaoImpl<PortDiagnostics> implements PowerPortDiagnosticsDAO {

	final Integer MAX_DIAG_MSG_SIZE = 200;
	
	private ResourceBundleMessageSource messageSource;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void reportError(PowerPort port, Errors errors, String logLevel) {
		
		if (errors.hasErrors()){
			Long portId = port.getPortId();
			String portName = port.getPortName();
			Item item = port.getItem();
			Long itemId = item.getItemId();
			String itemName = item.getItemName();
			String location = (null != item.getDataCenterLocation() && null != item.getDataCenterLocation().getCode()) ? item.getDataCenterLocation().getCode() : "";

			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
  				PortDiagnostics pd = new PortDiagnostics();
  				pd.setItemId(itemId);
  				pd.setItemName(itemName);
  				pd.setPortPowerId(portId);
  				pd.setPortName(portName);
  				pd.setPortSubClass(port.getPortSubClassLookup().getLkpValue());
  				String diagnosisMsg = logLevel + ":" + msg;
  				diagnosisMsg = (diagnosisMsg.length() > MAX_DIAG_MSG_SIZE) ? diagnosisMsg.substring(0, MAX_DIAG_MSG_SIZE - 1) : diagnosisMsg;
  				pd.setDiagnosisMsg(diagnosisMsg);
  				pd.setLocationCode(location);
  				
  				create(pd);
			}
		}

	}

	@Override
	public void reportError(Item item, Errors errors, String logLevel) {
    	
		if (errors.hasErrors()){
			Long itemId = item.getItemId();
			String itemName = item.getItemName();
			String location = (null != item.getDataCenterLocation() && null != item.getDataCenterLocation().getCode()) ? item.getDataCenterLocation().getCode() : "";

			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
  				PortDiagnostics pd = new PortDiagnostics();
  				pd.setItemId(itemId);
  				pd.setItemName(itemName);
  				pd.setDiagnosisMsg(logLevel + ":" + msg);
  				pd.setLocationCode(location);
  				
  				create(pd);
			}
		}
	}

}
