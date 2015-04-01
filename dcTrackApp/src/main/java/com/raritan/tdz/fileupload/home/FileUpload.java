/**
 * 
 */
package com.raritan.tdz.fileupload.home;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.raritan.tdz.fileupload.dto.FileUploadResultDTO;

/**
 * @author prasanna
 * This will perform the file upload to a specific folder
 * This will be used by the services to upload any kind of 
 * file
 */
public interface FileUpload {
	/**
	 * Upload a file for a specific service
	 * @param request
	 * @return TODO
	 * @throws IOException 
	 */
	public FileUploadResultDTO upload(HttpServletRequest request) throws IOException;
}
