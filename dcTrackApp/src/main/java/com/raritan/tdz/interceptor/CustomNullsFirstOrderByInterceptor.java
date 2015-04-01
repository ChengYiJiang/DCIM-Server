/**
 * 
 */
package com.raritan.tdz.interceptor;

import org.apache.commons.lang.StringUtils;
import org.hibernate.EmptyInterceptor;

/**
 * @author prasanna
 * This will take care of the nulls first order while ordering ascending or descending.
 * This will be currently used by the ItemSearchImpl. At some point in time when necessary
 * we can apply on the SessionFactory so that it is global.
 */
public class CustomNullsFirstOrderByInterceptor extends EmptyInterceptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6859256455850877995L;
	
	public static final String ORDER_BY_TOKEN = "order by";
	
	public String onPrepareStatement(String sql) {

	        int orderByStart = sql.toLowerCase().indexOf(ORDER_BY_TOKEN);
	        if (orderByStart == -1) {
	            return super.onPrepareStatement(sql);
	        }
	        orderByStart += ORDER_BY_TOKEN.length() + 1;
	        int orderByEnd = sql.indexOf(")", orderByStart);
	        if (orderByEnd == -1) {
	            orderByEnd = sql.indexOf(" UNION ", orderByStart);
	            if (orderByEnd == -1) {
	                orderByEnd = sql.length();
	            }
	        }
	        String orderByContent = sql.substring(orderByStart, orderByEnd);
	        String[] orderByNames = orderByContent.split("\\,");
	        for (int i=0; i<orderByNames.length; i++) {
	            if (orderByNames[i].trim().length() > 0) {
	                if (orderByNames[i].trim().toLowerCase().endsWith("desc")) {
	                    orderByNames[i] += " NULLS LAST";
	                } else {
	                    orderByNames[i] += " NULLS FIRST";
	                }
	            }
	        }
	        orderByContent = StringUtils.join(orderByNames, ",");
	        sql = sql.substring(0, orderByStart) + orderByContent + sql.substring(orderByEnd); 
	        return super.onPrepareStatement(sql);
	    }

}
