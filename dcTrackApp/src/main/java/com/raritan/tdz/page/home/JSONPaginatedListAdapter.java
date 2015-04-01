package com.raritan.tdz.page.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.reports.json.JSONReportFilterResult;

public interface JSONPaginatedListAdapter {

    // Column names from client-side JSON
    public static final String JSON_COLUMN_LOCATION = "location";
    public static final String JSON_COLUMN_CLASS = "class";
    public static final String JSON_COLUMN_STATUS = "status";
    public static final String JSON_COLUMN_NAME = "name";
    public static final String JSON_COLUMN_CABINET = "cabinet";
    public static final String JSON_COLUMN_ROW_LABEL = "rowLabel";
    public static final String JSON_COLUMN_GROUPING = "grouping";

    // Field names in Paginated List
    public static final String PAGINATED_LIST_FIELD_ITEM_ID = "itemId";
    public static final String PAGINATED_LIST_FIELD_LOCATION = "Location";
    public static final String PAGINATED_LIST_FIELD_CLASS = "Class";
    public static final String PAGINATED_LIST_FIELD_STATUS = "Status";
    public static final String PAGINATED_LIST_FIELD_NAME = "Name";
    public static final String PAGINATED_LIST_FIELD_CABINET = "Cabinet";
    public static final String PAGINATED_LIST_FIELD_ROW_LABEL = "Row Label";
    public static final String PAGINATED_LIST_FIELD_GROUPING = "Grouping";
    public static final String PAGINATED_LIST_FIELD_SUB_CLASS = "Subclass";

    /**
     * Mapping of JSON columns to paginated List fields
     */
    public static final Map<String, String> JSON_TO_PAGINATED_LIST_MAP = new HashMap<String, String>();

    /**
     * Adapt the ListResultDTO from paginated List to a JSON array.
     * 
     * @param dto
     *            ListResultDTO
     * @return JSON Array
     */
    public List<JSONReportFilterResult> adaptPaginatedListResultToJSONArray(
            ListResultDTO dto);
}