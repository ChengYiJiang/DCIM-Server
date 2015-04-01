package com.raritan.tdz.port.home;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
// import org.apache.log4j.Logger;

public class PortObjectFactory implements ApplicationContextAware, 	IPortObjectFactory {

	// private final Logger log = Logger.getLogger( this.getClass() );
	
	private Map<String, String> portObjectBeans;
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setPortClasses(Map<String, String> portObjectBeans) {
		this.portObjectBeans = portObjectBeans;
	}

	@Override
	public IPortObject getPortObject(IPortInfo port, Errors errors) {
		if (null == port || null == portObjectBeans)  {
			return null;
		}
		if (null == port.getPortSubClassLookup() || null == port.getPortSubClassLookup().getLkpValueCode()) {
			Object[] errorArgs = { "'Port Type'" };
			errors.rejectValue("Ports", "PortValidator.dataPortFieldRequired", errorArgs, "Port required fields not provided");
			return null;
		}
		String portSubCls = port.getPortSubClassLookup().getLkpValueCode().toString();
		if (null == portSubCls) {
			Object[] errorArgs = { "'Port Type'" };
			errors.rejectValue("Ports", "PortValidator.dataPortFieldRequired", errorArgs, "Port required fields not provided");
			return null;
		}
		String portBean = portObjectBeans.get(portSubCls);
		
		return createPortObject(portBean, port, errors);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}
	
	private IPortObject createPortObject(String portSubCls, IPortInfo port, Errors errors) {
		IPortObject portObj = (IPortObject) applicationContext.getBean(portSubCls);
		portObj.init(port, errors);
		return portObj;
	}


}
