package com.era7.lib.servletlibrary.listeners;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.era7.lib.communication.util.SessionAttributes;
import com.era7.lib.era7jdbcapi.DBConnection;

/**
 * This class must be included in the application descriptor as an application listener
 * Otherwise the session guide system for connections with the db would not work properly
 * @author Pablo Pareja Tobes
 * @deprecated
 */
@Deprecated
public class SessionListener implements HttpSessionListener {
	
		
	/**
	 * Method called when a new session has been created
	 */
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		
		//HttpSession session = event.getSession();
		//Code needed when a session has been created should be placed here
		
	}
	
	/**
	 *	Method called when a session has been destroyed
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		
		HttpSession session = event.getSession();	
		
		//-------------------------------------------------------------------------------------------------------
		//-------------Active user-driven connections are closed when their sessions are destroyed---------------		
		if(DBConnection.SESSION_GUIDED_CONNECTIONS_FLAG){
			Connection connection = (Connection)session.getAttribute(SessionAttributes.CONNECTION_ATTRIBUTE);
			try{
				//The connection is only closed in case it is not the LOGIN_CONNECTION, otherwise
				//we will be closing the default initial connection
				if(connection.hashCode() != DBConnection.LOGIN_CONNECTION_HASH_CODE){
					connection.close();
					DBConnection.ACTIVE_DB_CONNECTIONS_COUNTER--;
				}				
			}catch (SQLException e) {				
				System.out.println("The connection could not be closed!!");
			}
		}
		//-------------------------------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------------------------------
		
	}

}
