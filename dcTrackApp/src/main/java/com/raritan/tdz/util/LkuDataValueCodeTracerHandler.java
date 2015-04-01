package com.raritan.tdz.util;
import java.lang.reflect.Field;
import java.util.List;

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
public class LkuDataValueCodeTracerHandler implements ObjectTracerHandler {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.ObjectTracerHandler#handleTrace(com.raritan.tdz.util.ObjectTracer, java.lang.Class, java.lang.String)
	 */
	@Override
	public void handleTrace(ObjectTracer tracer, Class<?> rootClass,
			String fieldName) {
		String lkuFieldName = fieldName.split("LkuValueCode")[0] + "Lookup";
		List<Field> traceFields = tracer.traceObject(rootClass, lkuFieldName);
		if (traceFields.size() > 0){
			ObjectTracer lkuValueTracer = new ObjectTracer();
			List<Field> fields = lkuValueTracer.traceObject(LkuData.class, "lkuId");
			if (fields.size() > 0){
				for (Field field : fields)
					tracer.appendField(field);
			}
		}
	}

}
