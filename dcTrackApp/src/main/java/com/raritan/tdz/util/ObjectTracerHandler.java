/**
 * 
 */
package com.raritan.tdz.util;

/**
 * @author prasanna
 *
 */
public interface ObjectTracerHandler {
	public void handleTrace(ObjectTracer tracer, Class<?> rootClass, String fieldName);
}
