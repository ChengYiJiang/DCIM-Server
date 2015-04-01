package com.raritan.tdz.page.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.home.JSONPaginatedListAdapter;
import com.raritan.tdz.page.home.PaginatedHomeBase;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;

public class PaginatedServiceRESTAPIImpl implements PaginatedServiceRESTAPI {

    private final Logger log = Logger.getLogger(this.getClass());

    @Autowired(required = true)
    private PaginatedService paginatedService;

    @Autowired(required = true)
    private JSONPaginatedListAdapter jsonPaginatedListAdapter;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raritan.tdz.reports.service.ReportsServiceRESTAPI#getItem(com.raritan
     * .tdz.reports.json.JSONReportFilterConfig)
     */
    @Override
    @Transactional(readOnly = true)
    public List<JSONReportFilterResult> getItem(
            JSONReportFilterConfig filterConfig) throws DataAccessException {

        ListCriteriaDTO criteriaDto = getListCriteria();

        // column criteria
        ColumnCriteriaDTO dto;
        ColumnCriteriaDTO criteriaLocation = null;
        ColumnCriteriaDTO criteriaClass = null;
        ColumnCriteriaDTO criteriaStatus = null;
        ColumnCriteriaDTO criteriaName = null;
        ColumnCriteriaDTO criteriaCabinet = null;

        // sorting
        Map<String, Boolean> sortMap = filterConfig.getSortMap();
        if (sortMap != null) {
            Iterator<String> keys = sortMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();

                if (JSONPaginatedListAdapter.JSON_TO_PAGINATED_LIST_MAP
                        .containsKey(key)) {

                    dto = new ColumnCriteriaDTO();
                    dto.setName(JSONPaginatedListAdapter.JSON_TO_PAGINATED_LIST_MAP
                            .get(key));
                    dto.setToSort(true);
                    dto.setSortDescending(sortMap.get(key));
                    criteriaDto.getColumnCriteria().add(dto);

                    switch (key) {
                    case JSONPaginatedListAdapter.JSON_COLUMN_LOCATION:
                        criteriaLocation = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_CLASS:
                        criteriaClass = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_STATUS:
                        criteriaStatus = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_NAME:
                        criteriaName = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_CABINET:
                        criteriaCabinet = dto;
                        break;
                    }
                }
            }
        }

        // filter location
        if (filterConfig.getLocationList() != null
                && filterConfig.getLocationList().size() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_LOCATION,
                            filterConfig.getLocationList(), criteriaLocation));
        }

        // filter class
        if (filterConfig.getClassList() != null
                && filterConfig.getClassList().size() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_CLASS,
                            filterConfig.getClassList(), criteriaClass));
        } else {
            // default classes
            List<String> classList = new ArrayList<String>();
            classList.add("CRAC");
            classList.add("Data Panel");
            classList.add("Device");
            classList.add("Network");
            classList.add("Power Outlet");
            classList.add("Probe");
            classList.add("Rack PDU");
            classList.add("UPS");

            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_CLASS,
                            classList, criteriaClass));
        }

        // filter status
        if (filterConfig.getStatusList() != null
                && filterConfig.getStatusList().size() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_STATUS,
                            filterConfig.getStatusList(), criteriaStatus));
        }

        // filter name
        if (filterConfig.getName() != null
                && filterConfig.getName().trim().length() > 0) {
            criteriaDto.getColumnCriteria().add(
                    getTextFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_NAME,
                            filterConfig.getName(), criteriaName));
        }

        // filter cabinet
        if (filterConfig.getCabinet() != null
                && filterConfig.getCabinet().trim().length() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getTextFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_CABINET,
                            filterConfig.getCabinet(), criteriaCabinet));
        }

        ListResultDTO resultDto = paginatedService.getPageList(criteriaDto,
                PaginatedHomeBase.PAGE_TYPE_ITEMLIST);

        return jsonPaginatedListAdapter
                .adaptPaginatedListResultToJSONArray(filterItem(resultDto));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raritan.tdz.reports.service.ReportsServiceRESTAPI#getCabinet(com.
     * raritan.tdz.reports.json.JSONReportFilterConfig)
     */
    @Override
    @Transactional(readOnly = true)
    public List<JSONReportFilterResult> getCabinet(
            JSONReportFilterConfig filterConfig) throws DataAccessException {

        ListCriteriaDTO criteriaDto = getListCriteria();

        // filter class, "Cabinet" only
        List<String> classList = new ArrayList<String>();
        classList.add("Cabinet");
        criteriaDto.getColumnCriteria().add(
                getLookupFilter(
                        JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_CLASS,
                        classList, null));

        // column criteria
        ColumnCriteriaDTO dto;
        ColumnCriteriaDTO criteriaLocation = null;
        ColumnCriteriaDTO criteriaName = null;
        ColumnCriteriaDTO criteriaRowLabel = null;
        ColumnCriteriaDTO criteriaGrouping = null;

        // sorting
        Map<String, Boolean> sortMap = filterConfig.getSortMap();
        if (sortMap != null) {
            Iterator<String> keys = sortMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();

                if (JSONPaginatedListAdapter.JSON_TO_PAGINATED_LIST_MAP
                        .containsKey(key)) {

                    dto = new ColumnCriteriaDTO();
                    dto.setName(JSONPaginatedListAdapter.JSON_TO_PAGINATED_LIST_MAP
                            .get(key));
                    dto.setToSort(true);
                    dto.setSortDescending(sortMap.get(key));
                    criteriaDto.getColumnCriteria().add(dto);

                    switch (key) {
                    case JSONPaginatedListAdapter.JSON_COLUMN_LOCATION:
                        criteriaLocation = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_NAME:
                        criteriaName = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_ROW_LABEL:
                        criteriaRowLabel = dto;
                        break;
                    case JSONPaginatedListAdapter.JSON_COLUMN_GROUPING:
                        criteriaGrouping = dto;
                        break;
                    }
                }
            }
        }

        // filter location
        if (filterConfig.getLocationList() != null
                && filterConfig.getLocationList().size() > 0) {

            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_LOCATION,
                            filterConfig.getLocationList(), criteriaLocation));
        }

        // filter name
        if (filterConfig.getName() != null
                && filterConfig.getName().trim().length() > 0) {
            criteriaDto.getColumnCriteria().add(
                    getTextFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_NAME,
                            filterConfig.getName(), criteriaName));
        }

        // filter row label
        if (filterConfig.getRowLabel() != null
                && filterConfig.getRowLabel().trim().length() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getTextFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_ROW_LABEL,
                            filterConfig.getRowLabel(), criteriaRowLabel));
        }

        // filter grouping
        if (filterConfig.getGroupingList() != null
                && filterConfig.getGroupingList().size() > 0) {
            criteriaDto
                    .getColumnCriteria()
                    .add(getLookupFilter(
                            JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_GROUPING,
                            filterConfig.getGroupingList(), criteriaGrouping));
        }

        ListResultDTO resultDto = paginatedService.getPageList(criteriaDto,
                PaginatedHomeBase.PAGE_TYPE_ITEMLIST);

        return jsonPaginatedListAdapter
                .adaptPaginatedListResultToJSONArray(resultDto);
    }

    /**
     * Get the paginated list criteria for reports.
     * 
     * @return
     */
    private ListCriteriaDTO getListCriteria() {

        ListCriteriaDTO dto = new ListCriteriaDTO();
        dto.setFitType(ListCriteriaDTO.ALL);
        dto.setMaxLinesPerPage(Integer.MAX_VALUE);
        dto.setColumns(new ArrayList<ColumnDTO>());
        dto.setColumnCriteria(new ArrayList<ColumnCriteriaDTO>());

        // Trick: Add at least one ColumnDTO to avoid the UserInfo null
        // exception from saveUserConfig() in
        // PagiantedHomeBase.getListCriteriaDTO().
        ColumnDTO columnDto = new ColumnDTO();
        columnDto.setFieldLabel("");
        dto.getColumns().add(columnDto);

        return dto;
    }

    /**
     * Get a "text" column criteria of Paginated List.
     * 
     * @param filterName
     * @param condition
     * @return
     */
    private ColumnCriteriaDTO getTextFilter(String filterName,
            String condition, ColumnCriteriaDTO dto) {

        if (dto == null) {
            dto = new ColumnCriteriaDTO();
        }

        dto.setName(filterName);
        dto.setFilter(new FilterDTO());
        dto.getFilter().setEqual(condition);

        return dto;
    }

    /**
     * Get a "lookup" column criteria of Paginated List.
     * 
     * @param filterName
     * @param condition
     * @return
     */
    private ColumnCriteriaDTO getLookupFilter(String filterName,
            List<String> condition, ColumnCriteriaDTO dto) {

        if (dto == null) {
            dto = new ColumnCriteriaDTO();
        }

        dto.setName(filterName);
        dto.setFilter(new FilterDTO());
        dto.getFilter().setIsLookup(true);

        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < condition.size(); i++) {
            codes.append(condition.get(i));
            if (i < condition.size() - 1) {
                codes.append(PaginatedHomeBase.COMMA);
            }
        }
        dto.getFilter().setLookupCodes(codes.toString());

        return dto;
    }

    /**
     * Filter Paginated List result because Paginated List API does not provide
     * not equal to" condition.
     * 
     * @param dto
     *            ListResultDTO
     * @return ListResultDTO
     */
    private ListResultDTO filterItem(ListResultDTO dto) {

        // CR58617 Filter out Subclass = Virtual Machine
        int subClassIndex = -1;
        // CR58577 Filter out WHEN-MOVED items
        int nameIndex = -1;

        // Get column index
        List<ColumnDTO> list = dto.getListCriteriaDTO().getColumns();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)
                    .getFieldName()
                    .equals(JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_SUB_CLASS)) {
                subClassIndex = i;
                continue;
            }

            if (list.get(i).getFieldName()
                    .equals(JSONPaginatedListAdapter.PAGINATED_LIST_FIELD_NAME)) {
                nameIndex = i;
                continue;
            }
        }

        // filter results
        List<Object[]> filtered = new ArrayList<Object[]>();
        if (subClassIndex >= 0) {
            for (Object[] obj : dto.getValues()) {
                if ("Virtual Machine".equals(obj[subClassIndex])) {
                    // CR58617 Filter out Subclass = Virtual Machine
                    continue;
                }

                if (obj[nameIndex] != null
                        && ((String) obj[nameIndex]).endsWith("^^WHEN-MOVED")) {
                    // CR58577 Filter out WHEN-MOVED items
                    continue;
                }

                filtered.add(obj);
            }
        }
        dto.setValues(filtered);

        return dto;
    }
}