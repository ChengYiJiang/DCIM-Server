package com.raritan.tdz.floormaps.dto;

import java.util.*;

public class ReportDataDTO  implements java.io.Serializable {

	private static final long serialVersionUID = 2636137755639081349L;

	public ReportDataDTO() {
	
	}

	private List<Map> data;
	
	private List<Map> columns;

	private List<Map> legend;
	
	private Map legendSetting;
	
	public List<Map> getData() {
		return data;
	}
	
	public void setData(List<Map> data) {
		this.data=data;
	}
	
	public List<Map> getColumns() {
		return columns;
	}
	
	public void setColumns(List<Map> columns) {
		this.columns=columns;
	}
	
	public List<Map> getLegend() {
		return legend;
	}
	
	public void setLegend(List<Map> legend) {
		this.legend=legend;
	}
	
	public Map getLegendSetting() {
		return legendSetting;
	}
	
	public void setLegendSetting(Map legendSetting) {
		this.legendSetting=legendSetting;
	}

}
