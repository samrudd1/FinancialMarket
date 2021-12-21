package good;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import utils.AppProperties;
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
    private static final int PRICE_VARIANCE_PERCENT = 20;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static Random rand = new Random();
    @Getter private int id;
    @Getter private String name;
    @Getter private float price;
    @Getter @Setter private float prevPrice;
    @Getter private int amountAvailable;
    @Getter @Setter private int amountUnsold;
    @Getter @Setter private int supply;
    @Getter @Setter private int demand;
    @Getter private Map<Integer,Float> priceData = new HashMap<>();

    public Good(){
        id = findId();
        name = "Good" + id;
        price = Float.parseFloat(df.format(definePrice()));
        prevPrice = 0;
        amountAvailable = 5;
        supply = 0;
        demand = 0;
        amountUnsold = amountAvailable;
        Session.getGoods().put(id,this);
        saveGood(true);
    }

    public Good(String name){
        id = findId();
        this.name = name.substring(0, 1).toUpperCase() + name.substring(1); //capitalizing
        price = definePrice();
        prevPrice = 0;
        amountAvailable = 5;
        supply = 0;
        demand = 0;
        amountUnsold = amountAvailable;
        Session.getGoods().put(id,this);
        saveGood(true);
    }

    public Good(int id, String name, float price, float prevPrice, int amountAvailable, int amountUnsold, int supply, int demand){
        this.id = id;
        this.name = name;
        this.price = price;
        this.prevPrice = prevPrice;
        this.amountAvailable = amountAvailable;
        this.amountUnsold = amountUnsold;
        this.supply = supply;
        this.demand = demand;
        Session.getGoods().put(id,this);
    }

    /**
     * This gets the most recent ID and is used for setting the static id.
     * @return the highest ID number present in the agent table
     */
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

    /**
     * This looks at the average market price of other goods and sets a price within a percentage range
     * @return the price of the ownedGood
     */
    private static float definePrice(){
        float price = getAvgMarketPrice();
//        float floor = price * ((100-PRICE_VARIANCE_PERCENT)/100f);
//        float ceiling = price * ((100+PRICE_VARIANCE_PERCENT)/100f);
        float floor = 100;
        float ceiling = 2000;
        return rand.nextInt((int)(ceiling - floor) + 1 ) + floor;
    }

    /**
     * This takes the all of the prices of goods in the market and returns the average price
     * @return the average price for all goods in the market
     */
    private static float getAvgMarketPrice(){
        float price = 100;
        try(SQLConnector connector = new SQLConnector()){
            ResultSet resultSet = connector.runQuery(SQLQueries.GET_AVERAGE_MARKET_PRICE,PropertiesLabels.getMarketDatabase());
            while(resultSet.next()){
                price = resultSet.getFloat("price");
            }
        } catch (SQLException e) {
           LOGGER.info("Error retrieving an average market price. returning default of 100");
        }
        return price;
    }

    public void saveGood(boolean isNew){
        String query;
        if(isNew){
            query = SQLQueries.createInsertQuery(this);
        } else {
            query = SQLQueries.createUpdateQuery(this);
        }
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query,AppProperties.getProperty(PropertiesLabels.getMarketDatabase()));
        } catch (Exception e){
            LOGGER.info("Error saving ownedGood with id " + this.getId() + " : " + e.getMessage());
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
        this.price = newPrice;
        priceData.put(Session.getNumOfRounds(),price);
    }

    @Override
    public int compareTo(Object o) {
        try{
            Good other = (Good)o;
            return Float.compare(this.getPrice(), other.getPrice());
        } catch (Exception e){
            log.warning("Comparison between an OwnedGood and a different object!");
            return 1;
        }
    }
}


