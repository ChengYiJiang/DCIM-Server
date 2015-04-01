/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.reservation.dao.*;
import com.raritan.tdz.domain.Reservations;
import com.raritan.tdz.tests.TestBase;

public class ReservationDaoTest extends TestBase {
	ReservationDAO resDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		resDAO = (ReservationDAO)ctx.getBean("reservationDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesInsertRecord() throws Throwable {
		Reservations rec = new Reservations();
		rec.setReservationName("Reserve Testing 01");
		rec.setReservationNo("12002");
		rec.setReservationPurpose("Testing this DAO");
		rec.setReservedBy(this.getTestAdminUser().getUserName());
		rec.setReservedDate(new Timestamp(0L));
		rec.setUpdateDate(new Timestamp(0L));		
		rec.setStatusLookup(new LksData(301L));
		
		Long id = resDAO.create(rec);
		
		System.out.println(id);
		
		rec = resDAO.read(id);
		
		resDAO.delete(rec);
	}
	
}
