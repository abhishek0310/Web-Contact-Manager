package com.smart.service;


import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to)
	{
		boolean f = false;
		
		String from = "abhishekpanda0310@gmail.com";
		
		//variable for gmail
		String host = "smtp.gmail.com";
		
		//get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES "+ properties);
		
		
		
		//host set
		
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		
		//step-1: to get the session object..
		
		Session session = Session.getInstance(properties, new Authenticator(){
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication("abhishekpanda0310@gmail.com", "zxibrvkivuxcyxce");
			}
		});
		
		session.setDebug(true);
		
		//step-2 "Compose the message [text, multi media]"
		MimeMessage m = new MimeMessage(session);
		
		try {
			
			//from email
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
			m.setText(message);
			
			//step 3: send message using Transport class
			Transport.send(m);
			
			System.out.println("Sent Success.........");
			f=true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return f; 
		
	}

}
