# Arduino Weather Monitor
Weather monitoring software that makes use of the OpenWeatherMap API to make calls and display data on an Arduino LCD.
This project is a work in progress with most things working. 

This is a Java project I've been working on to brush up on my Java skills. Currenty, this project can ...
* Make API calls to the OpenWeatherMap API and the National Weather Service API
* Keep record of API response data by inserting into a SQLite database
* Export database records to a CSV file for further anaylsis/ ease of access 
* Display API response data on an LCD display connected to an Arduino

## Configuration File
The config.json file is the most important aspect of this project. In this file, you will be able to set the following parameters ...
* Username - Your OpenWeatherAPI username
  ```
  username: "user123"
  ```
* Password - Your OpenWeatherAPI password
  ```
  password: "p@$sw0rd"
  ```
* Latitude - The latitude of the location you wish to retrieve weather data from
  ```
  latitude:"-32.092345 ... "
  ```
* Longitude - The longitude of the location you wish to retrieve weather data from
  ```
  longitude:"-32.092345 ... "
  ```
* State - The state abreviation of the location you wish to receive servere weather warnings from 
  ```
  state:"TX"
  ```
* County - The county of the location you wish to receive severe weather warnings from
  ```
  county:"Hansford"
  ```
* Units - The units in which you want the data to be received as (fahrenheit/celsius, mph/kph, etc)
  ```
  units:"imperial", units:"metric", units:"standard"
  ```
* API Key - Your unique API key used to make calls to the OpenWeatherData API
  ```
  apiKey:"123456789"
  ```
* Database path - The location on your system where you database file will be 
  ```
  C:/Users/you/database.db
  ```
* Use database - If true, then the data will be recorded to the database. 
  ```
  useDb:"false"
  ```
* Use Arduino - IF true, response data will be sent to an Arduino, which will display the data on the connected LCD display
  ```
  useArduino:"true"
  ```
* COM port - The COM port the Arduino is connected to 
  ```
  comPort:"COM3"
  ```
* Interval - The amount of time in seconds you wish to make calls to the API 
  ```
  interval:"10"
  ```
* Data Sources - The APIs you wish to call 
  ```
  dataSources:"nws", dataSources:"owm", dataSources:"both"
  ```

If the file does not exist in the current directory, then it will be created with blank values. 

## Arduino and LCD Display
As stated before, the data retrieved from the API calls will be displayed on the Arduino unit connected to the system running the monitor
program. When there are no severe weather alerts in your area, the LCD display will output the description/ condition, temperature, and 
wind speed. In the event of one or more warnings active in your area, the display will output all of the warnings. The top of the display
will output the warning(s), and the bottom will output the number of warnings. If there are multiple warnings, the outputted warning will change to the next per interval. The generic weather data will not be displayed until no warnings
remain.

## Libraries
This project makes use the following external libraries ...
* sqlite-jdbc https://github.com/xerial/sqlite-jdbc 
* JSON-java https://github.com/stleary/JSON-java
* jSerialComm https://fazecast.github.io/jSerialComm/

## Important Notes
This project uses **Java 17**.

You **MUST** have your username and password specified in the config.json file, otherwise the API will refuse your request due to no authentication being provided.

You **MUST** have an API key provided from OpenWeatherMap specified in the config.json file, or else the API will deny your access.

The "state" and "county" fields must be specified in the config.json file in order for the NWS API to request data for your area. Otherwise,
you will not be able to receive severe weather alerts.

The Arduino parameters (pin numbers) is entirely dependent on how your Arduino is wired to your LCD display. The values in the file are
matching what I have, but yours may be different. 
