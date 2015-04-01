/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.Timestamp;

import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

import org.springframework.integration.annotation.Payload;

import org.apache.commons.codec.binary.Base64;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.config.*;
import org.apache.http.client.methods.*; 

import org.apache.http.protocol.*; 
import org.apache.http.util.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.*;

import org.apache.http.config.*;
import org.apache.http.conn.*;
import org.apache.http.conn.socket.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;

import com.raritan.tdz.events.home.EventHome;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSyncFloorMapImpl extends PIQSyncBase implements PIQSyncFloorMap {

	private EventHome eventHome;
	
	public final static int CONNECT_FAIL=-1;
	public final static int FILE_NOT_FOUND=-2;

	public PIQSyncFloorMapImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncFloorMap#uploadFloorMap(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	//public void uploadFloorMap( @Payload("#this[0]")  String piqHost,  @Payload("#this[1]")  String filePath)
	public Map uploadFloorMap( 
		@Payload("#this[0]")  String piqHost,
		@Payload("#this[1]")  String filePath, 
		@Payload("#this[2]")  String pdqId,
		@Payload("#this[3]")  String httpUsername,
		@Payload("#this[4]")  String httpPassword
	)
			throws RemoteDataAccessException {
		
		log.debug("uploadAndConvertDWG...");
		
		Map httpStatusMap=new HashMap();
		httpStatusMap.put(400, "Bad request -Request that client sent is not correct and cannot be processed (e.g. requested item is not available in DB)");
		httpStatusMap.put(401, "Not Authorized - User authentication failed");
		httpStatusMap.put(404, "User provided invalid path and it does not match any of available REST API calls");
		httpStatusMap.put(406, "Not Acceptable - Provided request is not acceptable. (User specified wrong data within \"Accept\" header)");
		httpStatusMap.put(422, "Unprocessable Entity - There is most likely an error in the JSON's body and it cannot be interpreted by server");
		httpStatusMap.put(500, "Internal server error - Unexpected error at the server side");

		Map returnMap=new HashMap();
		
		String originalFilename="";
		String responseString="";
		
		String fileDescription="";
		
		String newFileUUID="";
		String newOriginalFilename="";
		
		int httpStatusCode=CONNECT_FAIL;
		
		File file=new File(filePath);
		
		log.debug("file.exists()="+file.exists());
		
		String exceptionMsg="";

		if(file.exists()) {
		
			try {
			
				int connectionTimeout=30; //sec
			
				//Upload
				HttpClient httpClient = createHttpClient();
				
				RequestConfig config = RequestConfig.custom()
					.setSocketTimeout(connectionTimeout * 1000)
					.setConnectTimeout(connectionTimeout * 1000)
					.build();
					
				//Check permission for PowerIQUploading
				HttpGet httpGet = new HttpGet("https://"+piqHost+"/powerIQService/api/permission?locationName="+pdqId);

				Base64 base64 = new Base64();
				String encodeString = new String(base64.encode((httpUsername+":"+httpPassword).getBytes()));

				httpGet.addHeader("Authorization","Basic "+encodeString);

				HttpResponse httpResponse = httpClient.execute(httpGet);
				httpStatusCode=httpResponse.getStatusLine().getStatusCode();

				responseString=EntityUtils.toString(httpResponse.getEntity());
				log.debug("P1 httpStatusCode="+httpStatusCode);
				log.debug("P1 responseString="+responseString);
				
				if(httpStatusCode==HttpStatus.SC_OK) {
					int resultCode=0;
					ObjectMapper mapper = new ObjectMapper(); 
					JsonNode jsonNode = null;
					try {
						jsonNode = mapper.readTree(responseString);
						resultCode=jsonNode.get("resultCode").getIntValue();
					} catch(Exception e) {
						httpStatusCode=HttpStatus.SC_UNPROCESSABLE_ENTITY;
						throw new Exception("Unexpected API response");
					}
					
					if(resultCode != 0) {
						String errorMessage=jsonNode.get("error").getTextValue();
						httpStatusCode=HttpStatus.SC_UNAUTHORIZED;
						throw new Exception(errorMessage);
					} else {
						Boolean settingsEnabled=jsonNode.get("settingsEnabled").getBooleanValue();
						if(settingsEnabled==false) {
							httpStatusCode=HttpStatus.SC_UNAUTHORIZED;
							throw new Exception(responseString);
						}
					}
				} else {
					log.error("Get PIQ permission API httpStatusCode="+httpStatusCode);
					throw new Exception(responseString);
				}

				//start uploading
				HttpPost httpPost = new HttpPost("https://"+piqHost+"/floorMapsService/api/upload");
				
				FileBody fileBody = new FileBody(file);
				
				MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
            	multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				multipartEntity.addPart("attachment", fileBody);
				
				httpPost.setEntity(multipartEntity.build());

				//Send request
				httpResponse = httpClient.execute(httpPost);
			 	
				//Verify response if any
				if (httpResponse != null) {
					
					httpStatusCode=httpResponse.getStatusLine().getStatusCode();
					responseString=EntityUtils.toString(httpResponse.getEntity());
										
					log.debug("statusCode="+httpStatusCode);
					log.debug("responseString="+responseString);
					
					if(httpStatusCode==HttpStatus.SC_OK) {
					
						ObjectMapper mapper = new ObjectMapper(); 
						JsonNode jsonNode = mapper.readTree(responseString);
		
						int resultCode=0;
						try {
							resultCode=jsonNode.get("result_code").getIntValue();
							String resultMessage=jsonNode.get("result_msg").getTextValue();
							newFileUUID=jsonNode.get("file_uuid").getTextValue();
							newOriginalFilename=jsonNode.get("original_filename").getTextValue();
						} catch(Exception e) {
							resultCode=-1;
						}
					
						if(resultCode==0) {
							log.debug("newFileUUID="+newFileUUID);
							log.debug("newOriginalFilename="+newOriginalFilename);
						
							//send doConvert
							Map jsonMap=new HashMap();
							jsonMap.put("file_uuid", newFileUUID);
							jsonMap.put("site_code", pdqId);
							jsonMap.put("original_filename", newOriginalFilename);
							jsonMap.put("auto_update", new Boolean(true));				
				
							ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
							String jsonText = ow.writeValueAsString(jsonMap);
				
							httpPost = new HttpPost("https://"+piqHost+"/floorMapsService/api/convert");

							HttpEntity entity = new StringEntity(jsonText);
							httpPost.setEntity(entity);
							httpResponse = httpClient.execute(httpPost);
				
							if (httpResponse != null) {
								httpStatusCode=httpResponse.getStatusLine().getStatusCode();
								responseString=EntityUtils.toString(httpResponse.getEntity());
					
								log.debug("httpStatusCode="+httpStatusCode);
								log.debug("responseString="+responseString);
							}

						}
						
					}
				
				}
				
				returnMap.put("result_code",0);
				
			} catch(Exception e) {
				returnMap.put("result_code",-1);
				exceptionMsg=e.getMessage();
				log.error(exceptionMsg);
			}
			
		} else {
			httpStatusCode=FILE_NOT_FOUND;
			returnMap.put("result_code",-1);
			exceptionMsg="File not found";
			log.error(exceptionMsg);
		}
		
		log.debug("exceptionMsg="+exceptionMsg);
		log.debug("httpStatusCode="+httpStatusCode);
		
		if(httpStatusCode != HttpStatus.SC_OK) {
		
			try {
				
				Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
				Event ev = eventHome.createEvent(
					createdAt, EventType.PIQ_UPDATE,
					EventSeverity.CRITICAL,
				"Power IQ: "+piqHost);

				String summary="";
				String cause="";
				String message="";
				
				if(httpStatusCode == FILE_NOT_FOUND) {
					summary="File not found";
					cause="Uploaded file is not found in dcTrack";
					message="file name:"+filePath;
				} else if(httpStatusCode == CONNECT_FAIL) {
					summary="Cannot reach PIQ at "+piqHost;
					cause="Connectivity Failure";
					message=exceptionMsg;
				} else {
				
					cause=httpStatusCode+" - "+(String)httpStatusMap.get(httpStatusCode);
					if(cause==null) {
						cause=""+httpStatusCode;
					}
					summary="Failure during upload of floor map drawing from dcTrack to Power IQ";
					message=("".equals(exceptionMsg)) ? responseString : exceptionMsg;
				}
				
				log.debug("createEvent...");
				log.debug(" summary="+summary);
				log.debug(" cause="+cause);
				log.debug(" message="+message);
				
				ev.setSummary(summary);
				ev.addParam("Cause", cause);
				ev.addParam("Error", "FloorMaps Service");
				ev.addParam("Message", message);
				eventHome.saveEvent(ev);
				
			} catch(DataAccessException dae) {
				log.error("",dae);
			}
		
		}
	
		return returnMap;
		
	}

	private HttpClient createHttpClient() throws NoSuchAlgorithmException, KeyManagementException{
		SSLContext sslContext = SSLContext.getInstance("SSL");
		
		// Set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] {
				new X509TrustManager() {
					
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
				}
		}, new SecureRandom());
		
		X509HostnameVerifier verifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		
		SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext,verifier);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sf)
                .build());
		
		HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build();
		
		return httpClient;
	}
	
	public EventHome getEventHome() {
		return eventHome;
	}

	public void setEventHome(EventHome eventHome) {
		this.eventHome = eventHome;
	}

}
