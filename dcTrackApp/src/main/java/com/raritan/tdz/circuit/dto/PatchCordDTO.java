package com.raritan.tdz.circuit.dto;

import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.util.UnitConverterHelperImpl;
import com.raritan.tdz.util.UnitConverterImpl;

public class PatchCordDTO extends WireNodeDTO {
	
	public PatchCordDTO() {
	}
	
	public PatchCordDTO(ConnectionCord connCord, UserInfo userInfo) {
		if (connCord != null) {
			if (connCord.getColorLookup() != null) {
				setCordColor( connCord.getColorLookup().getLkuId() );
			}
			setCordId( connCord.getCordId() );
			setCordLabel( connCord.getCordLabel() );
			Integer length = connCord.getCordLength();
			if (null != userInfo) {
				UnitConverterImpl lenghtConverter = new UnitConverterImpl(UnitConverterImpl.FEET_TO_METER);
				lenghtConverter.setUnitConverterHelper( new UnitConverterHelperImpl());
				Object len = lenghtConverter.convert(new Integer(connCord.getCordLength()), ((userInfo != null) ? userInfo.getUnits(): "1"));
				if (len instanceof Double) {
					length = ((Double) len).intValue();
				}
				else if (len instanceof Integer) {
					length = (Integer) len;
				}
				else if (len instanceof Float) {
					length = ((Float) len).intValue();
				}
			}
			setCordLength( length );
			if (connCord.getCordLookup() != null) {
				setCordLkuId( connCord.getCordLookup().getLkuId() );
				setCordLkuDesc( connCord.getCordLookup().getLkuValue() );
			}
		}
	}
}
