package com.raritan.tdz.port.diagnostics;

import java.util.List;
import java.util.Locale;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.diagnostics.domain.CircuitDiagnostics;
import com.raritan.tdz.domain.PowerConnection;

public class CircuitDiagnosticsDAOImpl extends DaoImpl<CircuitDiagnostics>  implements CircuitDiagnosticsDAO {

	private ResourceBundleMessageSource messageSource;
	
	final Integer MAX_DIAG_MSG_SIZE = 200;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void reportError(PowerConnection conn, Errors errors, String defaultLogLevel, List<String> informationErrorCode) {

		if (errors.hasErrors()){
			
			String srcItemName = conn.getSourcePowerPort().getItem().getItemName();
			String dstItemName = conn.getDestPowerPort().getItem().getItemName();
			String srcPortName = conn.getSourcePowerPort().getPortName();
			String dstPortName = conn.getDestPowerPort().getPortName();
			
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
  				CircuitDiagnostics cd = new CircuitDiagnostics();
  				cd.setFirstNodeItemName(srcItemName);
  				cd.setFirstNodePortName(srcPortName);
  				cd.setLastNodeItemName(dstItemName);
  				cd.setLastNodePortName(dstPortName);
  				cd.setConnectionPowerId(conn.getConnectionId());
  				String logLevel = defaultLogLevel; 
  				if (null != informationErrorCode && informationErrorCode.contains(error.getCode())) {
  					logLevel = "INFORMATION";
  				}
  				String diagnosisMsg = logLevel + ":" + msg;
  				diagnosisMsg = (diagnosisMsg.length() > MAX_DIAG_MSG_SIZE) ? diagnosisMsg.substring(0, MAX_DIAG_MSG_SIZE -1) : diagnosisMsg;
  				cd.setDiagnosisMsg(diagnosisMsg);

  				
  				create(cd);
			}
		}


	}

}
