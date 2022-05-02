package session;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.Getter;
import lombok.Setter;
import utilities.PropertiesLabels;
import utilities.SQLConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * this is used to establish a connection with the database and act as a central data point in the program
 * @version 1.0
 */
public class Session {
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    private static final String SESSION_DATABASE = PropertiesLabels.getMarketDatabase();
    private static boolean isOpen = false; //if the connection to the database is open
    @Getter @Setter private static int numAgents; //number of agents currently on the market
    @Getter private static final Map<Integer, Agent> agents = new HashMap<>(); //map of all agents
    @Getter private static final ArrayList<Good> goods = new ArrayList<>(); //contains the stock object
    @Getter private static final Map<String,OwnedGood> ownerships = new HashMap<>(); //keeps track of stock ownerships
    @Getter @Setter private static int numOfRounds = 0;

    /**
     * private constructor to prevent instantiation.
     */
    private Session(){}

    /**
     * retrieves data from database if it is there
     * @return true if the session has been opened successfully
     */
    public static boolean openSession(){
        if(isOpen){
            LOGGER.info("Session is already open.");
            return false;
        } else {
            boolean agentsPopulated = populateAgents();
            boolean goodsPopulated = populateGoods();
            boolean ownershipsPopulated = populateOwnerShip();
            Agent.resetAgentIncrement();
            Good.resetGoodIncrement();
            return agentsPopulated && goodsPopulated && ownershipsPopulated;
        }

    }

    /**
     * pulls data of stock ownerships from the database
     * @return if the connection was successful
     */
    private static boolean populateOwnerShip(){
        boolean didOpen = true;
        try(SQLConnector connector = new SQLConnector()){
            Agent.resetAgentIncrement();
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_OWNERSHIPS,SESSION_DATABASE);

            while(resultSet.next()){
                int goodId = resultSet.getInt("good_id");
                int agentId = resultSet.getInt("agent_id");
                int noOwned = resultSet.getInt("noOwned");
                float boughtAt = resultSet.getFloat("boughtAt");
                Agent agent = agents.get(agentId);
                Good good = goods.get(goodId);
                if (noOwned > 0) {
                    if (noOwned <= Good.getOutstandingShares()) {
                        Good.setDirectlyAvailable(Good.getOutstandingShares() - noOwned);
                    } else {
                        LOGGER.info("too many goods owned in database for " + good.getId());
                    }
                }
                agent.getGoodsOwned().add(0, new OwnedGood(agent, good, noOwned, noOwned, boughtAt, true));
            }
            resultSet.close();
            isOpen = true;
        } catch (SQLException e) {
            LOGGER.info("Error opening session, populating agents: " + e.getMessage());
            didOpen = false;
        }
        return didOpen;
    }

    /**
     * creates agent objects if there was data in the database
     * @return if the connection was successful
     */
    private static boolean populateAgents(){
        boolean didOpen = true;
        try(SQLConnector connector = new SQLConnector()){
            Agent.resetAgentIncrement();
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_AGENTS,SESSION_DATABASE);
            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                float funds = resultSet.getFloat("funds");
                new Agent(id,name,funds);
            }
            resultSet.close();
            isOpen = true;
        } catch (SQLException e) {
            LOGGER.info("Error opening session, populating agents: " + e.getMessage());
            didOpen = false;
        }
        return didOpen;
    }

    /**
     * creates a stock object if there is data in the database
     * @return if the connection was successful
     */
    private static boolean populateGoods(){
        boolean didOpen = true;
        try(SQLConnector connector = new SQLConnector()){
            Agent.resetAgentIncrement();
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_GOODS,SESSION_DATABASE);
            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("price");
                float prevPrice = resultSet.getFloat("prevPrice");
                int amountAvailable = resultSet.getInt("amountAvailable");
                new Good(id,name,price,prevPrice,amountAvailable);
            }
            resultSet.close();
            isOpen = true;
        } catch (SQLException | InterruptedException e) {
            LOGGER.info("Error opening session, populating goods: " + e.getMessage());
            didOpen = false;
        }
        return didOpen;
    }
}
