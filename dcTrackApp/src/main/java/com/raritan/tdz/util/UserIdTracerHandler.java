package com.raritan.tdz.util;
import java.lang.reflect.Field;
import java.util.List;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ObjectTracerHandler;

/**
 * 
 */

/**
 * @author prasanna
 *
 */
public class UserIdTracerHandler implements ObjectTracerHandler {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.ObjectTracerHandler#handleTrace(com.raritan.tdz.util.ObjectTracer, java.lang.Class, java.lang.String)
	 */
	@Override
	public void handleTrace(ObjectTracer tracer, Class<?> rootClass,
			String fieldName) {
		
		String internalFieldName = "userId";
		
		if (fieldName.contains(".")){
			internalFieldName = fieldName.substring(fieldName.lastIndexOf(".") + 1, fieldName.length());
			fieldName = fieldName.substring(0,fieldName.lastIndexOf("."));
		}
		
		ObjectTracer itemAdminUserTracer = new ObjectTracer();
		List<Field> traceFields = itemAdminUserTracer.traceObject(rootClass, fieldName);
		for (Field field: traceFields){
			tracer.appendField(field);
		}
		
		if (traceFields.size() > 0){
			ObjectTracer usersTracer = new ObjectTracer();
			tracer.appendField(usersTracer.traceObject(Users.class, internalFieldName).get(0));
		}		
	}

}
