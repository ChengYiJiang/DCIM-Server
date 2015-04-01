/**
 * 
 */
package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;

import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

/**
 * @author prasanna
 *
 */
public class ItemNameFieldDomainAdaptor implements ValueIDFieldToDomainAdaptor{

	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
