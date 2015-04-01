/**
 * 
 */
package com.raritan.tdz.util;



import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;


/**
 * @author prasanna
 * This is to handle the creation of objects for a given JXPath
 */
public class JXPathAbstractFactory extends AbstractFactory {
	   @Override
	   public boolean createObject(JXPathContext context, Pointer pointer,
               Object parent, String name, int index) {
		
		try {
			List<Field> fields = getFieldTrace(parent.getClass(), name);
			
			if (fields.isEmpty() && parent.getClass().isAssignableFrom(parent.getClass())){
				fields = getFieldTrace(parent.getClass().getSuperclass(), name);
			}
			
			for (Field field: fields){
				StringBuffer setMethod = new StringBuffer();
				setMethod.append("set");
				setMethod.append(field.getName().substring(0, 1).toUpperCase());
				setMethod.append(field.getName().substring(1, field.getName().length()));
			
				Method setterMethod = parent.getClass().getMethod(setMethod.toString(), field.getType());
				if (parent.getClass().isAssignableFrom(parent.getClass()) && !parent.getClass().getSuperclass().equals(Object.class)){
					setterMethod = parent.getClass().getSuperclass().getMethod(setMethod.toString(), field.getType());
				}

				setterMethod.invoke(parent, field.getType().newInstance());
			}
		} catch (ClassNotFoundException e) {
			return false;
		} catch (InstantiationException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		}
		
		   return true;
		}
	   
	   private List<Field> getFieldTrace(Class<?> remoteType, String fieldName) throws ClassNotFoundException{
			//Create Alias
			ObjectTracer objectTrace = new ObjectTracer();
			objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
			objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
			objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
			objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
			return (objectTrace.traceObject(remoteType, fieldName));
		}
}
