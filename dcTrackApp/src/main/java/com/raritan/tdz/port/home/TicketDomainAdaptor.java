package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

public class TicketDomainAdaptor implements ValueIDFieldToDomainAdaptor {

	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, DataAccessException {

		saveTicketForAnItem(dbObject, valueIdDTO);

		return null;
	}
	
	private void saveTicketForAnItem(Object itemObj, ValueIdDTO dto) {
		Item item = (Item)itemObj;
		// String ticketNum = (String) dto.getData();
		if (null == dto || null == dto.getData()) return;
		if (!GlobalUtils.isNumeric(dto.getData().toString())) return;

		Long ticketId = null;
		if (dto.getData() instanceof Long) {
			ticketId = (Long) dto.getData();
		}
		else {
			ticketId = Long.valueOf((Integer) dto.getData());
		}
		
		item.setTicketId(ticketId);
		
	}

}
