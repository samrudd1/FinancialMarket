package utilities;

import java.sql.*;
import java.util.logging.Logger;

public class SQLConnector implements AutoCloseable{
    private static final Logger LOGGER = Logger.getLogger(SQLConnector.class.getName());
    private Statement statement;
    private Connection connection;

    /**
     *  This method uses jdbc to make a connection to the mysql server.
     * @param database The database we need to connect to
     * @return the connection to the database, allowing for querying
     */
    static Connection makeConnection(String database){
        database = "marketdb";
        try {
            // db parameters
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

    public ResultSet runQuery(String query, String database){
        //database = "marketdb";
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

    public boolean runUpdate(String query, String database){
        //database = "marketdb";
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

    private void open(String database){
        //database = "marketdb";
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

    private boolean isClosed(){
        return statement == null && connection == null;
    }

}