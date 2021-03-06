package good;

/**
 * this class purely contains all of the queries used in the good package
 * this is to make the other classes more readable
 * @version 1.0
 */
public class SQLQueries {

    private SQLQueries(){}

    static final String GET_LATEST_ID =
            "SELECT MAX(id) AS id FROM good";

    static final String GET_AVERAGE_MARKET_PRICE =
            "SELECT AVG(price) AS price FROM good";

    static final String GET_HIGHEST_ID = "SELECT MAX(id) AS id FROM good";

    private static final String GOOD_INSERT = "INSERT INTO good ( name,price,prevPrice,amountAvailable,amountUnsold,supply,demand ) VALUES ( @data )";

    private static final String GOOD_UPDATE = "UPDATE good SET price = @price , prevPrice = @prevPrice , amountAvailable = @available , amountUnsold = @unsold , supply = @supply , demand = @demand WHERE id = @id ";

    private static final String GOOD_DELETE = "DELETE FROM good WHERE id = @id ";

    private static final String RESET_INCREMENT = "ALTER TABLE good AUTO_INCREMENT = @id ";

    /**
     * takes an ownedGood and creates a query to add it to the database
     * @param good the ownedGood to be inserted
     * @return the query needing to be used to perform insertion
     */
    static String createInsertQuery(Good good){
        String data = "'" + Good.getName() + "' , " + good.getPrice() + "," + 0 + "," + Good.getOutstandingShares() + "," + Good.getDirectlyAvailable() + "," + 0 + "," + 0;
        return GOOD_INSERT.replace("@data",data);
    }

    /**
     * creates a query for updating existing goods
     * @param good the ownedGood that needs to be updated
     * @return the query needed
     */
    static String createUpdateQuery(Good good){
        String price = String.valueOf(good.getPrice());
        String prevPrice = String.valueOf(Good.getPrevPrice());
        String available = String.valueOf(Good.getOutstandingShares());
        String unsold = String.valueOf(Good.getDirectlyAvailable());
        String id = String.valueOf(good.getId());
        String supply = "0";
        String demand = "0";
        String updatedQuery = GOOD_UPDATE;
        updatedQuery = updatedQuery.replace("@price",price);
        updatedQuery = updatedQuery.replace("@prevPrice",prevPrice);
        updatedQuery = updatedQuery.replace("@available",available);
        updatedQuery = updatedQuery.replace("@unsold",unsold);
        updatedQuery = updatedQuery.replace("@id", id);
        updatedQuery = updatedQuery.replace("@supply",supply);
        updatedQuery = updatedQuery.replace("@demand", demand);
        return updatedQuery;
    }

    /**
     * creates a query to delete an ownedGood object from the database
     * @param good the ownedGood needing to be deleted
     * @return the query needed
     */
    static String createDeleteQuery(Good good){
        String id = String.valueOf(good.getId());
        return GOOD_DELETE.replace("@id",id);
    }

    /**
     * creates the query needed to reset the AUTO_INCREMENT number in the database
     * @param id the highest existing id in the database
     * @return the query needed
     */
    static String createIncrementReset(int id){
        return RESET_INCREMENT.replace("@id",String.valueOf(id+1));
    }

}
