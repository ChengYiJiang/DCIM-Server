package com.raritan.tdz.web.context;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class overrides the default spring application context. This allows
 * us to disable XSD validation in a production environment.
 * 
 * @author Andrew Cohen
 *
 */
public class XmlApplicationContext extends XmlWebApplicationContext {

	/*
	 * Override to disable XSD validation in production.
	 * @see org.springframework.context.support.AbstractXmlApplicationContext#initBeanDefinitionReader(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		if (!Boolean.parseBoolean(System.getProperty("dcTrack.developer"))) {
			// For production...
			beanDefinitionReader.setValidationMode( XmlBeanDefinitionReader.VALIDATION_NONE );
			beanDefinitionReader.setNamespaceAware( true );
		}
	}
}
