package com.raritan.tdz.dctimport.integration.transformers;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DataConnImport;
import com.raritan.tdz.dctimport.dto.StructuredCablingConnImport;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

import com.raritan.tdz.user.home.UserHome;

/**
 * 
 * @author kc.chen
 *
 */
public class StructuredCablingConnBeanDeleteTransformer extends StructuredCablingConnBeanTransformer {
	@Autowired
	private UserHome userHome;		

	private Logger log = Logger.getLogger("StructuredCablingConnBeanDeleteTransformer");
	
	public StructuredCablingConnBeanDeleteTransformer(String uuid) {
		super(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception {		
		List<StructureCableDTO> recList = null;
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());
		
		StructuredCablingConnImport dcImport = (StructuredCablingConnImport) beanObj;
						
		recList = getStructuredCablingList(dcImport);
		
		Object[] parameters = {recList, userInfo};
		
		return parameters;
	}
	
	private List<StructureCableDTO> getStructuredCablingList(StructuredCablingConnImport dcImport){
		List<StructureCableDTO> recList = null;
		recList = new ArrayList<StructureCableDTO>();
		
		return recList;
	}
	
}
