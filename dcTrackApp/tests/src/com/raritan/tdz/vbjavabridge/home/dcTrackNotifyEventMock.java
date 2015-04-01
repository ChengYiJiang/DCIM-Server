package com.raritan.tdz.vbjavabridge.home;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;

public class dcTrackNotifyEventMock implements Work {

	private SessionFactory sessionFactory = null;
	private int times = 0;
	private String message = null;
	
	public dcTrackNotifyEventMock(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	public void notifyMessage(String message, int times ){
		this.times = times;
		this.message = message;
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			session.doWork(this);
		}
	}
	
	@Override
	public void execute(Connection connection) throws SQLException {
		String cmd = "NOTIFY " + message + ";";
		for (int i = 0; i < times; i++){
			Statement stmt = connection.createStatement();
			try {
				Boolean result = stmt.execute(cmd);
			} catch (SQLException e) {
				// TODO: handle exception
				System.out.println(e.getMessage());
			}
			int updateCnt = stmt.getUpdateCount();
			stmt.close();
		}
	}

}
