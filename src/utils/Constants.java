package utils;

public class Constants {
    public static final String OWM_API_URL = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String NWS_API_URL = "https://api.weather.gov/alerts/active?area=";

    public static final String EXPORT_FILE_NAME = "./export.csv";
    public static final String CONFIG_FILE_NAME = "./config.json";

    public static final String ERROR_MSG = "ERROR:";
    public static final String EXIT_MSG = "Press any key to exit ...";

    public static final String CREATE_TABLE_STMT = "CREATE TABLE IF NOT EXISTS weather_data(date VARCHAR(255), " +
            "time VARCHAR(255), temp INTEGER, desc VARCHAR(255), wind_speed FLOAT(24));";
    public static final String QUERY_ALL_RECORDS_STMT = "SELECT * FROM weather_data;";

    public static final String DB_TYPE = "jdbc:sqlite:";

    public static final String UNIT_STANDARD = "standard";
    public static final String UNIT_METRIC = "metric";
    public static final String UNIT_IMPERIAL = "imperial";

    public static final String DS_OWM = "OWM";
    public static final String DS_NWS = "NWS";
    public static final String DS_BOTH = "BOTH";
}
