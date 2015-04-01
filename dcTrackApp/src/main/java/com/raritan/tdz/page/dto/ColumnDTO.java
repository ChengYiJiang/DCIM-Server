package com.raritan.tdz.page.dto;

public class ColumnDTO implements java.io.Serializable, Cloneable {

	private static final long serialVersionUID = 1275348379282837589L;
	
	//Filter Type
	public final static int UNFILTERABLE = 0;
	public final static int TEXT = 1;
	public final static int LOOKUP = 2;
	public final static int INTEGER = 3;
	public final static int FLOAT = 4;
	public final static int DATE = 5;
	
	private String fieldName;

	private String fieldLabel;
	
	private String uiComponentId;
	
	private boolean defaultColumn;
	
	private boolean sortable;

	private boolean filterable;
	
	private boolean visible;
	
	private int filterType;
	
	private int width=-1;
	
	private boolean filtered;
	
	private String format;
	
	/** This field is for generating the repeat custom field sql in condition. */
	private String customIds = "";
	
	/**
	* Attribute for client use
	*/
	private boolean hideColumn;
	
	public String getCustomIds() {
		return customIds;
	}

	public void setCustomIds(String customIds) {
		this.customIds = customIds;
	}

	/**
	* Attribute for client use
	*/
	private boolean defaultSort;
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}
	
	public String getUiComponentId() {
		return uiComponentId;
	}

	public void setUiComponentId(String uiComponentId) {
		this.uiComponentId = uiComponentId;
	}
	
	public boolean isDefaultColumn() {
		return defaultColumn;
	}

	public void setDefaultColumn(boolean defaultColumn) {
		this.defaultColumn = defaultColumn;
	}
	
	public boolean isDefaultSort() {
		return defaultSort;
	}

	public void setDefaultSort(boolean defaultSort) {
		this.defaultSort = defaultSort;
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}

	public int getFilterType() {
		return filterType;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}
	
	public boolean isFiltered() {
		return filtered;
	}
	
	public void setHideColumn(boolean hideColumn) {
		this.hideColumn = hideColumn;
	}
	
	public boolean isHideColumn() {
		return hideColumn;
	}
	
	public String toString() {
		String str = super.toString();
		StringBuffer buffer = new StringBuffer();
		buffer.append(str+"\n");
		
		buffer.append("fieldName:"+fieldName+" ");
		buffer.append("fieldLabel:"+fieldLabel+" ");
		buffer.append("uiComponentId:"+uiComponentId+" ");
		buffer.append("defaultColumn:"+defaultColumn+" ");
		buffer.append("defaultSort:"+defaultSort+" ");
		buffer.append("sortable:"+sortable+" ");
		buffer.append("filterable:"+filterable+" ");
		buffer.append("visible:"+visible+" ");
		buffer.append("filterType:"+filterType+" ");
		buffer.append("width:"+width+" ");
		buffer.append("filtered:"+filtered+" ");
		buffer.append("format:"+format+" ");
		buffer.append("hideColumn:"+hideColumn+" ");
		buffer.append("customIds:"+customIds+" ");
		buffer.append("\n");
		
		return buffer.toString();
	}
	
	@Override
	public Object clone() {
		ColumnDTO copy = new ColumnDTO();

		copy.setFieldName( this.getFieldName() );
		copy.setFieldLabel( this.getFieldLabel() );
		copy.setUiComponentId( this.getUiComponentId() );
		copy.setDefaultColumn( this.isDefaultColumn() );
		copy.setDefaultSort( this.isDefaultSort() );
		copy.setSortable( this.isSortable() );
		copy.setFilterable( this.isFilterable() );
		copy.setFilterType( this.getFilterType() );
		copy.setVisible( this.isVisible() );
		copy.setWidth( this.getWidth() );
		copy.setFormat( this.getFormat() );
		copy.setFiltered( this.isFiltered() );
		copy.setHideColumn( this.isHideColumn() );
		copy.setCustomIds(this.getCustomIds());
		
		return copy;
	}

}
