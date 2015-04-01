/**
 * 
 */
package com.raritan.tdz.dctexport.integration.splitters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dctexport.dto.ExportSplitDTO;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;


/**
 * Splits the incoming resultDTOMsgs which will eventually get
 * aggregated to form the CSV file
 * @author prasanna
 *
 */
public class ItemExportSplitter implements DCTExportSplitter{
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	

	private static String tiClass = "tiClass";
	private static String tiMounting = "tiMounting";
	private static String tiFormFactor = "tiFormFactor";
	private static String tiSubclass = "tiSubclass";
	
	private final Map<String, Long> itemClassLksMap = new LinkedHashMap<String, Long>();
	private final Map<String,Long> itemSubclassLksMap = new LinkedHashMap<String, Long>();
	
	private final List<String> columnNames = new ArrayList<String>(){{
		add(tiClass);
		add(tiMounting);
		add(tiFormFactor);
		add(tiSubclass);
	}};
	
	
	private final List<Long> ignoreUniqueValues;
	
	public ItemExportSplitter(List<Long> ignoreUniqueValues){
		this.ignoreUniqueValues = ignoreUniqueValues;
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.dctexport.integration.splitters.DCTExportSplitter#split(org.springframework.integration.Message)
	 */
	@Transactional(readOnly=true)
	@Override
	public List<Message<ExportSplitDTO>> split(Message<List<Map<String,Object>>> resultDTOMsg) throws Exception{
		
		initLks();
		
		List<Map<String,Object>> resultDTOList = resultDTOMsg.getPayload();
		MessageHeaders headers = resultDTOMsg.getHeaders();
		
		//The total count is used while we are aggregating these messages and we want to release the aggregate
		Map<Long, Integer> totalCountMap = new LinkedHashMap<Long, Integer>();
		
		//Go through the resultDTOList and convert it to a map of uniqueValue and ExportSplitDTO
		List<ExportSplitDTO> splitDTOs = createExportSplitDTOMap(resultDTOList, totalCountMap);
		
		//Go through the splitDTOs and create the list of messages
		List<Message<ExportSplitDTO>> resultDTOMsgList = createMessages(totalCountMap,splitDTOs,headers);

		return resultDTOMsgList;
	}

	private void initLks(){
		if (itemClassLksMap.size() > 0 && itemSubclassLksMap.size() > 0) return;
		
		List<LksData> classLksList = systemLookupFinderDAO.findByLkpType(SystemLookup.LkpType.CLASS);
		List<LksData> subclassLksList = systemLookupFinderDAO.findByLkpType(SystemLookup.LkpType.SUBCLASS);
		
		for (LksData classLks:classLksList){
			itemClassLksMap.put(classLks.getLkpValue(), classLks.getLksId());
		}
		
		for (LksData subclassLks:subclassLksList){
			itemSubclassLksMap.put(subclassLks.getLkpValue(), subclassLks.getLkpValueCode());
		}
	}


	/**
	 * Go through the resultDTOList and convert it to a map of uniqueValue and ExportSplitDTO
	 * @param resultDTOList
	 * @param totalCountMap
	 * @return
	 */
	private List<ExportSplitDTO> createExportSplitDTOMap(
			List<Map<String, Object>> resultDTOList,
			Map<Long, Integer> totalCountMap) {
		
		List<ExportSplitDTO> splitDTOs = new ArrayList<ExportSplitDTO>();
		
		for (Map<String,Object> resultDTO:resultDTOList){
			if (resultDTO.get(tiClass) == null 
					|| resultDTO.get(tiMounting) == null 
					|| resultDTO.get(tiFormFactor) == null) 
				continue;
			
			//Find the uniqueValue
			String subclass = resultDTO.get(tiSubclass) != null ? resultDTO.get(tiSubclass).toString():null;
			Long uniqueValue = getClassMountingFormFactorUniqueValue(
						resultDTO.get(tiClass).toString(),
						resultDTO.get(tiMounting).toString(),
						resultDTO.get(tiFormFactor).toString(), subclass);
			
			if (ignoreUniqueValues.contains(uniqueValue)) continue;
			
			//Create the ExportSplitDTO and assign the result 
			ExportSplitDTO splitDTO = createExportSplitDTO(resultDTO);
			splitDTO.setUniqueValue(uniqueValue);
			splitDTOs.add(splitDTO);
			
			//Fill the totalCountMap for the uniqueValue
			fillTotalCountMap(totalCountMap, uniqueValue);
		}
		
		return splitDTOs;
	}

	
	/**
	 * Go through the splitDTOs and create the list of messages
	 * @param totalCountMap
	 * @param splitDTOs
	 * @return
	 */
	private List<Message<ExportSplitDTO>> createMessages(Map<Long, Integer> totalCountMap, 
				List<ExportSplitDTO> splitDTOs,MessageHeaders headers) {
		List<Message<ExportSplitDTO>> resultDTOMsgList = new  ArrayList<Message<ExportSplitDTO>>();
		Integer totalSplitDTOCount = splitDTOs.size();
		for (ExportSplitDTO splitDTO:splitDTOs) {
			
			Long uniqueValue = splitDTO.getUniqueValue();
			Integer uniqueValueCnt = totalCountMap.get(uniqueValue);
			
			Message<ExportSplitDTO> itemListResultSplitDTOMsg = 
				MessageBuilder
				.withPayload(splitDTO)
				.copyHeaders(headers)
				.setHeader(EXPORT_SPLITTER_HEADER, uniqueValue)
				.setHeader(EXPORT_SPLITTER_COUNT_HEADER, uniqueValueCnt)
				.setHeader(EXPORT_SPLITTER_COUNT_TOTAL, totalSplitDTOCount)
				.build();
			
			resultDTOMsgList.add(itemListResultSplitDTOMsg);
		}
		
		return resultDTOMsgList;
	}



	/**
	 * Fill the totalCount map per uniqueValue
	 * @param totalCountMap
	 * @param uniqueValue
	 */
	private void fillTotalCountMap(Map<Long, Integer> totalCountMap,
			Long uniqueValue) {
		Integer uniqueValueCnt = totalCountMap.get(uniqueValue);
		if (uniqueValueCnt == null){
			totalCountMap.put(uniqueValue, new Integer(1));
		} else {
			uniqueValueCnt++;
			totalCountMap.put(uniqueValue, uniqueValueCnt);
		}
	}


	/**
	 * create a splitDTO and fill that with the resultDTO
	 * @param resultDTO
	 * @return
	 */
	private ExportSplitDTO createExportSplitDTO(Map<String, Object> resultDTO) {
		ExportSplitDTO splitDTO = new ExportSplitDTO();
		splitDTO.setResultMap(resultDTO);
		return splitDTO;
	}
	
	

	
	//TODO: Move this to common utilities.
	private Long getClassMountingFormFactorUniqueValue(String className, String mounting, String formFactor, String subClass){
		Long lookupValue = null;
		
		Long classLksId = null;
		
		//Get the class lks id
		if (className != null){
			classLksId = itemClassLksMap.get(className);
		}
		
		
		//====== Compute unique value ==========
		
		//Check to see if we have any value in the 
		//SystemLookup.MountingFormFactorUniqueValue.mountingFormFactorUniqueMap
		//...
		String mountingFormFactorUniqueValue = null;
		
		Long subclassLkpValueCode = null;
		if (subClass != null){
			subclassLkpValueCode = itemSubclassLksMap.get(subClass);
		}
		//For Subclass
		if (mountingFormFactorUniqueValue == null && subclassLkpValueCode != null){
			mountingFormFactorUniqueValue = SystemLookup.MountingFormFactorUniqueValue.mountingFormFactorUniqueMap.get(subclassLkpValueCode.toString());
		}

		//If not check for mounting
		if (mountingFormFactorUniqueValue == null && mounting != null){
			mountingFormFactorUniqueValue = SystemLookup.MountingFormFactorUniqueValue.mountingFormFactorUniqueMap.get(mounting);
		}	
		
		//If not check for formFactor
		if (mountingFormFactorUniqueValue == null && formFactor != null){
			mountingFormFactorUniqueValue = SystemLookup.MountingFormFactorUniqueValue.mountingFormFactorUniqueMap.get(formFactor);
		}
		
		//If we have both mountingFormFactorUniqueValue and classLksId we can combine them to return 
		//the unique lookup value.
		if (mountingFormFactorUniqueValue != null && classLksId != null){
			String lookupValueStr = classLksId.toString() + mountingFormFactorUniqueValue;
			lookupValue = Long.parseLong(lookupValueStr);
		}
		
		return lookupValue;
	}
}
