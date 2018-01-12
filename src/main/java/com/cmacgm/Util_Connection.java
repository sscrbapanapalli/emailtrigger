package com.cmacgm;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class Util_Connection {

	private Properties configProp = new Properties();
	InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
	
	public  Connection GetConnection() throws SQLException{
		
		try {
			configProp.load(in);
		} catch (IOException e) {
			throw new RuntimeException("config.properties not loaded properly");
		}
		Connection con = null;
		  String url = configProp.getProperty("spring.datasource.url");
		  String driver = configProp.getProperty("spring.datasource.driver-class-name");
		  String user = configProp.getProperty("spring.datasource.username");
		  String pass = configProp.getProperty("spring.datasource.password");
		  try{
		  Class.forName(driver).newInstance();
		  con = (Connection) DriverManager.getConnection(url, user, pass);	
		  
		  }
		  catch (SQLException s){
		  System.out.println("SQL code does not execute.");
		  }  
		  catch (Exception e){
		  e.printStackTrace();
		  }
	
		return con;
	}	
	private Date yesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	private Date formattedLastThree() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -2);
	    return cal.getTime();
	}
	
	public String formmatedDate(){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");

		    String dmy = dmyFormat.format(yesterday());

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	
	public String formattedLastThreeDayDate(){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");

		    String dmy = dmyFormat.format(formattedLastThree());

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	public String todayFormattedDate(){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");

		    String dmy = dmyFormat.format(new Date());

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	public String formmatedDateHHMMSS(){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");

		    String dmy = dmyFormat.format(yesterday());

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	public String todayFormattedDateHHMMSS(){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		    String dmy = dmyFormat.format(new Date());

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	public String getFormattedDate(Date date){
		try {		  

		    SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");

		    String dmy = dmyFormat.format(date);

		   return dmy;
		} catch (Exception exp) {
		    exp.printStackTrace();
		}
		return null;
	
	}
	
	public  String getLastMondayDateddMMyyyy() {		
		// Converting Calendar to Date.
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.DAY_OF_WEEK, -7);
		Date currentDate = calendar.getTime();
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		// Converting Date to String.
		String myCurrentDate = format.format(currentDate);
		return myCurrentDate;
	}
	public String getFormatDate() {
		DateFormat outputformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
		String output = null;
		output = outputformat.format(new Date());
		return output;
	}
	
	
	public static String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss"); //
	    Date now = new Date();
	    String strCurrTime = sdfDate.format(now);
	    return strCurrTime;
	}

	
}
