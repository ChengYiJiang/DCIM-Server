package com.raritan.tdz.mail;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;


public class DcTrackMailClientImpl {
	
	public static final String SMTP_AUTH_TYPE_NONE ="NONE";	
	public static final String SMTP_AUTH_TYPE_PASSWORD ="PASSWORD";	
	public static final String SMTP_ENCRYPTION_SSL ="SSL";	
	public static final String SMTP_ENCRYPTION_TLS ="TLS";	
	public static final String SMTP_ENCRYPTION_NONE ="NONE";
	
	public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol"; 
	public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
	public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";

	@Autowired(required = true)
	private JavaMailSenderImpl mailSender;
	
	@Autowired(required = true)
	private MailDao mailDao;
	
	public DcTrackMailClientImpl() {
		
	}

	public void sendSimpleMail(String from, String[] to, String subject, String body)  throws MessagingException, MailException {
		// configure e-mail server properties
		
		if (configureMailProperties()) {
			// configure message
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSentDate(new Date());
			message.setSubject(subject);
			message.setText(body);
			// send
			mailSender.send(message);
		}
	}
	
	public void sendMail( String from, String[] to, String subject, String messageBody ) throws MessagingException, MailException {

		// configure e-mail server properties
		if (configureMailProperties()) {
			// configure message
			MimeMessage message = configureMessage (from, to, subject, messageBody, null);
			// send
			mailSender.send(message);
		}
	}
	
	public void sendMail(String from, String[] to, String subject, String messageBody, String fileToAttach) throws MessagingException, MailException {
		
		// configure e-mail server properties
		if (configureMailProperties()) {
			// configure message and attachment
			MimeMessage message = configureMessage (from, to, subject, messageBody, fileToAttach);
			// send
			mailSender.send(message);
		}
	}
	
	private boolean configureMailProperties() {
		
		boolean configurationOk = false;
		
		// configure host, port, protocol, username, password
		
		// read settings from database
		Map<String, Object> mailSettings = mailDao.getMailClientProperties();		

		// setup java mail properties
		Properties javaMailProperties = new Properties();

		if (mailSettings.get(MailDao.SMTP_SERVER) != null &&
				!((String)mailSettings.get(MailDao.SMTP_SERVER)).isEmpty() &&
				mailSettings.get(MailDao.SMTP_PORT) != null &&
				((Integer)mailSettings.get(MailDao.SMTP_PORT)) > 0 &&
				mailSettings.get(MailDao.SMTP_FROM_EMAIL) != null) {
			
			javaMailProperties.setProperty(MAIL_TRANSPORT_PROTOCOL, "smtp");
			mailSender.setHost((String)mailSettings.get(MailDao.SMTP_SERVER));
			mailSender.setPort((Integer)mailSettings.get(MailDao.SMTP_PORT));
			
			if (mailSettings.get(MailDao.SMTP_AUTH_TYPE).equals(SMTP_AUTH_TYPE_PASSWORD)) {
				
				javaMailProperties.setProperty(MAIL_SMTP_AUTH, TRUE);
				mailSender.setUsername((String)mailSettings.get(MailDao.SMTP_USER));
				mailSender.setPassword((String)mailSettings.get(MailDao.SMTP_PASSWORD));
			} else {
				javaMailProperties.setProperty(MAIL_SMTP_AUTH, FALSE);
			}
	
			Object encryptionType = mailSettings.get(MailDao.SMTP_ENCRYPTION_METHOD);
			
			if (encryptionType.equals(SMTP_ENCRYPTION_TLS)) {
				javaMailProperties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, TRUE);
			}
			else if (encryptionType.equals(SMTP_ENCRYPTION_SSL)) {
				javaMailProperties.setProperty(MAIL_SMTP_SSL_ENABLE, TRUE);
			}
			else if (encryptionType.equals(SMTP_ENCRYPTION_NONE)) {
				// do nothing.
			}
			
			//javaMailProperties.setProperty("mail.debug", "true");
			
			mailSender.setJavaMailProperties(javaMailProperties);
			
			configurationOk = true;
		}
		
		return configurationOk;
	}
	
	private Multipart setMessageBody (MimeMessage message, String messageBody) throws MessagingException {
		// message body 
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(messageBody);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// set parts in message content
		message.setContent(multipart);

		return multipart;
	}
	
	private Multipart setMessageAttachment(MimeMessage message, Multipart multiPart, String fileToAttach) throws MessagingException {
		// add attachment
		if (fileToAttach != null) {
			BodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(fileToAttach);

			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileToAttach);
			multiPart.addBodyPart(messageBodyPart);
		}
		return multiPart;
	}

	private MimeMessage configureMessage(String from, String[] to, String subject, String messageBody, String fileToAttach) throws AddressException, MessagingException {
		// configure from, to, subject, message & file attachment
		
		MimeMessage message = mailSender.createMimeMessage();
		// set from: 
		message.setFrom(new InternetAddress(from));
		// set to:
		for (String recepient: to) {
			message.addRecipient(Message.RecipientType.TO,  new InternetAddress(recepient));
		}		
		
		message.setSentDate(new Date());
		message.setSubject(subject);

		// message body 
		Multipart multiPart = setMessageBody (message, messageBody);
		// file attachment
		multiPart = setMessageAttachment(message, multiPart, fileToAttach);
		message.setContent(multiPart);
		return message;
	}
}
