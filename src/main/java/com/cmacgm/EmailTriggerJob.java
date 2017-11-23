package com.cmacgm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

@Component
@PropertySource("classpath:config.properties")
public class EmailTriggerJob {

	private Properties configProp = new Properties();
	private String emailsender = "";
	private String emailsenderfrance = "";
	private String pathToStore = "C:\\testing\\";
	private String startDate="2017-10-16";

	@Scheduled(cron = "${cronExpressionHtml}")
	public void JupiterRecordAlert() throws SQLException {

		Connection connection = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);
			emailsender = configProp.getProperty("senderemail");			
			startDate= configProp.getProperty("startDate");;
		} catch (IOException e) {
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {
			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();			
			
			StringBuilder buf = new StringBuilder();
			buf.append("<html><body><b>Job Last Run Time: </b>" + util_connection.getFormatDate() + "</br>");
			
			String dayWiseDate="";
			String dailyIndexRate =null;
			ResultSet res = null;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			buf.append("<b>Monthly Wise Summary Report: </b></br> <table border='1'>" + "<tr>" + "<th>Month</th><th>Total Index</th>"
					+ "<th>Manual Index</th>" + "<th>Auto Index</th>" + "<th>Draft Sent</th></tr>");
			    Calendar calm = Calendar.getInstance();
			    calm.set(2017,8, 16); //October 16th 2017      
			  
	           Calendar currentDate = Calendar.getInstance();	 
	           currentDate.add(Calendar.MONTH, -1);  
	           while (!calm.getTime().after(currentDate.getTime())) {	        	
	        	   calm.add(Calendar.MONTH, 1);  
	            Date getmonth = calm.getTime();
	            String monthDate=util_connection.getFormattedDate(getmonth);

	        			dailyIndexRate = " select * from ((select COUNT(*) as total_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
					+ monthDate
					+ "', '%b') and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as total_index, (select COUNT(*) as auto_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
					+ monthDate
					+ "', '%b')  and IndStartTime IS NOT NULL and IndStartUser='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as auto_index,(select COUNT(*) as manual_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
					+ monthDate
					+ "', '%b')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as manual_index,(select COUNT(*) as draft_sent FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
					+ monthDate
					+ "', '%b')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') ) as draft_sent)";
	        
			res = st.executeQuery(dailyIndexRate);

			while (res.next()) {
				String total_index = res.getString("total_index");
				String manual_index = res.getString("manual_index");
				String auto_index = res.getString("auto_index");
				String draft_sent = res.getString("draft_sent");
				formatter = new SimpleDateFormat("MMMM");
				String monthName = formatter.format(getmonth);
				/*  System.out.println("month_name:"+monthName);
				  System.out.println("total_index " +total_index);
				  System.out.println("manual_index " +manual_index);
				  System.out.println("auto_index " +auto_index);
				  System.out.println("draft_sent " +draft_sent);
				 */
				buf.append("<tr><td>").append(monthName).append("</td><td>").append(total_index).append("</td><td>").append(manual_index).append("</td><td>")
						.append(auto_index).append("</td><td>").append(draft_sent).append("</td></tr>");
			}
				
				
			}
			buf.append("</table>");
			
			 buf.append("</br><b>Weekly Wise Summary Report: </b></br> <table border='1'>" + "<tr>" + "<th>Week</th><th>Total Index</th>"
						+ "<th>Manual Index</th>" + "<th>Auto Index</th>" + "<th>Draft Sent</th></tr>");
			     int wtotal_index = 0;
				 int wmanual_index = 0;
				 int wauto_index =  0;
				 int wdraft_sent =  0;				
				 int tempRate=0;
				 formatter = new SimpleDateFormat("yyyy-MM-dd");
				 Date startDateWise = formatter.parse(startDate);
				 Date endDateWise =formatter.parse(util_connection.getFormattedDate(new Date()));
				 Calendar startw = Calendar.getInstance();
				 startw.setTime(startDateWise);
				 Calendar endw = Calendar.getInstance();
				 endw.setTime(endDateWise);
			 for (Date date = startw.getTime(); startw.before(endw); startw.add(Calendar.DATE, 1), date = startw.getTime()) {
				 tempRate++;		
				 int WEEK_OF_YEAR = 1;						
				 WEEK_OF_YEAR= startw.get(Calendar.WEEK_OF_YEAR);
				 dayWiseDate= util_connection.getFormattedDate(date);
			     dailyIndexRate = "select (SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as total_index,"
					+ "(SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartUser!='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as manual_index ,"
					+ "(SELECT COUNT(*)  FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartUser='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as auto_index,"
					+ "(SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as draft_sent ";

			 res = st.executeQuery(dailyIndexRate);
			while (res.next()) {
				
				if(tempRate>0 && tempRate<=7){
				     wtotal_index+=Integer.parseInt(res.getString("total_index"));
					 wmanual_index+=Integer.parseInt(res.getString("manual_index"));
					 wauto_index+=Integer.parseInt(res.getString("auto_index"));
					 wdraft_sent+=Integer.parseInt(res.getString("draft_sent"));
			     }
				 if(tempRate>0 && tempRate==7){					   
				     tempRate=0;
				     buf.append("<tr><td>").append(WEEK_OF_YEAR).append("</td><td>").append(wtotal_index).append("</td><td>").append(wmanual_index).append("</td><td>")
						.append(wauto_index).append("</td><td>").append(wdraft_sent).append("</td></tr>");
				      wtotal_index = 0;
					  wmanual_index = 0;
					  wauto_index =  0;
					  wdraft_sent =  0;
				  }
				
				
				/*
				 System.out.println(dayWiseDate); System.out.println(
				 "wtotal_index " +wtotal_index); System.out.println(
				  "wmanual_index " +wmanual_index); System.out.println(
				 "wauto_index " +wauto_index); System.out.println("wdraft_sent "
				  +wdraft_sent);*/
				 
			}
			   }
				buf.append("</table></br>");
		
			
			 Calendar start = Calendar.getInstance();
			 start.setTime(endDateWise);
			 start.add(Calendar.DATE, -7);
			 Calendar end = Calendar.getInstance();
			 end.setTime(endDateWise);
			 buf.append("<b>Day Wise Summary Report: </b></br> <table border='1'>" + "<tr>" + "<th>Day</th><th>Total Index</th>"
						+ "<th>Manual Index</th>" + "<th>Auto Index</th>" + "<th>Draft Sent</th></tr>");
			 for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
				 dayWiseDate= util_connection.getFormattedDate(date);
			
			 dailyIndexRate = "select (SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as total_index,"
					+ "(SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartUser!='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as manual_index ,"
					+ "(SELECT COUNT(*)  FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and IndStartUser='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as auto_index,"
					+ "(SELECT COUNT(*) FROM dtl_index where DATE(IndStartTime)='" + dayWiseDate
					+ "' and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as draft_sent ";

			 res = st.executeQuery(dailyIndexRate);
			while (res.next()) {
				String total_index = res.getString("total_index");
				String manual_index = res.getString("manual_index");
				String auto_index = res.getString("auto_index");
				String draft_sent = res.getString("draft_sent");
				
				buf.append("<tr><td>").append(dayWiseDate).append("</td><td>").append(total_index).append("</td><td>").append(manual_index).append("</td><td>")
						.append(auto_index).append("</td><td>").append(draft_sent).append("</td></tr>");
				
				/* System.out.println(dayWiseDate); System.out.println(
				 "total_index " +total_index); System.out.println(
				  "manual_index " +manual_index); System.out.println(
				  "auto_index " +auto_index); System.out.println("draft_sent "
				  +draft_sent);*/
				 
			}
			   }buf.append("</table>");
			
				
				
			
			/*dailyIndexRate = " select * from ((select COUNT(*) as total_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
					+ dateString
					+ "', '%Y') and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as total_index, (select COUNT(*) as auto_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
					+ dateString
					+ "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as auto_index,(select COUNT(*) as manual_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
					+ dateString
					+ "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as manual_index,(select COUNT(*) as draft_sent FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
					+ dateString
					+ "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') ) as draft_sent)";

			res = st.executeQuery(dailyIndexRate);

			while (res.next()) {
				String total_index = res.getString("total_index");
				String manual_index = res.getString("manual_index");
				String auto_index = res.getString("auto_index");
				String draft_sent = res.getString("draft_sent");
				Format formatter = new SimpleDateFormat("yyyy");
				String year = formatter.format(new Date());
				buf.append("<b>Year: " + year + "</b><table border='1'>" + "<tr>" + "<th>Total Index</th>"
						+ "<th>Manual Index</th>" + "<th>Auto Index</th>" + "<th>Draft Sent</th></tr>");
				buf.append("<tr><td>").append(total_index).append("</td><td>").append(manual_index).append("</td><td>")
						.append(auto_index).append("</td><td>").append(draft_sent).append("</td></tr></table>");
				
				 * System.out.println("year:"+year); System.out.println(
				 * "total_index " +total_index); System.out.println(
				 * "manual_index " +manual_index); System.out.println(
				 * "auto_index " +auto_index); System.out.println("draft_sent "
				 * +draft_sent);
				 
			}*/
			buf.append("</body></html>");	
			SendMail sendEmail=new SendMail();
			Thread.sleep(10);
			sendEmail.SendMail("Jupiter Record Count",emailsender,buf.toString());
			
			
		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		
	}
	
	@Scheduled(cron = "${cronExpressionFrance}")
	public void JupiterRecordAttachment() throws SQLException {

		Connection connection = null;
		String dailyIndexRate =null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);		
			pathToStore = configProp.getProperty("pathToStore");
			emailsenderfrance= configProp.getProperty("emailsenderfrance");
		} catch (IOException e) {
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {
			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();			
			String yesterdayDate="";
			String todayDate="";
			ResultSet res = null;
			    yesterdayDate=util_connection.formmatedDate();
				todayDate=util_connection.todayFormattedDate();
				yesterdayDate=yesterdayDate+" 06:00:00";
				todayDate=todayDate+" 05:59:59";				
			dailyIndexRate="Select * from dtl_query where id!='' and query_created_date between '"+ yesterdayDate+"'  AND '"+todayDate+"'";		
			res = st.executeQuery(dailyIndexRate);
			Thread.sleep(10);
			writeQueryResult(res,"QueryCreated");
			
			dailyIndexRate="Select * from dtl_index where id!='' and IndStartTime  between '"+ yesterdayDate+"' AND '"+todayDate+"'";
			res = st.executeQuery(dailyIndexRate);
			Thread.sleep(10);
			writeQueryResult(res,"IndexStarted");
		
			dailyIndexRate="Select * from dtl_audit_errors where id!='' and error_marked_on between '"+ yesterdayDate+"'  AND '"+todayDate+"'";		
			res = st.executeQuery(dailyIndexRate);
			Thread.sleep(10);
			writeQueryResult(res,"ErrorMarked");
			
			dailyIndexRate="Select * from dtl_correction_query where id!='' and created_date between '"+ yesterdayDate+"'  AND '"+todayDate+"'";		
			res = st.executeQuery(dailyIndexRate);
			Thread.sleep(10);
			writeQueryResult(res,"QueryCorrection");
	
			Thread.sleep(10);
			SendEmailAttachment("Yesterday Jupiter Daily Records Count Attached for Reference");
			
			
		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		
	}

	public void writeQueryResult(ResultSet rs,String fileName) throws IOException, SQLException {
		File pathToFileStore=new File(pathToStore); 
		if(!pathToFileStore.exists())
		pathToFileStore.mkdir();
		
		File file1=new File(pathToStore+fileName+".xls");
		if(file1.exists())
			file1.delete();
		else
		file1.createNewFile();		
		writeToOutputPath(rs,fileName,file1);	
	}
	
	
	public void writeToOutputPath(ResultSet rs,String filename, File file) throws SQLException, IOException{
		try{			
			WritableWorkbook Wworkbook = Workbook.createWorkbook(file);	
		WritableSheet Wsheet =Wworkbook.createSheet(filename,0); 
		int colCount,col,row=1; 
		ResultSetMetaData rsmd = rs.getMetaData(); 
		colCount = rsmd.getColumnCount();	
		Label label = new Label(0,0,filename); 
		Wsheet.mergeCells(0,0,4,0); 
		Wsheet.addCell(label); 	
		for(col=0;col<colCount;col++) 
		{ 
		label = new 
		Label(col,row,rsmd.getColumnName(col+1)); 
		Wsheet.addCell(label); 
		} 
		row++; 


		while(rs.next()) 
		{ 
		for(col=0;col<colCount;col++) 
		{ 
		label = new Label(col,row, 
		rs.getString(col+1)); 
		Wsheet.addCell(label); 
		} 
		row++; 
		} 

		Wworkbook.write(); 
		Wworkbook.close(); 
		
		} 
		catch(Exception e) 
		{ 
		System.out.println("Exception in file generation "+e); 
		} 
	
				
	}
	
public void SendEmailAttachment(String content) throws IOException, SQLException {
		SendMail sendEmail=new SendMail();
		boolean filesend1=false;
		boolean filesend2=false;
		boolean filesend3=false;
		boolean filesend4=false;
		
		File file1=new File(pathToStore+"QueryCreated.xls");		
		if(file1.exists())
			filesend1=true;
		else
			file1=null;
		File file2=new File(pathToStore+"IndexStarted.xls");		
		if(file2.exists())
			filesend2=true;
		else
			file2=null;
	
		File file3=new File(pathToStore+"ErrorMarked.xls");		
		if(file3.exists())
			filesend3=true;
		else
			file3=null;
		File file4=new File(pathToStore+"QueryCorrection.xls");		
		if(file4.exists())
			filesend4=true;
		else
			file4=null;
		
		if(filesend1||filesend2||filesend3||filesend4)
		sendEmail.SendMailwithattachement("Jupiter Record Day Count", emailsenderfrance, content, file1, file2,file3,file4);
	}
}


