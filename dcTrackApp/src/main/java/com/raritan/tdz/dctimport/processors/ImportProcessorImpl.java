/**
 * 
 */
package com.raritan.tdz.dctimport.processors;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctimport.dto.DCTImport;

/**
 * @author prasanna
 *
 */
public class ImportProcessorImpl implements ImportProcessor {
	
	private final Logger logger = Logger.getLogger("dctImport");
	
	public static final String ADD_OPERATION = "add";
	public static final String EDIT_OPERATION = "edit";
	public static final String DELETE_OPERATION = "delete";
	public static final String UNMAP_OPERATION = "unmap";
	
	private final Map<String, MessageChannel> messageChannelMap;
	
	
	public ImportProcessorImpl(Map<String, MessageChannel> messageChannelMap){
		this.messageChannelMap = messageChannelMap;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.processors.ImportProcessor#process(com.raritan.tdz.dctimport.domain.DCTImport, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public void process(DCTImport importDTO)
			throws Exception {
		Message<DCTImport> message = MessageBuilder
				.withPayload(importDTO)
				.setReplyChannelName("nullChannel")
				.setErrorChannelName("importExceptionHandlerChannel")
				.build();
		
		Map<String,Object> map = BeanUtils.describe(importDTO);
		if (importDTO.getOperation().equalsIgnoreCase(ADD_OPERATION)) {
			processAdd(message);
		} else if (importDTO.getOperation().equalsIgnoreCase(EDIT_OPERATION)) {
			processUpdate(message);
		} else if (importDTO.getOperation().equalsIgnoreCase(DELETE_OPERATION)) {
			processDelete(message);
		} else if (importDTO.getOperation().equalsIgnoreCase(UNMAP_OPERATION)) {
				processUnmap(message);
		} else
			throw new Exception();

	}

	/**
	 * Process Add operation
	 * @param importDTO
	 * @throws Exception
	 */
	private void processAdd(Message<DCTImport> importDTOMsg)
			throws Exception{
		
		MessageChannel channel = messageChannelMap.get(ADD_OPERATION);
		
		logger.warn("Process Add Called");
		
		channel.send(importDTOMsg);
	}
	

	/**
	 * Process Update operation
	 * @param importDTO
	 * @throws Exception
	 */
	private  void processUpdate(Message<DCTImport> importDTOMsg)
			throws Exception{
		MessageChannel channel = messageChannelMap.get(EDIT_OPERATION);
		
		logger.warn("Process Update Called");
		
		channel.send(importDTOMsg);
	}

	/**
	 * Process Delete operation
	 * @param importDTO
	 * @throws Exception
	 */
	private  void processDelete(Message<DCTImport> importDTOMsg)
			throws Exception{
		MessageChannel channel = messageChannelMap.get(DELETE_OPERATION);
		
		logger.warn("Process Delete Called");
		
		channel.send(importDTOMsg);
	}

	/**
	 * Process Unmap operation
	 * @param importDTO
	 * @throws Exception
	 */
	private  void processUnmap(Message<DCTImport> importDTOMsg)
			throws Exception{
		MessageChannel channel = messageChannelMap.get(UNMAP_OPERATION);
		
		logger.warn("Process Unmap Called");
		
		channel.send(importDTOMsg);
	}

}
