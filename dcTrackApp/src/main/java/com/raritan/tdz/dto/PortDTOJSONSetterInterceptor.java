/**
 * 
 */
package com.raritan.tdz.dto;

import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This aspect takes care of setting the dirty flag
 * based on when user sets a value into the JSON object
 * for a PortDTO.
 * @author prasanna
 *
 */
@Aspect
public class PortDTOJSONSetterInterceptor {
	
	@Pointcut("@annotation( org.codehaus.jackson.annotate.JsonProperty )")
	public void isJsonAnnotation(){};
	
	@Pointcut("execution(public * com.raritan.tdz.dto.*Port*.set*(..))")
	public void isSetterMethod(){};
	
	@After("isJsonAnnotation() && isSetterMethod()")
	public void setDirtFlag(JoinPoint joinPoint){
		if (joinPoint.getTarget() instanceof PortDTOBase){
			PortDTOBase portDTO = (PortDTOBase)joinPoint.getTarget();
			Map<String,Boolean> dirtyFlagMap = portDTO.getDirtyFlagMap();
			
			dirtyFlagMap.put(joinPoint.getSignature().getName(), true);
		}
	}
}
