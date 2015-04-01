/**
 * 
 */
package com.raritan.tdz.dctimport.integration.routers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.MessageChannel;
import org.springframework.web.context.support.ServletContextResource;

import com.raritan.tdz.dctimport.integration.exceptions.ImportFileTypeInvalidException;

import static com.raritan.tdz.dctimport.utils.ContentTypeResolver.*;

/**
 * @author prasanna
 *
 */
public class ImportFileTypeRouterImpl implements ImportFileTypeRouter {
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	Map<String, MessageChannel> fileTypeMap = new LinkedHashMap<String, MessageChannel>();
	
	public ImportFileTypeRouterImpl(Map<String, MessageChannel> fileTypeMap){
		this.fileTypeMap = fileTypeMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.ImportFileTypeRouter#resolve(java.lang.String)
	 */
	@Override
	public MessageChannel resolve(String fileName) throws Exception {

		Resource inResource = servletContext != null ? new ServletContextResource(servletContext, "/../dcTrackImport/" + fileName):resourceLoader.getResource("../../" + fileName);
		
		MessageChannel channel = fileTypeMap.get(getContentType(inResource.getFile()));
		if (channel == null){
			throw new ImportFileTypeInvalidException();
		}
		return channel;
	}

}
