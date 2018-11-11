package com.mandan.server.main;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mandan.services.DownloadPrices;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Assert;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootPriceQueryApplicationTests {

	DownloadPrices downloadPrices;
	
	@Test
	public void contextLoads() 
	{
		
	}
	
	@Before
	public void setUp() throws Exception {
		downloadPrices = new DownloadPrices();
	}
	
	@After
	public void tearDown() throws Exception 
	{
		downloadPrices = null;
	}
	
	@Test
	public void testWeeklyPrices()
	{
		String priceData = downloadPrices.getLastWeekPrices();
		System.out.println(priceData);
		
		String[] dailyData = priceData.split("\n");
		
		Assert.assertTrue(dailyData.length >= 6 && dailyData.length <= 8);
	}
	
	@Test
	public void testMonthlyPrices()
	{
		String priceData = downloadPrices.getLastMonthPrices();
		System.out.println(priceData);
		
		String[] dailyData = priceData.split("\n");
		
		Assert.assertTrue(dailyData.length >= 30 && dailyData.length <= 32);
	}
	
	@Test
	public void testYearlyPrices()
	{
		String priceData = downloadPrices.getLastYearPrices();
		System.out.println(priceData);
		
		String[] dailyData = priceData.split("\n");
		
		Assert.assertTrue(dailyData.length >= 364 && dailyData.length <= 366);
	}
	
	@Test
	public void testSpecificDates()
	{
		Calendar endDate = new GregorianCalendar(2018,10,11);
		Calendar startDate = new GregorianCalendar(2018,10,8);

		String priceData = downloadPrices.getPrices(startDate, endDate);
		System.out.println(priceData);
		
		String[] dailyData = priceData.split("\n");
		
		Assert.assertTrue(dailyData.length == 2);
	}
	
	@Test
	public void testBothDatesBefore()
	{
		Calendar endDate = new GregorianCalendar(2012,10,11);
		Calendar startDate = new GregorianCalendar(2012,10,8);

		String priceData = downloadPrices.getPrices(startDate, endDate);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("ERROR : ") != -1);
	}
	
	@Test
	public void testBothDatesAfter()
	{
		Calendar endDate = Calendar.getInstance();		
		endDate.add(Calendar.YEAR, 1);
		
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.MONTH, 6);

		String priceData = downloadPrices.getPrices(startDate, endDate);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("ERROR : ") != -1);
	}
	
	@Test
	public void testStartDateBefore()
	{
		Calendar endDate = Calendar.getInstance();	
		
		Calendar startDate = new GregorianCalendar(2012,10,8);

		String priceData = downloadPrices.getPrices(startDate, endDate);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("WARNING : ") != -1);
		Assert.assertTrue(priceData.indexOf("2018-") != -1);
	}
	
	@Test
	public void testEndDateAfterToday()
	{
		Calendar endDate = Calendar.getInstance();		
		endDate.add(Calendar.YEAR, 1);
		
		Calendar startDate = new GregorianCalendar(2017,10,8);

		String priceData = downloadPrices.getPrices(startDate, endDate);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("WARNING : ") != -1);
		Assert.assertTrue(priceData.indexOf("2018-") != -1);
	}
	
	@Test
	public void testMovingAverage()
	{
		Calendar endDate = Calendar.getInstance();			
		
		Calendar startDate = new GregorianCalendar(2017,10,8);

		String priceData = downloadPrices.getMovingAverage(startDate, endDate, 10);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("movingAverage of 10 days = ") != -1);
	}
	
	@Test
	public void testMovingBuySellDecision()
	{
		Calendar endDate = new GregorianCalendar(2018,10,8);			
		
		Calendar startDate = new GregorianCalendar(2017,10,8);

		String priceData = downloadPrices.getMovingAverage(startDate, endDate, 10);
		System.out.println(priceData);
		
		Assert.assertTrue(priceData.indexOf("buyDecision = HOLD") != -1);
		Assert.assertTrue(priceData.indexOf("buyDecision = BUY") != -1);
		Assert.assertTrue(priceData.indexOf("buyDecision = SELL") != -1);
	}
	

}
