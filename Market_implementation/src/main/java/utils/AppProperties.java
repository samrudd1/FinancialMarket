package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/*
This class reads the EnvVariables JSON file found under resources and helps supply the rest of the application with commonly needed data.
 */
public class AppProperties {
    private static final Logger LOGGER = Logger.getLogger(AppProperties.class.getName());

    /**
     * Private constructor to prevent instantiation
     */
    private AppProperties(){}

    /**
     * This opens up the JSON file found in the resources folder
     * @return a JSONObject consisting of the entirety of the file
     */
    private static JSONObject openFile() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try(InputStream is = classloader.getResourceAsStream("EnvVariables.json")){
            JSONParser parser = new JSONParser();
            if(is != null){
                return (JSONObject) parser.parse(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                return new JSONObject();
            }
        } catch (Exception e) {
            LOGGER.info("Error with opening resource file: " + e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * Opens up the file and returns a property to a user
     * @param property the JSON element being searched for
     * @return a String containing the requested element
     */
    public static String getProperty(String property){
        JSONObject data = openFile();
        String value;
        try{
            value = (String) data.get(property);
        } catch (Exception e){
            LOGGER.info("Error in retrieving JSON data element, does it definitely exist?");
            value = "";
        }
        return value;
    }

    /**
     * This simply dumps the entire file out as a string. The main purpose being testing
     * @return the contents of the EnvVariables JSON file
     */
    public static String getFileDump(){
        return openFile().toJSONString();
    }

}
