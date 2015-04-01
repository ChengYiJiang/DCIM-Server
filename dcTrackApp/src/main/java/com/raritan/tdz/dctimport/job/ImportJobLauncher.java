/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import org.springframework.batch.core.JobExecution;

import com.raritan.tdz.domain.UserInfo;

/**
 * @author prasanna
 *
 */
public interface ImportJobLauncher {
	/**
	 * Launch the import job with validator step
	 * @param userInfo TODO
	 * @param fileName
	 * @throws Exception
	 */
	public JobExecution launchValidatorJob(UserInfo userInfo, String fileName) throws Exception;
	
	/**
	 * Launch the import job with import step
	 * @param userInfo
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public JobExecution launchImportJob(UserInfo userInfo, String fileName) throws Exception;
	
	/**
	 * Cancel the job
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public JobExecution cancel(UserInfo userInfo) throws Exception;
}
