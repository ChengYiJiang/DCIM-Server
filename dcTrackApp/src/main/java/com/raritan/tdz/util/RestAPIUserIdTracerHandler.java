package com.raritan.tdz.util;

import java.lang.reflect.Field;
import java.util.List;


public class RestAPIUserIdTracerHandler implements ObjectTracerHandler{

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
	}	
}
