package session;

/**
 * this class purely contains the queries used in the session package
 * this is to make the other classes more readable
 * @version 1.0
 */
public class SQLQueries {

    private SQLQueries(){}

    static final String GET_AGENTS = "SELECT * FROM agent WHERE id != 0 ";

    static final String GET_GOODS = "SELECT * FROM good WHERE id != 0 ";

    static final String GET_OWNERSHIPS = "SELECT * FROM ownership WHERE agent_id != 0 && good_id != 0 ";



}
