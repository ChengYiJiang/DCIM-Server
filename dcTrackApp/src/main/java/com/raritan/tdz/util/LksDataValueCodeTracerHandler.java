package com.raritan.tdz.util;
import java.lang.reflect.Field;
import java.util.List;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ObjectTracerHandler;

/**
 * 
 */

/**
 * @author prasanna
 *
 */
public class LksDataValueCodeTracerHandler implements ObjectTracerHandler {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.ObjectTracerHandler#handleTrace(com.raritan.tdz.util.ObjectTracer, java.lang.Class, java.lang.String)
	 */
	@Override
	public void handleTrace(ObjectTracer tracer, Class<?> rootClass,
			String fieldName) {
		String lksFieldName = fieldName.split("LkpValueCode")[0] + "Lookup";
		List<Field> traceFields = tracer.traceObject(rootClass, lksFieldName);
		if (traceFields.size() > 0){
			ObjectTracer lksValueTracer = new ObjectTracer();
			tracer.appendField(lksValueTracer.traceObject(LksData.class, "lkpValueCode").get(0));
		}		
	}

}
