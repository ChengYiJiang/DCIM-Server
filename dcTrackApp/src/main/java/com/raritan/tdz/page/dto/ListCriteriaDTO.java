package com.raritan.tdz.page.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListCriteriaDTO implements java.io.Serializable {	
	
	/**
	   * Determines if a de-serialized file is compatible with this class.
	   *
	   * Maintainers must change this value if and only if the new version
	   * of this class is not compatible with old versions. See Sun docs
	   * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
	   * /serialization/spec/version.doc.html> details. </a>
	   *
	   * Not necessary to include in first version of the class, but
	   * included here as a reminder of its importance.
	   */
	private static final long serialVersionUID = 5636137710639081369L;
	
	/** The parameter of fitType. */
	public static final int FIT=0;
	
	/** The parameter of fitType. */
	//public static final int FIRST_LIST_QUERY=-2;
	
	/** The parameter of fitType. */
	//public static final int NONE = 1;
	
	public static final int ROWS_PER_PAGE_50 = 50;
	
	public static final int ROWS_PER_PAGE_100 = 100;
	
	public static final int ROWS_PER_PAGE_250 = 250;
	
	public static final int ROWS_PER_PAGE_500 = 500;
	
	public static final int ROWS_PER_PAGE_1000 = 1000;
	
	/** The parameter of fitType. */
	public static final int ALL = -1;		
	
	/** The max lines per page. */
	private int maxLinesPerPage;
	
	/** 
	 * The state of the rows per page. -1 for ALL, 0 for Fit, 1 for none, -2 for first query. 
	 */
	private int fitType = 0;
	
	private int pageNumber;

	private List<ColumnCriteriaDTO> columnCriteria;
	
	private List<ColumnDTO> columns;
	
	/** Client attribute for show/hide column */
	private boolean userAddColumn=false; 
	
	/** Client attribute for show/hide column */
	private boolean userRemoveColumn=false; 
	
	/** For the query with timezone from client */
	private String currentUtcTimeString = "";		
	
	/** This parameter is for identifying the settings version, if we have to replace it. */
	private String dcTrackVersion = "";
	
	public static final String DCTRACK_VERSION = "3.1";
	
	/** For the first time entering item list page  **/
	public boolean firstQuery=false;
	
	public ListCriteriaDTO(){
		setCurrentUtcTimeString();
		setDcTrackVersion(DCTRACK_VERSION);
	}
	
	public String getDcTrackVersion(){
		return this.dcTrackVersion;
	}
	
	private void setDcTrackVersion(String dcVersion){
		this.dcTrackVersion = dcVersion;
	}
	
	public int getFitType() {
		return fitType;
	}

	private static String DEFAULT_TIMEZONE_OFFSET = "-4";
	
	/** Create the default time string. NOTE: This is the time string of application server. */
	public void setCurrentUtcTimeString(){
		
		StringBuilder sb=new StringBuilder("");
		
		Calendar cal = Calendar.getInstance();				
		
		//MM/dd/yyyy HH:mm:ss offset
		sb.append(cal.get(Calendar.MONTH + 1) ).append("/");
		sb.append(cal.get(Calendar.DAY_OF_MONTH) + 1).append("/");
		sb.append(cal.get(Calendar.YEAR)).append(" ");
		sb.append(cal.get(Calendar.HOUR_OF_DAY)).append(":");
		sb.append(cal.get(Calendar.MINUTE)).append(":");
		sb.append(cal.get(Calendar.SECOND)).append(" ");
		sb.append(DEFAULT_TIMEZONE_OFFSET);
		
		currentUtcTimeString = sb.toString();
	}
	
	public void setCurrentUtcTimeString(String t){		
		currentUtcTimeString = t;
	}
	
	public String getCurrentUtcTimeString(){		
		return currentUtcTimeString;
	}
	
	public void setFitType(int fitType) {
		this.fitType = fitType;
	}
	
	public int getMaxLinesPerPage() {
		return maxLinesPerPage;
	}

	public void setMaxLinesPerPage(int maxLinesPerPage) {
		this.maxLinesPerPage = maxLinesPerPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public List<ColumnCriteriaDTO> getColumnCriteria() {
		return columnCriteria;
	}

	public void setColumnCriteria(List<ColumnCriteriaDTO> columnCriteria) {
		this.columnCriteria = columnCriteria;
	}

	public List<ColumnDTO> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnDTO> columns) {
		this.columns = columns;
	}
	
	public boolean isUserAddColumn() {
		return userAddColumn;
	}

	public void setUserAddColumn(boolean userAddColumn) {
		//log.info("setUserAddColumn() : "+userAddColumn);
		this.userAddColumn = userAddColumn;
	}
	
	public boolean isUserRemoveColumn() {
		return userRemoveColumn;
	}

	public void setFirstQuery(boolean firstQuery) {
		this.firstQuery = firstQuery;
	}
	
	public boolean isFirstQuery() {
		return firstQuery;
	}
	
	public String toString() {
		String str = super.toString();
		StringBuffer buffer = new StringBuffer();
		buffer.append(str+"\n");
		
		buffer.append("pageNumber="+pageNumber+"\n");
		buffer.append("fitType="+fitType+"\n");
		buffer.append("firstQuery="+firstQuery+"\n");
		buffer.append("maxLinesPerPage="+maxLinesPerPage+"\n");
		buffer.append("userAddColumn="+userAddColumn+"\n");
		buffer.append("userRemoveColumn="+userRemoveColumn+"\n");
		
		if(columnCriteria==null) {
			buffer.append("columnCriteria is null\n");
		} else {
			buffer.append("columnCriteria.size()="+columnCriteria.size()+"\n");
		}
		
		if(columns==null) {
			buffer.append("columns is null\n");
		} else {
			buffer.append("columns.size()="+columns.size()+"\n");
		}
				
		return buffer.toString();
	}

}
