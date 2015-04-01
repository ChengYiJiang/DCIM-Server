/**
 * 
 */
package com.raritan.tdz.controllers.dctimport;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.dctimport.dto.ImportStatusDTO;
import com.raritan.tdz.dctimport.service.ImportService;
import com.raritan.tdz.fileupload.dto.FileUploadResultDTO;
import com.raritan.tdz.fileupload.home.FileUpload;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * @author prasanna
 * 
 * version 1.0
 */
@Controller
@RequestMapping("/v1/import")
public class FileImportController extends BaseController {
	
	private final Logger log = Logger.getLogger(FileImportController.class);
	
	@Autowired
	private ImportService importService;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private FileUpload fileUploadHome;

	/**
	 * The startImport API
	 * API call (example):
	 * curl -i -k -H "Content-Type: application/json" -X POST -H 
	 * 	"Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/import
	 * @param filePath
	 * @return
	 * @throws DCTRestAPIException 
	 */
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody  ImportStatusDTO startImport(
			HttpServletRequest request, 
			HttpServletResponse response) throws DCTRestAPIException {
		if(log.isDebugEnabled()){
			log.info("REST GET call: startImport() invoked");
		}
		checkAcceptMediaType(request);
		ImportStatusDTO ret = new ImportStatusDTO();
		
		try {
			ret = importService.startImport(RESTAPIUserSessionContext.getUser());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * The startImport API
	 * API call (example):
	 * curl -i -k -H "Content-Type: application/json" -X POST -H 
	 * 	"Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/import
	 * @param filePath
	 * @return
	 * @throws DCTRestAPIException 
	 */
	@RequestMapping(params = {"path"}, method=RequestMethod.POST)
	public @ResponseBody  ImportStatusDTO startValidate(
			HttpServletRequest request, 
			@RequestParam(value="path", required=true) String filePath,
			HttpServletResponse response) throws DCTRestAPIException {
		if(log.isDebugEnabled()){
			log.info("REST GET call: startValidate(" + filePath +") invoked");
		}
		checkAcceptMediaType(request);
		ImportStatusDTO ret = new ImportStatusDTO();
		
		try {
			ret = importService.startValidation(filePath, RESTAPIUserSessionContext.getUser());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * This is to upload the file
	 * API call example:
	 * curl -v -s -k -F "file=@AddOneLocation.csv" 
	 * 	http://admin:raritan@localhost:8080/dcTrackApp/api/v1/import/file
	 * @param request
	 * @param response
	 * @return
	 * @throws DCTRestAPIException
	 */
	@RequestMapping(value="file",method=RequestMethod.POST)
	public void upload(HttpServletRequest request,
			HttpServletResponse response)  
			throws DCTRestAPIException {
		
		HttpStatus status = HttpStatus.OK;
		
		if(log.isDebugEnabled()){
			log.info("REST GET call: upload");
		}
		
		FileUploadResultDTO resultDTO = new FileUploadResultDTO();
				
				
		try {
			resultDTO = fileUploadHome.upload(request);
		} catch (IOException e) {
			status = HttpStatus.BAD_REQUEST;
			throw new DCTRestAPIException(status,e);
		}
		
		response.setContentType("text/html");
		try {
			PrintWriter writer = response.getWriter();
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(writer, resultDTO);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			status = HttpStatus.BAD_REQUEST;
			throw new DCTRestAPIException(status,e);
		}
	}
	
	/**
	 * The getImportStatus API
	 * API call (example):
	 * curl -i -k -H "Content-Type: application/json" -X GET -H 
	 * 	"Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/import
	 * @param filePath
	 * @return
	 * @throws DCTRestAPIException 
	 */
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody  ImportStatusDTO getImportStatus(
			HttpServletRequest request,
			HttpServletResponse response) throws DCTRestAPIException {
		if(log.isDebugEnabled()){
			log.info("REST GET call: getImportStatus");
		}
		checkAcceptMediaType(request);
		ImportStatusDTO ret = new ImportStatusDTO();
		
		try {
			ret = importService.getImportStatus(RESTAPIUserSessionContext.getUser());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	@RequestMapping(method=RequestMethod.DELETE)
	public @ResponseBody ImportStatusDTO cancelImport(HttpServletRequest request, 
			HttpServletResponse response) 
					throws DCTRestAPIException{
		
		if(log.isDebugEnabled()){
			log.info("REST GET call: cancelImport");
		}
		checkAcceptMediaType(request);
		ImportStatusDTO ret = new ImportStatusDTO();
		
		try {
			ret = importService.cancelImport(RESTAPIUserSessionContext.getUser());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
}
