package session;

import agent.Agent;
import agent.OwnedGood;
import altair.PythonCallAgent;
import altair.PythonCallGood;
import lombok.Getter;
import good.Good;
import lombok.Setter;
import utils.PropertiesLabels;
import utils.SQLConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The session class is responsible for keeping track of what is going on in each round of execution and will handle database communication.
 */
public class Session {
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    private static final String SESSION_DATABASE = PropertiesLabels.getMarketDatabase();
    private static boolean isOpen = false;
    @Getter @Setter private static int numAgents;
    @Getter private static Map<Integer, Agent> agents = new HashMap<>();
    @Getter private static ArrayList<Agent> agentsToDelete = new ArrayList<>();
    @Getter private static ArrayList<Agent> agentsToUpdate = new ArrayList<>();
    @Getter private static ArrayList<Good> goods = new ArrayList<>();
    @Getter private static ArrayList<Good> directGoods = new ArrayList<>();
    @Getter private static ArrayList<Good> goodsToDelete = new ArrayList<>();
    @Getter private static ArrayList<Good> goodsToUpdate = new ArrayList<>();
    @Getter private static Map<String,OwnedGood> ownerships = new HashMap<>();
    @Getter private static ArrayList<OwnedGood> ownershipsToDelete = new ArrayList<>();
    @Getter private static ArrayList<OwnedGood> ownershipsToUpdate = new ArrayList<>();
    @Getter @Setter private static int numOfRounds = 0;

    /**
     * Private constructor as we don't want instantiation.
     */
    private Session(){}

    /**
     * Retrieve the data needed from MySQL.
     * @return true if the session has been opened successfully.
     */
    public static boolean openSession(){
        if(isOpen){
            LOGGER.info("Session is already open.");
            return false;
        } else {
            //retrieve agents
            boolean agentsPopulated = populateAgents();
            boolean goodsPopulated = populateGoods();
            //ownership MUST be populated after agents and goods
            boolean ownershipsPopulated = populateOwnerShip();
            Agent.resetAgentIncrement();
            Good.resetGoodIncrement();
            return agentsPopulated && goodsPopulated && ownershipsPopulated;
        }

    }

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
                agent.getGoodsOwned().add(0, new OwnedGood(agent, good, noOwned, boughtAt, true));
            }
            resultSet.close();
            isOpen = true;
        } catch (SQLException e) {
            LOGGER.info("Error opening session, populating agents: " + e.getMessage());
            didOpen = false;
        }
        return didOpen;
    }

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
                int amountUnsold = resultSet.getInt("amountUnsold");
                int supply = resultSet.getInt("supply");
                int demand = resultSet.getInt("demand");
                new Good(id,name,price,prevPrice,amountAvailable,amountUnsold,supply,demand);
            }
            resultSet.close();
            isOpen = true;
        } catch (SQLException e) {
            LOGGER.info("Error opening session, populating goods: " + e.getMessage());
            didOpen = false;
        }
        return didOpen;
    }

    /**
     * save the data needed in MySQL.
     * @return true if the session has been closed successfully.
     */
    public static boolean closeSession(){
        numOfRounds++;
        if(!isOpen){
            LOGGER.info("No Session is currently open.");
            return false;
        } else {
            boolean didClose = false;
            try{
                makeCharts();
                //deleteOwnership();
                //deleteAgents();
                //deleteGoods();
                //Agent.resetAgentIncrement();
                //Good.resetGoodIncrement();
                //clearLists();
                didClose = true;
                isOpen = false;
            } catch (Exception e){
                LOGGER.info("Error writing agent changes to MySQL: " + e.getMessage());
            }
            return didClose;
        }
    }

    private static void makeCharts(){
        for(Agent agent : agents.values()){
            new PythonCallAgent(agent).execute();
        }
        for(Good good : goods){
            new PythonCallGood(good).execute();
        }
    }

    /**
     * When closing a session all lists are cleared.
     */
    private static void clearLists(){
        /*
        Agent.resetAgentIncrement();
        Good.resetGoodIncrement();
        agents.clear();
        agentsToDelete.clear();
        agentsToUpdate.clear();
        goods.clear();
        goodsToDelete.clear();
        goodsToUpdate.clear();
        ownerships.clear();
        ownershipsToDelete.clear();
        ownershipsToUpdate.clear();
         */
    }


    /**
     * Deletes existing agents no longer needed.
     */
    private static void deleteAgents(){
        for(Agent agent : agentsToDelete){
            agent.deleteUser();
        }
    }

    /**
     * Deletes existing goods no longer needed.
     */
    private static void deleteGoods(){
        for(Good good : goodsToDelete){
            good.deleteGood();
        }
    }


    /**
     * Deletes existing ownerships no longer needed.
     */
    private static void deleteOwnership(){
        for(OwnedGood owned : ownershipsToDelete){
            owned.delete();
        }
    }


}
