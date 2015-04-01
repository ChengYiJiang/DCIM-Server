/**
 * 
 */
package com.raritan.tdz.dctimport.home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.Errors;
import org.springframework.web.context.support.ServletContextResource;

import com.raritan.tdz.dctimport.dto.ImportStatusDTO;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.dctimport.job.ImportJobLauncher;
import com.raritan.tdz.dctimport.job.ImportJobListener;
import com.raritan.tdz.dctimport.job.ImportStepListener;
import com.raritan.tdz.dctimport.logger.ImportLogger;
import com.raritan.tdz.domain.UserInfo;

import flex.messaging.log.Log;

/**
 * @author prasanna
 *
 */
public class ImportHomeImpl implements ImportHome {
	
	private final Logger logger = Logger.getLogger("dctImport");
	
	ImportJobLauncher launchJobGateway;
	

	private ImportErrorHandler importErrorHandler;
	
	@Autowired
	private ImportJobListener importJobListener;
	
	@Autowired
	private ImportStepListener importValidationStepListener;
	
	@Autowired
	private ImportStepListener importImportStepListener;
	
	@Autowired
	private ImportLogger importLogger;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	@Autowired
	private ResourceLoader resourceLoader;

	private final List<String> fileNames;
	
	
	public ImportHomeImpl(ImportJobLauncher launchJobGateway, ImportErrorHandler importErrorHandler) {
		this.launchJobGateway = launchJobGateway;
		this.importErrorHandler = importErrorHandler;
		this.fileNames = new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.home.ImportHome#doImport(java.lang.String)
	 */
	@Override
	public ImportStatusDTO doImport(UserInfo userInfo) throws Exception {
		
		if (importErrorHandler.getErrors().size() == 0){
			resetForNewJob();
			
			try {
				launchJobGateway.launchImportJob(userInfo, importJobListener.getImportFilePath());
			} catch (Exception e){
				//Most of the exceptions are handled and included in the status DTO object
				//We may decide to throw a different exception here. TBD
			}
		} else {
			final List<ImportStepListener> importStepListeners = new ArrayList<ImportStepListener>(){{
				add(importValidationStepListener);
				add(importImportStepListener);
			}};
			
			final List<String> msgs = new ArrayList<String>(){{
					add(messageSource.getMessage("Import.validationErrors", null, Locale.getDefault()));
			}}; 
			
			
			
			return new ImportStatusDTO(importErrorHandler,importJobListener,importStepListeners,msgs, null);
		}
		
		return getImportStatus(userInfo);
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.home.ImportHome#doValidate(java.lang.String)
	 */
	@Override
	public ImportStatusDTO doValidate(UserInfo userInfo, String fileName)
			throws Exception {
		resetForNewJob();
		
		//Capture the file name so that we can delete when the context gets destroyed.
		this.fileNames.add(fileName);
		
		try {
			launchJobGateway.launchValidatorJob(userInfo, fileName);
		} catch (Exception e){
			//Most of the exceptions are handled and included in the status DTO object
			//We may decide to throw a different exception here. TBD
		}
		return getImportStatus(userInfo);
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.home.ImportHome#getImportStatus()
	 */
	@Override
	public ImportStatusDTO getImportStatus(UserInfo userInfo) throws Exception {
		final List<ImportStepListener> importStepListeners = new ArrayList<ImportStepListener>(){{
			add(importValidationStepListener);
			add(importImportStepListener);
		}};
		
		List<String> msgs = new ArrayList<String>();
		
		if (importErrorHandler.getErrors().isEmpty() && importErrorHandler.getWarnings().isEmpty()){
			String step = "Import.successful." + importJobListener.getCurrentStepName();
			msgs.add(messageSource.getMessage(step, null, Locale.getDefault())); 
		}
		return new ImportStatusDTO(importErrorHandler,importJobListener,importStepListeners,null, msgs);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.home.ImportHome#cancelImport()
	 */
	@Override
	public ImportStatusDTO cancelImport(UserInfo userInfo) throws Exception {
		
		launchJobGateway.cancel(userInfo);
		
		return getImportStatus(userInfo);
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.home.ImportHome#destroy()
	 */
	@Override
	public void destroy() {
		deleteImportFiles();
	}
	
	private void resetForNewJob() throws IOException {
		if (!importJobListener.isJobRunning()){
			importErrorHandler.clearErrors();
			importErrorHandler.clearWarnings();
			importValidationStepListener.resetCounts();
			importImportStepListener.resetCounts();
			importLogger.setFileName(null);
		}
	}

	private String getRealPath(String inputFile) throws IOException{
		Resource inResource = servletContext != null ? new ServletContextResource(servletContext, "/../dcTrackImport/" + inputFile):resourceLoader.getResource("../../" + inputFile);
		return inResource.getFile().getAbsolutePath();
	}
	
	private void deleteImportFiles(){
		for (String fileName: fileNames){
			try {
				Files.delete(Paths.get(getRealPath(fileName)));
				Files.delete(Paths.get(getRealPath(fileName.substring(0,fileName.lastIndexOf(".csv")) + "-error.csv")));
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not delete Import File while logging out");
					e.printStackTrace();
				}
			}
		}
		fileNames.clear();
	}



}
