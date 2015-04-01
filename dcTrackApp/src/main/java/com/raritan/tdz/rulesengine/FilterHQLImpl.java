/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.util.Collection;

/**
 * @author prasanna
 *
 */
public class FilterHQLImpl implements Filter {

	StringBuilder queryStringBuilder = new StringBuilder();
	
	private FilterHQLImpl(){
		queryStringBuilder = new StringBuilder();
	}
	
	
	public static Filter createFilter(){
		return new FilterHQLImpl();
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#and(com.raritan.tdz.rulesengine.Filter)
	 */
	@Override
	public Filter and(Filter rhs) {
		queryStringBuilder.append(" and");
		queryStringBuilder.append(rhs.toSqlString());
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#or(com.raritan.tdz.rulesengine.Filter)
	 */
	@Override
	public Filter or(Filter rhs) {
		queryStringBuilder.append(" or");
		queryStringBuilder.append(rhs.toSqlString());
		return this;
	}
	
	@Override
	public Filter openParentheses() {
		queryStringBuilder.append(" (");
		return this;
	}


	@Override
	public Filter closeParentheses() {
		queryStringBuilder.append(" )");
		return this;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter eq(String property, Object value) {
		setPropertyValueOperator(property, value, "=");
		return this;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter gt(String property, Object value) {
		setPropertyValueOperator(property, value, ">");
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter le(String property, Object value) {
		setPropertyValueOperator(property, value, "<=");
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter ne(String property, Object value) {
		setPropertyValueOperator(property, value, "<>");
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter ilike(String property, Object value) {
		setPropertyValueOperator(property, value, "ilike");
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public Filter like(String property, Object value) {
		setPropertyValueOperator(property, value, "like");
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#in(java.lang.String, java.util.Collection)
	 */
	@Override
	public Filter in(String property, Collection<?> values) {
		queryStringBuilder.append(" ");
		queryStringBuilder.append(getPropertyWithAlias(property));
		queryStringBuilder.append(" ");
		queryStringBuilder.append("in");
		queryStringBuilder.append(" ");
		String valueString = values.toString().replace("[", "( ");
		valueString = valueString.replace("]", " )");
		queryStringBuilder.append(valueString);
		return this;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#not(com.raritan.tdz.rulesengine.Filter)
	 */
	@Override
	public Filter not(Filter expression) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#isEmpty(java.lang.String)
	 */
	@Override
	public Filter isEmpty(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#isNotEmpty(java.lang.String)
	 */
	@Override
	public Filter isNotEmpty(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#isNull(java.lang.String)
	 */
	@Override
	public Filter isNull(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#isNotNull(java.lang.String)
	 */
	@Override
	public Filter isNotNull(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#orderBy(java.lang.String)
	 */
	@Override
	public Filter orderBy(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.Filter#toSqlString()
	 */
	@Override
	public String toSqlString() {
		return queryStringBuilder.toString();
	}
	

	
	private void setPropertyValueOperator(String property, Object value, String operator) {
		queryStringBuilder.append(" ");
		queryStringBuilder.append(getPropertyWithAlias(property));
		queryStringBuilder.append(" ");
		queryStringBuilder.append(operator);
		queryStringBuilder.append(" ");
		queryStringBuilder.append(value);
	}


	private String getPropertyWithAlias(String property){
		if (property.contains(".")){
			return property;
		} else {
			return "this_." + property;
		}
	}
	
}
