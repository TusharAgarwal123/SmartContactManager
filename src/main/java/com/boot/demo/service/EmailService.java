package com.boot.demo.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String message, String subject, String to) {

		// variable for gmail host.
		// host for gmail is smtp(simple mail transfer protocol)

		boolean flag = false;

		String from = "tushar.agarwal@unthinkable.co";

		String host = "smtp.gmail.com";

		// get the system properties.

		Properties properties = System.getProperties();
		System.out.println("Properties" + properties);

		// setting important information to properties object.

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465"); // this is port
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step1: to get the session object...

		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("tushar.agarwal@unthinkable.co", "tushar@12345");
			}

		});

		session.setDebug(true);

		// step2: compose the message[text,multi media]

		MimeMessage m = new MimeMessage(session);

		try {
			// from email

			m.setFrom(from);

			// adding receiver
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message

			m.setSubject(subject);

			// adding text to message

			m.setText(message);

			// m.setContent(message, "text/hmtl"); // now this message will go in html type.

			// send

			// step3: send the message using transport class.
			Transport.send(m);

			System.out.println("Send Success...");

			flag = true;

		} catch (MessagingException e) {

			e.printStackTrace();
		}

		return flag;

	}

}
