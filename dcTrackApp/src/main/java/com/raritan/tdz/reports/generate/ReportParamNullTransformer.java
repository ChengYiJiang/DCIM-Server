/**
 * 
 */
package com.raritan.tdz.reports.generate;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;

/**
 * This transformer does not do anything with the parameters since
 * the report is expecting no change in the parameters that client sent.
 * @author prasanna
 *
 */
public class ReportParamNullTransformer implements ReportParamTransformer {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportParamTranslator#translate(com.raritan.tdz.reports.dto.ReportCriteriaDTO)
	 */
	@Override
	public ReportCriteriaDTO translate(ReportCriteriaDTO criteriaDTO) {
		return criteriaDTO;
	}

}
