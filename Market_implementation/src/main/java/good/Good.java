package good;

import agent.Agent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import utils.PropertiesLabels;
import utils.SQLConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
    @Getter static  private float startingPrice;
    private boolean goodLock;
    @Getter private static Agent company;
    //@Getter @Setter private float boughtPrice;
    @Getter @Setter static private int outstandingShares;
    @Getter @Setter static private int directlyAvailable;
    @Getter private static Map<Integer,Float> priceData = new HashMap<>();

    /*
    public Good(){
        id = name + findId();
        Session.getGoods().add(this);
        Session.getDirectGoods().add(this);
        saveGood(true);
    }
    */

    public Good(boolean isNew){
        setGoodLock(false);
        id = findId();
        if (Session.getGoods().isEmpty()) {
            Session.getGoods().add(0, this);
        } else {
            Session.getGoods().set(0, this);
        }
        if (isNew) Session.getDirectGoods().add(this);
        company = new Agent(findId(), (getName() + " company"), 0);
        saveGood(true);
    }

    public Good(int outstandingShares){
        setGoodLock(false);
        id = findId();
        Good.outstandingShares = outstandingShares;
        directlyAvailable = Good.outstandingShares;
        if (Session.getGoods().isEmpty()) {
            Session.getGoods().add(0, this);
        } else {
            Session.getGoods().set(0, this);
        }
        company = new Agent(findId(), (getName() + " company"), 0);
        saveGood(true);
    }


    public Good(int id, String name, float price, float prevPrice, int amountAvailable, int amountUnsold, int supply, int demand){
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
        //saveGood(true);
    }

    public boolean getGoodLock() {
        return goodLock;
    }
    public void setGoodLock(boolean val) {
        goodLock = val;
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

    public static void createPrice(){
        float floor = 1;
        float ceiling = 100;
        Good.price = rand.nextInt((int)(ceiling - floor) + 1 ) + floor;
        Good.startingPrice = Good.getPrice();
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

    public static void setPrice(float newPrice){
        prevPrice = price;
        price = (((float)Math.round(newPrice*100))/100);
        priceData.put(Session.getNumOfRounds(),price);
        Good.runUpdate(false);
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


