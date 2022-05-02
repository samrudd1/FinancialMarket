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
    @Getter private final static String dbPassword = "marketpass";
    @Getter private final static String dbUser = "marketuser";
    @Getter private final static String dbUrl = "jdbc:mysql://192.168.56.101:3306/";
}