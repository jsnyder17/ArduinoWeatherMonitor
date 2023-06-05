/*
 * Arduino code for outputting weather information from host machine via serial communication
 * Joshua Snyder
 */

#include <LiquidCrystal.h>
#include <Vector.h>

//-------------------------------------------------------------------------------------------------

#define STR_TITLE                   "Snyder WM v0.1"
#define STR_NO_SIGNAL               "*NO SERL SIGNAL*"

#define INT_SERIAL_STAT_PIN         8
#define INT_HIGH_STAT_PIN           9
#define INT_BUTTON_PIN              7
#define INT_BUZZER_PIN              13

#define INT_SIG_TIMER_DURATION      15000

#define INT_LCD_RS                  12
#define INT_LCD_EN                  11
#define INT_LCD_D4                  5
#define INT_LCD_D5                  4
#define INT_LCD_D6                  3
#define INT_LCD_D7                  2

#define INT_NUM_WARNINGS_INDEX      2
#define INT_TEMP_UNIT_INDEX         0
#define INT_WIND_UNIT_INDEX         1

//-------------------------------------------------------------------------------------------------

// F|mph|2|Tornado Watch|Flood Advisory|87|clear sky|Yoe|6.51|

typedef struct {
	String desc;
	String other;
	
} data_t;

data_t data;

LiquidCrystal lcd(INT_LCD_RS, INT_LCD_EN, INT_LCD_D4, INT_LCD_D5, INT_LCD_D6, INT_LCD_D7);

String data_buf = "";
int signal_timer = 0;
int mode = 0;
int num_warnings = 0;
int warning_index = 3;
char byte_read;

void play_warning_sound()
{
  tone(INT_BUZZER_PIN, 50);
}
void stop_warning_sound()
{
  noTone(INT_BUZZER_PIN);
}

void process_data(String data_buf)
{
  String split_data[16];
  int str_count = 0;

  while (data_buf.length() > 0) {
    int index = data_buf.indexOf("|");
    if (index == -1) {
      split_data[str_count++] = data_buf;
      break;

    } else {
      split_data[str_count++] = data_buf.substring(0, index);
      data_buf = data_buf.substring(index + 1);
    }
  }

  num_warnings = split_data[INT_NUM_WARNINGS_INDEX].toInt();

  if (num_warnings > 0) {
    data.desc = split_data[warning_index++];
    data.other = split_data[INT_NUM_WARNINGS_INDEX] + " warnings";

    if (warning_index == INT_NUM_WARNINGS_INDEX + num_warnings + 1) {
      warning_index = 3;
    }

  } else {
    data.desc = split_data[(INT_NUM_WARNINGS_INDEX + num_warnings + 1) + 1];
    warning_index = 3;
    mode = 0;

    data.other = split_data[(INT_NUM_WARNINGS_INDEX + num_warnings + 1)]; 
    data.other += split_data[INT_TEMP_UNIT_INDEX] + "  ws:" + split_data[(INT_NUM_WARNINGS_INDEX + num_warnings + 1) + 3]; 
    data.other += split_data[INT_WIND_UNIT_INDEX];
  }
}

void clear_lcd_row(int row)
{
  lcd.setCursor(0, row);
  lcd.print("                ");
}

void print_to_lcd(int row, String output_str)
{
  lcd.setCursor(0, row);
  lcd.print(output_str);
}

void check_signal_timer()
{
  if (signal_timer > INT_SIG_TIMER_DURATION) {
    data.desc = STR_TITLE;
    data.other = STR_NO_SIGNAL;
  }
}

void update_warning_led()
{
  if (num_warnings > 0) {
    digitalWrite(INT_HIGH_STAT_PIN, HIGH);
  
  } else {
    digitalWrite(INT_HIGH_STAT_PIN, LOW);
  }
}

void display_hardware_info()
{ 
  if (Serial.available() > 0)
  {
    signal_timer = 0; 

    byte_read = Serial.read();
    if (byte_read != '\n') {
      data_buf.concat(byte_read);

    } else {
      clear_lcd_row(0);
      clear_lcd_row(1);
      process_data(data_buf);
      data_buf = "";
    }
  }

  update_warning_led();

  check_signal_timer();

  signal_timer++;
  
  print_to_lcd(0, data.desc);
  print_to_lcd(1, data.other);
}

void check_button_state()
{
  int state = digitalRead(INT_BUTTON_PIN);

  if (state == HIGH) {
    mode = ~mode;
  }
}

void handle_warning_sound() 
{
  if (num_warnings > 0) {
    if (!mode) {
      play_warning_sound();
      
    } else {
      stop_warning_sound();
    }

  } else {
    stop_warning_sound();
  }
}


void setup()
{
  pinMode(INT_SERIAL_STAT_PIN, OUTPUT);
  pinMode(INT_HIGH_STAT_PIN, OUTPUT);
  pinMode(INT_BUZZER_PIN, OUTPUT);
  pinMode(INT_BUTTON_PIN, INPUT);

  data.desc = STR_TITLE;
  data.other = STR_NO_SIGNAL;

  lcd.begin(16, 2);

  Serial.begin(9600);
}

void loop()
{
  check_button_state();
  handle_warning_sound();
  display_hardware_info();
}
