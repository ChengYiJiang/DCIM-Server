package com.raritan.tdz.util;
import java.lang.reflect.Field;
import java.util.List;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ObjectTracerHandler;

/**
 * 
 */

/**
 * @author Randy Chen
 *
 */
public class ChassisTracerHandler implements ObjectTracerHandler {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.ObjectTracerHandler#handleTrace(com.raritan.tdz.util.ObjectTracer, java.lang.Class, java.lang.String)
	 */
	@Override
	public void handleTrace(ObjectTracer tracer, Class<?> rootClass,
			String fieldName) {
		ObjectTracer objectTracer = new ObjectTracer();
		List<Field> traceFields = objectTracer.traceObject(com.raritan.tdz.domain.ItItem.class, fieldName);
		for (Field field: traceFields){
			tracer.appendField(field);
		}
				
		if (traceFields.size() > 0){
			ObjectTracer usersTracer = new ObjectTracer();
			tracer.appendField(usersTracer.traceObject(Item.class, "itemName").get(0));
		}		
	}

}
