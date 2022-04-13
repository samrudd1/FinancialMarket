package good;

import agent.Agent;
import agent.OwnedGood;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import trade.Exchange;
import trade.TradeData;
import utils.PropertiesLabels;
import utils.SQLConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

@Log
@EqualsAndHashCode
public class Good implements Comparable{
    //TODO Lombok logger is used here, remove the below logger
    private static final Logger LOGGER = Logger.getLogger(Good.class.getName());
    //private static final int PRICE_VARIANCE_PERCENT = 20;
    //private static DecimalFormat df = new DecimalFormat("0.00");
    private static final Random rand = new Random();
    @Getter private final int id;
    @Getter static private String name = "Stock";
    @Getter static private float prevPrice;
    @Getter static private float price;
    @Getter static private float startingPrice;
    @Getter static private float vwap = 0;
    @Getter static private double volume = 0;
    @Getter static private float lowest = 110;
    @Getter static private float highest = 1;
    @Getter @Setter static private Integer numTrades = 0;
    static private ArrayList<Offer> bid = new ArrayList<>();
    static private ArrayList<Offer> ask = new ArrayList<>();
    static private boolean goodLock;
    static private boolean bidLock;
    static private boolean askLock;
    @Getter private static Agent company;
    //@Getter @Setter private float boughtPrice;
    @Getter @Setter static private int outstandingShares;
    @Getter @Setter static private int directlyAvailable;
    @Getter private static Map<Integer,Float> priceData = new HashMap<>();
    @Getter private static ArrayList<Float> priceList = new ArrayList<>();
    @Getter private static ArrayList<Float> avgPriceList = new ArrayList<>();
    @Getter private static ArrayList<TradeData> tradeData = new ArrayList<>();

    /*
    public Good(){
        id = name + findId();
        Session.getGoods().add(this);
        Session.getDirectGoods().add(this);
        Exchange.lastPrice = price;
        saveGood(true);
    }
    */

    public Good(boolean isNew) throws InterruptedException {
        setGoodLock(false);
        id = findId();
        if (Session.getGoods().isEmpty()) {
            Session.getGoods().add(0, this);
        } else {
            Session.getGoods().set(0, this);
        }
        if (isNew) Session.getDirectGoods().add(this);
        company = new Agent((getName() + " company"), true);
        createPrice();
        Exchange.getInstance().addGood(this);
        getCompany().getGoodsOwned().add(0, new OwnedGood(getCompany(), this, directlyAvailable, 0, 0, true));
        directlyAvailable = 0;
        ask.add(new Offer(price, getCompany(), this, getCompany().getGoodsOwned().get(0).getNumOwned()));
        Exchange.lastPrice = price;
        saveGood(true);
    }

    public Good(int outstandingShares) throws InterruptedException {
        setGoodLock(false);
        id = findId();
        Good.outstandingShares = outstandingShares;
        directlyAvailable = Good.outstandingShares;
        if (Session.getGoods().isEmpty()) {
            Session.getGoods().add(0, this);
        } else {
            Session.getGoods().set(0, this);
        }
        company = new Agent((getName() + " company"), true);
        createPrice();
        Exchange.getInstance().addGood(this);
        getCompany().getGoodsOwned().add(0, new OwnedGood(getCompany(), this, directlyAvailable, 0, 0, true));
        directlyAvailable = 0;
        ask.add(new Offer(price, getCompany(), this, getCompany().getGoodsOwned().get(0).getNumOwned()));
        Exchange.lastPrice = price;
        saveGood(true);
    }


    public Good(int id, String name, float price, float prevPrice, int amountAvailable, int amountUnsold, int supply, int demand) throws InterruptedException {
        setGoodLock(false);
        this.id = id;
        Good.name = name;
        //Good.price = price;
        //Good.outstandingShares = amountAvailable;
        //Good.directlyAvailable = amountUnsold;
        if (Session.getGoods().isEmpty()) {
            Session.getGoods().add(0, this);
        } else {
            Session.getGoods().set(0, this);
        }
        company = new Agent((getName() + " company"), true);
        createPrice();
        Exchange.getInstance().addGood(this);
        getCompany().getGoodsOwned().add(0, new OwnedGood(getCompany(), this, directlyAvailable, 0, 0, true));
        directlyAvailable = 0;
        ask.add(new Offer(price, getCompany(), this, getCompany().getGoodsOwned().get(0).getNumOwned()));
        Exchange.lastPrice = price;
        //saveGood(true);
    }

    public boolean getGoodLock() {
        return goodLock;
    }
    public void setGoodLock(boolean val) {
        goodLock = val;
    }
    public static ArrayList<Offer> getBid() { return Good.bid; }
    public static ArrayList<Offer> getAsk() { return Good.ask; }

