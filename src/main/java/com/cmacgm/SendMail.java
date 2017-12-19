package com.cmacgm;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServlet;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Servlet implementation class SMPTemail
 */
public class SendMail extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Boolean SendMail(String subject, String text) {

		Boolean mailsuccess = false;
		try {
			Boolean flag = true;
			Session session;
			Properties props = new Properties();
			props.load(SendMail.class.getClassLoader().getResourceAsStream("config.properties"));
			props.put("mail.transport.protocol", props.get("mail.transport.protocol"));
			props.put("mail.smtp.host", props.get("mail.smtp.host"));
			String fromAddress = (String) props.get("mail.fromAddress");
			String toAddress = (String) props.get("emailsenderTo");
			String ccAddress = (String) props.get("emailsenderCC");
			String bccAddress = (String) props.get("emailsenderScheduledReports_BCC");
			if(fromAddress.isEmpty())
          	  return flag;
			// tls enabled
			props.put("mail.smtp.starttls.enable", props.get("mail.smtp.starttls.enable"));
			props.put("mail.smtp.port", props.get("mail.smtp.port"));

			MailSSLSocketFactory sf = null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e1) {
				e1.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.socketFactory", props.get("mail.smtp.ssl.socketFactory"));
			session = Session.getInstance(props, null);
			Message message = new MimeMessage(session);
			
			message.setFrom(new InternetAddress(fromAddress));
			
			InternetAddress[] iAdressArrayTo = InternetAddress.parse(toAddress.trim());
			message.setRecipients(Message.RecipientType.TO, iAdressArrayTo);	
			
			if(!ccAddress.isEmpty()){
			InternetAddress[] iAdressArrayCC = InternetAddress.parse(ccAddress.trim());
			message.setRecipients(Message.RecipientType.CC, iAdressArrayCC);
			}
			if(!bccAddress.isEmpty()){
				InternetAddress[] iAdressArrayBBC = InternetAddress.parse(bccAddress.trim());
				message.setRecipients(Message.RecipientType.BCC, iAdressArrayBBC);
				}
		  	     
		
			message.setSubject(subject);
			message.setContent(text, "text/html; charset=utf-8");

			Transport.send(message);
			mailsuccess = true;
			flag = true;
			if (flag == false) {
				mailsuccess = false;

			}
		} catch (Exception e) {
			e.printStackTrace();
			mailsuccess = false;
		}
		return mailsuccess;
	}

	public Boolean SendMailwithattachement(String subject,String text, File QueryCreatedDate,
			File IndexStartedTime, File errorMarked, File correctionQuery) {

		Boolean mailsuccess = false;
		try {

			Boolean flag = true;
			Session session;
			Properties props = new Properties();
			props.load(SendMail.class.getClassLoader().getResourceAsStream("config.properties"));
			props.put("mail.transport.protocol", props.get("mail.transport.protocol"));
			props.put("mail.smtp.host", props.get("mail.smtp.host"));
			String fromAddress = (String) props.get("mail.fromAddress");
			String toAddress = (String) props.get("emailsenderfranceTo");
			String ccAddress = (String) props.get("emailsenderfranceCC");
			String bccAddress = (String) props.get("emailsenderScheduledReports_BCC");
              if(fromAddress.isEmpty())
            	  return flag;
			// tls enabled
			props.put("mail.smtp.starttls.enable", props.get("mail.smtp.starttls.enable"));
			props.put("mail.smtp.port", props.get("mail.smtp.port"));

			MailSSLSocketFactory sf = null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e1) {
				e1.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.socketFactory", props.get("mail.smtp.ssl.socketFactory"));
			session = Session.getInstance(props, null);
			Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
			
			InternetAddress[] iAdressArrayTo = InternetAddress.parse(toAddress.trim());
			message.setRecipients(Message.RecipientType.TO, iAdressArrayTo);	
			
			if(!ccAddress.isEmpty()){
			InternetAddress[] iAdressArrayCC = InternetAddress.parse(ccAddress.trim());
			message.setRecipients(Message.RecipientType.CC, iAdressArrayCC);
			}
			if(!bccAddress.isEmpty()){
				InternetAddress[] iAdressArrayBBC = InternetAddress.parse(bccAddress.trim());
				message.setRecipients(Message.RecipientType.BCC, iAdressArrayBBC);
				}
		  	      
			message.setSubject(subject);

			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(text, "text/html; charset=utf-8");
			MimeBodyPart mbp1 = new MimeBodyPart();
			MimeBodyPart mbp = new MimeBodyPart();
			MimeBodyPart mbp2 = new MimeBodyPart();
			MimeBodyPart mbp3 = new MimeBodyPart();
			Multipart mp = new MimeMultipart();
			FileDataSource fds = null;
			if (QueryCreatedDate != null) {
				fds = new FileDataSource(QueryCreatedDate);
				mbp.setDataHandler(new DataHandler(fds));
				mbp.setFileName(fds.getName());
				mp.addBodyPart(mbp);
			}
			// attach the file to the message
			if (IndexStartedTime != null) {
				fds = new FileDataSource(IndexStartedTime);
				mbp1.setDataHandler(new DataHandler(fds));
				mbp1.setFileName(fds.getName());
				mp.addBodyPart(mbp1);
			}
			if (errorMarked != null) {
				fds = new FileDataSource(errorMarked);
				mbp2.setDataHandler(new DataHandler(fds));
				mbp2.setFileName(fds.getName());
				mp.addBodyPart(mbp2);
			}
			if (correctionQuery != null) {
				fds = new FileDataSource(correctionQuery);
				mbp3.setDataHandler(new DataHandler(fds));
				mbp3.setFileName(fds.getName());
				mp.addBodyPart(mbp3);
			}
			message.setText(text);

			mp.addBodyPart(textBodyPart);
			message.setContent(mp);
			message.setSentDate(new Date());

			Transport.send(message);

			mailsuccess = true;
			flag = true;
			if (flag == false) {
				mailsuccess = false;

			}
		} catch (Exception e) {
			e.printStackTrace();
			mailsuccess = false;
		}
		return mailsuccess;
	}

	public Boolean SendScheduledMailwithAttachement(String subject,String text, File QueryCreatedDate) {

		Boolean mailsuccess = false;
		try {

			Boolean flag = true;
			Session session;
			Properties props = new Properties();
			props.load(SendMail.class.getClassLoader().getResourceAsStream("config.properties"));
			props.put("mail.transport.protocol", props.get("mail.transport.protocol"));
			props.put("mail.smtp.host", props.get("mail.smtp.host"));
			String fromAddress = (String) props.get("mail.fromAddress");
			String toAddress = (String) props.get("emailsenderScheduledReports_To");
			String ccAddress = (String) props.get("emailsenderScheduledReports_CC");
			String bccAddress = (String) props.get("emailsenderScheduledReports_BCC");
			if(subject.trim().equals("Log_History"))
			toAddress=(String) props.get("emailsenderlogReports_To");
			if(subject.trim().equals("Audit_Report"))
				toAddress=(String) props.get("emailsenderauditReports_To");
              if(fromAddress.isEmpty())
            	  return flag;
			// tls enabled
			props.put("mail.smtp.starttls.enable", props.get("mail.smtp.starttls.enable"));
			props.put("mail.smtp.port", props.get("mail.smtp.port"));

			MailSSLSocketFactory sf = null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e1) {
				e1.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.socketFactory", props.get("mail.smtp.ssl.socketFactory"));
			session = Session.getInstance(props, null);
			Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
			
			InternetAddress[] iAdressArrayTo = InternetAddress.parse(toAddress.trim());
			message.setRecipients(Message.RecipientType.TO, iAdressArrayTo);	
			
			if(!ccAddress.isEmpty()){
			InternetAddress[] iAdressArrayCC = InternetAddress.parse(ccAddress.trim());
			message.setRecipients(Message.RecipientType.CC, iAdressArrayCC);
			}
			if(!bccAddress.isEmpty()){
				InternetAddress[] iAdressArrayBBC = InternetAddress.parse(bccAddress.trim());
				message.setRecipients(Message.RecipientType.BCC, iAdressArrayBBC);
				}
		  	   
			message.setSubject(subject);

			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(text, "text/html; charset=utf-8");
		
			MimeBodyPart mbp = new MimeBodyPart();			
			Multipart mp = new MimeMultipart();
			FileDataSource fds = null;
			if (QueryCreatedDate != null) {
				fds = new FileDataSource(QueryCreatedDate);
				mbp.setDataHandler(new DataHandler(fds));
				mbp.setFileName(fds.getName());
				mp.addBodyPart(mbp);
			}
		
			message.setText(text);

			mp.addBodyPart(textBodyPart);
			message.setContent(mp);
			message.setSentDate(new Date());

			Transport.send(message);

			mailsuccess = true;
			flag = true;
			if (flag == false) {
				mailsuccess = false;

			}
		} catch (Exception e) {
			e.printStackTrace();
			mailsuccess = false;
		}
		return mailsuccess;
	}

	
}
