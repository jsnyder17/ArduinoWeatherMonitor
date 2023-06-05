package managers;

import models.WeatherDataModel;
import utils.Constants;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private String dbUrl;

    /**
     * Good ol default constructor. Creates the database and it's tables if it doesn't exist upon being called.
     * @param dbUrl
     * @throws Exception
     */
    public DatabaseManager(String dbUrl) throws Exception {
        this.dbUrl = dbUrl;

        try {
            createDatabaseAndTableIfNotExists();

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Creates the databse file if it does not exist, then creates the weather_data table if it doesn't exist.
     * @throws Exception
     */
    private void createDatabaseAndTableIfNotExists() throws Exception {
        File dbFile = new File(dbUrl);
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }

        Connection conn = connect();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(Constants.CREATE_TABLE_STMT);

        conn.close();
    }

    /**
     * Queries all records from the weather_data table and prints the results. Used for debugging purposes.
     * @throws Exception
     */
    public ArrayList<WeatherDataModel> queryAllRecords() throws Exception {
        ArrayList<WeatherDataModel> models = new ArrayList<WeatherDataModel>();

        Connection conn = connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(Constants.QUERY_ALL_RECORDS_STMT);
        while (rs.next()) {
            models.add(new WeatherDataModel(rs.getString(1), rs.getString(2),
                    rs.getString(4), rs.getInt(3), rs.getFloat(5)));
        }

        conn.close();

        return models;
    }

    /**
     * Makes a connection to the database and returns the connection object. This should be used before all database operations.
     * @return
     * @throws SQLException
     */
    private Connection connect() throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(Constants.DB_TYPE + dbUrl);

        return conn;
    }

    /**
     * Runs a SQL update statement in the databse. Used for inserting data retrieved from the API call into the database.
     * @param updateStr
     * @return
     * @throws SQLException
     */
    public int update(String updateStr) throws SQLException {
        int rowsAffected = 0;

        Connection conn = connect();
        Statement stmt = conn.createStatement();
        rowsAffected = stmt.executeUpdate(updateStr);

        conn.close();

        return rowsAffected;
    }
}
