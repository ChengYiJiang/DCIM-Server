package com.raritan.tdz.floormaps.controllers;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import javax.servlet.http.*;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.page.dto.*;
import com.raritan.tdz.page.service.*;
import com.raritan.tdz.floormaps.dto.*;
import com.raritan.tdz.floormaps.service.*;

@Controller("fileUploadController")
@RequestMapping("/floormaps")
public class FileUploadController extends BaseController  {
	
	private final Logger log = Logger.getLogger( FileUploadController.class );
	
	/**
	 *  Post DWG file
	 *  
	 */
	@RequestMapping(value="/upload", method=RequestMethod.POST)
	public void upload(HttpServletRequest request, HttpServletResponse response)  {
		String resultJSON="";

		int RESULT_OK=0;
		int RESULT_FAIL=1;
		
		String RESULT_CODE="result_code";
		String RESULT_MSG="result_msg";
		String FILE_UUID="file_uuid";
		String SITE_CODE="site_code";
		String ORIGINAL_FILENAME="original_filename";
		
		int resultCode=RESULT_OK;
		String resultMsg="";
		String fileUUID="";
		String originalFilename="";
		
		log.info("upload..request="+request);
		
		try {
		
			String filePath="/var/oculan/floormaps/tmp/";
		
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;	
			MultiValueMap<String, MultipartFile> map = multipartRequest.getMultiFileMap();
			if(map != null) {
				Iterator iter = map.keySet().iterator();
				while(iter.hasNext()) {
					String str = (String) iter.next();
					List<MultipartFile> fileList =  map.get(str);
					for(MultipartFile mpf : fileList) {
					
						fileUUID=getUUID();
						String fileName=filePath+fileUUID+".dwg";
						originalFilename=mpf.getOriginalFilename();
						
						log.info("originalFilename="+originalFilename);
						log.info("fileName="+fileName);
						
						File localFile = new File( fileName);
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
						out.write(mpf.getBytes());
						out.close();							
					}
				}
			}
		} catch(Exception e) {
			log.error("",e);
			 resultCode=RESULT_FAIL;
			 resultMsg=e.getMessage();
		}
		
		 resultJSON="{"+
								"\""+RESULT_CODE+"\":"+resultCode+","+
								"\""+RESULT_MSG+"\":\""+resultMsg+"\","+
								"\""+FILE_UUID+"\":\""+fileUUID+"\","+
								"\""+ORIGINAL_FILENAME+"\":\""+originalFilename+"\""+
							 "}";
		
		try {
		
			//Compatiable for ExtJS file upload
			PrintWriter out=response.getWriter();
			response.setContentType("text/html");
			response.setContentLength(resultJSON.length());
			out.print(resultJSON);
			
		} catch(java.io.IOException ioe) {
			log.error("",ioe);
		}
	
	}
	
	private String getUUID() {
		String s = UUID.randomUUID().toString(); 
		
		return s;
	}

	
}
