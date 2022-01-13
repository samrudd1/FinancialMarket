package agent;

import good.Good;

/**
 * This class purely contains all of the queries used in the agent package.
 * This is to make the other classes more readable
 */
class SQLQueries {

    private SQLQueries(){}

    private static final String AGENT_INSERT = "INSERT INTO agent ( name,funds ) VALUES ( @data )";

    private static final String AGENT_UPDATE = "UPDATE agent SET funds = @funds WHERE id = @id ";

    private static final String AGENT_DELETE = "DELETE FROM agent WHERE id = @id ";

    static final String GET_HIGHEST_ID = "SELECT MAX(id) AS id FROM agent";

    private static final String RESET_INCREMENT = "ALTER TABLE agent AUTO_INCREMENT = @id ";

    private static final String OWNERSHIP_DELETE = "DELETE FROM ownership WHERE agent_id = @agent_id AND good_id = @good_id ";

    private static final String OWNERSHIP_UPDATE = "UPDATE ownership SET noOwned = @noOwned, boughtAt = @boughtAt WHERE agent_id = @agent_id AND good_id = @good_id";

    private static final String OWNERSHIP_INSERT  = "INSERT into ownership (agent_id,good_id,noOwned,boughtAt) VALUES ( @data )";

    /**
     * Takes an agent and creates a query to add it to the database
     * @param agent the agent to be inserted
     * @return the query needing to be used to perform insertion.
     */
    static String createInsertQuery(Agent agent){
        String data = "'" + agent.getName() + "' , " + agent.getFunds();
        return AGENT_INSERT.replace("@data",data);
    }

    /**
     * Takes an owned ownedGood and creates a query to add it to the database
     * @param ownedGood the owned ownedGood to be inserted
     * @return the query needing to be used to perform insertion.
     */
    static String createInsertQuery(OwnedGood ownedGood){
        String data = ownedGood.getOwner().getId() + "," + ownedGood.getGood().getId() + "," + ownedGood.getNumOwned() + "," + (((float)Math.round(Good.getPrice() * 100)) / 100);
        return OWNERSHIP_INSERT.replace("@data",data);
    }

    /**
     * Creates a query for updating existing agents
     * @param agent the agent that needs to be updated
     * @return the query needed
     */
    static String createUpdateQuery(Agent agent){
        String funds = String.valueOf(agent.getFunds());
        String id = String.valueOf(agent.getId());
        String updatedQuery = AGENT_UPDATE.replace("@funds",funds);
        updatedQuery = updatedQuery.replace("@id", id);
        return updatedQuery;
    }

    /**
     * Creates a query for updating existing agents
     * @param ownedGood the agent that needs to be updated
     * @return the query needed
     */
    static String createUpdateQuery(OwnedGood ownedGood){
        String noOwned = String.valueOf(ownedGood.getNumOwned());
        String boughtAt = String.valueOf(ownedGood.getBoughtAt());
        String agentId = String.valueOf(ownedGood.getOwner().getId());
        String goodId = String.valueOf(ownedGood.getGood().getId());
        String updatedQuery = OWNERSHIP_UPDATE.replace("@noOwned",noOwned);
        updatedQuery = updatedQuery.replace("@boughtAt", boughtAt);
        updatedQuery = updatedQuery.replace("@agent_id", agentId);
        updatedQuery = updatedQuery.replace("@good_id", goodId);
        return updatedQuery;
    }

    /**
     * Creates a query to delete a user from the database
     * @param agent the agent needing to be deleted
     * @return the query needed
     */
    static String createDeleteQuery(Agent agent){
        String id = String.valueOf(agent.getId());
        return AGENT_DELETE.replace("@id",id);
    }

    /**
     * Creates a query to delete an owned ownedGood from the database
     * @param ownedGood the owned ownedGood needing to be deleted
     * @return the query needed
     */
    static String createDeleteQuery(OwnedGood ownedGood){
        String agentId = String.valueOf(ownedGood.getOwner().getId());
        String goodId = String.valueOf(ownedGood.getGood().getId());
        String query = OWNERSHIP_DELETE;
        query = query.replace("@agent_id",agentId);
        query = query.replace("@good_id",goodId);
        return query;
    }

    /**
     * Creates the query needed to reset the AUTO_INCREMENT number in the database
     * @param id the highest existing id in the database
     * @return the query needed
     */
    static String createIncrementReset(int id){
        return RESET_INCREMENT.replace("@id",String.valueOf(id+1));
    }

}