    public synchronized float getHighestBid() throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        Collections.sort(bid);
        if (bid.size() > 0) {
            bidLock = false;
            notify();
            return bid.get(bid.size() - 1).getPrice();
        }
        bidLock = false;
        notify();
        return 0;
    }
    public synchronized float getLowestAsk() throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        Collections.sort(ask);
        if (ask.size() > 0) {
            askLock = false;
            notify();
            return ask.get(0).getPrice();
        }
        askLock = false;
        notify();
        return 99999;
    }
    public synchronized float getSecondHighestBid() throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        Collections.sort(bid);
        if (bid.size() > 1) {
            bidLock = false;
            notify();
            return bid.get(bid.size() - 2).getPrice();
        }
        bidLock = false;
        notify();
        return 0;
    }
    public synchronized float getSecondLowestAsk() throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        Collections.sort(ask);
        if (ask.size() > 1) {
            askLock = false;
            notify();
            return ask.get(1).getPrice();
        }
        askLock = false;
        notify();
        return 99999;
    }
    public synchronized Offer getHighestBidOffer() throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        bid.trimToSize();
        Collections.sort(bid);
        if (bid.size() > 0) {
            bidLock = false;
            notify();
            return bid.get(bid.size() - 1);
        }
        bidLock = false;
        notify();
        return null;
    }
    public synchronized Offer getLowestAskOffer() throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        ask.trimToSize();
        Collections.sort(ask);
        if (ask.size() > 0) {
            askLock = false;
            notify();
            return ask.get(0);
        }
        askLock = false;
        notify();
        return null;
    }

    public synchronized String bidString() throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        Collections.sort(bid);
        StringBuilder str = new StringBuilder();
        if (bid.size() > 20) {
            for (int i = (bid.size() - 1); i >= (bid.size() - 21); i--) {
                Offer offer = bid.get(i);
                str.append("[q: ").append(offer.getNumOffered()).append(" p: ").append(offer.getPrice()).append("] ");
            }
        } else {
            for (int i = (bid.size() - 1); i >= 0; i--) {
                Offer offer = bid.get(i);
                str.append("[q: ").append(offer.getNumOffered()).append(" p: ").append(offer.getPrice()).append("] ");
            }
        }
        bidLock = false;
        notify();
        return str.toString();
    }
    public synchronized String askString() throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        Collections.sort(ask);
        StringBuilder str = new StringBuilder();
        if (ask.size() > 20) {
            for (int i = 0; i < 20; i++) {
                Offer offer = ask.get(i);
                str.append("[q: ").append(offer.getNumOffered()).append(" p: ").append(offer.getPrice()).append("] ");
            }
        } else {
            for (Offer offer : ask) {
                str.append("[q: ").append(offer.getNumOffered()).append(" p: ").append(offer.getPrice()).append("] ");
            }
        }
        askLock = false;
        notify();
        return str.toString();
    }

    public synchronized void addBid(Offer offer) throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        if (offer.getPrice() > 0) {
            bid.add(offer);
            //log.info("new bid of " + offer.getNumOffered() + " shares at " + offer.getPrice());
        }
        bid.trimToSize();
        Collections.sort(bid);
        bidLock = false;
        notify();
        /*
        while (bidLock) wait();
        bidLock = true;
        if (ask.size() > 0) {
            if ((offer.getPrice() > 0) && (offer.getPrice() < ask.get(0).getPrice())) {
                bid.add(offer);
                offer.getOfferMaker().setPlacedBid(true);
                //log.info("new bid of " + offer.getNumOffered() + " shares at " + offer.getPrice());
            }
        } else {
            float highBid;
            if (bid.size() > 0) {
                bid.trimToSize();
                Collections.sort(bid);
                highBid = bid.get(bid.size()-1).getPrice();
            } else {
                highBid = 999;
            }
            if (offer.getPrice() < highBid) {
                bid.add(offer);
                offer.getOfferMaker().setPlacedBid(true);
                //log.info("new bid of " + offer.getNumOffered() + " shares at " + offer.getPrice());
            }
        }
        bid.trimToSize();
        Collections.sort(bid);
        bidLock = false;
        notify();

         */
    }
    public synchronized void addAsk(Offer offer) throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        if (offer.getPrice() > 0) {
            ask.add(offer);
            //log.info("new ask of " + offer.getNumOffered() + " shares at " + offer.getPrice());
        }
        ask.trimToSize();
        Collections.sort(ask);
        askLock = false;
        notify();
        /*
        while (askLock) wait();
        askLock = true;
        if (bid.size() > 0) {
            if (offer.getPrice() > bid.get(bid.size()-1).getPrice()) {
                ask.add(offer);
                offer.getOfferMaker().setPlacedAsk(true);
                //log.info("new ask of " + offer.getNumOffered() + " shares at " + offer.getPrice());
            }
        } else {
            float lowAsk;
            if (ask.size() > 0) {
                lowAsk = ask.get(0).getPrice();
            } else {
                lowAsk = 0;
            }
            if (offer.getPrice() > lowAsk) {
                ask.add(offer);
                offer.getOfferMaker().setPlacedAsk(true);
                //log.info("new ask of " + offer.getNumOffered() + " shares at " + offer.getPrice());
            }
        }
        ask.trimToSize();
        Collections.sort(ask);
        askLock = false;
        notify();
        */
    }
    public synchronized void removeBid(Offer offer) throws InterruptedException {
        while (bidLock) wait();
        bidLock = true;
        if (bid.contains(offer)) {
            bid.remove(offer);
            offer.getOfferMaker().removedBid(offer);
        }
        bid.trimToSize();
        Collections.sort(bid);
        bidLock = false;
        notify();
    }
    public synchronized void removeAsk(Offer offer) throws InterruptedException {
        while (askLock) wait();
        askLock = true;
        if (ask.contains(offer)) {
            ask.remove(offer);
            offer.getOfferMaker().removedAsk(offer);
        }
        ask.trimToSize();
        Collections.sort(ask);
        askLock = false;
        notify();
    }

    public static void addTradeData(float price, int amount, int round) {
        tradeData.add(new TradeData(price, amount, round));
    }

    /**
     * This gets the most recent ID and is used for setting the static id.
     * @return the highest ID number present in the agent table
     */
    /*
    private static int retrieveLatestId(){
        int latestId = 0;
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(good.SQLQueries.GET_LATEST_ID, PropertiesLabels.getMarketDatabase());
            while(resultSet.next()){
                latestId = resultSet.getInt("id");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.info("Error retrieving latest agent id: " + e.getMessage());
        }
        return latestId + 1;
    }
     */

    private void createPrice(){
        float floor = 10;
        float ceiling = 50;
        price = rand.nextInt((int)(ceiling - floor) + 1 ) + floor;
        Good.startingPrice = price;
        Good.prevPrice = price;
    }

    public void saveGood(boolean isNew){
        String query;
        if(isNew){
            query = SQLQueries.createInsertQuery(this);
        } else {
            query = SQLQueries.createUpdateQuery(this);
        }
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query,PropertiesLabels.getMarketDatabase());
        } catch (Exception e){
            LOGGER.info("Error saving Good with id " + this.getId() + " : " + e.getMessage());
        }
    }

    public static void runUpdate(boolean isNew) {
        String query = null;
        Good good = null;
        if (!(Session.getGoods().isEmpty())) {
            good = Session.getGoods().get(0);
        }
        if (!(good == null)) {
            if (isNew) {
                query = SQLQueries.createInsertQuery(good);
            } else {
                query = SQLQueries.createUpdateQuery(good);
            }
            try (SQLConnector connector = new SQLConnector()) {
                connector.runUpdate(query, PropertiesLabels.getMarketDatabase());
            } catch (Exception e) {
                LOGGER.info("Error updated shares db: " + e.getMessage());
            }
        }
    }

    private int findId(){
        int idToReturn = 0;
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_HIGHEST_ID,PropertiesLabels.getMarketDatabase());
            while(resultSet.next()){
                idToReturn = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            LOGGER.info("Error retrieving highest ownedGood ID.");
        }
        return idToReturn + 1;
    }

    /**
     * This deletes the referenced user from the MySQL database.
     */
    public void deleteGood(){
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(SQLQueries.createDeleteQuery(this),PropertiesLabels.getMarketDatabase());
        } catch (Exception e){
            LOGGER.info("Error deleting agent with id " + this.getId() + " : " + e.getMessage());
        }
    }

    /**
     * This resets the AUTO_INCREMENT number in the database, mainly used so that tests correctly clean up after themselves but will be handy if there are large numbers of deletions for keeps id number sensibly sized.
     */
    public static void resetGoodIncrement(){
        int highestId = 0;
        //first get the highest existing id
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_HIGHEST_ID,PropertiesLabels.getMarketDatabase());
            while(resultSet.next()){
                highestId = resultSet.getInt("id");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.info("Error finding highest ownedGood id: " + e.getMessage());
        }
        //now reset the autoincrement to that value + 1
        String query = SQLQueries.createIncrementReset(highestId);
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query,PropertiesLabels.getMarketDatabase());
        } catch (Exception e) {
            LOGGER.info("Error resetting auto increment: " +  e.getMessage());
        }
    }

    public void setPrice(float newPrice){
        Good.prevPrice = price;
        price = (((float)Math.round(newPrice*100))/100);
        priceData.put(numTrades, price);
        if (newPrice > highest) { highest = newPrice; }
        if (newPrice < lowest) { lowest = newPrice; }
        //Good.runUpdate(false);
    }

    public void setPrice(Offer offer, int traded){
        float newPrice = offer.getPrice();
        prevPrice = price;
        numTrades += 1;
        price = (float)(((float)Math.floor(newPrice*100))*0.01);
        priceData.put(numTrades + 1, price);
        priceList.add(price);
        if (newPrice > highest) { highest = newPrice; }
        if (newPrice < lowest) { lowest = newPrice; }
        vwap = (float) (((vwap * volume) + (offer.getPrice() * traded)) / (volume + traded));
        volume += traded;
        Exchange.lastPrice = price;
        //Good.runUpdate(false);
    }

    @Override
    public int compareTo(Object o) {
        try{
            //Good other = (Good)o;
            //return Float.compare(getPrice(), other.getPrice());
        } catch (Exception e){
            log.warning("Comparison between an OwnedGood and a different object!");
            return 1;
        }
        return 0;
    }
}


