package com.raritan.tdz.dctexport.integration.aggregators;

import org.springframework.integration.annotation.Header;

import com.raritan.tdz.dctexport.integration.splitters.DCTExportSplitter;

/**
 * This strategy is used to correlate groups of messages for aggregation based on
 * header EXPORT_SPLITTER_HEADER
 * @author prasanna
 *
 */
public class ExportAggregatorCorrelationStrategy {
	public Object correlatedBy(@Header(DCTExportSplitter.EXPORT_SPLITTER_HEADER) Object itemSplitterHeader){
		return itemSplitterHeader;
	}
}
