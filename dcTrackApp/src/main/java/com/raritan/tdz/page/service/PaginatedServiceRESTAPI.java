package com.raritan.tdz.page.service;

import java.util.List;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;

public interface PaginatedServiceRESTAPI {

    /**
     * Get items for item filter.
     * 
     * @param filterConfig
     * @return
     * @throws DataAccessException
     */
    List<JSONReportFilterResult> getItem(JSONReportFilterConfig filterConfig)
            throws DataAccessException;

    /**
     * Get cabinets for cabinet filter.
     * 
     * @param filterConfig
     * @return
     * @throws DataAccessException
     */
    List<JSONReportFilterResult> getCabinet(JSONReportFilterConfig filterConfig)
            throws DataAccessException;
}