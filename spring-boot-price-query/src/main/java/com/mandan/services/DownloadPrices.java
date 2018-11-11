package com.mandan.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DownloadPrices {	
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	LinkedHashMap<Calendar, Double> lhmDailyPrices = new LinkedHashMap<Calendar, Double>();
	Calendar maxEndDate;
	Calendar minStartDate;

	public LinkedHashMap<Calendar, Double> getLhmDailyPrices() {
		return lhmDailyPrices;
	}

	public DownloadPrices() {
		getPrice();
	}

	public boolean getPrice() {

		try 
		{
			URL url = new URL("https://www.coinbase.com//api//v2//prices//BTC-USD//historic?period=all");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			int responsecode = conn.getResponseCode();
			
			if(responsecode != 200)
			{
				System.out.println("Got error response code = "+ responsecode);
				return false;
			}
			else
			{
				System.out.println("Got good response code = "+ responsecode);				

				Scanner sc = new Scanner(url.openStream());
				String inline = "";
				
				while(sc.hasNext())				
				{				
					inline+=sc.nextLine();
				}
				
				System.out.println("\nJSON data in string format");				
				System.out.println(inline);
				
				sc.close();
				
				JSONParser parse = new JSONParser();
				JSONObject jobj = (JSONObject)parse.parse(inline);
				
				JSONObject jsonarr_1 =  (JSONObject) jobj.get("data"); 
				
				String base = (String) jsonarr_1.get("base");
				System.out.println("base = "+ base);
				String currency = (String) jsonarr_1.get("currency");
				System.out.println("currency = "+ currency);
				JSONArray jsonarr_2 = (JSONArray) jsonarr_1.get("prices");
								
				for(int i = 0 ; i < jsonarr_2.size(); ++i)
				{
					JSONObject onePrice = (JSONObject) jsonarr_2.get(i);					
					Double price = Double.parseDouble((String)onePrice.get("price"));

					Calendar cal = Calendar.getInstance();
					Date oneDay = dateFormat.parse((String)onePrice.get("time"));
					cal.setTime(oneDay);
					
					//Save min & max date values as it will be used to give 
					//error messages later
					if(i == 0)
						maxEndDate = cal;
					if(i == (jsonarr_2.size() -1))
						minStartDate = cal;
					
					lhmDailyPrices.put(cal, price);
				}
			}			
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		} 
		
		return true;
	}

	public String getLastWeekPrices() 
	{
		Calendar endDate = Calendar.getInstance();
		
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.WEEK_OF_YEAR, -1);
				
		System.out.println("Doing query for dates startDate = "+ startDate + " End date = "+ endDate);
		
		return getPrices(startDate ,endDate);
		
	}
	
	public String getLastMonthPrices() 
	{
		Calendar endDate = Calendar.getInstance();
		
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.MONTH, -1);
				
		System.out.println("Doing query for dates startDate = "+ startDate.getTime() + " End date = "+ endDate.getTime());
		
		return getPrices(startDate ,endDate);
		
	}
	
	public String getLastYearPrices() 
	{
		Calendar endDate = Calendar.getInstance();
		
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.YEAR, -1);
				
		System.out.println("Doing query for dates startDate = "+ startDate.getTime() + " End date = "+ endDate.getTime());
		
		return getPrices(startDate ,endDate);
		
	}
	
	
	private ArrayList<ArrayList<Object>> getData(Calendar startDate, Calendar endDate)
	{
		ArrayList<Object> alDates = new ArrayList<Object>();
		ArrayList<Object> alPrices = new ArrayList<Object>();
		
		for(Calendar oneDate :lhmDailyPrices.keySet())
		{
			if(oneDate.after(startDate))
			{
				if(oneDate.before(endDate))
				{
					alDates.add(oneDate);
					alPrices.add(lhmDailyPrices.get(oneDate));
				}				
			}
			else
				break;
		}
		
		ArrayList<ArrayList<Object>> alResults = new ArrayList<ArrayList<Object>>();
		alResults.add(alDates);
		alResults.add(alPrices);
		
		
		return alResults;
	}
	
	private boolean doDataChecks(Calendar startDate, Calendar endDate , StringBuilder sb)
	{
		System.out.println("Query received between dates startDate = "+ startDate.getTime() + " End date = "+ endDate.getTime());
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		
		if(endDate.before(minStartDate) || startDate.after(maxEndDate))
		{
			sb.append("ERROR : We only have data between these 2 dates please query between these dates. Start date = "
					+ dateFormat.format(minStartDate.getTime()) 
					+ " End date = "+ dateFormat.format(maxEndDate.getTime()) + "\n");
			return false;
		}
		else if((startDate.before(minStartDate) && endDate.after(minStartDate) && endDate.compareTo(maxEndDate) > 0)
				|| (startDate.after(minStartDate) && startDate.before(maxEndDate) && endDate.compareTo(maxEndDate) > 0))
		{
			sb.append("WARNING : We only have data between these 2 dates please try to query between these dates. Start date = "
					+ dateFormat.format(minStartDate.getTime()) 
					+ " End date = "+ dateFormat.format(maxEndDate.getTime())+ "\n");
		}
		
		return true;
	}

	public String getPrices(Calendar startDate, Calendar endDate) 
	{
		StringBuilder sb = new StringBuilder();
		
		//In case user interchange start & end date handle this scenario
		if(startDate.after(endDate))
		{			
			Calendar temp = endDate;
			endDate = startDate;
			startDate = temp;
		}
		
		//If data checks fail return error message
		if(!doDataChecks(startDate, endDate, sb))
		{
			return sb.toString();
		}		
		
		ArrayList<ArrayList<Object>> alFullData = getData(startDate, endDate);
		
		for(int i = 0 ; i < alFullData.get(0).size() ; ++i)
		{
			sb.append(dateFormat.format(((Calendar)alFullData.get(0).get(i)).getTime()));
			sb.append("\t");
			sb.append(alFullData.get(1).get(i));
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
	public String getMovingAverage(Calendar startDate, Calendar endDate, int daysAverage)
	{		
		System.out.println("Moving average Query received between dates startDate = "+ startDate.getTime() + " End date = "+ endDate.getTime() + " daysAverage = "+ daysAverage);
		StringBuilder sb = new StringBuilder();
		
		//In case user interchange start & end date handle this scenario
		if(startDate.after(endDate))
		{			
			Calendar temp = endDate;
			endDate = startDate;
			startDate = temp;
		}
		
		Calendar dataStartDate = (Calendar)startDate.clone();
		dataStartDate.add(Calendar.DATE, -1 * (daysAverage));
		
		//If data checks fail return error message
		if(!doDataChecks(dataStartDate, endDate, sb))
		{
			return sb.toString();
		}		
		
		ArrayList<ArrayList<Object>> alFullData = getData(dataStartDate, endDate);
		
		String status = "";
		String prevStatus = "";
		String buyDecision = "HOLD";
		
		for(int i =  0 ; i < alFullData.get(0).size() - daysAverage ; ++i)
		{
			Double movingAverage = findMovingAverage(alFullData.get(1) , i ,daysAverage);
			
			
			
			if(movingAverage > (Double)alFullData.get(1).get(i))			
				status = "above";			
			else
				status = "below";
			
			if(i == 0)
				prevStatus = status;
			
			if(!prevStatus.equals(status))
			{
				if(prevStatus.equalsIgnoreCase("above"))
					buyDecision = "BUY";
				else
					buyDecision = "SELL";
			}
			else
				buyDecision = "HOLD";
			
			sb.append("Date = "+ dateFormat.format((((Calendar)alFullData.get(0).get(i)).getTime())) + " price = "+ String.format("%.2f",alFullData.get(1).get(i))
					+ " movingAverage of "+ daysAverage + " days = "+ String.format("%.2f", movingAverage) + " Status = "+ status + " buyDecision = "+ buyDecision + "\n");
			
			prevStatus = status;
		}
		
		return sb.toString();
	}

	private Double findMovingAverage(ArrayList<Object> arrayList, int loc, int daysAverage) 
	{
		int count = 0;
		double total = 0.0;
		
		for(int i = loc; count < daysAverage ; ++i )
		{
			total += (Double)arrayList.get(i);
			//System.out.println("Number found = "+ arrayList.get(i));
			++count;
		}

		double average = total/count;		
		
		return average;
	}



}
