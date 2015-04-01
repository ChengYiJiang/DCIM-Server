/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;

/**
 * This will transform the report parameters which as arrayList to an object array since
 * this is what is expected on the report end.
 * @author prasanna
 *
 */
public class ReportParamObjectArrayTransformer implements
		ReportParamTransformer {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportParamTransformer#translate(com.raritan.tdz.reports.dto.ReportCriteriaDTO)
	 */
	@Override
	public ReportCriteriaDTO translate(ReportCriteriaDTO criteriaDTO) {
		Map<String, Object> originalParams = criteriaDTO.getReportParams();
		ReportCriteriaDTO criteriaDTO2 = criteriaDTO.clone();
		Map<String,Object> modifiedParams = criteriaDTO2.getReportParams();
		
		if (originalParams != null){
			for (Map.Entry<String, Object> originalParamEntry:originalParams.entrySet()){
				if (originalParamEntry.getValue() instanceof ArrayList){
					@SuppressWarnings("rawtypes")
					ArrayList arrayList = (ArrayList) originalParamEntry.getValue();
					Object[] array = arrayList.toArray();
					
					modifiedParams.put(originalParamEntry.getKey(), array);
				} else {
					modifiedParams.put(originalParamEntry.getKey(), originalParamEntry.getValue());
				}
			}
		}
		return criteriaDTO2;
	}

}
