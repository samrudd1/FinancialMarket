package session;

import static org.junit.jupiter.api.Assertions.*;

import agent.Agent;
import agent.OwnedGood;
import org.junit.jupiter.api.Test;
import utils.PropertiesLabels;
import utils.SQLConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

class SessionTest {

    /**
     * This tests opening a session and retrieving users correctly.
     * The number of users in the session list is compared to the number of agents in the database.
     */
    @Test
    void sessionAgentsTest(){
        //first run a query to get the number of agents
        int agentNo = 0;
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery("SELECT COUNT(id) AS count FROM agent WHERE id != 0 ", PropertiesLabels.getMarketDatabase());
            while(resultSet.next()){
                agentNo = resultSet.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Session.getAgents().clear();
        boolean didOpen = Session.openSession();
        int size = Session.getAgents().size();
        assertEquals(agentNo,size);
        assertTrue(didOpen);
        Session.closeSession();
    }

    /**
     * Opens a new session, creates a user, then add that user to the delete list and close the session.
     * This should result in the user never being added to the database.
     */
    @Test
    void sessionCloseTest(){
        Session.openSession();
        Agent agent = new Agent();
        Session.getAgentsToDelete().add(agent);
        Map<String, OwnedGood> test = Session.getOwnerships();
        boolean didClose = Session.closeSession();
        assertTrue(didClose);
    }

}
