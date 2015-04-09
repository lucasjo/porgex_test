package org.poregex.core.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class PorgexAuthehtication extends Authenticator {	

	private PasswordAuthentication pa;
	
	private static final String USERNAME = "openpaasmaster@gmail.com";
	private static final String PASSWORD = "fighter7";
	
	public PorgexAuthehtication() {
		this(USERNAME,PASSWORD);
	}

	public PorgexAuthehtication(String userName, String password) {
		// TODO Auto-generated constructor stub
		this.pa = new PasswordAuthentication(userName, password);
	}
	
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		// TODO Auto-generated method stub
		return pa;
	}
	
	

}
