package utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



class SQLConnectorTest {
    @Test
    void makeConnectionTest(){
        boolean complete = false;
        try{
            SQLConnector.makeConnection(AppProperties.getProperty("marketDatabase"));
            complete = true;
        } catch (Exception e){
            e.printStackTrace();
        }
        assertTrue(complete);
    }


    @Test
    void lookForFakeTable(){
        boolean didfail = false;
        try(Connection conn = SQLConnector.makeConnection(AppProperties.getProperty("marketDatabase"))){
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM fakeTable WHERE testCol = '1' ");
        } catch (Exception e){
            didfail = true;
        }
        assertTrue(didfail);
    }

    @Test
    void runQueryTest(){
        int minId = 999;
        String query = "SELECT MIN(id) AS id FROM agent";
        try(SQLConnector connector = new SQLConnector()){
            ResultSet results = connector.runQuery(query, "market");
            while(results.next()){
                minId = results.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertEquals(0,minId);
    }
}
