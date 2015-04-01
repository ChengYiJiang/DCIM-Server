package com.raritan.tdz.mail;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MailDaoImpl implements MailDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Map<String, Object> getMailClientProperties() {
		
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createSQLQuery(new StringBuffer()
		.append("SELECT smtp_server, smtp_port, smtp_auth_type, smtp_username,")
		.append(" smtp_password, from_email, smtp_encryption_method ")
		.append("from rails_options;").toString());
        Map<String, Object> recMap = new HashMap<String, Object>();

        if (q.list().size() == 1) { 
        	Object rec = q.list().get(0);
            Object[] row = (Object[]) rec;
            String smtpServer = (String)row[0];
            recMap.put("smtpServer", smtpServer);
            Integer smtpPort = (Integer)row[1];
            recMap.put("smtpPort", smtpPort);
            String smtpAuthType = (String)row[2];
            recMap.put("smtpAuthType", smtpAuthType);
            String smtpUsername = (String)row[3];
            recMap.put("smtpUsername", smtpUsername);
            String smtpPassword = (String)row[4];
            recMap.put("smtpPassword", smtpPassword);
            String fromEmail = (String)row[5];
            recMap.put("fromEmail", fromEmail);
            String smtpEncryptionMethod = (String)row[6];
            recMap.put("smtpEncryptionMethod", smtpEncryptionMethod);
        }
        return recMap;
	}

}
