package com.raritan.tdz.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQRestClient;

/**
 * The base class for dcTrack Rest Clients. This class handles REST communication failures 
 * and logs them to the events database and automatically clears these failure events
 * when subsequent REST calls succeed.
 * 
 * @author Andrew Cohen
 */
public abstract class RestClient {

	// Common logger used by PIQ services
	protected static Logger log = Logger.getLogger(PIQRestClient.class);
	
	// The event home interface - used for logging REST communication failure events
	private EventHome eventHome;
	
	// The RestTemplate to use - this is configured in spring rest.xml config file and injected
	private RestTemplate restTemplate;
	
	// Rest ERROR tracking on a particular URL
	private Map<String, RestErrors> urlErrors = new HashMap<String, RestErrors>();
	
	private MessageSource messageSource;
	
	/**
	 * Sets the event home. (invoked via Spring property setter)
	 * @param eventHome
	 */
	public void setEventHome(EventHome eventHome) {
		this.eventHome = eventHome;
	}
	
	/**
	 * @return the eventHome for logging system events.
	 */
	public EventHome getEventHome() {
		return eventHome;
	}
	
	/**
	 * Sets the RestTemplate object. (invoked via Spring property setter)
	 * @param eventHome
	 */
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Required
	public final void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Executes a Rest GET call.
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source source of the entity executing this call
	 * @return
	 */
	protected final Map<String, Object> doRestGet(String url, HttpHeaders headers, String source)
		throws RemoteDataAccessException{
		ResponseEntity<Map> resp = null;
		
		try {
			resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>( headers), Map.class);
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.GET, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec ,url);
		}
		
		return validateResponse(resp, url);
	}
	
	/**
	 * Executes a REST DELETE call.
	 * @param url the URL
	 * @param headers HTTP Headers
	 * @param source source of the entity executing this call
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestDelete(String url, HttpHeaders headers, String source) 
		throws RemoteDataAccessException{
		
		try {
			restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<String>( headers), String.class); // JB check this
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.GET, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec, url);
		}
	}
	
	/**
	  * Executes a Rest GET call.
	  * @param url the URL
	  * @param headers HTTP headers
	  * @param source source of the entity executing this call
	  * @return ResponseEntity of type returnType
	  */
	 protected final ResponseEntity<?> doRestGet(String url, HttpHeaders headers, String source, Class<?> returnType)
	 	throws RemoteDataAccessException {
	  ResponseEntity<?> resp = null;
	  
	  try {
		   resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>( headers ), returnType);
		   clearCommFailureEvent( url );
	  }
	  catch (RestClientException e) {
		   long eventId = logCommFailureEvent(url, HttpMethod.GET, e, source);
		   ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
		   ec.addExceptionItem("eventId", eventId);
		   throw new RemoteDataAccessException(ec, url);
	  }
	  
	  return resp;
	 }
	 
	
	/**
	 * Execute a REST POST.
	 * @param body the post body
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source source of the entity executing this call
	 * @return
	 */
	protected final Map<String, Object> doRestPost(Map<String, Object> body, String url, HttpHeaders headers, String source)
		throws RemoteDataAccessException  {
		ResponseEntity<Map> resp = null;
		
		try {
			resp = restTemplate.postForEntity(url, new HttpEntity<Map>(body, headers), Map.class);
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.GET, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec ,url);
		}
		
		return validateResponse(resp, url);
	}
	

	/**
	 * Execute a REST POST.
	 * @param request
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source source of the entity executing this call
	 * @param responseType type of response
	 * @return
	 * @throws RemoteDataAccessException
	 */
	protected final Object doRestPost(Object request, String url, HttpHeaders headers, String source, Class<?> responseType)
		throws RemoteDataAccessException  {
		Object resp = null;
		
		try {
			resp = restTemplate.postForObject(url, new HttpEntity<Object>(request, headers), responseType);
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.POST, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec, url);
		}
		
		return resp;
	}
	
	/**
	 * Execute a REST PUT.
	 * @param body the body
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source of the entity executing this call
	 */
	protected final void doRestPut(Map<String, Object> body, String url, HttpHeaders headers, String source)
			throws RemoteDataAccessException   {
		try {
			restTemplate.put(url, new HttpEntity<Map>(body, headers));
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.GET, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec, url);
		}
	}
	
	/**
	 * Execute a REST PUT.
	 * @param request Object to be posted.
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source of the entity executing this call
	 * @throws RemoteDataAccessException
	 */
	protected final void doRestPut(Object request, String url, HttpHeaders headers, String source)
			throws RemoteDataAccessException   {
		try {
			restTemplate.put(url, new HttpEntity<Object>(request, headers));
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			long eventId = logCommFailureEvent(url, HttpMethod.PUT, e, source);
			ExceptionContext ec = new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e);
			ec.addExceptionItem("eventId", eventId);
			throw new RemoteDataAccessException(ec, url);
		}
	}
	
	/**
	 * Execute a REST POST.
	 * @param request Object to be updated.
	 * @param url the URL
	 * @param headers HTTP headers
	 * @param source source of the entity executing this call
	 * @param responseType type of response
	 * @return
	 * @throws RemoteDataAccessException
	 */
	protected final Object doRestPut(Object request, String url, HttpHeaders headers, String source, Class<?> responseType)
		throws RemoteDataAccessException  {
		Object resp = null;
		
		try {
			ResponseEntity<?> respEntity = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<Object>(request, headers), responseType);
			if (respEntity != null)
				resp = respEntity.getBody();
			clearCommFailureEvent( url );
		}
		catch (RestClientException e) {
			logCommFailureEvent(url, HttpMethod.PUT, e, source);
			throw new RemoteDataAccessException(new ExceptionContext(getApplicationCodeForException(e), this.getClass(), e),url);
		}
		
		return resp;
	}

		
	/**
	 * Builds an appropriate event summary based on the a REST exception.
	 * @param restException a REST exception
	 * @return the event summary
	 */
	protected abstract String getEventSummaryForException(RestClientException restException, String url);
		
	/**
	 * Builds an appropriate event summary for when a previous REST call failure now succeeds.
	 */
	protected abstract String getEventSummaryForCommRestored(String url);
		
	/**
	 * A hook for handling any HTTP error responses.
	 * @param exception the  status code exception
	 * @param event the REST error event
	 */
	protected abstract void handleHttpStatusCodeException(HttpStatusCodeException exception, Event event);
	
	//
	// Private methods
	//

	/**
	 * Logs a communication failure event to the event log.
	 */
	private long logCommFailureEvent(String url, HttpMethod method, RestClientException error, String source) {
		String msg = messageSource.getMessage("remote.error",
				new Object[] { url, error.getMessage() },
				null
		);
		log.error( msg );
		long eventId;
		
		//
		// We synchronize here to ensure only one thread per RestClient
		// can be logging or clearing a communication failure.
		//
		synchronized (this) {
			RestErrors errors = urlErrors.get( url );
			if (errors == null) {
				errors = new RestErrors(url, source);
				urlErrors.put(url, errors);
			}
			eventId = errors.handleError( error );
		}
		
		return eventId;
	}
	
	/**
	 * REturns the dcTrack event type for the given Rest error.
	 * @param exception
	 * @return
	 */
	private ApplicationCodesEnum getApplicationCodeForException(RestClientException exception) {
		ApplicationCodesEnum code = null;
		
		if (exception instanceof ResourceAccessException) {
			// Can't connect
			code = ApplicationCodesEnum.REST_CALL_FAILED_CANNOT_ACCESS_RESOURCE;
		}
		else if (exception instanceof HttpClientErrorException) {
			// HTTP 4xx status code
			code = ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR;
		}
		else if (exception instanceof HttpServerErrorException) {
			// HTTP 5xx status code
			code = ApplicationCodesEnum.REST_CALL_FAILED_SERVER_ERROR;
		}
		
		return code;
	}
	
	/**
	 * Clears the current communication failure event in the log.
	 */
	private void clearCommFailureEvent(String url) {
		//
		// We synchronize here to ensure only one thread per RestClient
		// can be logging or clearing a communication failure.
		//
		synchronized (this) {
			RestErrors errors = urlErrors.remove( url );
			if (errors != null) {
				errors.clearErrors();
			}
		}
	}
		
	/**
	 * Validates a raw REST client response.
	 * @param resp the response as a map
	 * @param url the URL for the REST call
	 * @return the response body
	 */
	private Map<String, Object> validateResponse(ResponseEntity<Map> resp, String url) {
		Map<String, Object> respMap = null;
		
		// Validate response
		if (resp == null) {
			log.warn("PIQRestClient: No response found! url="+ url);
			return respMap;
		}
		
		// Validate response body
		respMap = resp.getBody();
		if (respMap == null) {
			log.warn("PIQRestClient: No response body found! url=" + url);
		}
		log.debug(resp.getBody());
		return respMap;
	}
	
	/**
	 * Internal class used for tracking and logging REST errors associated with a particular URL.
	 */
	private class RestErrors {
		private String url;
		private String source;
		private Map<EventType, Long> loggedEvents; // Map error event types to event Ids
		private Map<EventType, Long> errorCounts; // Map error event types to a running tally of failures
		
		/**
		 * Create a new RestErrors object associated with a url.
		 * @param url the url
		 * @param source the source of error events to create for this url
		 */
		RestErrors(String url, String source) {
			this.url = url;
			this.source = source;
			if (url.contains("///")) {
				log.error(this.getClass().getSimpleName() + ": Potentially bad PIQ URL Detected for REST calls: " + url);
			}
			loggedEvents = new HashMap<EventType, Long>();
			errorCounts = new HashMap<Event.EventType, Long>();
		}
		
		/**
		 * Handles a Rest Error by writing it to the event log if it is the first
		 * occurrence of this event type.
		 * @param type event type
		 * @param exception the REST exception detail
		 * @return
		 */
		public long handleError(RestClientException exception) { 
			EventType type = getEventTypeForException( exception );
			Long eventId = loggedEvents.get( type );
			
			// Increment error count
			Long count = errorCounts.get( type );
			if (count == null) count = 0L;
			errorCounts.put(type, count + 1);
			
			if (eventId == null) {
				// No failure of this event type logged for URL, so log it.
				try {
					Event event = eventHome.createEvent(type, EventSeverity.CRITICAL, source);
					eventId = event.getId();
					if (url.contains("///")) {
						System.out.println("BAD url: " + url);
					}
					loggedEvents.put(type, eventId);
					//event.setSummary("REST call failed for URL " + url);
					event.setSummary( getEventSummaryForException(exception, url) );
					event.addParam("Cause", exception.getMessage());
					eventHome.saveEvent( event );
					
					if (exception instanceof HttpStatusCodeException) {
						handleHttpStatusCodeException((HttpStatusCodeException)exception, event);
					}
				}
				catch (DataAccessException e) {
					log.warn("RestClient: Failed to log communication failure event!", e);
				}
			}
			else {
				// A failure event has already been logged
				if (log.isDebugEnabled()) {
					log.debug( errorCounts.get( type ) + " failures for REST URL " + url );
				}
			}
			
			return eventId;
		}
		
		/**
		 * Clear any errors associated with the URL.
		 */
		public void clearErrors() {
			List<Long> eventIds = new LinkedList<Long>();
			
			for (EventType type : loggedEvents.keySet()) {
				long eventId = loggedEvents.get( type );
				Event ev = null;
				try {
					ev = eventHome.getEventDetail( eventId );
					if (ev != null) {
						eventIds.add( ev.getId() );
					}
				}
				catch (DataAccessException e) {
					log.warn("", e);
				}
			}
			
			if (!eventIds.isEmpty()) {
				try {
					// Create a communication restored event that clears any failure events for the URL
					Event restoreEvent = eventHome.createEvent( EventType.COMMUNICATION_RESTORED, EventSeverity.INFORMATIONAL, source );
					//restoreEvent.setSummary("REST call Succeeded for URL " + url);
					restoreEvent.setSummary( getEventSummaryForCommRestored( url ) );
					// Clear problem events
					eventHome.clearEvents( eventIds, restoreEvent );
				}
				catch (DataAccessException e) {
					log.warn("RestClient: Failed to clear  ", e);
				}
			}
			
			loggedEvents.clear();
			errorCounts.clear();
		}
		
		/**
		 * REturns the dcTrack event type for the given Rest error.
		 * @param exception
		 * @return
		 */
		private EventType getEventTypeForException(RestClientException exception) {
			EventType type = null;
			
			if (exception instanceof ResourceAccessException) {
				// Can't connect
				type = EventType.CANNOT_ACCESS_RESOURCE;
			}
			else if (exception instanceof HttpClientErrorException) {
				// HTTP 4xx status code
				type = EventType.CLIENT_ERROR;
			}
			else if (exception instanceof HttpServerErrorException) {
				// HTTP 5xx status code
				type = EventType.SERVER_ERROR;
			}
			else {
				// Default to server error
				type = EventType.SERVER_ERROR;
			}
			
			return type;
		}	
	}
}
