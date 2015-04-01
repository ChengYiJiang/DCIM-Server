/**
 * 
 */
package com.raritan.tdz.dctexport.processor;

import com.raritan.tdz.page.dto.ListResultDTO;

/**
 * <p>This interface represents the processing of the ListResultDTO
 * and getting a CSV file out of it.</p>
 * <p>There need not be any implementation of this as the aggregators
 * will take care of creating the CSV file and returning the URL</p>
 * <p>The purpose of this interface is to use it as a gateway</p>
 * @author prasanna
 */
public interface ExportProcessor {
	/**
	 * Processes the list result dto and gives back a file name of
	 * the CSV file
	 * @param listResultDTO
	 * @return
	 * @throws Exception
	 */
	public String process(ListResultDTO listResultDTO) throws Exception;
}
