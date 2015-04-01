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
public class ExportObjectTypeAggregatorReleaseStrategy {
	
	public boolean canRelease(List<Message<?>> splitDTOs) {
		boolean result = false;
		if (splitDTOs.size() > 0){
			Integer listCnt = (Integer) splitDTOs.get(0).getHeaders().get(DCTExportSplitter.EXPORT_SPLITTER_COUNT_HEADER);
			result = splitDTOs.size() == listCnt;
		}
		return result;
	}
	
}
