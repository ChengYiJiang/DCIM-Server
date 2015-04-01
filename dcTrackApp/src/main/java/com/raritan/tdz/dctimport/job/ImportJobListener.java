/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.dctimport.logger.ImportLogger;

/**
 * @author prasanna
 * This will capture the progress of the import job
 */
public class ImportJobListener {
	
	@Autowired
	private ImportLogger importLogger;
	
	// Logger for all Import operations
	private Logger log = Logger.getLogger("dctImport");
	
	private AtomicInteger totalCount = new AtomicInteger(0);
	private AtomicLong importId = new AtomicLong(0);
	
	
	private JobExecution jobExecution;


	private ImportErrorHandler importErrorHandler;
	
	@Autowired
	private ImportStepListener importValidationStepListener;
	
	@Autowired
	private ImportStepListener importImportStepListener;
	
	private String importFilePath;
	
	@Autowired
	ImportStepResolveListener importStepResolveListener;
	
	@Autowired
	private AtomicInteger lineNumber;
	
	
	public ImportJobListener(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}
	
	public String getImportFilePath(){
		return importFilePath;
	}
	
	@BeforeJob
	public void beforeJob(JobExecution jobExecution) throws Exception {
		String fileName = jobExecution.getJobParameters().getString("import.file.name");
		
		Resource inResource = new FileSystemResource(fileName);
		totalCount = new AtomicInteger(countLineNumber(inResource));
		log.info("Total number of lines: " + totalCount);
		
		this.jobExecution = jobExecution;
		
		if (importStepResolveListener.getTransitionToStep().equals(TransitionStep.VALIDATE_STEP))
			importFilePath = fileName;
		
		importErrorHandler.clearErrors();
		importErrorHandler.clearWarnings();
		
		importValidationStepListener.resetCounts();
		importImportStepListener.resetCounts();
		
		importLogger.clean(true);
		importLogger.setFileName(fileName);
	}
	
	@AfterJob
	public void afterJob(JobExecution jobExecution) throws IOException{
		importId.incrementAndGet();
		
		if (importStepResolveListener.getTransitionToStep().equals(TransitionStep.IMPORT_STEP))
			importFilePath = "";
	}
	
	public String getCurrentStepName(){
		String stepName = ImportStepListener.VALIDATION_STEP;
		if (jobExecution != null){
			for (StepExecution stepExecution:jobExecution.getStepExecutions()){
				if (stepExecution.getExitStatus().equals(ExitStatus.EXECUTING)){
					stepName = stepExecution.getStepName();
					break;
				} else if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)){
					stepName = stepExecution.getStepName();
				}
			}
		}
		
		return stepName;
	}
	
	public long getImportId(){
		return importId.get();
	}
	
	public boolean isJobRunning(){
		boolean result = false;
		if (jobExecution != null){
			result = jobExecution.isRunning();
		}
		
		return result;
	}
	
	public String getLogURL(){
		return importLogger.getURL();
	}
	
	public Date getLastUpdatedDate(){
		Date lastUpdated = null;
		
		if (jobExecution != null){
			lastUpdated = jobExecution.getLastUpdated();
		}
		return lastUpdated;
	}
	
	
	/**
	 * @return the totalCount
	 */
	public final int getTotalCount() {
		return totalCount.get();
	}
	
	public final long getJobId() {
		if (jobExecution != null)
			return jobExecution.getJobId();
		return 0;
	}
	
	public final int getLineNumber(){
		return lineNumber.get();
	}
	
	private int countLineNumber(Resource inResource) throws IOException{
		int lines = 0;
	
		File file;
		try {
			//Try to get the last line number
			file = inResource.getFile();
			LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
			long numChars = lineNumberReader.skip(Long.MAX_VALUE);
			lines = lineNumberReader.getLineNumber();
			lineNumberReader.close();
			
			//Check if the last line number has a carriage return or new line
			lineNumberReader = new LineNumberReader(new FileReader(file));
			lineNumberReader.skip(lines);
			char[] buffer = new char[(int)numChars];
			boolean carriageReturn = false;
			int readCnt = lineNumberReader.read(buffer);
			carriageReturn = buffer[readCnt-1] == '\n' || buffer[readCnt-1] == '\r';
			
			//If there is no carriage return or newLine, then we need to add one more
			//as the line number provided by the lineReader will not have 
			//this in the count.
			if (!carriageReturn)
				lines++;
			
			lineNumberReader.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			Errors errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName()); 
			String msg = e.getMessage().contains("../../") ? e.getMessage().substring(e.getMessage().indexOf("[../../")):"";
			String fileName = msg.contains("/") && msg.contains("]") ? msg.substring(msg.lastIndexOf("/")+1,msg.indexOf("]")):msg;
			Object[] errorArgs = {fileName};
			errors.reject("Import.fileNotFound", errorArgs, "Import file not found");
			importErrorHandler.handleLineErrors(errors);
			throw e;
		}
		
		return lines;
	}

}
