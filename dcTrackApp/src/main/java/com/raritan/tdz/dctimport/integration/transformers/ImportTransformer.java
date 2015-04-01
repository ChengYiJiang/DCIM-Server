/**
 * 
 */
package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.integration.Message;



/**
 * @author prasanna
 *
 */
public interface ImportTransformer {

	/**
	 * Transform the file contents or beans from one type to another
	 * For example it could be from Excel spreadsheet to CSV file
	 * 
	 * <p><b>Note:</b> The paths are assumed to be relative here and should be appropriately interpreted
	 * by the underlying implementation to this interface</p>
	 * @param message TODO
	 * @return Output File Name generated using input fileName and its type.
	 * @throws Exception - This could be an IOException or ImportFileTypeInvalidException exception
	 */
	public Message<?> transform(Message<?> message) throws Exception;
	
	
}
