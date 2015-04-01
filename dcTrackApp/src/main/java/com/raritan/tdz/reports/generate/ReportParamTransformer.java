/**
 * 
 */
package com.raritan.tdz.reports.generate;

import com.raritan.tdz.reports.dto.ReportCriteriaDTO;

/**
 * This interface will translate the parameters for the report from the
 * format given by the client to a format that the report generator expects
 * @author prasanna
 *
 */
public interface ReportParamTransformer {
	/**
	 * Given the original criteria DTO, translate the format of the 
	 * filter so that the report generator can use it.
	 * <p>Please note that the original criteria object is not replaced with new one.
	 * This will return a new copy of the original with the filterMap replaced to 
	 * the format the report generator expects</p>
	 * @param criteriaDTO
	 * @return
	 */
	ReportCriteriaDTO translate(ReportCriteriaDTO criteriaDTO);
}
