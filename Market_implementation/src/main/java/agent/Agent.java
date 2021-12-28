package agent;

import good.Good;
import session.Session;
import utils.PropertiesLabels;
import utils.SQLConnector;
import lombok.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This class represents agents, or the individuals buying and selling goods on the ownedGood.
 */
@ToString
public class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
    private static final int MIN_STARTING_FUNDS = 100;
    private static final int MAX_STARTING_FUNDS = 10000;
    private static final String AGENT_DATABASE = PropertiesLabels.getMarketDatabase();

    private static Random rand = new Random();
    @Getter private ArrayList<String> namesOwned = new ArrayList<>();
    @Getter @Setter private int id;
    @Getter @Setter private String name;
    @Getter private float funds;
    @Getter private float targetPrice;
    @Getter private ArrayList<OwnedGood> goodsOwned = new ArrayList<>();
    @Getter private Map<Integer,Float> fundData = new HashMap<Integer,Float>();
    @Getter private int startingRound;

    /**
     * If a name is not given to the constructor
     */
    public Agent(){
        id = findId(); //Make sure nextId is handled okay with concurrency
        name = "Agent" + id;
        funds = assignFunds();
        createTargetPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(true);
    }

    /**
     * This constructor gives the agent a custom name. Useful for testing or presentation purposes
     * @param name The String value to set as the name of the agent.
     */
    Agent(String name){
        id = findId();
        this.name = name.substring(0, 1).toUpperCase() + name.substring(1); //capitalizing
        funds = assignFunds();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(true);
    }

    /**
     * This recreates agent objects read from the MySQL database
     * @param id the agent id
     * @param name the agent name
     * @param funds the agents funds
     */
    public Agent(int id, String name, float funds){
        this.id = id;
        this.name = name;
        this.funds = funds;
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
    }

    private void createTargetPrice() {
        targetPrice = (float) (((Math.random() * 0.1) + 0.95) * Good.getStartingPrice());
        LOGGER.info(name + " target price: " + targetPrice);
    }

    /**
     * This takes the max and min funds values and returns a float between those two numbers
     * @return a float between the minimum and maximum starting funds number
     */
    private float assignFunds(){
        int fundsInt = rand.nextInt((MAX_STARTING_FUNDS - MIN_STARTING_FUNDS) + 1 ) + MIN_STARTING_FUNDS;
        return (float) fundsInt;
    }

    /**
     * This sells the owned goods back to the direct good and gives the user funds. Currently only a debug tool.
     */
    public void closeAccount(){
        if(!(goodsOwned.isEmpty())){
            for(OwnedGood ownedGood : goodsOwned){
                Good good = ownedGood.getGood();
                /*
                Good.setDirectlyAvailable(Good.getDirectlyAvailable() + ownedGood.getNumberOwned());
                float fundsIncrease = ownedGood.getNumberOwned() * good.getPrice();
                setFunds(fundsIncrease + funds);
                Session.getOwnerships().values().remove(ownedGood);
                Session.getOwnershipsToDelete().add(ownedGood);
                 */
            }
            //goodsOwned.clear(); //could add back to direct for sale, or sell all for under market price
            saveUser(false);
        }
    }

    private int findId(){
        int idToReturn = 0;
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_HIGHEST_ID,PropertiesLabels.getMarketDatabase());
            while (resultSet.next()){
                idToReturn = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            LOGGER.info("Error retrieving highest agent ID.");
        }
        return idToReturn + 1;
    }

    public void saveUser(boolean isNew){
        String query;
        if(isNew){
            query = SQLQueries.createInsertQuery(this);
        } else {
            query = SQLQueries.createUpdateQuery(this);
        }
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query,AGENT_DATABASE);
        } catch (Exception e){
            LOGGER.info("Error saving agent with id " + this.getId() + " : " + e.getMessage());
        }
    }

    /**
     * This deletes the referenced user from the MySQL database.
     */
    public void deleteUser(){
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(SQLQueries.createDeleteQuery(this),AGENT_DATABASE);
        } catch (Exception e){
            LOGGER.info("Error deleting agent with id " + this.getId() + " : " + e.getMessage());
        }
    }

    /**
     * This gets the most recent ID and is used for setting the static id.
     * @return the highest ID number present in the agent table
     */
    private static int retrieveLatestId(){
        int latestId = 0;
        try(SQLConnector connector = new SQLConnector()){
             ResultSet resultSet = connector.runQuery(SQLQueries.GET_HIGHEST_ID,AGENT_DATABASE);
            while(resultSet.next()){
                latestId = resultSet.getInt("id");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.info("Error retrieving latest agent id: " + e.getMessage());
        }
        LOGGER.info("highest id is " + latestId);
        return latestId;
    }

    /**
     * This resets the AUTO_INCREMENT number in the database, mainly used so that tests correctly clean up after themselves but will be handy if there are large numbers of deletions for keeps id number sensibly sized.
     */
    public static void resetAgentIncrement(){
        int highestId = 0;
        //first get the highest existing id
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_HIGHEST_ID,AGENT_DATABASE);
            while(resultSet.next()){
                highestId = resultSet.getInt("id");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.info("Error finding highest agent id: " + e.getMessage());
        }
        //now reset the autoincrement to that value + 1
        String query = SQLQueries.createIncrementReset(highestId);
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query,AGENT_DATABASE);
        } catch (Exception e) {
            LOGGER.info("Error resetting auto increment: " +  e.getMessage());
        }
    }

    public void setFunds(float newFunds){
        this.funds = newFunds;
        fundData.put(Session.getNumOfRounds(),funds);
    }
}
