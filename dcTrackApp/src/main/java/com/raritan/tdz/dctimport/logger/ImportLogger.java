/**
 * 
 */
package com.raritan.tdz.dctimport.logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.validation.Errors;

/**
 * @author prasanna
 * Logs each line that is read from the file and adds a column for reporting errors
 * This will be the file sent to the client where users can view errors for each line
 * and try to re-import it with corrections.
 */
public interface ImportLogger {
	/**
	 * Sets up the log file name
	 * @param fileName
	 */
	public void setFileName(String fileName) throws IOException;
	
	/**
	 * Log the line
	 * <p><b>Note:</b> This will add error column</p>
	 * @param line
	 * @param lineNumber
	 */
	public void logLine(String line, int lineNumber);
	
	/**
	 * Returns the original line that is getting processed
	 * @return
	 */
	public String getCurrentOriginalLine();
	
	/**
	 * Log the error by adding a column
	 * <p>If error column exists, adds it to the same column with a semicolon separator</p>
	 * <p>Assumption: This will assume that we are logging this per line</p>
	 * @param errors
	 */
	public void logError(Errors errors);
	/**
	 * Log the warning by adding a column
	 * <p>If error column exists, adds it to the same column with a semicolon separator</p>
	 * <p>Assumption: This will assume that we are logging this per line</p>
	 * @param warnings
	 */
	public void logWarning(Errors warnings);
	/**
	 * Performs a commit
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 * @throws Exception 
	 */
	public void commit() throws Exception;
	/**
	 * Cleans the logger
	 * @param deleteFile TODO
	 * @throws Exception 
	 */
	public void clean(boolean deleteFile) throws Exception;
	
	/**
	 * Get the logged file URL
	 * @return
	 */
	public String getURL();
}
