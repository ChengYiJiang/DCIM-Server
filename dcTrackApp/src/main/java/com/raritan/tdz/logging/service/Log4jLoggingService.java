package com.raritan.tdz.logging.service;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import com.raritan.tdz.logging.dto.ClientRuntimeErrorDTO;

/**
 * Logging service that logs client information using a log4j logger.
 * @author Andrew Cohen
 */
public class Log4jLoggingService implements LoggingService {

	private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
	
	private Logger log;
	private MessageSource msgSource;
	
	public Log4jLoggingService() {
	}
	
	public void setLoggerName(String loggerName) {
		log = Logger.getLogger( loggerName );
	}
	
	public void setMessageSource(MessageSource msgSource) {
		this.msgSource = msgSource;
	}
	
	@Override
	public void logRuntimeError(ClientRuntimeErrorDTO runtimeError) {
		SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
		
		String msg = msgSource.getMessage("client.runtimeError",
				new Object[] {
					sdf.format(runtimeError.getTime()),
					runtimeError.getUser(),
					runtimeError.getDcTrackVersion(),
					runtimeError.getBrowserName(),
					runtimeError.getBrowserAgent(),
					runtimeError.getBrowserVersion(),
					runtimeError.getFlashPlayerVersion(),
					runtimeError.isDebuggerVersion(),
					runtimeError.getClientOS(),
					runtimeError.getStackTrace()
				},
				null);
		
		log.error( msg );
	}
}
