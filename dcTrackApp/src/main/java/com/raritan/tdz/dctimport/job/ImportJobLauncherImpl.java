/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.UserInfo;

/**
 * @author prasanna
 *
 */
public class ImportJobLauncherImpl implements ImportJobLauncher {
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	Job importJob;
	
	@Autowired
	ImportJobListener importJobListener;
	
	@Autowired
	ImportStepResolveListener importStepResolveListener;
	
	private JobExecution currentJobExecution;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.job.ImportJobLauncher#launch(java.lang.String)
	 */
	@Override
	public JobExecution launchValidatorJob(UserInfo userInfo, String fileName) throws Exception {
		
		importStepResolveListener.setTransitionToStep(TransitionStep.VALIDATE_STEP);
	   	 JobParameters params = 
				 new JobParametersBuilder()
		 				.addString("import.file.name", fileName)
		 				.addString("importId", userInfo.getSessionId() + importJobListener.getImportId())
		 				.toJobParameters();
	 	currentJobExecution = jobLauncher.run(importJob, params);
	 	
	 	return currentJobExecution;
	}
	
	@Override
	public JobExecution launchImportJob(UserInfo userInfo, String fileName)
			throws Exception {
		
		importStepResolveListener.setTransitionToStep(TransitionStep.IMPORT_STEP);
	   	 JobParameters params = 
					 new JobParametersBuilder()
			 				.addString("import.file.name", fileName)
			 				.addString("importId", userInfo.getSessionId() + importJobListener.getImportId())
			 				.toJobParameters();
		 currentJobExecution = jobLauncher.run(importJob, params);
		 	
		 return currentJobExecution;
	}

	@Override
	public JobExecution cancel(UserInfo userInfo) throws Exception {
		if (currentJobExecution != null){
			currentJobExecution.stop();
		} else {
			throw new JobExecutionNotRunningException("No import is running at this time to stop");
		}
		return currentJobExecution;
	}



}
