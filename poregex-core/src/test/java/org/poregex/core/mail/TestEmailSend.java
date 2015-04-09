package org.poregex.core.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.google.common.base.Charsets;

public class TestEmailSend {

	@Test
	public void testSendEmail() throws MessagingException {
		Properties prop = System.getProperties();
		
		prop.put("mail.smtp.starttls.enable", "true");     // gmail은 무조건 true 고정
		prop.put("mail.smtp.host", "smtp.gmail.com");      // smtp 서버 주소
		prop.put("mail.smtp.auth","true");                 // gmail은 무조건 true 고정
		prop.put("mail.smtp.port", "587");                 // gmail 포트
		
		Authenticator auth = new PorgexAuthentication();
		
		Session session = Session.getDefaultInstance(prop, auth);
		MimeMessage msg = new MimeMessage(session);
		
		
			//발송시간
		msg.setSentDate(new Date());
		
		InternetAddress from = new InternetAddress("kikimans.lucas@gmail.com"); //발신자			
		msg.setFrom(from);
		
		InternetAddress to = new InternetAddress("kikimans@jyes.co.kr");
		msg.setRecipient(RecipientType.TO, to);
		
		msg.setSubject("메일 전송 테스트", Charsets.UTF_8.toString());
		
		msg.setText("테스트 메일 발송입니다", Charsets.UTF_8.toString());
		
		msg.setHeader("content-Type", "text/html");
		
		Transport.send(msg);	
			
		
		
	}
	
	public static class PorgexAuthentication extends Authenticator{
		PasswordAuthentication pa;

		public PorgexAuthentication() {
			String emailId = "kikimans.lucas@gmail.com";
			String pw = "alsgh@1716";
			
			pa = new PasswordAuthentication(emailId, pw);
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			// TODO Auto-generated method stub
			return pa;
		}
		
		
		
		
	}
	
	@Test
	public void test1(){
		System.out.println(Charsets.UTF_8.toString());
	}

}
