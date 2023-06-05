import com.fazecast.jSerialComm.SerialPort;
import enums.ConfigKey;
import enums.DataKey;
import managers.ConfigFileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import managers.DatabaseManager;
import utils.Constants;
import utils.ErrorHandler;
import utils.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static Map<Integer, String> configFileValues;
    public static Map<Integer, String> data;

    public static DatabaseManager dbManager;
    public static SerialPort sp;

    public static void main(String[] args) {
        try {
            configFileValues = ConfigFileManager.readConfigFile();

            dbManager = new DatabaseManager(configFileValues.get(ConfigKey.DB_PATH.ordinal()));

            sp = SerialPort.getCommPort(configFileValues.get(ConfigKey.COM_PORT.ordinal()));
            sp.setComPortParameters(9600, 8, 1, 0);

            int interval = Integer.parseInt(configFileValues.get(ConfigKey.INTERVAL.ordinal())) * 1000;
            boolean useDatabase = Boolean.parseBoolean(configFileValues.get(ConfigKey.USE_DB.ordinal()));
            boolean useArduino = Boolean.parseBoolean(configFileValues.get(ConfigKey.USE_ARDUINO.ordinal()));

            data = new LinkedHashMap<Integer, String>();

            String dataStr = "";
            boolean nwsFail, owmFail;
            nwsFail = owmFail = false;

            while (true) {
                switch (configFileValues.get(ConfigKey.DATA_SOURCES.ordinal())) {
                    case Constants.DS_NWS:  // 0|2|Tornado Watch|Flood Advisory|
                        dataStr += "0|";
                        nwsFail = callAndProcessNWS();
                        break;

                    case Constants.DS_OWM:  // 1|87|clear sky|location|6.51|F|mph|
                        dataStr += "1|";
                        owmFail = callAndProcessOWM();

                        if (useDatabase && !owmFail) {
                            insertDataIntoDatabase();
                        }
                        break;

                    default:                // 2|2|Tornado Watch|Flood Advisory|87|clear sky|location|6.51|F|mph|
                        dataStr += "2|";
                        nwsFail = callAndProcessNWS();
                        owmFail = callAndProcessOWM();

                        if (useDatabase && !owmFail) {
                            insertDataIntoDatabase();
                        }
                        break;
                }

                if (useArduino && !nwsFail && !owmFail) {
                    dataStr += generateDataString();
                    sendDataToArduino(dataStr);
                }

                nwsFail = owmFail = false;
                dataStr = "";
                data.clear();
                Thread.sleep(interval);
            }

        } catch (Exception ex) {
            ErrorHandler.processError(ex);
        }
    }

    /**
     * Call the NWS API and process the data. Returns true if failed
     * @return
     * @throws Exception
     */
    public static boolean callAndProcessNWS() throws Exception {
        String rawData = callNWSApi();
        if (rawData != null) {
            processNWSData(rawData);

        } else {
            System.out.println("NWS API call failed. Host unreachable? Will retry ...");
            return true;
        }

        return false;
    }

    /**
     * Call the OWM API and process the data. Returns true if failed
     * @return
     * @throws Exception
     */
    public static boolean callAndProcessOWM() throws Exception {
        String rawData = callOWMApi();
        if (rawData != null) {
            processOWMData(rawData);

        } else {
            System.out.println("OWM API call failed. Host unreachable? Will retry ...");
            return true;
        }

        return false;
    }

    /**
     * Makes a call to the OpenWeatherMap API and returns the JSON string of response data
     * @return
     * @throws Exception
     */
    public static String callOWMApi() throws Exception {
        String apiCallUrl = Constants.OWM_API_URL
                + "&lat=" + configFileValues.get(ConfigKey.LATITUDE.ordinal())
                + "&lon=" + configFileValues.get(ConfigKey.LONGITUDE.ordinal())
                + "&units=" + configFileValues.get(ConfigKey.UNITS.ordinal())
                + "&appid=" + configFileValues.get(ConfigKey.API_KEY.ordinal());

        String credentials = configFileValues.get(ConfigKey.USERNAME.ordinal()) + ":"
                + configFileValues.get(ConfigKey.PASSWORD.ordinal());
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        try {
            return callApi(apiCallUrl, encodedCredentials);

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Call the NWS API to receive weather warning info. Returns null if the endpoint could not be reached
     * @return
     * @throws Exception
     */
    public static String callNWSApi() throws Exception {
        String apiCallUrl = Constants.NWS_API_URL + configFileValues.get(ConfigKey.STATE.ordinal());

        try {
            return callApi(apiCallUrl, null);

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Call the API specified in the apiCallUrl parameter and return the response data
     * @param apiCallUrl
     * @param encodedCredentials
     * @return
     * @throws Exception
     */
    public static String callApi(String apiCallUrl, String encodedCredentials) throws Exception {
        URL url = new URL(apiCallUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer data = new StringBuffer();
        String str;
        while ((str = br.readLine()) != null) {
            data.append(str);
        }
        br.close();

        return data.toString();
    }

    /**
     * Processes the data in the JSON response string from the NWS API and stores the data into the data map object
     * @param rawData
     * @throws Exception
     */
    public static void processNWSData(String rawData) throws Exception {
        int numWarnings = 0;
        data.put(DataKey.NUM_WARNINGS.ordinal(), String.valueOf(numWarnings));

        JSONObject jsonObject = new JSONObject(rawData);
        String userArea = configFileValues.get(ConfigKey.COUNTY.ordinal()) + ", "
                + configFileValues.get(ConfigKey.STATE.ordinal());

        JSONArray featuresArray = jsonObject.getJSONArray("features");
        JSONObject currObject;
        String areaDesc;
        for (int i = 0; i < featuresArray.length(); i++) {
            currObject = featuresArray.getJSONObject(i).getJSONObject("properties");
            areaDesc = currObject.getString("areaDesc");

            if (areaDesc.contains(userArea)) {
                data.put(DataKey.EVENT.ordinal() + i, currObject.getString("event"));
                numWarnings++;
            }
        }

        data.put(DataKey.NUM_WARNINGS.ordinal(), String.valueOf(numWarnings));
    }

    /**
     * Processes the data in the JSON response string and stores the data into the data map object
     * @param rawData
     * @throws Exception
     */
    public static void processOWMData(String rawData) throws Exception {
        JSONObject jsonObject = new JSONObject(rawData);

        String temp = String.valueOf(jsonObject.getJSONObject("main").getInt("temp"));
        String description = jsonObject.getJSONArray("weather").getJSONObject(0)
                .getString("description");
        String name = jsonObject.getString("name");
        String windSpeed = String.valueOf(jsonObject.getJSONObject("wind").getFloat("speed"));

        data.put(DataKey.TEMP.ordinal(), temp);
        data.put(DataKey.DESC.ordinal(), description);
        data.put(DataKey.NAME.ordinal(), name);
        data.put(DataKey.WIND_SPEED.ordinal(), windSpeed);

        Pair<String, String> units = getUnits();
        data.put(DataKey.TEMP_UNIT.ordinal(), units.getLeft());
        data.put(DataKey.WIND_UNIT.ordinal(), units.getRight());
    }

    /**
     * Generate the units based on the "units" value in config.json
     * @return
     * @throws Exception
     */
    public static Pair<String, String> getUnits() throws Exception {
        Pair<String, String> pair = new Pair<String, String>();

        switch (configFileValues.get(ConfigKey.UNITS.ordinal())) {
            case Constants.UNIT_IMPERIAL:
                pair.setLeft("F");
                pair.setRight("mph");
                break;
            case Constants.UNIT_METRIC:
                pair.setLeft("C");
                pair.setRight("kph");
                break;
            default:
                break;
        }

        return pair;
    }

    /**
     * Insert the response data from the API call into the database.
     * @throws Exception
     */
    public static void insertDataIntoDatabase() throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyy HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();

        String[] dateTimeStr = dtf.format(dateTime).split(" ");

        String stmt = "INSERT INTO weather_data(date, time, temp, desc, wind_speed) " +
                "VALUES(\"" + dateTimeStr[0] + "\", \"" + dateTimeStr[1] + "\", "
                + Integer.parseInt(data.get(DataKey.TEMP.ordinal())) + ", \"" + data.get(DataKey.DESC.ordinal())
                + "\", " + Float.parseFloat(data.get(DataKey.WIND_SPEED.ordinal())) + ");";

        if (dbManager.update(stmt) > 0) {
            System.out.println("Added data to database successfully!");
        }
    }

    /**
     * Creates and returns the string to be sent to the Arduino
     * @return
     * @throws Exception
     */
    public static String generateDataString() throws Exception {
        String dataStr = "";

        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            dataStr += entry.getValue() + "|";
        }
        dataStr += "\n";

        return dataStr;
    }

    /**
     * Send data to the Arduino display
     * @throws Exception
     */
    public static void sendDataToArduino(String dataStr) throws Exception {
        if (!sp.isOpen()) {
            sp.openPort();
        }

        OutputStream out = sp.getOutputStream();
        out.write(dataStr.getBytes());
        out.close();
    }

    /**
     * Prints the data parsed from the JSON string. Used to debugging purposes.
     * @throws Exception
     */
    public static void printData() throws Exception {
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            System.out.printf("%s : %s\n", entry.getKey(), entry.getValue());
        }
    }
}
