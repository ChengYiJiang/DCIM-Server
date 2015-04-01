package com.raritan.tdz.page.dto;

public class ColumnCriteriaDTO implements java.io.Serializable {

	private static final long serialVersionUID = 3935829420182564896L;

	private String name;

	private boolean toSort;

	private boolean sortDescending;

	private FilterDTO filter;
	
	public boolean visible;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isToSort() {
		return toSort;
	}

	public void setToSort(boolean toSort) {
		this.toSort = toSort;
	}

	public boolean isSortDescending() {
		return sortDescending;
	}

	public void setSortDescending(boolean sortDescending) {
		this.sortDescending = sortDescending;
	}

	public FilterDTO getFilter() {
		return filter;
	}

	public void setFilter(FilterDTO filter) {
		this.filter = filter;
	}

}
