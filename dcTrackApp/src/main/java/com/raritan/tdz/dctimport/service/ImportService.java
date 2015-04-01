/**
 * 
 */
package com.raritan.tdz.dctimport.service;

import com.raritan.tdz.dctimport.dto.ImportStatusDTO;
import com.raritan.tdz.domain.UserInfo;

/**
 * @author prasanna
 *
 */
public interface ImportService {
	
	/**
	 * Starts the import process given the file name
	 * @param userInfo TODO
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO startImport(UserInfo userInfo) throws Exception;
	
	
	/**
	 * Performs the import file validation
	 * @param fileName
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO startValidation(String fileName, UserInfo userInfo) throws Exception;
	
	/**
	 * Returns the import status that includes progress and any errors.
	 * @param userInfo TODO
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO getImportStatus(UserInfo userInfo) throws Exception;
	
	/**
	 * Stops the import process
	 * @param userInfo TODO
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO cancelImport(UserInfo userInfo) throws Exception;

}
