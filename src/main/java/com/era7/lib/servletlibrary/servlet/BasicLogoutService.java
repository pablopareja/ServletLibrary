package com.era7.lib.servletlibrary.servlet;

import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.util.ActiveSessions;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import com.era7.lib.era7jdbcapi.DBConnection;
import com.era7.lib.era7jdbcapi.DataBaseException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class that should be extended for a Logout service implementation
 * @author Pablo Pareja Tobes
 *
 */
public abstract class BasicLogoutService extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Parameter name used in the GET/POST request to pass the xml request
     */
    public static String PARAMETER_NAME = "request";
    /**
     * Flag indicating whether the servlet should log the successful operations
     */
    public boolean loggableFlag = false;
    /**
     * Flag indicating whether the servlet should log the errors
     */
    public boolean loggableErrorsFlag = false;
    /**
     * Flag indicating whether the servlet should retrieve a connection with the DB system and pass
     * it to the processRequest() method or not (The sevlet logic does not require connecting to a DB).
     * If dbConnectionNeedeFlag is set to false in the init method of the servlet, the instance
     * of {@link java.sql.Connection} passed to the processRequest() method will be <code>null</code>
     */
    public boolean dbConnectionNeededFlag = true;

    @Override
    public final void init() {

        this.loggableErrorsFlag = defineLoggableErrorsFlag();
        this.loggableFlag = defineLoggableFlag();

        initServlet();

    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        servletLogic(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        servletLogic(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Basic logout service";
    }

    /**
     * Logic for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request Servlet request
     * @param response Servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private final void servletLogic(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BasicSession session = null;
        Connection connection = null;

        String requestString = (String) request.getParameter(PARAMETER_NAME);

        Request myRequest = null;
        Response myResponse = new Response();

        try {

            myRequest = new Request(requestString);

            if (dbConnectionNeededFlag) {

                if (DBConnection.SESSION_GUIDED_CONNECTIONS_FLAG) {
                    connection = DBConnection.getLoginConnection();
                } else {
                    //---> getting a new connection <--
                    connection = DBConnection.getNewConnection();
                }
            }

            BasicSession tempSession = ActiveSessions.getSession(myRequest.getSessionID());

            proceedWithLogoutOperations(myRequest, tempSession, connection);

            tempSession = ActiveSessions.destroySession(tempSession.getSessionId());

            if (tempSession != null) {
                myResponse.setStatus(Response.SUCCESSFUL_RESPONSE);
            } else {
                myResponse.setStatus(Response.ERROR_RESPONSE);
                myRequest.detach();
                myResponse.setRequestSource(myRequest);
            }
            //--> Assigning the request id to its response
            myResponse.setId(myRequest.getId());
            //--> Assigning the request method to its response
            myResponse.setMethod(myRequest.getMethod());

            if (loggableFlag) {

                if (myResponse.getStatus().equals(Response.SUCCESSFUL_RESPONSE)) {
                    /*
                     * The call to logSuccessfulOperation will include as many parameters as needed
                     * to perform the successful logging operation.
                     * (For example, the logged user could be passed as a parameter)
                     *
                     * this.logSuccessfulOperation(myRequest,myResponse,connection,user);
                     *
                     */
                    logSuccessfulOperation(myRequest, myResponse, connection, session);
                } else if (myResponse.getStatus().equals(Response.ERROR_RESPONSE)) {
                    /*
                     * The call to logSuccessfulOperation will include as many parameters as needed
                     * to perform the error logging operation.
                     * (For example, the logged user could be passed as a parameter)
                     *
                     * this.logErrorResponseOperation(myRequest,myResponse,connection,user);
                     *
                     */
                    logErrorResponseOperation(myRequest, myResponse, connection, session);
                }
            }

            //The connection is only closed in case it is not the LOGIN_CONNECTION, otherwise
            //we will be closing the default initial connection
            if (DBConnection.SESSION_GUIDED_CONNECTIONS_FLAG && connection.hashCode() != DBConnection.LOGIN_CONNECTION_HASH_CODE) {
                DBConnection.closeConnection(connection);                
            }


            response.setContentType("text/html");
            // write response
            PrintWriter writer = response.getWriter();
            writer.println(myResponse.toString());
            writer.close();

        } catch (Throwable e) {
            e.printStackTrace();
            if (loggableErrorsFlag) {
                /*
                 * The call to logErrorExceptionOperation will include as many parameters as needed
                 * to perform the error exception logging operation.
                 * (For example, the logged user could be passed as a parameter)
                 *
                 * this.logErrorExceptionOperation(myRequest,myResponse, user, e,connection);
                 *
                 */
                logErrorExceptionOperation(myRequest, myResponse, e, connection);
            }

        } finally {

            if (connection != null) {
                try {

                    if (!DBConnection.SESSION_GUIDED_CONNECTIONS_FLAG) {
                        DBConnection.closeConnection(connection);
                    }

                } catch (DataBaseException e) {
                    System.out.println(e.toString());
                }
            }
        }
    }

    /**
     * This method must be implemented in order to define the loggable flag
     * @return True if requests must be logged, false otherwise.
     */
    protected abstract boolean defineLoggableFlag();

    /**
     * This method must be implemented in order to define the loggable flag
     * @return True if error requests must be logged, false otherwise.
     */
    protected abstract boolean defineLoggableErrorsFlag();

    public abstract void initServlet();

    protected final boolean defineCheckSessionFlag() {
        return true;
    }

    protected final boolean defineCheckPermissionsFlag() {
        return false;
    }

    /**
     * Method to include the logic for the logout
     * @param request Object request
     * @param connection Connection with the DB
     */
    abstract protected void proceedWithLogoutOperations(Request request, BasicSession session, Connection connection);

    /**
     * Method called when the operation has been performed successfully plus the flag
     * 'loggableFlag' is true
     */
    protected abstract void logSuccessfulOperation(Request request, Response response, Connection connection,
            BasicSession session);

    /**
     * Method called when the operation could not be performed because of an error ocurred
     * in the processRequest method
     * This method is called as long as 'loggableErrorsFlag' is true
     */
    protected abstract void logErrorResponseOperation(Request request, Response response, Connection connection,
            BasicSession session);

    /**
     * Method called when the operation could not be performed because of an exception ocurred
     * This method is called as long as 'loggableErrorsFlag' is true
     */
    protected abstract void logErrorExceptionOperation(Request request, Response response, Throwable e, Connection connection);
}

