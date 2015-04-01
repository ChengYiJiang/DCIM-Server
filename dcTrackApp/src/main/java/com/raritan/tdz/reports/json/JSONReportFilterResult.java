package com.raritan.tdz.reports.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class JSONReportFilterResult {

    private Long id;
    private String location;
    private String className;
    private String status;
    private String name;
    private String cabinet;
    private String rowLabel;
    private String grouping;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("class")
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("cabinet")
    public String getCabinet() {
        return cabinet;
    }

    public void setCabinet(String cabinet) {
        this.cabinet = cabinet;
    }

    @JsonProperty("rowLabel")
    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    @JsonProperty("grouping")
    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    @Override
    public String toString() {
        return "JSONReportFilterResult [id=" + id + ", location=" + location
                + ", className=" + className + ", status=" + status + ", name="
                + name + ", cabinet=" + cabinet + ", rowLabel=" + rowLabel
                + ", grouping=" + grouping + "]";
    }
}