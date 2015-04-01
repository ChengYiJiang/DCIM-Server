package com.raritan.tdz.controllers.assetmgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.page.service.PaginatedServiceRESTAPI;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.json.JSONReportConfig;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;
import com.raritan.tdz.reports.service.ReportsServiceRESTAPI;
import com.raritan.tdz.session.RESTAPIUserSessionContext;


@Controller
@RequestMapping("/v1/reports")
public class ReportsController extends BaseController {

	private final Logger log = Logger.getLogger(this.getClass());

	@Autowired(required = true)
	private ReportsServiceRESTAPI reportingServiceRESTAPI;

    @Autowired(required = true)
    private PaginatedServiceRESTAPI paginatedServiceRESTAPI;

    @Autowired(required = true)
    private ItemService itemService;

	/**
	 *  QUERY report settings by reportId for current user. If reportId is not
	 *  provided, send all report settings for this user.
	 *  	
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/reports/settings?id=<reportId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/reports/settings?id=<reportId>
	 *  
	 *  examples of usage:
	 *  get report settings for report #1 
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i 
	 *  -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/reports/settings?reportId=1
	 *  
	 *   or to get all report settings:
	 *   curl -H Content-Type:application/json -H Accept:application/json -s -i 
	 *  -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/reports/settings
	 */
	@RequestMapping(value="reportSettings",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getReportSettings(HttpServletRequest request, 
			@RequestParam(value="reportId", required=false)  Long reportId,
			HttpServletResponse response) 
					throws BusinessValidationException, Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query report settings() id=" + reportId );
		}
		checkAcceptMediaType(request);

		try{
			if( reportId != null ){
				ret = reportingServiceRESTAPI.getReportDetailsExt(reportId, RESTAPIUserSessionContext.getUser());
			}else { //send all reports
				ret = reportingServiceRESTAPI.getAllReports(RESTAPIUserSessionContext.getUser());
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}

		return ret;
	}

	/**
	 *  Update report configuration. Must provide new report configuration and reportId
	 *  	
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/reports/config?reportId=<reportId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/reports/config?id=<reportId>
	 *  
	 *  examples of usage:
	 *  delete all previous report configs for report #1
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i -X PUT
	 *  -d@'[]' -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/reports/config?reportId=1
	 *  
	 *   or update existing report #1 configs with new one stored in file f1
	 *   curl -H Content-Type:application/json -H Accept:application/json -s -i -X PUT 
	 *  -d@f1 -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/reports/config?reportId=1
	 */
	@RequestMapping(value="reportConfig", method=RequestMethod.PUT)
	public @ResponseBody Map<String,Object> updateReportConfiguration (
			HttpServletRequest request,
			@RequestParam(value="reportId", required=true) Long reportId,
			@RequestBody Set<JSONReportConfig> reportConfig 
		) throws Throwable {
		
		if(log.isDebugEnabled()){
			log.info("REST PUT call: updateReportConfiguration() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();

		try{
			ret = reportingServiceRESTAPI.updateReportConfiguration(reportId, reportConfig, RESTAPIUserSessionContext.getUser()); 
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch (IllegalArgumentException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));	
		}catch(Exception e){
			e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;

	}

	
	@RequestMapping(value="report",method=RequestMethod.POST)
	public @ResponseBody ReportStatusDTO generateReport(HttpServletRequest request, 
	@RequestBody ReportCriteriaDTO criteriaDTO,
	HttpServletResponse response) 
			throws BusinessValidationException, Throwable {
		checkAcceptMediaType(request);
		ReportStatusDTO ret = new ReportStatusDTO();
		try{
			ret = reportingServiceRESTAPI.generateReport(criteriaDTO);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
	}
	
	@RequestMapping(value="report",method=RequestMethod.GET)
	public @ResponseBody ReportStatusDTO getReportStatus(HttpServletRequest request, 
			@RequestParam(value="reportId", required=false)  Long reportId,
			HttpServletResponse response) 
					throws BusinessValidationException, Throwable {
		ReportStatusDTO ret = new ReportStatusDTO();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query report status id=" + reportId );
		}
		checkAcceptMediaType(request);

		try{
			if( reportId != null ){
				ret = reportingServiceRESTAPI.getReportStatus(reportId);
			} //TODO: Need to implement when reportId is bad. Probably it is a BAD_REQUEST???
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}

		return ret;
	}

	@RequestMapping(value="report",method=RequestMethod.DELETE)
	public @ResponseBody void cancelReportGeneration(HttpServletRequest request, 
			@RequestParam(value="reportId", required=false)  Long reportId,
			HttpServletResponse response) 
					throws BusinessValidationException, Throwable {
		if(log.isDebugEnabled()){
			log.debug("REST DELETE call: cancel report generation, id=" + reportId );
		}
		checkAcceptMediaType(request);

		try{
			if( reportId != null ){
				reportingServiceRESTAPI.cancelReportGeneration(reportId);
			} //TODO: Need to implement when reportId is bad. Probably it is a BAD_REQUEST???
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
	}

    /**
     * Get items for item filter.
     * 
     * @param request
     * @param filterConfig
     * @return
     * @throws Throwable
     */
    @RequestMapping(value = "itemFilter", method = RequestMethod.PUT)
    public @ResponseBody
    List<JSONReportFilterResult> getItem(HttpServletRequest request,
            @RequestBody JSONReportFilterConfig filterConfig) throws Throwable {

        if (log.isDebugEnabled()) {
            log.info("REST PUT call: getItem() invoked");
        }

        checkAcceptMediaType(request);

        try {
            // return paginatedServiceRESTAPI.getItem(filterConfig);
            // CR59096
            return itemService.getItemForReport(filterConfig);

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                log.error(e.getMessage());
            }
            throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
    }

    /**
     * Get cabinets for cabinet filter.
     * 
     * @param request
     * @param filterConfig
     * @return
     * @throws Throwable
     */
    @RequestMapping(value = "cabinetFilter", method = RequestMethod.PUT)
    public @ResponseBody
    List<JSONReportFilterResult> getCabinet(HttpServletRequest request,
            @RequestBody JSONReportFilterConfig filterConfig) throws Throwable {

        if (log.isDebugEnabled()) {
            log.info("REST PUT call: getCabinet() invoked");
        }

        checkAcceptMediaType(request);

        try {
            return paginatedServiceRESTAPI.getCabinet(filterConfig);

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                log.error(e.getMessage());
            }
            throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
    }
}