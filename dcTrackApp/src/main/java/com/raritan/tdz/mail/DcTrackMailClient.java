package com.raritan.tdz.mail;

import javax.mail.MessagingException;

import org.springframework.mail.MailException;

public interface DcTrackMailClient {
	
	/**
	 * Send simple e-mail without any attachment 
	 * @param from
	 * @param to
	 * @param subject
	 * @param body
	 * @throws MessagingException
	 * @throws MailException
	 */
	public void sendSimpleMail(String from, String[] to, String subject, String body)  throws MessagingException, MailException;
	
	/**
	 * Send e-mail with mime message. This API does not support sending e-mail attachment.
	 * @param from
	 * @param to
	 * @param subject
	 * @param messageBody
	 * @throws MessagingException
	 * @throws MailException
	 */
	public void sendMail( String from, String[] to, String subject, String messageBody ) throws MessagingException, MailException;

	/**
	 * Send e-mail with mime message and attachment
	 * @param from
	 * @param to
	 * @param subject
	 * @param messageBody
	 * @param fileToAttach
	 * @throws MessagingException
	 * @throws MailException
	 */
	public void sendMail(String from, String[] to, String subject, String messageBody, String fileToAttach) throws MessagingException, MailException;

}
