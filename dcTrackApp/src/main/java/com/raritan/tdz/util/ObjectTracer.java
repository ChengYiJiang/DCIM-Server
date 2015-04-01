/**
 * 
 */
package com.raritan.tdz.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author prasanna
 * Given a field, this utility will find the path to that on a root class
 */
public class ObjectTracer {
	private List<Field> memberList = new ArrayList<Field>();
	private Package rootClassPkg;
	private Class<?> rootClass;
	private Class<?> parentClass;
	private Map<String, ObjectTracerHandler> handlers = new HashMap<String, ObjectTracerHandler>();
	
	/**
	 * Given a root class and a fieldName, this method will trace for that object from root.
	 * If it is found, this will return a list of Field objects that contain the trace starting
	 * from root
	 * @param rootClass
	 * @param fieldName
	 * @return
	 */
	public List<Field> traceObject(Class<?> rootClass, String fieldName){
		if (rootClass == null || fieldName == null){
			throw new IllegalArgumentException("rootClass or fieldName cannot be null");
		}
		
		memberList.clear();
		
		Set<String> patterns = handlers.keySet();
		for(String pattern:patterns){
			if (Pattern.matches(pattern, fieldName)){
				ObjectTracerHandler handler = handlers.get(pattern);
				handler.handleTrace(this, rootClass, fieldName);
				return memberList;
			}
		}
		
	
		this.rootClass = rootClass;
		this.parentClass = rootClass.getSuperclass();
		rootClassPkg = rootClass.getPackage();
		traceAttribute(rootClass,fieldName);
		
		Collections.reverse(memberList);
		
		return memberList;
	}

	/**
	 * Returns the object trace field list. Please use traceObject method to first 
	 * generate the field list and then you may call this method any time to get the 
	 * list.
	 * @return A list of fields corresponding to the trace is returned.
	 */
	public List<Field> getObjectTrace(){
		return memberList;
	}
	
	@Override
	public String toString(){
		
		StringBuffer trace = new StringBuffer();
		for (Field member:memberList){
			trace.append(member.getName());
			trace.append(".");
		}
		return (trace.substring(0, trace.length() > 0 ? trace.lastIndexOf("."):0));
	}
	
	public void addHandler(String pattern, ObjectTracerHandler handler){
		handlers.put(pattern, handler);
	}
	
	public void prependField(Field field){
		memberList.add(0,field);
	}
	
	public void appendField(Field field){
		memberList.add(field);
	}
	
	private boolean traceAttribute(Class<?> searchClass, String fieldName){
		//Read the class to see if the attribute is found
		
		Field[] mbrs = searchClass.getDeclaredFields();
		
		//System.out.println("searchClass="+searchClass);
		
		for (int i = 0; i < mbrs.length; i++){
			if (fieldName.equals(mbrs[i].getName())){
				memberList.add(mbrs[i]);
				return true;
			} else if (mbrs[i].getType().getPackage() != null && !mbrs[i].getType().equals(searchClass)
						&& !(mbrs[i].getType().equals(rootClass)) && mbrs[i].getType().getPackage().getName().contains(rootClassPkg.getName())
						&& !(mbrs[i].getType().equals(parentClass))){
				boolean found = traceAttribute(mbrs[i].getType(), fieldName);
				if (found == true){
					memberList.add(mbrs[i]);
					return found;
				}
			}
			
		}
		return false;
	}
	
    public static ArrayList<String> getAliases(String traceStr) throws ClassNotFoundException{
        ArrayList<String> aliases = new ArrayList<String>();
        
        String aliasStr = traceStr.contains(".") ? traceStr.substring(0,  traceStr.lastIndexOf(".")) : null;
        if (aliasStr != null){
                StringBuffer buffer = new StringBuffer();
                for (String token: aliasStr.split("\\.")){
                        buffer.append(token);
                        aliases.add(buffer.toString());
                        buffer.append(".");
                }
        }
        return aliases;
    }
}
