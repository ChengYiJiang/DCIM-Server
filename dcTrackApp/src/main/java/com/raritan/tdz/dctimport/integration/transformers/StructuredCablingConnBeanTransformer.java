package com.raritan.tdz.dctimport.integration.transformers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;


import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.user.home.UserHome;

/**
 * 
 * @author KC
 *
 */
public class StructuredCablingConnBeanTransformer implements ImportBeanToParameter {

	private String uuid;						
	
	@Autowired
	private UserHome userHome;
	
	private Logger log = Logger.getLogger("StructuredCablingConnBeanDeleteTransformer");
	
	public StructuredCablingConnBeanTransformer(String uuid) {
		super();
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception, ServiceLayerException {
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());
		log.debug("userInfo="+userInfo.getId());
		return null;
	}
	
		

	

}
