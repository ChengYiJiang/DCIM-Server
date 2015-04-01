package com.raritan.tdz.controllers;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.util.HTTPRequester;

@Controller
public class AssetController {

	String endPoint = "https://192.168.59.243/api/v2/";
	String rackUnitContext= "rack_units";
	
	@RequestMapping(value="/rackunits", method=RequestMethod.GET)
	public @ResponseBody String getRestGetData(HttpServletResponse response) throws Throwable {
		ControllerUtil.addCommonResponseHeaders(response);
		return HTTPRequester.sendGetRequest(endPoint+rackUnitContext,null);
	}
	
//	@RequestMapping(value="/saveRackUnit/{rackUnitId}", method=RequestMethod.POST)
//	public @ResponseBody String putRestPostData(@PathVariable int rackUnitId, HttpServletResponse response) throws Throwable {
//		ControllerUtil.addCommonResponseHeaders(response);
//		return HTTPRequester.sendPutOrPostRequest(endPoint+rackUnitContext+"/"+rackUnitId, "", "PUT");
//	}
	
	@RequestMapping(value="/saveRackUnit/{rackUnitId}", method=RequestMethod.GET)
	public @ResponseBody String putRestGetData(@PathVariable int rackUnitId, @RequestParam String data, HttpServletResponse response) throws Throwable {
		ControllerUtil.addCommonResponseHeaders(response);
		return HTTPRequester.sendPutOrPostRequest(endPoint+rackUnitContext+"/"+rackUnitId, data, "PUT");
	}
}


