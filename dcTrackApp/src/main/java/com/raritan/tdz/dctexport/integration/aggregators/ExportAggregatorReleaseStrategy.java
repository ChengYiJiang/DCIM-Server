package com.raritan.tdz.dctexport.integration.aggregators;

import java.util.List;

import org.springframework.integration.Message;

import com.raritan.tdz.dctexport.integration.splitters.DCTExportSplitter;

/**
 * This is the strategy that will release the aggregated messages based on the
 * size of the DTO list and the EXPORT_SPLITTER_COUNT_HEADER
 * @author prasanna
 *
 */
public class ExportAggregatorReleaseStrategy {
	
	public boolean canRelease(List<Message<?>> splitDTOs) {
		boolean result = false;
		if (splitDTOs.size() > 0){
			
			int currentCount = 0;
			for (Message<?> splitDTO:splitDTOs){
				Integer commentsSize = (Integer) splitDTO.getHeaders().get(DCTExportObjectTypeAggregator.OBJECT_TYPE_COMMENTS_SIZE);
				List<String> myList = (List<String>) splitDTO.getPayload();
				currentCount += (myList.size() - (commentsSize + 1) );
			}
			Integer listCnt = (Integer) splitDTOs.get(0).getHeaders().get(DCTExportSplitter.EXPORT_SPLITTER_COUNT_TOTAL);
			result = currentCount == listCnt;
		}
		return result;
	}
	
}
