/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.util.Collection;

/**
 * This is the interface used for building a filter for
 * rules processor to fetch data. Based on Builder pattern
 * @author prasanna
 *
 */
public interface Filter {
	/**
	 * This will perform "and" on filter expressions
	 * @param rhs Filter object
	 * @return Filter object
	 */
	Filter and(Filter rhs);
	
	/**
	 * This will perform "or" on filter expressions
	 * @param rhs Filter object
	 * @return Filter object
	 */
	Filter or(Filter rhs);
	
	/**
	 * This is to open a parentheses. Make sure you close
	 * after opening it!
	 * @see closeParentheses()
	 * @return Filter object
	 */
	Filter openParentheses();
	
	/**
	 * This is to close a parentheses. Make sure you have
	 * opened it before
	 * @see openParentheses()
	 * @return Filter object
	 */
	Filter closeParentheses();
	
	/**
	 * This will create property equal to value expression
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter eq(String property, Object value);
	
	/**
	 * This will create property greater than value expression
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter gt(String property, Object value);
	
	/**
	 * This will create property less than or equal to value expression
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter le(String property, Object value);
	
	/**
	 * 
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter ne(String property, Object value);
	
	/**
	 * 
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter ilike(String property, Object value);
	
	/**
	 * 
	 * @param property
	 * @param value
	 * @return Filter object
	 */
	Filter like(String property, Object value);
	
	/**
	 * 
	 * @param property
	 * @param values
	 * @return Filter object
	 */
	Filter in(String property, Collection<?> values);
	
	/**
	 * 
	 * @param expression
	 * @return Filter object
	 */
	Filter not(Filter expression);
	
	/**
	 * 
	 * @param property
	 * @return Filter object
	 */
	Filter isEmpty(String property);
	
	/**
	 * 
	 * @param property
	 * @return Filter object
	 */
	Filter isNotEmpty(String property);
	/**
	 * 
	 * @param property
	 * @return Filter object
	 */
	Filter isNull(String property);
	
	/**
	 * 
	 * @param property
	 * @return Filter object
	 */
	Filter isNotNull(String property);
	
	/**
	 * 
	 * @param property
	 * @return Filter object
	 */
	Filter orderBy(String property);
	
	/**
	 * 
	 * @return SQL String representation of filter
	 */
	String toSqlString();
}
