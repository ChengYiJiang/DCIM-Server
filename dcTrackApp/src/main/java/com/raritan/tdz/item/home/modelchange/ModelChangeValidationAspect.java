package com.raritan.tdz.item.home.modelchange;

import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.Item;


/**
 * @author Santo Rosario
 * This aspect will take care of validation the change() method in the ModelChange interface
 */
@Aspect
public class ModelChangeValidationAspect {
	@Autowired
	ChangeModelDAO changeModelDAO;
	
	@Around("execution(* com.raritan.tdz.item.home.modelchange.ChangeModel.change*(..))")
	public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
		Long itemId = getItemId(joinPoint);
		
		changeModelDAO.validatePortUsage(itemId);
		
		Object retVal = joinPoint.proceed();
				
		return retVal;
	}

	private Long getItemId(JoinPoint joinPoint){
		Long itemId = null;
		
		Object[] args = joinPoint.getArgs();
		
		if (args.length == 2 && args[1] != null){
			Item item = (Item) args[1];
			itemId = item.getItemId();
		}		
		return itemId;
	}	
}
