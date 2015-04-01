package com.raritan.tdz.mail;

import java.util.Map;

public interface  MailDao {

	public static final String SMTP_SERVER ="smtpServer";
	public static final String SMTP_PORT ="smtpPort";
	public static final String SMTP_AUTH_TYPE ="smtpAuthType";
	public static final String SMTP_USER ="smtpUsername";
	public static final String SMTP_PASSWORD ="smtpPassword";
	public static final String SMTP_FROM_EMAIL ="fromEmail";
	public static final String SMTP_ENCRYPTION_METHOD ="smtpEncryptionMethod";
	
	
	public Map<String, Object> getMailClientProperties();
	
}
