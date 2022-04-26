package agent;

import good.Good;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import utilities.PropertiesLabels;
import utilities.SQLConnector;

import java.util.logging.Logger;

@EqualsAndHashCode
@Log
//currently not saved to database, add at end of sim or run on other thread
public class OwnedGood implements Comparable{
    private static final Logger LOGGER = Logger.getLogger(OwnedGood.class.getName());
    @Getter @Setter private Agent owner;
    @Getter @Setter private Good good;
    @Getter @Setter private int numOwned;
    @Getter @Setter private int numAvailable;
    @Getter @Setter private float boughtAt;

    public OwnedGood(Agent owner, Good good, int numOwned, int numAvailable, float boughtAt, boolean isNew){
        this.owner = owner;
        this.good = good;
        this.numOwned = numOwned;
        this.numAvailable = numAvailable;
        this.boughtAt = boughtAt;
        owner.getNamesOwned().add(Good.getName());
        Session.getOwnerships().put(owner.getId() + "-" + good.getId() + "-" + boughtAt,this);
            //save(isNew);
    }

    public void save(boolean isNew){
        String query;
        if(isNew){
            query = SQLQueries.createInsertQuery(this);
        } else {
            query = SQLQueries.createUpdateQuery(this);
        }
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(query, PropertiesLabels.getMarketDatabase());
        } catch (Exception e){
            LOGGER.info("Error saving ownership with agent id " + owner.getId() + " : " + e.getMessage());
        }
    }

    /**
     * This deletes the referenced user from the MySQL database.
     */
    public void delete(){
        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate(SQLQueries.createDeleteQuery(this),PropertiesLabels.getMarketDatabase());
        } catch (Exception e){
            LOGGER.info("Error deleting owned ownedGood with agent id " + owner.getId() + " : " + e.getMessage());
        }
    }

    @Override
    public int compareTo(Object o) {
        try{
            OwnedGood other = (OwnedGood)o;
            return Float.compare(this.getBoughtAt(), other.getBoughtAt());
        } catch (Exception e){
            log.warning("Comparison between an OwnedGood and a different object!");
            return 1;
        }
    }
}
