package com.raritan.tdz.util;


import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@Aspect
public class ObjectDumper {

	public void dumpItemObjectAsXML(Object itemObj) throws Throwable {
		Item item = (Item)itemObj;
		XStream  xstream = new XStream(new DomDriver("utf-8"));
		System.out.println("###################################### Dumping item object to XML");
		System.out.println(xstream.toXML(item));
		System.out.println("###################################### End of XML dump");
	}


	@SuppressWarnings("unchecked")
	public Object dumpValueIdDTOList(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("################ Dumping " + pjp.getSignature().getDeclaringTypeName() + "." +
				pjp.getSignature().getName() + " arguments:  #####");
		if(pjp.getSignature().getName().equals("saveItem")){
			Object[] args = pjp.getArgs();
			for( int i=0; i< args.length; i++){
				System.out.printf("args[%d]= " , i);
				if( args[i] instanceof UserInfo) dumpUserInfo( (UserInfo) args[i]);
				else if( args[i] instanceof List<?>) dumpValueIdDTO((List<ValueIdDTO>) args[i]);
				else System.out.println(args[i]);
			}
		}
		Object o = pjp.proceed();
		if( pjp.getSignature().getName().equals("convertToDTOList")){
			List<ValueIdDTO> dtolist = (List<ValueIdDTO>) o;
			dumpValueIdDTO(dtolist);
		}
		System.out.println("########## End of dump #############");

		return o;
	}

	private void dumpUserInfo( UserInfo userInfo){
		System.out.println("userInfo:");
		System.out.println(" accessLevelId=" + userInfo.getAccessLevelId());
		System.out.println(" defaultLocationCode=" + userInfo.getDefaultLocationCode());
		System.out.println(" defaultLocationId=" + userInfo.getDefaultLocationId());
		System.out.println(" email=" + userInfo.getEmail());
		System.out.println(" firstName=" + userInfo.getFirstName());
		System.out.println(" groupName=" + userInfo.getGroupName());
		System.out.println(" id=" + userInfo.getId());
		System.out.println(" lastName=" + userInfo.getLastName());
		System.out.println(" locale=" + userInfo.getLocale());
		System.out.println(" sessionId=" + userInfo.getSessionId());
		System.out.println(" sessionTimeout=" + userInfo.getSessionTimeout());
		System.out.println(" units=" + userInfo.getUnits());
		System.out.println(" userId=" + userInfo.getUserId());
		System.out.println(" userName=" + userInfo.getUserName());
	}

	private void dumpValueIdDTO(List<ValueIdDTO>dtoList){
		System.out.println("{");
		for( ValueIdDTO dto : dtoList ){
			String uiId = dto.getLabel();
			Object value = dto.getData();
			if( value instanceof Map<?, ?> ) {
				System.out.println("\t" + uiId + ":" + "{");
				dumpObjectMap((Map<String, String>) value);
				System.out.println("\t}");
			}
			else{
				String strVal = value != null ? value.toString() : null;
				System.out.println("\t" + uiId + ":" + strVal);
			}			
		}
		System.out.println("}");
	}


	private void dumpObjectMap(Map<String, String> objMap) {
		for( String key : objMap.keySet() ){
			System.out.println("\t\t key=" + key + ", value=" + objMap.get(key));
		}
	}
}
