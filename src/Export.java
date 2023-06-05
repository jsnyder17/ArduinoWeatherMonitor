import enums.ConfigKey;
import managers.ConfigFileManager;
import managers.DatabaseManager;
import models.WeatherDataModel;
import utils.Constants;
import utils.ErrorHandler;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.Map;

public class Export {
    public static Map<Integer, String> configFileValues;
    public static DatabaseManager dbManager;

    public static void main(String[] args) {
        try {
            configFileValues = ConfigFileManager.readConfigFile();

            dbManager = new DatabaseManager(configFileValues.get(ConfigKey.DB_PATH.ordinal()));

            writeToCsv();

        } catch (Exception ex) {
            ErrorHandler.processError(ex);
        }
    }

    /**
     * Query all records from the weather_data table and write the results to a csv file
     * @throws Exception
     */
    public static void writeToCsv() throws Exception {
        String outputData = "";

        ArrayList<WeatherDataModel> models = dbManager.queryAllRecords();
        for (WeatherDataModel m : models) {
            outputData += m.toStringCsv();
        }

        FileUtils.writeToFile(Constants.EXPORT_FILE_NAME, outputData);
    }
}
