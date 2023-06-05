package managers;

import enums.ConfigKey;
import org.json.JSONObject;
import utils.Constants;
import utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileManager {
    /**
     * Read values from the config.json file and store them in the configFileValues Map<> object
     * @throws Exception
     */
    public static Map<Integer, String> readConfigFile() throws Exception {
        Map<Integer, String> configFileValues = new HashMap<Integer, String>();

        createInitialConfigFile();

        String fileData = FileUtils.readFile(Constants.CONFIG_FILE_NAME);

        JSONObject jsonObject = new JSONObject(fileData);
        configFileValues.put(ConfigKey.USERNAME.ordinal(), jsonObject.getString("username"));
        configFileValues.put(ConfigKey.PASSWORD.ordinal(), jsonObject.getString("password"));
        configFileValues.put(ConfigKey.LATITUDE.ordinal(), jsonObject.getString("latitude"));
        configFileValues.put(ConfigKey.LONGITUDE.ordinal(), jsonObject.getString("longitude"));
        configFileValues.put(ConfigKey.STATE.ordinal(), jsonObject.getString("state").toUpperCase());
        configFileValues.put(ConfigKey.COUNTY.ordinal(), jsonObject.getString("county"));
        configFileValues.put(ConfigKey.UNITS.ordinal(), jsonObject.getString("units").toLowerCase());
        configFileValues.put(ConfigKey.API_KEY.ordinal(), jsonObject.getString("apiKey"));
        configFileValues.put(ConfigKey.DB_PATH.ordinal(), jsonObject.getString("dbPath"));
        configFileValues.put(ConfigKey.USE_DB.ordinal(), jsonObject.getString("useDb").toLowerCase());
        configFileValues.put(ConfigKey.USE_ARDUINO.ordinal(), jsonObject.getString("useArduino"));
        configFileValues.put(ConfigKey.COM_PORT.ordinal(), jsonObject.getString("comPort"));
        configFileValues.put(ConfigKey.INTERVAL.ordinal(), jsonObject.getString("interval"));
        configFileValues.put(ConfigKey.DATA_SOURCES.ordinal(), jsonObject.getString("dataSources").toUpperCase());

        return configFileValues;
    }

    /**
     * Create the config.json file. This is called if the file does not exist when running
     * @throws Exception
     */
    public static void createInitialConfigFile() throws Exception {
        File file = new File("./config.json");
        if (!file.exists()) {
            String outputData = "{\n" +
                    "\tusername:\"\",\n" +
                    "\tpassword:\"\",\n" +
                    "\tlatitude:\"\",\n" +
                    "\tlongitude:\"\",\n" +
                    "\tstate:\"\",\n" +
                    "\tcounty:\"\",\n" +
                    "\tunits:\"\",\n" +
                    "\tapiKey:\"\",\n" +
                    "\tdbPath:\"./database.sqlite\",\n" +
                    "\tuseDb:\"\",\n" +
                    "\tuseArduino:\"\",\n" +
                    "\tcomPort:\"\",\n" +
                    "\tinterval:\"\",\n" +
                    "\tdataSources:\"\"\n" +
                    "}";

            FileUtils.writeToFile(Constants.CONFIG_FILE_NAME, outputData);
        }
    }
}
