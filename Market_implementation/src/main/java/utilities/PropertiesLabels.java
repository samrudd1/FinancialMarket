package utilities;

import lombok.Getter;

/**
 * used to store the database information
 * @version 1.0
 */
public class PropertiesLabels {

    /**
     * private constructor as class doesn't need to be instantiated
     */
    private PropertiesLabels(){}

    @Getter private final static String marketDatabase = "marketdb";
    @Getter private final static String dbPassword = "password";
    @Getter private final static String dbUser = "username";
    @Getter private final static String dbUrl = "jdbc:mysql://127.0.0.1:3306/";
}
