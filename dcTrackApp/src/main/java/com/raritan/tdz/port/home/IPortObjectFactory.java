package com.raritan.tdz.port.home;

import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;

public interface IPortObjectFactory {

	/**
	 * Registers an PortObject class for each port subclass lookup value code.
	 * @param portObjectBeans
	 */
	public void setPortClasses( Map<String, String> portObjectBeans);
	
	
	/**
	 * Creates an PortObject business wrapper for the specified port domain interface
	 * 
	 * @param item
	 * @return
	 */
	public IPortObject getPortObject(IPortInfo port, Errors errors);

}
