package com.raritan.tdz.component.inspector.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.component.inspector.adaptor.CabinetMetricAdaptor;
import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.component.inspector.home.CabinetMetricHome;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * Controller for cabinet metrics.
 */
@Controller
@RequestMapping("/inspector")
public class CabinetMetricController extends BaseController {
    private static Logger log = Logger.getLogger(CabinetMetricController.class);

    @Autowired
    private CabinetMetricHome cabinetMetricHome;

    /**
     * Get cabinet metrics.
     * 
     * @param cabinetId
     * @param request
     * @param response
     * @return a map of metrics
     * @throws Throwable
     * @throws NumberFormatException
     */
    @RequestMapping(value = "/metrics/{cabinetId}", method = RequestMethod.GET)
    @ResponseBody
    public String getCabinetMetrics(@PathVariable long cabinetId,
            HttpServletRequest request, HttpServletResponse response)
            throws NumberFormatException, Throwable {

        CabinetMetricDto dto = cabinetMetricHome.getCabinetMetrics(cabinetId,
                RESTAPIUserSessionContext.getUser().getUnits());

        return CabinetMetricAdaptor.convert(dto);
    }
}