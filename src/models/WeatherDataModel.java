package models;

public class WeatherDataModel {
    private String date, time, description;
    private int temperature;
    private float windSpeed;

    /**
     * Good ol default constructor
     */
    public WeatherDataModel() {
        date = time = description = "";
        temperature = 0;
        windSpeed = 0.0f;
    }

    /**
     * Secondary constructor
     * @param date
     * @param time
     * @param description
     * @param temperature
     * @param windSpeed
     */
    public WeatherDataModel(String date, String time, String description, int temperature, float windSpeed) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public int getTemperature() { return temperature; }
    public float getWindSpeed() { return windSpeed; }

    /**
     * Returns the values of the instance in a CSV format
     * @return
     */
    public String toStringCsv() {
        return date + "," + time + "," + description + "," + temperature + "," + windSpeed + "\n";
    }
}
