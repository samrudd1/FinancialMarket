package agent;

import good.Good;
import good.Offer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import session.Session;
import trade.Exchange;
import trade.TradingCycle;
import utils.PropertiesLabels;
import utils.SQLConnector;

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
    private static final int MIN_STARTING_FUNDS = 1000;
    private static final int MAX_STARTING_FUNDS = 1000000;
    private static final String AGENT_DATABASE = PropertiesLabels.getMarketDatabase();

    private static Random rand = new Random();
    @Getter private ArrayList<String> namesOwned = new ArrayList<>();
    @Getter @Setter private int id;
    @Getter @Setter private String name;
    private float funds;
    @Getter @Setter private float targetPrice;
    private boolean agentLock;
    private ArrayList<OwnedGood> goodsOwned = new ArrayList<>();
    @Getter private Map<Integer,Float> fundData = new HashMap<Integer,Float>();
    @Getter private int startingRound;
    @Getter private boolean placedBid;
    @Getter private boolean placedAsk;
    @Getter private ArrayList<Offer> bidsPlaced = new ArrayList<>();
    @Getter private ArrayList<Offer> asksPlaced = new ArrayList<>();
    @Getter @Setter private static int sentiment;
    //@Getter @Setter private Offer bidMade;
    //@Getter @Setter private Offer AskMade;


    public Agent(){
        setAgentLock(false);
        id = findId(); //Make sure nextId is handled okay with concurrency
        name = "Agent" + id;
        funds = assignFunds();
        createTargetPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(true);
    }

    public Agent(String name, boolean company){
        setAgentLock(false);
        id = (findId()); //Make sure nextId is handled okay with concurrency
        this.name = name;
        funds = 0;
        targetPrice = Good.getStartingPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(true);
    }

    /*
    Agent(String name){
        setAgentLock(false);
        id = findId();
        this.name = name.substring(0, 1).toUpperCase() + name.substring(1); //capitalizing
        funds = assignFunds();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(true);
    }
    */
    public Agent(int id, String name, float funds){
        setAgentLock(false);
        setPlacedBid(false);
        setPlacedAsk(false);
        this.id = id; //Make sure nextId is handled okay with concurrency
        this.name = name;
        this.funds = funds;
        createTargetPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        saveUser(false);
    }

    public void createTargetPrice() {
        Random rand = new Random();
        int chance = rand.nextInt(10);
        targetPrice = (float)(((float)Math.round((chance + 96) * Good.getPrice())) * 0.01);
        placedAsk = false;
        placedBid = false;
        //LOGGER.info(name + " target price: " + targetPrice);
    }
    public void changeTargetPrice() {
        Random rand = new Random();
        int chance = rand.nextInt(sentiment);
        targetPrice = (float)(((float)Math.round((chance + 91) * targetPrice)) * 0.01);
        placedAsk = false;
        placedBid = false;
    }
    public void changeTargetPrice(float price) {
        targetPrice = price;
        Random rand = new Random();
        int chance = rand.nextInt(sentiment);
        targetPrice = (float)(((float)Math.round((chance + 91) * targetPrice)) * 0.01);
        placedAsk = false;
        placedBid = false;
    }

    /**
     * This takes the max and min funds values and returns a float between those two numbers
     * @return a float between the minimum and maximum starting funds number
     */
    private float assignFunds(){
        int fundsInt = rand.nextInt((MAX_STARTING_FUNDS - MIN_STARTING_FUNDS) + 1 ) + MIN_STARTING_FUNDS;
        return (float) fundsInt;
    }

    public synchronized void CheckInitial(int wantToBuy, TradingCycle tc) {
        Offer offer = Good.getAsk().get(0);
        if (getFunds() < (wantToBuy * offer.getPrice())) {
            wantToBuy = (int) Math.floor(getFunds() / offer.getPrice());
        }
        synchronized (tc) {
            if (wantToBuy > 0) {
                InitiateBuy ib1 = new InitiateBuy(this, offer, wantToBuy, tc);
                Thread t1 = new Thread(ib1);
                t1.start();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    LOGGER.info("failed sleep");
                }
                //saveUser(false);
            }
        }
    }

    public boolean getAgentLock() {
        return agentLock;
    }
    public void setAgentLock(boolean val) {
        agentLock = val;
    }
    public ArrayList<OwnedGood> getGoodsOwned() { return goodsOwned; }
    public boolean getPlacedAsk() { return placedAsk; }
    public boolean getPlacedBid() { return placedBid; }
    public void setPlacedAsk(boolean val) { this.placedAsk = val; }
    public void setPlacedBid(boolean val) { this.placedBid = val; }

    /*
    public synchronized void startTrade(Agent agent, TradingCycle tc) {
        //synchronized (tc) {
            ChooseTrade ct = new ChooseTrade(agent, tc);
            Thread ts = new Thread(ct);
            ts.start();
        //}
    }
    */

    public void removedBid(Offer offer) {
        funds = (funds + (offer.getNumOffered() * offer.getPrice()));
        placedBid = false;
        changeTargetPrice();
    }
    public void removeAsk(Offer offer) {
        for (OwnedGood good: goodsOwned) {
            if (offer.getGood().getId() == good.getGood().getId()) {
                good.setNumAvailable(good.getNumAvailable() + offer.getNumOffered());
                placedAsk = false;
            }
        }
        changeTargetPrice();
    }

    /*
    private class InitiateSell implements Runnable {
        @Getter private Agent seller;
        @Getter private Offer offer;
        @Getter private int amountBought;

        public InitiateSell(Agent seller, Offer offer, int amountBought) {
            this.seller = seller;
            this.offer = offer;
            this.amountBought = amountBought;
        }

        @Override
        public void run() {
            try {
                Exchange.getInstance().execute(offer.getOfferMaker(), seller, offer, amountBought);
                System.out.println("trade executed between " + offer.getOfferMaker().getName() + " and "
                        + seller.getName() + " for " + amountBought + " share/s at a price of "
                        + ((float)Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                        + offer.getNumOffered() + " left on the bid.");
            } catch (InterruptedException e) {
                LOGGER.info("trade failed");
            }
        }
    }
    */


    private class InitiateBuy implements Runnable {
        @Getter private Agent buyer;
        @Getter private Offer offer;
        @Getter private int amountBought;
        private TradingCycle tc;

        public InitiateBuy(Agent buyer, Offer offer, int amountBought, TradingCycle tc) {
            this.buyer = buyer;
            this.offer = offer;
            this.amountBought = amountBought;
            this.tc = tc;
        }

        @Override
        public synchronized void run() {
            synchronized (tc) {
                try {
                    Exchange.getInstance().execute(buyer, offer.getOfferMaker(), offer, amountBought, tc);
                } catch (InterruptedException e) {
                    LOGGER.info("trade failed");
                }
            }

            //LOGGER.info("executing trade with " + buyer.getName() + " directly for " + amountBought + " share/s at a price of " + Good.getPrice() + " each.");
        }
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

    public float getFunds() {
        return (((float)Math.round(this.funds * 100)) / 100);
    }
}
