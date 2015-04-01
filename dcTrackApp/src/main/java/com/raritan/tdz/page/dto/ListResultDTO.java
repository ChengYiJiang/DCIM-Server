package com.raritan.tdz.page.dto;

import java.util.*;

public class ListResultDTO  implements java.io.Serializable {

private static final long serialVersionUID = 3636137710639081349L;

	public ListResultDTO() {
	
	}
	
	public ListCriteriaDTO getListCriteriaDTO() {
		return listCriteriaDTO;
	}

	public void setListCriteriaDTO(ListCriteriaDTO listCriteriaDTO) {
		this.listCriteriaDTO = listCriteriaDTO;
	}

	public String[] getFieldLabels() {
		return fieldLabels;
	}

	public void setFieldLabels(String[] fieldLabels) {
		this.fieldLabels = fieldLabels;
	}

	public List<Object[]> getValues() {
		return values;
	}

	public void setValues(List<Object[]> values) {
		this.values = values;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	private ListCriteriaDTO listCriteriaDTO;
	
	private String[] fieldLabels;

	private List<Object[]> values;

	private int totalRows;	

}
