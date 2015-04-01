package com.raritan.tdz.validator;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.DCTColumnsSchema;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;



public class FieldValidatorImpl implements FieldValidator {
	@Autowired
	private DCTColumnsSchema dctColumnsSchema;


	public DCTColumnsSchema getDctColumnsSchema() {
		return dctColumnsSchema;
	}

	public void setDctColumnsSchema(DCTColumnsSchema dctColumnsSchema) {
		this.dctColumnsSchema = dctColumnsSchema;
	}

	@Autowired
	private FieldHome fieldHome;
	
	private RemoteRef remoteRef;

	private List<Field> traceFields(String remoteType, String fieldName)
			throws ClassNotFoundException {
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		List<Field> fields = objectTrace.traceObject(Class.forName(remoteType), fieldName);
		return fields;
	}

	private double roundDecimal(double d, int numDigits){
		StringBuffer format = new StringBuffer("#.");
		for (int cnt = 0; cnt < numDigits; cnt++){
			format.append("#");
		}

		DecimalFormat df = new DecimalFormat(format.toString());
		return Double.valueOf(df.format(d));
	}

	private int getNumberOfDecimals(String uiId){
		String lengthStr = dctColumnsSchema.getPropertyLength(uiId);

		//Get the lengths before and after decimal point from the schema
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		Integer lengthAfterDecimal = scaleStr != null ? Integer.parseInt(scaleStr) : 0;

		return lengthAfterDecimal;
	}

	private int getNumberOfDigits(String uiId){
		String lengthStr = dctColumnsSchema.getPropertyLength(uiId);

		//Get the lengths before and after decimal point from the schema
		String precisionStr = lengthStr.substring(0,lengthStr.indexOf("."));
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		Integer lengthBeforeDecimal = precisionStr != null && scaleStr != null ? Integer.parseInt(precisionStr) - Integer.parseInt(scaleStr) : 0;

		return lengthBeforeDecimal;
	}
	
	
	@Override
	public void validate(String uiId, String remoteReference,
			String remoteType, Object value, MapBindingResult validationErrors) throws ClassNotFoundException {
		if (value == null) return;

		//First get the last field in the trace
		List<Field> fields = traceFields(remoteType,
					remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE));
		Field field = fields.size() > 0 ? fields.get(fields.size() - 1) : null;

		if (field != null){
			if (field.getType().equals(String.class)){
				dctColumnsSchema.validate(uiId, value.toString(), validationErrors);
			} else if (field.getType().equals(Long.class) || field.getType().equals(long.class)){
				try {
					dctColumnsSchema.validate(uiId, Integer.parseInt(value.toString()), validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value, fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId)};
					validationErrors.reject("SchemaValidation.Integer.invalidValue", args, "The given value for this field is invalid");
				}
			} else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)){
				try {
					dctColumnsSchema.validate(uiId, Integer.parseInt(value.toString()), validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value,fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId)};
					validationErrors.reject("SchemaValidation.Integer.invalidValue", args, "The given value for this field is invalid");
				}
			}  else if (field.getType().equals(Double.class) || field.getType().equals(double.class)){
				try {
					double doubleValue = Double.parseDouble(value.toString());
					//Round it to the decimal places given by the database.
					doubleValue = roundDecimal(doubleValue,getNumberOfDecimals(uiId));
					dctColumnsSchema.validate(uiId, doubleValue, validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value,fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId),getNumberOfDecimals(uiId)};
					validationErrors.reject("SchemaValidation.Double.invalidValue", args, "The given value for this field is invalid");
				}
			}
		}
		
	}

	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}


}
