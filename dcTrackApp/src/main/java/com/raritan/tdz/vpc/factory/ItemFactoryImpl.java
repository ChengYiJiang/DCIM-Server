package com.raritan.tdz.vpc.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.raritan.tdz.domain.Item;

public class ItemFactoryImpl implements ItemFactory, ApplicationContextAware {

	private ApplicationContext ctx;
	
	@Override
	public Item getItem(String beanId) {
		
		//The only reason we are doing a context getBean is because we have to get a new item
		//every time (note that the bean itself has a scope of prototype)
		Item item = (Item) ctx.getBean(beanId);
				
		return item;
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		
		this.ctx = applicationContext;
		
	}

}
