package com.raritan.tdz.page.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.reports.json.JSONReportFilterResult;

public class JSONPaginatedListAdapterImpl implements JSONPaginatedListAdapter {

    static {
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_LOCATION, PAGINATED_LIST_FIELD_LOCATION);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_CLASS, PAGINATED_LIST_FIELD_CLASS);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_STATUS, PAGINATED_LIST_FIELD_CLASS);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_NAME, PAGINATED_LIST_FIELD_NAME);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_CABINET, PAGINATED_LIST_FIELD_CABINET);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_ROW_LABEL, PAGINATED_LIST_FIELD_ROW_LABEL);
        JSON_TO_PAGINATED_LIST_MAP.put(JSON_COLUMN_GROUPING, PAGINATED_LIST_FIELD_GROUPING);
    }

    private final Logger log = Logger.getLogger(this.getClass());

    /*
     * (non-Javadoc)
     * 
     * @see com.raritan.tdz.reports.home.JSONReportFilterAdapter#
     * adaptPaginatedListResultToJSONArray
     * (com.raritan.tdz.page.dto.ListResultDTO)
     */
    @Override
    public List<JSONReportFilterResult> adaptPaginatedListResultToJSONArray(
            ListResultDTO dto) {

        Map<String, Integer> columnIndexMap = getColumnIndexMap(dto
                .getListCriteriaDTO().getColumns());

        // Set data to a JSON array.
        JSONReportFilterResult result;
        List<JSONReportFilterResult> resultList = new ArrayList<JSONReportFilterResult>();
        for (Object[] obj : dto.getValues()) {

            result = new JSONReportFilterResult();

            result.setId((Long) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_ITEM_ID)]);

            result.setLocation((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_LOCATION)]);

            result.setClassName((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_CLASS)]);

            result.setStatus((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_STATUS)]);

            result.setName((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_NAME)]);

            result.setCabinet((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_CABINET)]);

            result.setRowLabel((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_ROW_LABEL)]);

            result.setGrouping((String) obj[columnIndexMap
                    .get(PAGINATED_LIST_FIELD_GROUPING)]);

            resultList.add(result);
        }

        return resultList;
    }

    /**
     * Get indexes of Paginated List fields.
     * 
     * @param list
     *            ColumnDTO list
     * @return
     */
    private Map<String, Integer> getColumnIndexMap(List<ColumnDTO> list) {

        Map<String, Integer> map = new HashMap<String, Integer>();

        for (int i = 0; i < list.size(); i++) {

            switch (list.get(i).getFieldName()) {

            case PAGINATED_LIST_FIELD_ITEM_ID:
                map.put(PAGINATED_LIST_FIELD_ITEM_ID, i);
                break;

            case PAGINATED_LIST_FIELD_LOCATION:
                map.put(PAGINATED_LIST_FIELD_LOCATION, i);
                break;

            case PAGINATED_LIST_FIELD_CLASS:
                map.put(PAGINATED_LIST_FIELD_CLASS, i);
                break;

            case PAGINATED_LIST_FIELD_STATUS:
                map.put(PAGINATED_LIST_FIELD_STATUS, i);
                break;

            case PAGINATED_LIST_FIELD_NAME:
                map.put(PAGINATED_LIST_FIELD_NAME, i);
                break;

            case PAGINATED_LIST_FIELD_CABINET:
                map.put(PAGINATED_LIST_FIELD_CABINET, i);
                break;

            case PAGINATED_LIST_FIELD_ROW_LABEL:
                map.put(PAGINATED_LIST_FIELD_ROW_LABEL, i);
                break;

            case PAGINATED_LIST_FIELD_GROUPING:
                map.put(PAGINATED_LIST_FIELD_GROUPING, i);
                break;
            }
        }

        return map;
    }
}