/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.processors.ImportProcessor;

/**
 * @author prasanna
 *
 */
public class DCTImportWriter implements ItemWriter<DCTImport> {
	
	Logger logger = Logger.getLogger("dctImport");

	@Autowired
	private ImportProcessor importProcessorGateway;
	
	private boolean process = false;

	
	public boolean isProcess() {
		return process;
	}


	public void setProcess(boolean process) {
		this.process = process;
	}


	@Override
	public void write(List<? extends DCTImport> importDTOList) throws Exception {
		logger.warn(importDTOList);
		
		if (isProcess()){
			for (Object importDTO:importDTOList){
				if (importDTO instanceof DCTImport)
					importProcessorGateway.process((DCTImport)importDTO);
			}
		}
		
	}

}
