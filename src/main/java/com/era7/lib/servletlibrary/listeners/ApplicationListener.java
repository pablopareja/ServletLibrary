
package com.era7.lib.servletlibrary.listeners;

import com.era7.lib.communication.util.ActiveSessions;
import com.era7.lib.communication.util.SessionTimeoutChecker;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 *
 *
 * @author Pablo Pareja Tobes
 * @author Eduardo Pareja Tobes
 */
public class ApplicationListener implements ServletContextListener{

    protected final static int SESSION_TIMER_PERIOD = 60000;
    protected static Timer SESSION_TIMER = null;

    @Override
    public final void contextInitialized(ServletContextEvent sce) {
        ActiveSessions.init();

        TimerTask timeoutChecker = new SessionTimeoutChecker();

        SESSION_TIMER = new Timer();
        SESSION_TIMER.schedule(timeoutChecker,0,SESSION_TIMER_PERIOD);

        contextInitializedHandler(sce.getServletContext());
    }

    @Override
    public final void contextDestroyed(ServletContextEvent sce) {
        contextDestroyedHandler(sce.getServletContext());
    }

    /**
     * Method called when the servlet context has been initialized
     * @param context Servlet context
     */
    protected void contextInitializedHandler(ServletContext context){}
    /**
     * Method called when the servlet context has been destroyed
     * @param context Servlet context
     */
    protected void contextDestroyedHandler(ServletContext context){}

}
