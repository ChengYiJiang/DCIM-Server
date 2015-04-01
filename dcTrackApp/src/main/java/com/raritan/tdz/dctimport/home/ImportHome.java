/**
 * 
 */
package com.raritan.tdz.dctimport.home;

import com.raritan.tdz.dctimport.dto.ImportStatusDTO;
import com.raritan.tdz.domain.UserInfo;

/**
 * @author prasanna
 *
 */
public interface ImportHome {
	/**
	 * Performs the file import given a fileName
	 * @param userInfo TODO
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO doImport(UserInfo userInfo) throws Exception;
	
	/**
	 * Performs the file validation before import can start
	 * @param userInfo
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO doValidate(UserInfo userInfo, String fileName) throws Exception;
	
	/**
	 * Get the current import status
	 * <p>We may add in the userInfo as a parameter. This is 
	 * still unknown</p>
	 * @param userInfo TODO
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO getImportStatus(UserInfo userInfo) throws Exception;
	
	/**
	 * Cancel the import process
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public ImportStatusDTO cancelImport(UserInfo userInfo) throws Exception;
	
	/**
	 * Deletes the import files when the context gets deleted
	 */
	public void destroy();
}
