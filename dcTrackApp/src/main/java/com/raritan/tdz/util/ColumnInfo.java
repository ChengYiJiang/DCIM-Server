package com.raritan.tdz.util;

/**
 * 
 * @author bozana
 * This is a helper class used by DCTColumnsSchemaBase. Hibernate fills out this
 * class with data from INFORMATION_SCHEMA.COLUMNS table
 * 
 */
public class ColumnInfo{

	private String table_name;
	private String column_name;
	private String data_type;
	private Integer character_maximum_length;
	private Integer numeric_precision;
	private Integer numeric_precision_radix;
	private Integer numeric_scale;
	
	public ColumnInfo(){
	}

	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
	public String getData_type() {
		return data_type;
	}
	public void setData_type(String data_type) {
		this.data_type = data_type;
	}
	public Integer getCharacter_maximum_length() {
		return character_maximum_length;
	}
	public void setCharacter_maximum_length(Integer character_maximum_length) {
		this.character_maximum_length = character_maximum_length;
	}
	public Integer getNumeric_precision() {
		return numeric_precision;
	}
	public void setNumeric_precision(Integer numeric_precision) {
		this.numeric_precision = numeric_precision;
	}
	public Integer getNumeric_precision_radix() {
		return numeric_precision_radix;
	}
	public void setNumeric_precision_radix(Integer numeric_precision_radix) {
		this.numeric_precision_radix = numeric_precision_radix;
	}
	public Integer getNumeric_scale() {
		return numeric_scale;
	}
	public void setNumeric_scale(Integer numeric_scale) {
		this.numeric_scale = numeric_scale;
	}
}
