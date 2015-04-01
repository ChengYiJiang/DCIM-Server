package com.raritan.tdz.util;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.tests.TestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import java.text.DecimalFormat;


/**
 * @author Basker
 *
 */
public class UnitConverterTest extends TestBase {
	private static final String US_UNIT = "1";
	private static final String SI_UNIT = "2";

	private static final double POUND_TO_KG = 0.453592;
	private static final double TON_TO_KG = 907.18474;
	private static final double INCH_TO_MM = 25.4;
	private static final double SQFT_TO_SQM = 0.092903;
	private static final double TON_TO_KW = 3.516852842;
	private static final double KW_TO_TON = 0.284345136;

	private UnitConverterImpl poundToKgUnitConverter;
	private UnitConverterImpl tonToKgUnitConverter;
	private UnitConverterImpl inchToMMUnitConverter;
	private UnitConverterImpl sqFeetToSqMtrUnitConverter;
	private UnitConverterImpl tonToKwUnitConverter;
	private KwToTonConverterImpl kwToTonsUnitConverter;	

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		poundToKgUnitConverter = (UnitConverterImpl)ctx.getBean("poundToKgUnitConverter");
		tonToKgUnitConverter = (UnitConverterImpl)ctx.getBean("tonToKgUnitConverter");
		inchToMMUnitConverter = (UnitConverterImpl)ctx.getBean("inchToMMUnitConverter");
		sqFeetToSqMtrUnitConverter = (UnitConverterImpl)ctx.getBean("sqFeetToSqMtrUnitConverter");
		tonToKwUnitConverter = (UnitConverterImpl)ctx.getBean("tonToKwUnitConverter");
		kwToTonsUnitConverter = (KwToTonConverterImpl)ctx.getBean("kwToTonsUnitConverter");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
	}
	
	/* round input value to single decimal point */
	private Object roundSingleDecimals(Object d) {
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		return Double.valueOf(twoDForm.format(d));		
	}

	/* round input value to two decimal point */
	private Object roundTwoDecimals(Object d) {
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
    	return Double.valueOf(twoDForm.format(d));		
	}
	
	private void TestConvertAndNormalize (UnitConverterImpl converter, double convertValue, double convertExpectedValue, 
			double normalizeValue, double normalizeExpectedValue, String msg) {
		
		try {
			Double rval = (Double)converter.convert(convertValue, US_UNIT);
			assertEquals(rval,  1.0, "US_UNIT conversion: " + msg);

			rval = (Double)converter.convert(convertValue, SI_UNIT);
			System.out.println ("rval = " + rval + " " + roundSingleDecimals(convertExpectedValue));
			assertEquals(rval, (Double)roundSingleDecimals(convertExpectedValue), "SI_UNIT conversion: " + msg);

			// normalize value to database 
			rval = (Double)converter.normalize(normalizeValue, SI_UNIT);	
			assertEquals(rval, (Double)roundTwoDecimals(normalizeExpectedValue), "SI_UNIT normalization: " +msg);

		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void ConvertPoundToKG () {
		double convertValue = 1; // 1 pound
		double normalizeValue = 5; // 5 kg 
		double normalizeExpectedValue = 11.0231; // punds
		TestConvertAndNormalize (poundToKgUnitConverter,convertValue, POUND_TO_KG, 
				normalizeValue, normalizeExpectedValue,
				"pound to kg"
				);
	}
	
	@Test
	public void ConvertTonToKG () {
		double convertValue = 1; // 1 ton
		double normalizeValue = 907.185; // kg 
		double normalizeExpectedValue = 1; // ton
		TestConvertAndNormalize (tonToKgUnitConverter,convertValue, TON_TO_KG, 
				normalizeValue, normalizeExpectedValue,
				"ton to kg"
				);
	}

	@Test
	public void ConvertInchToMM () {
		double convertValue = 1; // 1 inch
		double normalizeValue = 254; // 10 mm 
		double normalizeExpectedValue = 10; // inches
		TestConvertAndNormalize (inchToMMUnitConverter,convertValue, INCH_TO_MM, 
				normalizeValue, normalizeExpectedValue,
				"inch to mm"
				);
	}
	
	@Test
	public void ConvertSqFeetToSqMtr () {
		double convertValue = 1; // 1 sq ft
		double normalizeValue = 10; // sq mtr
		double normalizeExpectedValue = 107.639; // sq ft
		TestConvertAndNormalize (sqFeetToSqMtrUnitConverter,convertValue, SQFT_TO_SQM, 
				normalizeValue, normalizeExpectedValue,
				"sqft to sqmtr"
				);
	}

	@Test
	public void ConvertTonToKw () {

		double convertValue = 1; // 1 refrigeration ton
		double normalizeValue = 100; // kw
		double normalizeExpectedValue = 28.434513609; // ton
		TestConvertAndNormalize (tonToKwUnitConverter,convertValue, TON_TO_KW, 
				normalizeValue, normalizeExpectedValue,
				"ton to kw"
				);
	}

	@Test
	public void ConvertKwToTon () {
		double value = 1; // 1 refrigeration kw
		try {
			Double rval = (Double)kwToTonsUnitConverter.convert(value, US_UNIT);
			assertEquals(rval,  (Double)roundSingleDecimals(KW_TO_TON), "1 kw to ton conversion for US_UNIT failed");

			rval = (Double)kwToTonsUnitConverter.convert(value, SI_UNIT);
			System.out.println ("rval = " + rval + " " + roundSingleDecimals(KW_TO_TON));
			assertEquals(rval, 1.0, "1 kw to ton conversion for SI_UNIT failed");
			
			double normalizeValue = 28.434513609; // ton
			double normalizeExpectedValue = 100; // kw

			rval = (Double)kwToTonsUnitConverter.normalize(normalizeValue, US_UNIT);	
			assertEquals(rval, (Double)roundTwoDecimals(normalizeExpectedValue), "US_UNIT normalization kw to tons failed" );
			
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}
}
