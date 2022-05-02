package utilities;

import java.sql.*;
import java.util.logging.Logger;

/**
 * used to communicate with the database
 * @version 1.0
 */
public class SQLConnector implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SQLConnector.class.getName());
    private Statement statement; //statement sent to database
    private Connection connection; //the SQL connection

    /**
     *  this method uses jdbc to make a connection to the mysql server.
     * @param database The database to connect to
     * @return the connection to the database, allowing for querying
     */
    static Connection makeConnection(String database){
        database = "marketdb";
        try {
            // database parameters
            String url       = PropertiesLabels.getDbUrl() + database;
            String user      = PropertiesLabels.getDbUser();
            String password  = PropertiesLabels.getDbPassword();

            // return a connection to the database
            return DriverManager.getConnection(url, user, password);
        } catch(SQLException e) {
            LOGGER.info("SQL Exception raised: " + e.getMessage());
            return null;
        }
    }

    /**
     * sends a query to the database
     * @param query the message to send to the database
     * @param database the database to connect to
     * @return the results of the query
     */
    public ResultSet runQuery(String query, String database){
        if(isClosed()) open(database);
        try {
            if(statement != null){
                return statement.executeQuery(query);
            }
        } catch (SQLException e) {
            LOGGER.info("Error running SQL query: " + e.getMessage());
        }
        return null;
    }

    /**
     * updates a record in the database
     * @param query the message sent to the database
     * @param database the database that the message is sent to
     * @return confirmation of update completion
     */
    public boolean runUpdate(String query, String database){
        if(isClosed()) open(database);
        boolean didUpdate = false;
        try {
            if(statement != null){
                statement.executeUpdate(query);
                didUpdate = true;
            }
        } catch (SQLException e) {
            LOGGER.info("Error running SQL query: " + e.getMessage());
            LOGGER.info("Query: " + query);
        }
        return didUpdate;
    }

    /**
     * opens the connection to the database
     * @param database the database that is being connected with
     */
    private void open(String database){
        if(connection != null || statement != null){
            close();
        }
        try {
            connection = makeConnection(database);
            if (connection != null) {
                statement = connection.createStatement();
            }
        } catch (SQLException e) {
            LOGGER.info("Error opening new SQL connection or statement: " + e.getMessage());
        }
    }

    /**
     * closes the database connection
     */
    public void close(){
        try{
            if(connection != null){
                connection.close();
                connection = null;
            }
            if(statement != null){
                statement.close();
                statement = null;
            }
        } catch (SQLException e) {
            LOGGER.info("Error closing an SQL connection or statement: " + e.getMessage());
        }
    }

    /**
     * checks connection is closed
     * @return if connection is closed
     */
    private boolean isClosed(){
        return statement == null && connection == null;
    }

}