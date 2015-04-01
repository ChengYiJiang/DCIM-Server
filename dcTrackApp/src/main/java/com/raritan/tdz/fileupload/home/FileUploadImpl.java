/**
 * 
 */
package com.raritan.tdz.fileupload.home;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.raritan.tdz.fileupload.dto.FileUploadResultDTO;
import com.raritan.tdz.floormaps.controllers.FileUploadController;

/**
 * @author prasanna
 *
 */
public class FileUploadImpl implements FileUpload {
	
	private final Logger log = Logger.getLogger( FileUpload.class );
	
	private final String destFilePath;
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	public FileUploadImpl(String destFilePath){
		this.destFilePath = destFilePath;
		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.fileupload.home.FileUpload#upload(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public FileUploadResultDTO upload(HttpServletRequest request) throws IOException {
		
		FileUploadResultDTO result = new FileUploadResultDTO();
		
		String fileUUID="";
		String originalFilename="";
		
		log.info("upload..request="+request);
		
			String contextRealPath = servletContext.getRealPath(File.separator);
			
			String filePath = contextRealPath + destFilePath;
		
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;	
			MultiValueMap<String, MultipartFile> map = multipartRequest.getMultiFileMap();
			if(map != null) {
				Iterator iter = map.keySet().iterator();
				while(iter.hasNext()) {
					String str = (String) iter.next();
					List<MultipartFile> fileList =  map.get(str);
					for(MultipartFile mpf : fileList) {
					
						fileUUID=getUUID();
						originalFilename=mpf.getOriginalFilename();
						
						String localFileName = originalFilename.contains(".") ? originalFilename.substring(0,originalFilename.lastIndexOf(".")) : originalFilename;
						String localFileExtn = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
						
						StringBuilder fileName = new StringBuilder(filePath).append("/").append(localFileName).append(".").append(fileUUID).append(localFileExtn);
						StringBuilder retFileName = new StringBuilder(localFileName).append(".").append(fileUUID).append(localFileExtn);
						
						
						log.info("originalFilename="+originalFilename);
						log.info("fileName="+fileName);
						
						File localFile = new File( fileName.toString() );
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
						out.write(mpf.getBytes());
						out.close();

						result.addResult(retFileName.toString(),originalFilename);
						
					}
				}
			}
			
		return result;
	}
	
	private String getUUID() {
		String s = UUID.randomUUID().toString(); 
		
		return s;
	}

}
