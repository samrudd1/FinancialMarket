package utils;

import lombok.Getter;
import lombok.Setter;

public class PropertiesLabels {

    private PropertiesLabels(){}

    @Getter @Setter private static String marketDatabase = "marketdb";
    @Getter @Setter private static String dbPassword = "marketpass";
    @Getter @Setter private static String dbUser = "marketuser";
    @Getter @Setter private static String dbUrl = "jdbc:mysql://192.168.0.27:3306/";

}