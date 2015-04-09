/**
 * 
 */
package org.poregex.core.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author kikimans
 *
 */
public class SendMail {

	private static final Logger logger = LoggerFactory.getLogger(SendMail.class);
	private Authenticator auth;	
	
	public SendMail() {
		this.auth = new PorgexAuthehtication();
	}
	
	public SendMail(String userName, String password){
		this.auth = new PorgexAuthehtication(userName, password);
	}
	
	private Properties getProperties(){
		Properties prop = System.getProperties();
		
		prop.put("mail.smtp.starttls.enable", "true");     // gmail은 무조건 true 고정
		prop.put("mail.smtp.host", "smtp.gmail.com");      // smtp 서버 주소
		prop.put("mail.smtp.auth","true");                 // gmail은 무조건 true 고정
		prop.put("mail.smtp.port", "587");                 // gmail 포트
		
		return prop;
	}
	
	public boolean sendTransper(String subJectText, String errlogText){			
		boolean isTransper = false;
		
		try {
			Session session = Session.getDefaultInstance(getProperties(), auth);
			MimeMessage msg = new MimeMessage(session);
			msg.setSentDate(new Date());
			
			InternetAddress from = new InternetAddress("kikimans.lucas@gmail.com"); //발신자			
			msg.setFrom(from);
			
			InternetAddress to = new InternetAddress("kikimans@jyes.co.kr");
			msg.setRecipient(RecipientType.TO, to);
			
			msg.setSubject(subJectText, Charsets.UTF_8.toString());
			
			msg.setText(errlogText, Charsets.UTF_8.toString());
			
			msg.setHeader("content-Type", "text/html");
			
			Transport.send(msg);
			isTransper = true;
		} catch (MessagingException e) {
			// TODO: handle exception
			logger.error("Eamil Alert Transport error ", e);
			
		}
		
		return isTransper;
			
	}
	
	
}
