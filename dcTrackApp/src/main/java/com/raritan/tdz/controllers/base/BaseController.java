package com.raritan.tdz.controllers.base;


import org.apache.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;

@RequestMapping(consumes="application/json", produces="application/json")
public class BaseController implements  ApplicationContextAware{
	private ApplicationContext applicationContext;

	private final Logger log = Logger.getLogger(BaseController.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
                    throws BeansException {
            this.applicationContext = applicationContext;
    }
    
    public ApplicationContext getApplicationContext(){
    	return applicationContext;
    }
	
	protected void checkAcceptMediaType(HttpServletRequest request) throws DCTRestAPIException{
		String returnMsgFormat = request.getHeader("accept");
		HttpStatus status = HttpStatus.OK;
		if( returnMsgFormat != null && !returnMsgFormat.equals("*/*")){
			String [] talkens = returnMsgFormat.split("/");
			if( talkens.length != 2 || !talkens[0].equalsIgnoreCase("application")){
				status = HttpStatus.BAD_REQUEST;
			}else if( ! talkens[1].equalsIgnoreCase("json")){
				status = HttpStatus.NOT_ACCEPTABLE;
			}
			
			if( status != HttpStatus.OK ){
				StringBuilder msg = new StringBuilder();
				msg.append("Accept '");
				msg.append(returnMsgFormat);
				msg.append("' not supported");
                log.info("[CR58782] request URL = " + request.getRequestURL());
                log.info("[CR58782] content type = " + request.getContentType());
				throw(new DCTRestAPIException(msg.toString(), status, Thread.currentThread().getStackTrace()));
			}
		}

	}
	@ExceptionHandler(Throwable.class)
	public ModelAndView handleException(Throwable e, HttpServletResponse response){
		MappingJacksonJsonView jsonView = new MappingJacksonJsonView();
		DCTRestAPIException dcte = null;
		e.printStackTrace();
		//we already created DCTRestAPIEXcpetion just read info and send it back
		if( e instanceof DCTRestAPIException ){
			dcte = (DCTRestAPIException)e;
		}else if( e instanceof HttpMediaTypeNotAcceptableException ||
				e instanceof HttpMediaTypeNotSupportedException ||
				e instanceof HttpRequestMethodNotSupportedException){
			dcte = new DCTRestAPIException(HttpStatus.NOT_ACCEPTABLE, e);
		}else if( e instanceof TypeMismatchException){
			dcte = new DCTRestAPIException(HttpStatus.NOT_FOUND, e);
		}else if( e instanceof HttpMessageNotReadableException ){
			dcte = new DCTRestAPIException(HttpStatus.UNPROCESSABLE_ENTITY, e);
		}else{
			dcte = new DCTRestAPIException(HttpStatus.BAD_REQUEST, e);
		}
		assert( dcte != null);
		Map<String, ?> retval = dcte.getExceptionDetails();
		response.setStatus(dcte.getStatus().value());
		
		return new ModelAndView(jsonView, retval);
	}

}
