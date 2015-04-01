package com.raritan.tdz.piq.home;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.RestClient;

/**
 * Base class for PIQ Rest clients which provides support for setting the credentials,
 * building the full REST URL, supplying common HTTP headers, and providing details
 * of communication failures.
 * 
 * @author Andrew Cohen
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public abstract class PIQRestClientBase extends RestClient implements PIQRestClient {
	
	// The list of all concrete PIQ Rest Client implementations.
	// This is maintained so that if the PIQ configuration changes, we can update all clients.
	private static final List<PIQRestClient> piqRestClients = new LinkedList<PIQRestClient>();
	
	@Autowired
	protected ItemDAO itemDAO;

	/**
	 * Reloads the current PIQ communication settings (i.e, host & credentials) and propagates
	 * these changes to all PIQ Rest Client implementations.
	 * @param appSettings
	 * @throws DataAccessException
	 */
	@Deprecated
	public static void reloadCommunicationSettings(ApplicationSettings appSettings) throws DataAccessException {
		String ipAddr = appSettings.getProperty( Name.PIQ_IPADDRESS );
		String username = appSettings.getProperty( Name.PIQ_USERNAME );
		String password = appSettings.getProperty( Name.PIQ_PASSWORD );
		
		for (PIQRestClient piqRestClient: piqRestClients) {
			 piqRestClient.setIPAddress( ipAddr );
			 piqRestClient.setCredentials( username, password );
		}
	}
	
	// Base64 encoded token for Basic Authentication
	private String authToken;
	
	// IP Address of remote PIQ
	private String ipAddr;
	
	// Relative path of REST service (Example: "v2/rack_units")
	private String service;
	
	protected String eventSource = EVENT_SOURCE;
	
	// AppSettings
	ApplicationSettings appSettings = null;
	
	// PIQ system event logger
	private PIQSystemEventLogger piqSysEventLogger;
	
	
	@Override
	public String getEventSource() {
		return eventSource;
	}

	public PIQRestClientBase(ApplicationSettings appSettings) throws DataAccessException {
//		// Get PIQ configuration settings
//		setIPAddress( appSettings.getProperty( Name.PIQ_IPADDRESS ) );
//		setCredentials( appSettings.getProperty( Name.PIQ_USERNAME ), appSettings.getProperty( Name.PIQ_PASSWORD ));
//		piqRestClients.add( this );
		this.appSettings = appSettings;
	}

	protected PIQSystemEventLogger getPiqSysEventLogger() {
		return piqSysEventLogger;
	}

	public void setPiqSysEventLogger(PIQSystemEventLogger piqSysEventLogger) {
		this.piqSysEventLogger = piqSysEventLogger;
	}

	public final boolean isAppSettingsEnabled(){
		boolean result = false;
		if (appSettings != null) {
			try {
				result = appSettings.getBooleanProperty( Name.PIQ_POLLING_ENABLED);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result;
	}

	protected PIQRestClientBase() {
	}
	

	@Override
	public String getIpAddress() {
		return ipAddr;
	}

	/**
	 * Set the IP address of the PIQ to connect to.
	 * @param ipAddress
	 */
	@Override
	public final void setIPAddress(String ipAddr) {
		if (StringUtils.hasText(ipAddr)) {
			this.ipAddr = ipAddr;
			if (!eventSource.contains(ipAddr)) {
				eventSource = (new StringBuilder(EVENT_SOURCE).append(" (").append(ipAddr).append(")")).toString();
			}
		}
		else {
			log.warn(this.getClass().getSimpleName() + ": set an empty or null PIQ IP Address!");
		}
	}
	
	@Override
	public final void setCredentials(String username, String password) {
		// Base64 encode "username:password" to get authorization header
		StringBuffer auth = new StringBuffer(username);
		auth.append(":");
		auth.append(password);
		authToken = Base64.encodeBase64String( auth.toString().getBytes() ).trim();
	}
	
	@Override
	public final String getService() {
		return service;
	}
	
	@Override
	public final void setService(String service) {
		this.service = service;
	}
	
	//
	// Package protected methods for executing REST calls
	//
	
	protected static final String INVALID_ITEM_ERR = "ActiveRecord::RecordInvalid";
	protected static final String IP_CONFLICT_MESSAGE = "ip address is already";
	protected static final String EXTERNAL_KEY_EXISTS = "external key";
	protected static final String LICENSE_ERR = "license";
	protected static final String JOB_ERR="Job::JobError";
	protected static final String JOB_COMPLETED="COMPLETED";
	protected static final String SNMP3_ERR = "snmp3";
	protected static final String IPADDRESS_CHANGE_ERR = "Api::AddressChangeError";
	protected static final String IPADDRESS_CHANGE_MSG = "ip address ";
	protected static final String TIMEOUT_MSG = "SocketTimeoutException";
	
	/**
	 * Determines if the error returned by PIQ is an IP Address conflict.
	 * @param error the error
	 * @return
	 */
	protected final boolean isPIQIpAddressConflict(ErrorJSON error) {
		return containsMessage(error, INVALID_ITEM_ERR, IP_CONFLICT_MESSAGE);
	}
	
	/**
	 * Determines if the error returned by PIQ is a license related error.
	 * @param error the error
	 * @return
	 */
	protected final boolean isPIQLicenseError(ErrorJSON error) {
		return containsMessage(error, INVALID_ITEM_ERR, LICENSE_ERR);
	}
	
	/**
	 * Determines if the error returned by PIQ is an IP Address conflict.
	 * @param error the error
	 * @return
	 */
	protected final boolean isPIQExternalKeyExist(ErrorJSON error) {
		return containsMessage(error, INVALID_ITEM_ERR,  EXTERNAL_KEY_EXISTS);
	}

	/**
	 * Determines if the error returned by PIQ is a job related error.
	 * @param error the error
	 * @return
	 */
	protected final boolean isJobError(ErrorJSON error) {
		return containsMessage(error, JOB_ERR, JOB_COMPLETED);
	}
	
	protected final boolean isSnmpError(ErrorJSON error) {
		return containsMessage(error, INVALID_ITEM_ERR, SNMP3_ERR);
	}

	/**
	 * Determines if the error returned by PIQ is for IPAddress change related error.
	 * @param error the error
	 * @return
	 */
	protected final boolean isPIQIpAddressChangeResponse(ErrorJSON error) {
		return containsMessage(error, IPADDRESS_CHANGE_ERR, IPADDRESS_CHANGE_MSG);
	}
	
	/**
	 * Execute a REST DELETE call for the specified PIQ ID.
	 * @param id the PIQ ID
	 */
	protected final void doRestDelete(String piqId) throws RemoteDataAccessException {
		doRestDelete(getRestURL(getService(), piqId), getHttpHeaders(), eventSource);
	}
	
	/**
	 * Execute a REST DELETE call for the specified PIQ ID and service.
	 * @param piqId the PIQ ID
	 * @param service the service name (example: "v2/pdus")
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestDelete(String piqId, String service) throws RemoteDataAccessException {
		doRestDelete(getRestURL(service, piqId), getHttpHeaders(), eventSource);
	}
	
	/**
	 * Execute a REST GET call for the specified service.
	 * @return the response as a map, or null if there was a problem with the REST call or response.
	 * @throws RemoteDataAccessException 
	 */
	protected final Map<String, Object> doRestGet() throws RemoteDataAccessException {
		return this.doRestGet( null );
	}
	
	/**
	 * Execute a REST GET call for the specified service and url suffix.
	 * @param urlSuffix optional suffix to append to URL
	 * @return the response as a map, or null if there was a problem with the REST call or response.
	 * @throws RemoteDataAccessException 
	 */
	protected final Map<String, Object> doRestGet(String urlSuffix) throws RemoteDataAccessException {
		String url = getRestURL( getService(),  urlSuffix );
		try {
			return super.doRestGet(url, getHttpHeaders(), eventSource);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST GET call for the specified service and url suffix.
	 * @param urlSuffix optional suffix to append to URL
	 * @return the response as a map, or null if there was a problem with the REST call or response.
	 * @throws RemoteDataAccessException 
	 */
	protected final ResponseEntity<?> doRestGet(String urlSuffix, Class<?> responseType) throws RemoteDataAccessException {
		String url = getRestURL( getService(),  urlSuffix );
		try {
			return super.doRestGet(url, getHttpHeaders(), eventSource,responseType);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST GET call for the specified service and url suffix.
	 * @param urlSuffix optional suffix to append to URL
	 * @return the response as a map, or null if there was a problem with the REST call or response.
	 * @throws RemoteDataAccessException 
	 */
	protected final ResponseEntity<?> doRestGet(String service, String urlSuffix, Class<?> responseType) throws RemoteDataAccessException {
		String url = getRestURL( service,  urlSuffix );
		try {
			return super.doRestGet(url, getHttpHeaders(), eventSource,responseType);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST POST call for the specified service.
	 * @param body the POST body
	 * @return response map
	 * @throws RemoteDataAccessException 
	 */
	protected final Map<String, Object> doRestPost(Map<String, Object> body) throws RemoteDataAccessException {
		return this.doRestPost(body, null);
	}
	
	protected final Object doRestPostRequest(Object request, Class<?> responseType) throws RemoteDataAccessException {
		return this.doRestPost(request, null,responseType);
	}
	
	
	/**
	 * Execute a REST POST call for the specified service and urlSuffix.
	 * @param body the POST body
	 * @param urlSuffix optional url suffix appended to service
	 * @return response map
	 * @throws RemoteDataAccessException 
	 */
	protected final Map<String, Object> doRestPost(Map<String, Object> body, String urlSuffix) throws RemoteDataAccessException {
		String url = getRestURL( getService(), urlSuffix );
		try {
			return super.doRestPost(body, url, getHttpHeaders(), eventSource);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST POST call for the specified service, urlSuffix and a return Type
	 * @param request
	 * @param urlSuffix
	 * @param responseType
	 * @return
	 * @throws RemoteDataAccessException
	 */
	protected final Object doRestPost(Object request, String urlSuffix, Class<?> responseType) throws RemoteDataAccessException {
		String url = getRestURL( getService(), urlSuffix );
		try {
			return super.doRestPost(request, url, getHttpHeaders(), eventSource, responseType);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	

	/**
	 * Execute a REST POST call for the specified service, urlSuffix and a return Type
	 * @param request
	 * @param urlSuffix
	 * @param responseType
	 * @return
	 * @throws RemoteDataAccessException
	 */
	protected final Object doRestPost(Object request, String service, String urlSuffix, Class<?> responseType) throws RemoteDataAccessException {
		String url = getRestURL( service, urlSuffix );
		try {
			return super.doRestPost(request, url, getHttpHeaders(), eventSource, responseType);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}

	
	/**
	 * Execute a REST PUT call for the specified Object Map.
	 * @param body the PUT body
	 * @return response map
	 * @throws RemoteDataAccessException 
	 */
	protected final void doRestPut(Map<String, Object> body) throws RemoteDataAccessException {
		this.doRestPut(body, null);
	}
	
	/**
	 * Execute a REST PUT call for the specified Object.
	 * @param request
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestPut(Object request) throws RemoteDataAccessException {
		this.doRestPut(request, null);
	}
	
	/**
	 * Execute a REST PUT call for the specified Object Map and urlSuffix.
	 * @param body the PUT body
	 * @param urlSuffix optional url suffix appended to service
	 * @return response map
	 * @throws RemoteDataAccessException 
	 */
	protected final void doRestPut(Map<String, Object> body, String urlSuffix) throws RemoteDataAccessException {
		String url = getRestURL( getService(), urlSuffix );
		try {
			super.doRestPut(body, url, getHttpHeaders(), eventSource);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	
	/**
	 * Execute a REST PUT call for the specified Object and urlSuffix.
	 * @param request
	 * @param urlSuffix
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestPut(Object request, String urlSuffix) throws RemoteDataAccessException {
		String url = getRestURL( getService(), urlSuffix );
		try {
			super.doRestPut(request, url, getHttpHeaders(), eventSource);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST PUT call for the specified Object and urlSuffix.
	 * @param request
	 * @param urlSuffix
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestPut(Object request, String service, String urlSuffix) throws RemoteDataAccessException {
		String url = getRestURL( service, urlSuffix );
		try {
			super.doRestPut(request, url, getHttpHeaders(), eventSource);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	/**
	 * Execute a REST PUT call for the specified service, urlSuffix and a return Type
	 * @param request
	 * @param urlSuffix
	 * @param responseType
	 * @return
	 * @throws RemoteDataAccessException
	 */
	protected final Object doRestPut(Object request, String service, String urlSuffix, Class<?> responseType) throws RemoteDataAccessException {
		String url = getRestURL( service, urlSuffix );
		try {
			return super.doRestPut(request, url, getHttpHeaders(), eventSource, responseType);
		}
		catch (RemoteDataAccessException e) {
			handlePIQRemoteDataException( e );
			throw e;
		}
	}
	
	//
	// RestClient implementation for setting the event summary
	//
	
	@Override
	protected String getEventSummaryForException(RestClientException restException, String url) {
		StringBuffer b = new StringBuffer();
		
		if (restException instanceof ResourceAccessException) {
			b.append("Cannot reach PIQ at ");
		}
		else if (restException instanceof HttpClientErrorException) {
			HttpStatus status = ((HttpClientErrorException) restException).getStatusCode();
			if (status == HttpStatus.NOT_ACCEPTABLE) {
				// PIQ authentication failed
				b.append("Cannot authenticate with PIQ at ");
			}
			else {
				b.append("Cannot reach PIQ at ");
			}
		}
		else if (restException instanceof HttpServerErrorException) {
			b.append("An internal error occurred with PIQ at ");
		}
		
		b.append( url );
		
		return b.toString();
	}

	@Override
	protected String getEventSummaryForCommRestored(String url) {
		StringBuffer b = new StringBuffer("Established communication with PIQ at ");
		b.append( url );
		return b.toString();
	}
	
	/**
	 * Builds the full Rest URL for the specified PIQ service.
	 * @param piqService the name of the service (i.e., relative url)
	 * @param urlSuffix optional suffix to service
	 */
	protected String getRestURL(String piqService, String urlSuffix) {
		StringBuffer buf = new StringBuffer("https://"); // All PIQ Web APIS work over SSL
		buf.append( ipAddr );
		if (ipAddr == null || ipAddr.trim().isEmpty()) {
			System.out.println( this.getClass().getSimpleName() + " : BAD IP Address: " + ipAddr);
		}
		buf.append("/api/");
		buf.append( piqService );
		if (urlSuffix != null) {
			buf.append("/");
			buf.append( urlSuffix );
		}
		return buf.toString();
	}
	
	/**
	 * Get common HTTP headers required by all PIQ Rest calls.
	 * @return
	 */
	protected HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		List<String> authList = new LinkedList<String>();
		
		// For basic authentication
		authList.add("Basic " + authToken);
		headers.put("Authorization", authList);
		List<String> accept = new LinkedList<String>();
		accept.add("application/json");
		headers.put("Accept", accept);
		
		// NOTE: We shouldn't have to add this explicitly as jackson will automatically
		// set the Content-Type header to "application/json; charset=utf-8".
		// However, PIQ 3.1.0-16 has a problem with the charset portion,
		// so we will set the Content-Type header explicitly here.
		List<String> contentType = new LinkedList<String>();
		contentType.add("application/json");
		headers.put("Content-Type", contentType);
		
		return headers;
	}
	
	/*
	 * Log the relevant PIQ error messages from the JSON response.
	 * @see com.raritan.tdz.util.RestClient#handleHttpStatusCodeException(org.springframework.web.client.HttpStatusCodeException, com.raritan.tdz.events.domain.Event)
	 */
	@Override
	protected final void handleHttpStatusCodeException(HttpStatusCodeException exception, Event event) {
		String resp = exception.getResponseBodyAsString();
		
		if (resp != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				ErrorJSON errors = mapper.readValue(resp, ErrorJSON.class);
				if (errors != null) {
					event.addParam("Error", errors.getError());
					int msgCount = 1;
					for (String msg: errors.getMessages()) {
						event.addParam("Message #" + msgCount, msg);
						msgCount++;
					}
				}
				
				if (isPIQLicenseError(errors)) {
					event.setSummary( getEventHome().getMessageSource().getMessage(
							"piqUpdate.licenseError",
							new Object[] { this.ipAddr },
							null)
					);
					try {
						EventHome home = getEventHome();
						home.saveEvent( event );
						home.setSeverity(event, EventSeverity.CRITICAL);
						home.setEventType(event, EventType.PIQ_UPDATE);
					}
					catch (DataAccessException e) {
						log.error("", e);
					}
				}
			} 
			catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	/**
	 * Returns true if this item can be synched to PIQ.
	 * Deleted and Discarded items will not be created PIQ.
	 * @param item the item to sync to PIQ
	 * @return
	 */
	protected final boolean canSyncItem(Item item) {
//		if (item == null) return false;
//		if (item.getStatusLookup() != null && 
//				(item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.DELETED ||
//					item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.DISCARDED)) {
//			return false;
//		}
		// FIXME: Need more testing before enabling above code
		return true;
	}
	
	//
	// Private methods
	//
	
	private void handlePIQRemoteDataException(RemoteDataAccessException e) {
		ExceptionContext ec = e.getExceptionContext();
		if (ec != null) {
			Object ex = ec.getExceptionItem( ExceptionContext.EXCEPTION );
			
			if ((ex != null) && (ex instanceof HttpClientErrorException)) {
				ErrorJSON error = null;
				
				try {
					HttpClientErrorException clientEx = (HttpClientErrorException)ex;
					ObjectMapper mapper = new ObjectMapper();
					JsonFactory jsonFactory = mapper.getJsonFactory();
					JsonParser jp = jsonFactory.createJsonParser( clientEx.getResponseBodyAsByteArray() );
					error = jp.readValueAs( ErrorJSON.class );
				}
				catch (Throwable t) {
					log.error("Problem handling PIQ REST 4xx response body", t);
				}
				
				e.setRemoteExceptionDetail( error );
			}
		}
	}
	
	private final boolean containsMessage(ErrorJSON error, String errorType, String message) {
		if (error == null) return false;
		String err = error.getError();
		//
		// WARNING - *** Brittle code alert! *** because we are relying on 
		// specific error messages coming from PIQ. It would be much
		// better for PIQ to send us something in the response header
		// for this case.
		//
		if (err != null && err.equals( errorType )) {
			for (String errMessage : error.getMessages()) {
				if (errMessage.toLowerCase().contains( message) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public final boolean isItemsPiqHostChanged(long itemId) { 
		String itemsPiqHost =  itemDAO.getItemsPiqHost(itemId);
		return  (!itemsPiqHost.equals(getIpAddress()));
	}

}
