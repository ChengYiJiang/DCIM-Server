package com.raritan.tdz.reports.json;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class JSONReportFilterConfig {

    /**
     * columnName:String and isDescending:Boolean
     */
    private Map<String, Boolean> sortMap;

    private List<String> locationList;
    private List<String> classList;
    private List<String> statusList;
    private List<String> groupingList;

    private String name;
    private String cabinet;
    private String rowLabel;

    @JsonProperty("sort")
    public Map<String, Boolean> getSortMap() {
        return sortMap;
    }

    @JsonProperty("location")
    public List<String> getLocationList() {
        return locationList;
    }

    @JsonProperty("class")
    public List<String> getClassList() {
        return classList;
    }

    @JsonProperty("status")
    public List<String> getStatusList() {
        return statusList;
    }

    @JsonProperty("grouping")
    public List<String> getGroupingList() {
        return groupingList;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("cabinet")
    public String getCabinet() {
        return cabinet;
    }

    @JsonProperty("rowLabel")
    public String getRowLabel() {
        return rowLabel;
    }

    @Override
    public String toString() {
        return "JSONReportFilterConfig [sortMap=" + sortMap + ", locationList="
                + locationList + ", classList=" + classList + ", statusList="
                + statusList + ", groupingList=" + groupingList + ", name="
                + name + ", cabinet=" + cabinet + ", rowLabel=" + rowLabel
                + "]";
    }
}