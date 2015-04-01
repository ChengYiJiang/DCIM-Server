package com.raritan.tdz.ip;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon.ValidationParams;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.UnitConverterLookup;

public class IpAddressValidators extends TestBase {
	IPAddressValidatorCommon ipAddressValidatorCommon;
	IPAddressDetailsDAO ipAddrDetailsDAO;
	Validator ipAssgDeleteOpValidator;
	Validator jsonAssgSaveOpReqFieldsValidator;
	


	private MapBindingResult getErrorsObject(Class<?> errorBindingClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, errorBindingClass.getName());
		return errors;
	}

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		ipAddressValidatorCommon = (IPAddressValidatorCommon)ctx.getBean("ipAddressValidatorCommon");
		ipAddrDetailsDAO = (IPAddressDetailsDAO)ctx.getBean("ipAddressDetailsDAO");
		ipAssgDeleteOpValidator = (Validator)ctx.getBean("ipAssgDeleteOpValidator");
		jsonAssgSaveOpReqFieldsValidator = (Validator)ctx.getBean("jsonAssgSaveOpReqFieldsValidator");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	protected UserInfo createGatekeeperUserInfo(){
		Long unit = UnitConverterLookup.US_UNIT;
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941",
				"", unit.toString(), "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}

	/**
	 * Provide minimal number of parameters (only port and ipaddress). Validation should pass
	 * since dataPort exists in demo DB and ipaddress is a new one.
	 */
	@Test
	public void testRequiredFieldsMin(){
		JSONIpAssignment ip = new JSONIpAssignment();
		ip.setIpAddress("105.4.56.100");
		MapBindingResult errors1 = getErrorsObject(IPAddressDetails.class);
		jsonAssgSaveOpReqFieldsValidator.validate(ip, errors1);
		Assert.assertTrue(errors1.hasErrors() == true );
		ip.setPortId(8504L);
		MapBindingResult errors2 = getErrorsObject(IPAddressDetails.class);
		jsonAssgSaveOpReqFieldsValidator.validate(ip, errors2);
		Assert.assertTrue(errors2.hasErrors() == false );
	}

	/**
	 * Provide existing dataPort and existing ipAddress. Should get error that ip has been already used
	 */
	@Test
	public void testDuplicateIPGetErrorCode(){
		Long dataPortId = 8504L;
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("10.3.9.18");
		ip.setIsDuplicatIpAllowed(false);

		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		ipAddressValidatorCommon.validateIsDuplicateInSite(ip, dataPortId, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == true );
		List<ObjectError> errorsList = errors.getAllErrors();
		ObjectError oe = errorsList.get(0);
		String [] codes = oe.getCodes();
		boolean foundCode = false;
		for( String s : codes ){
			if( s.equals("IpAddressValidator.duplicate")) foundCode = true;
		}
		Assert.assertTrue( foundCode == true );

	}

	/**
	 * Try to delete combination of port and ip that does not exists. Should 
	 * get error
	 */
	@Test
	public void testDeleteValidatorGetError(){
		Long dataPortId = 49104L;
		Long teamId=99999L;

		Map<ValidationParams, Object> paramsMap = new HashMap<ValidationParams, Object>();
		paramsMap.put(ValidationParams.OP_TYPE, ValidationParams.OP_DELETE_ASSIGNMENT);
		paramsMap.put(ValidationParams.PARAM_TEAM_ID, teamId);
		paramsMap.put(ValidationParams.PARAM_USER_INFO, createGatekeeperUserInfo());
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAssgDeleteOpValidator.validate(paramsMap, errors);
		Assert.assertTrue(errors.hasErrors() == true );
		List<ObjectError> errorsList = errors.getAllErrors();
		ObjectError oe = errorsList.get(0);
		String [] codes = oe.getCodes();
		boolean foundCode = false;
		for( String s : codes ){
			if( s.equals("IpAddressValidator.invalidIPAssignement")) foundCode = true;
		}
		Assert.assertTrue( foundCode == true );
	}


	/**
	 * Try to delete combination of port and ip that exists. Should 
	 * get no error
	 */
	@Test
	public void testDeleteValidatorNoError(){
		Long dataPortId = 8881L;
		Long teamId = 5L;

		Map<ValidationParams, Object> paramsMap = new HashMap<ValidationParams, Object>();
		paramsMap.put(ValidationParams.OP_TYPE, ValidationParams.OP_DELETE_ASSIGNMENT);
		paramsMap.put(ValidationParams.PARAM_TEAM_ID, teamId);
		paramsMap.put(ValidationParams.PARAM_USER_INFO, createGatekeeperUserInfo());
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAssgDeleteOpValidator.validate(paramsMap, errors);
		Assert.assertTrue(errors.hasErrors() == false );

	}

	@Test
	public void testTeamedIPGetErrorCode(){
		Long dataPortId = 49104L;
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("10.1.8.18");

		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		DataPort dp = ipAddressValidatorCommon.validateAndGetDataPort(dataPortId, errors);

		ipAddressValidatorCommon.validateIPIsTeamed(ip, dp, errors);
		Assert.assertTrue(errors.hasErrors() == true );
		List<ObjectError> errorsList = errors.getAllErrors();
		ObjectError oe = errorsList.get(0);
		String [] codes = oe.getCodes();
		boolean foundCode = false;
		for( String s : codes ){
			if( s.equals("IpAddressValidator.notTeamed")) foundCode = true;
		}
		Assert.assertTrue( foundCode == true );

	}

	/**
	 * Provide existing dataPort and ipAddress. Should NOT get error that ip has been already used
	 * because I am setting flag that duplicate ips are allowed
	 */
	@Test
	public void testDuplicateIPNoErrorCode(){
		Long dataPortId = 49104L;
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("10.3.9.18");
		ip.setIsDuplicatIpAllowed(true);

		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAddressValidatorCommon.validateIsDuplicateInSite(ip, dataPortId, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == false );		
	}

	/**
	 * IPAddress set to manage gateway in managed subnet
	 */
	@Test
	public void testDuplicateGWManagedSubnetGetErrorCode(){
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("10.1.8.1");
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAddressValidatorCommon.validateIfGW(ip, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == true );
		List<ObjectError> errorsList = errors.getAllErrors();
		ObjectError oe = errorsList.get(0);
		String [] codes = oe.getCodes();
		boolean foundCode = false;
		for( String s : codes ){
			if( s.equals("IpAddressValidator.ipAddressIsGW")) foundCode = true;
		}
		Assert.assertTrue( foundCode == true );

	}
	@Test
	public void testDuplicateGWMAnagedSubnetNoErrorCode(){
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("10.1.8.1");
		ip.setIsIpBeingGatewayAllowed(true);
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAddressValidatorCommon.validateIfGW(ip, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == false );		
	}


	@Test
	public void testDuplicateGWNOTManagedSubnetGetErrorCode(){
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("192.168.51.126");
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAddressValidatorCommon.validateIfGW(ip, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == true );
		List<ObjectError> errorsList = errors.getAllErrors();
		ObjectError oe = errorsList.get(0);
		String [] codes = oe.getCodes();
		boolean foundCode = false;
		for( String s : codes ){
			if( s.equals("IpAddressValidator.ipAddressIsGW")) foundCode = true;
		}
		Assert.assertTrue( foundCode == true );

	}

	@Test
	public void proba(){
		String match = ".";
		String text = "10.1.8.0";
		int pos=0;
		int i=0;
		int [] dotPosition = new int[3];
		while((pos=(text.indexOf(match, pos) + 1)) > 0){
			assert(i<3);
			dotPosition[i] = pos-1;
			System.out.println("dotPosition[" + i + "]=" + dotPosition[i]);
			i++;
		}

	}
	private String longToIp(long ip) {

		return ((ip >> 24) & 0xFF) + "." 
				+ ((ip >> 16) & 0xFF) + "." 
				+ ((ip >> 8) & 0xFF) + "." 
				+ (ip & 0xFF);

	}
	private long ipToLong(String ipAddress) {

		long result = 0;
		String[] ipAddressInArray = ipAddress.split("\\.");
		for (int i = 3; i >= 0; i--) {
			long ip = Long.parseLong(ipAddressInArray[3 - i]);

			//left shifting 24,16,8,0 and bitwise OR
			//1. 192 << 24
			//1. 168 << 16
			//1. 1   << 8
			//1. 2   << 0
			result |= ip << (i * 8);
		}	 
		return result;
	}

	@Test
	public void proba2(){
		/*String ipStart = "10.1.8.0";
		String ipEnd = "10.1.11.255";*/
		long ipStart = ipToLong("10.1.8.0");
		long ipEnd = ipToLong("10.1.11.25");
		List<String> str = new ArrayList<String>();
		for( long i= ipStart; i<= ipEnd; i ++){
			String n = longToIp(i);
			str.add(n);
			System.out.println("added: " + n);
		}
		

	}
	@Test
	public void testDuplicateGWNOTManagedSubnetNoErrorCode(){
		Long dataPortId = 49104L;
		IPAddressDetails ip = new IPAddressDetails();
		ip.setIpAddress("192.168.51.126");
		ip.setIsIpBeingGatewayAllowed(true);
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		ipAddressValidatorCommon.validateIfGW(ip, 1L, errors);
		Assert.assertTrue(errors.hasErrors() == false );		
	}


	private void testValidIpAddress(String ip) throws Throwable {
		System.out.println("testInvalidIpAddress:" + ip );

		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);

		try{
			ipAddressValidatorCommon.validateIpAddressFormat(ip, errors);
			ipAddressValidatorCommon.validateGWFormat(ip, errors);
			Assert.assertTrue(ip, errors.hasErrors() == false );
		}catch(Exception e){
			System.out.println("errors=" + errors.toString());
			Assert.assertTrue(ip, false);
		}
	}


	@Test
	public final void testIpAddress() throws Throwable {
		testValidIpAddress("1.0.0.0");
		testValidIpAddress("10.3.133.126");
		testValidIpAddress("255.255.0.0");
		testValidIpAddress("255.255.255.255");

		testInvalidIpAddress("Hi Bozana");
		testInvalidIpAddress("0.0.1.1");
		testInvalidIpAddress("0.1.1");
		testInvalidIpAddress("1");
		testInvalidIpAddress("1.1");
		testInvalidIpAddress("1.0.1.");
		testInvalidIpAddress("1.0.1.1.");
		testInvalidIpAddress("1.1.1.1.1");
		testInvalidIpAddress("1.01.2.1");
		testInvalidIpAddress("1/1/1/1");
		testInvalidIpAddress("1-1-1-1");
	}


	private void testInvalidIpAddress(String ip) throws Throwable {
		System.out.println("testInvalidIpAddress: " + ip );
		MapBindingResult errors = getErrorsObject(JSONIpAddressDetails.class);

		try{
			ipAddressValidatorCommon.validateIpAddressFormat(ip, errors);
			ipAddressValidatorCommon.validateGWFormat(ip, errors);
			Assert.assertTrue(ip, errors.hasErrors() == true );

		}catch(Exception e){
			System.out.println("errors=" + errors.toString());
			Assert.assertTrue(ip, false);
		}
	}


	public final void testValidateRequiredFields2() throws Throwable {
		JSONIpAddressDetails ipAddrInfo = new JSONIpAddressDetails();
		ipAddrInfo.setIpAddress("255.255.0.0");
		//ipAddrInfo.setDataPortId(3L);
		ipAddrInfo.setIsVirtual(false);
		List<String> requiredFields = new ArrayList<String>();
		requiredFields.add("ipAddress");
		requiredFields.add("portId");
		requiredFields.add("isVirtual");
		requiredFields.add("mask");


		if( requiredFields != null ){
			for( String rf : requiredFields ){
				Class<?> clazz = ipAddrInfo.getClass();
				Field f = org.springframework.util.ReflectionUtils.findField(clazz, rf);
				org.springframework.util.ReflectionUtils.makeAccessible(f);
				try {
					if( f != null && f.get( ipAddrInfo)   != null ){
						System.out.println("### REQ Field " + f.getName() + " is present and OK");
						boolean rfPresent = (f.getName().equals("ipAddress") ||
								f.getName().equals("portId") ||
								f.getName().equals("isVirtual"));
						Assert.assertTrue(rfPresent == true );
					}else{
						System.out.println("## REQ field " + f.getName() + " is not present !!!");
						Assert.assertTrue(f.getName().equals("mask"));
					}
				}catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
