/**
 * 
 */
package com.raritan.tdz.dctimport.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.dctimport.dto.ImportStatusDTO;
import com.raritan.tdz.dctimport.home.ImportHome;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * @author prasanna
 *
 */
public class ImportServiceImpl implements ImportService {

	@Autowired
	ImportHome importHomeGateway;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.service.ImportService#startImport(java.lang.String)
	 */
	@Override
	public ImportStatusDTO startImport(UserInfo userInfo) throws Exception {
		ImportStatusDTO dto = importHomeGateway.doImport(userInfo);
		return dto;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.service.ImportService#startValidation(java.lang.String)
	 */
	@Override
	public ImportStatusDTO startValidation(String fileName, UserInfo userInfo)
			throws Exception {
		return importHomeGateway.doValidate(userInfo, fileName);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.service.ImportService#getImportStatus()
	 */
	@Override
	public ImportStatusDTO getImportStatus(UserInfo userInfo) throws Exception {
		return importHomeGateway.getImportStatus(userInfo);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.service.ImportService#cancelImport()
	 */
	@Override
	public ImportStatusDTO cancelImport(UserInfo userInfo) throws Exception {
		return importHomeGateway.cancelImport(userInfo);
	}



}
