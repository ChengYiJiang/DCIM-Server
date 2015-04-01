/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.raritan.tdz.domain.Item;

/**
 * @author prasanna
 * This is the change model factory implementation
 * The format of the key to changeModelMap should be:
 * 	<Class/SubClass LkpValueCode of itemInDB>:<Class/SubClass LkpValueCode of itemToSave>
 * The value should be the spring bean id corresponding to 
 * changeModel implementation. Make sure that the bean scope = "proto"
 */
public class ChangeModelFactoryImpl implements ChangeModelFactory,ApplicationContextAware {

	Map<String,String> changeModelMap;
	ApplicationContext applicationContext;
	
	ChangeModelFactoryImpl(Map<String, String> changeModelMap){
		this.changeModelMap = changeModelMap;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.modelchange.ChangeModelFactory#getChangeModel(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.Item)
	 */
	@Override
	public ChangeModel getChangeModel(Item itemInDB, Item itemToSave) {
		ChangeModel changeModel = null;
		
		if (itemInDB != null && itemToSave != null){
			//Get the class and subclass from itemToDB
			Long itemInDBUniqueValue = itemInDB.getClassMountingFormFactorValue();
			Long itemToSaveUniqueValue = itemToSave.getClassMountingFormFactorValue();
			
			if (itemInDBUniqueValue != null && itemToSaveUniqueValue != null){
				String beanId = changeModelMap.get(itemInDBUniqueValue.toString() + ":" + itemToSaveUniqueValue.toString());
				changeModel = beanId != null ? (ChangeModel) applicationContext.getBean(beanId) : changeModel;
			}
		}
		
		return changeModel;
	}
	
	@Override
	public ChangeModel getChangeModel(Long itemInDBMountingFormFactorValue, Long itemToSaveMountingFormFactorValue) {
		ChangeModel changeModel = null;
		
		if (itemInDBMountingFormFactorValue != null && itemToSaveMountingFormFactorValue != null){
			String beanId = changeModelMap.get(itemInDBMountingFormFactorValue.toString() + ":" + itemToSaveMountingFormFactorValue.toString());
			changeModel = beanId != null ? (ChangeModel) applicationContext.getBean(beanId) : changeModel;
		}
		
		return changeModel;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;	
	}

}
