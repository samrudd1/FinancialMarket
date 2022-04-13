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
    private int MIN_STARTING_FUNDS = 1000;
    private final int MAX_STARTING_FUNDS = 1000000;
    private static final String AGENT_DATABASE = PropertiesLabels.getMarketDatabase();

    @Getter private ArrayList<String> namesOwned = new ArrayList<>();
    @Getter @Setter private int id;
    @Getter @Setter private String name;
    private float funds;
    @Getter private final float startingFunds;
    @Getter @Setter private float targetPrice;
    private boolean agentLock;
    private ArrayList<OwnedGood> goodsOwned = new ArrayList<>();
    @Getter private Map<Integer,Float> fundData = new HashMap<Integer,Float>();
    @Getter private final int startingRound;
    @Getter private ArrayList<Offer> bidsPlaced = new ArrayList<>();
    @Getter private ArrayList<Offer> asksPlaced = new ArrayList<>();
    @Setter @Getter private boolean placedBid;
    @Setter @Getter private boolean placedAsk;
    @Getter @Setter private static int sentiment;
    @Getter @Setter int chance;
    @Getter @Setter private int prevSentiment = 30;
    @Getter @Setter private float prevRoundPrice = Good.getPrice();
    @Setter private boolean prevPriceUp;
    @Getter @Setter public static int ID;


    public Agent(){
        setPlacedAsk(false);
        setPlacedBid(false);
        setAgentLock(false);
        Random rand = new Random();
        chance = rand.nextInt(7);
        id = ID;
        ID += 1;//Make sure nextId is handled okay with concurrency
        if (chance == 1) {
            name = "Momentum " + id;
        } else if (chance == 2) {
            name = "SentTrend " + id;
        } else if (chance == 3) {
            name = "High Frequency " + id;
            //MIN_STARTING_FUNDS *= 10;
        } else if (chance == 4) {
            name = "RSI " + id;
        } else if (chance == 5) {
            name = "RSI10 " + id;
        } else {
            name = "Default " + id;
        }
        funds = assignFunds();
        startingFunds = funds;
        createTargetPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        //saveUser(true);
        setPrevPriceUp(false);
    }

    public Agent(String name, boolean company){
        setPlacedAsk(false);
        setPlacedBid(false);
        setAgentLock(false);
        id = (findId()); //Make sure nextId is handled okay with concurrency
        this.name = name;
        funds = 0;
        startingFunds = funds;
        targetPrice = Good.getStartingPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        //saveUser(true);
        setPrevPriceUp(false);
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
        setPlacedAsk(false);
        setPlacedBid(false);
        setAgentLock(false);
        Random rand = new Random();
        chance = rand.nextInt(7);
        this.id = id;
        this.name = name;
        this.funds = funds;
        startingFunds = funds;
        createTargetPrice();
        fundData.put(Session.getNumOfRounds(),funds);
        this.startingRound = Session.getNumOfRounds();
        Session.getAgents().put(id,this);
        //saveUser(false);
        setPrevPriceUp(false);
    }

    public void createTargetPrice() {
        Random rand = new Random();
        int chance = rand.nextInt(10);
        targetPrice = (float) (((float) Math.round((chance + 98) * Good.getPrice())) * 0.01);
        if (chance == 1) {
            targetPrice = (float) (((float) Math.round((106) * Good.getPrice())) * 0.01);
        } else if (chance == 4) {
            targetPrice = (float) (((float) Math.round((106) * Good.getPrice())) * 0.01);
        }
        setPlacedBid(false);
        setPlacedAsk(false);
        //LOGGER.info(name + " target price: " + targetPrice);
    }
    public void changeTargetPrice() {
        if (chance != 1) {
            Random rand = new Random();
            int chance = rand.nextInt(sentiment);
            targetPrice = (float) (((float) Math.round((chance + 94) * targetPrice)) * 0.01);
        }
        setPlacedBid(false);
        setPlacedAsk(false);
    }

    /**
     * This takes the max and min funds values and returns a float between those two numbers
     * @return a float between the minimum and maximum starting funds number
     */
    private float assignFunds(){
        Random rand = new Random();
        int fundsInt = rand.nextInt((MAX_STARTING_FUNDS - MIN_STARTING_FUNDS) + 1 ) + MIN_STARTING_FUNDS;
        return (float) fundsInt;
    }

    public synchronized void CheckInitial(int wantToBuy, TradingCycle tc) {
        Offer offer = Good.getAsk().get(0);
        if (getFunds() < (wantToBuy * offer.getPrice())) {
            wantToBuy = (int) Math.floor(getFunds() / offer.getPrice());
        }
        //synchronized (tc) {
            if (wantToBuy > 0) {
                InitiateBuy ib1 = new InitiateBuy(this, offer, wantToBuy, tc);
                Runnable t1 = new Thread(ib1);
                t1.run();
                /*
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    LOGGER.info("failed sleep");
                }
                */
                //saveUser(false);
            }
        //}
    }

    public boolean getAgentLock() {
        return agentLock;
    }
    public void setAgentLock(boolean val) {
        agentLock = val;
    }
    public ArrayList<OwnedGood> getGoodsOwned() { return goodsOwned; }

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
        //changeTargetPrice();
        setPlacedBid(false);
    }
    public void removedAsk(Offer offer) {
        for (OwnedGood good: goodsOwned) {
            if (offer.getGood().getId() == good.getGood().getId()) {
                good.setNumAvailable(good.getNumAvailable() + offer.getNumOffered());
            }
        }
        //changeTargetPrice();
        setPlacedAsk(false);
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
        @Getter private final Agent buyer;
        @Getter private final Offer offer;
        @Getter private final int amountBought;
        private final TradingCycle tc;

        public InitiateBuy(Agent buyer, Offer offer, int amountBought, TradingCycle tc) {
            this.buyer = buyer;
            this.offer = offer;
            this.amountBought = amountBought;
            this.tc = tc;
        }

        @Override
        public synchronized void run() {
            //synchronized (tc) {
                try {
                    boolean success = Exchange.getInstance().execute(buyer, offer.getOfferMaker(), offer, amountBought, tc, 0);
                } catch (InterruptedException e) {
                    System.out.println("Initiate buy failed");
                }
            //}

            //LOGGER.info("executing trade with " + buyer.getName() + " directly for " + amountBought + " share/s at a price of " + Good.getPrice() + " each.");
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

    public boolean getPrevPriceUp() { return prevPriceUp; }
}
