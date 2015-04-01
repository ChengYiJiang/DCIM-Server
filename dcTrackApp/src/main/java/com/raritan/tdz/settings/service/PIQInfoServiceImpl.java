package com.raritan.tdz.settings.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQRestClientBase;
import com.raritan.tdz.settings.dto.PiqSettingDTO;
import com.raritan.tdz.util.ApplicationCodesEnum;


public class PIQInfoServiceImpl extends PIQRestClientBase implements PIQInfoService{				 	
	public PIQInfoServiceImpl(){}
	
	private RestTemplate piqRestTemplate;
	
	public void setPiqRestTemplate(RestTemplate restTemplate){
		this.piqRestTemplate = restTemplate;
	}
	
	public PiqSettingDTO getPIQVersion(PiqSettingDTO piqSettingDTO) throws RemoteDataAccessException{
		String piqVersion = "";
		String resultCode = "";
		
		setIPAddress(piqSettingDTO.getIpAddress());
		
		setCredentials(piqSettingDTO.getUserName(), piqSettingDTO.getPassword());
		
		String url = getRestURL("v2/system_info", null);
		
		ResponseEntity<?> resp = null;
		//PIQInfoJSON piqInfoJson = null;
		try {									
			//Get the PIQ system info file
			resp = piqRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>( getHttpHeaders() ), Map.class);
			
			//Parse the file and get the version
			if (resp != null){
				@SuppressWarnings("unchecked")
				LinkedHashMap<String,Map<String,String>> map = (LinkedHashMap<String,Map<String,String>>)resp.getBody();				
				
				LinkedHashMap<String,String> sysInfo = (LinkedHashMap<String,String>) map.get("system_info");
				
				piqVersion = sysInfo.get("poweriq_version");
				
				if(piqVersion == null)	piqVersion="";												
				
				piqSettingDTO.setVersion(piqVersion);
			}
			
		}catch (ResourceAccessException e) {
			resultCode = String.valueOf(ApplicationCodesEnum.PIQ_NOT_RUNNING.errCode());			
		}catch (HttpClientErrorException e) {
			resultCode = String.valueOf(ApplicationCodesEnum.PIQ_CREDENTIALS_INVALID.errCode());			
		}catch (HttpServerErrorException e) {
			resultCode = String.valueOf(ApplicationCodesEnum.PIQ_SERVICE_FAILED.errCode());					
		}catch (RestClientException e) {
			// It's possible that some service other than PIQ is listening
			// and responded with some unexpected JSON.
			resultCode = String.valueOf(ApplicationCodesEnum.PIQ_NOT_RUNNING.errCode());					
		}catch (Throwable t) {
			resultCode = String.valueOf(ApplicationCodesEnum.PIQ_SERVICE_FAILED.errCode());			
		}
		
		//piqSettingDTO.setResultCode(resultCode);
		
		return piqSettingDTO;				
	}


}
