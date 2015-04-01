package com.raritan.tdz.page.controllers;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.multiaction.*;
import org.apache.log4j.Logger;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.page.home.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import  org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@Controller
public class PaginatedController extends MultiActionController {

	private Logger log = Logger.getLogger(this.getClass());

	private ExportHome exportHome;
	
	@Autowired
	private HashMap<String, Object> paginatedHomes;
	
	public static String EXPORT_TO_IMPORT_ITEMS="itemsForImport";
	public static String EXPORT_TO_IMPORT_DATA_PORTS="dataPortsForImport";
		
	public PaginatedController() {

	}
	
	/*
	* URL Example: http://localhost:8080/dcTrackApp/rest/PaginatedExport?pageType=itemList&option=all
	* pageType=itemList | circuitList (for item list)
	* option=all | page
	*/
	@RequestMapping(value="/PaginatedExport",method=RequestMethod.POST)
	public ModelAndView exportToCSV(HttpServletRequest request, HttpServletResponse response) {
		
		try {

			ExportHome exportHome = (ExportHome)paginatedHomes.get( request.getParameter("pageType") );
			
			StringBuilder csv=exportHome.exportToCSV(request.getParameter("option"), request.getParameter("json"), request.getParameter("pageType"));
			
			String fileName=String.format("data_%1$tY%1$tm%1$te%1$tH%1$tM%1$tS", new Date())+".csv";
			
			//CR54034 - Change the encoding from iso8859_1
			response.setCharacterEncoding("UTF-8");
			
			PrintWriter out=response.getWriter();
			response.setContentType("plain/text");
			response.setContentLength(csv.length());
			response.addHeader("Content-Disposition", "attachment; filename="+fileName);

			out.print(csv.toString());
		} catch(Exception e) {
			log.error("PaginatedController.exportToCSV()",e);
		}
		
		return null;
	}
	
	@RequestMapping(value="/items/paginatedExportForImport", method=RequestMethod.POST)
	public void exportToCSVForImport(@RequestParam(value="option", required=true) String option, 
			@RequestParam(value="json", required=false) String json,
			HttpServletRequest request, HttpServletResponse response)  throws Throwable {
		
		String pageType = "itemList";
		ExportHome exportHome = (ExportHome)paginatedHomes.get( pageType );
		
		try {
			String csvFilePath = exportHome.exportToCSVForImport(option, json, pageType);	
			writeResponse(response, csvFilePath);
		} catch (DataAccessException de){
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, de));
		} catch (Exception ex){
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, ex));
		}
		
	}

	private void writeResponse(HttpServletResponse response, String csvFilePath)
			throws FileNotFoundException, UnsupportedEncodingException,
			IOException {
		File csvFile = new File(csvFilePath);
		
		BufferedReader br = getBufferedReader(csvFile);
		
		String line = null;
		int byteCnt = 0;
		while (( line = br.readLine()) != null){
			PrintWriter out=response.getWriter();
			out.print(line);
			out.print("\n");
			byteCnt += line.length() + "\n".length();
		}
		
		br.close();
		
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("plain/text");
		response.setContentLength(byteCnt);
		response.addHeader("Content-Disposition", "attachment; filename="+csvFile.getName());
		csvFile.delete();
	}
	
	
	private BufferedReader getBufferedReader(File newFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		FileInputStream inputStream = new FileInputStream(newFile);
	    InputStreamReader isr = new InputStreamReader(inputStream,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
}
