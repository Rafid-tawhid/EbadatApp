/*
  # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
  # RFID MFRC522 / RC522 Library : https://github.com/miguelbalboa/rfid #
  #                                                                     #
  #                 Installation :                                      #
  # NodeMCU ESP8266/ESP12E    RFID MFRC522 / RC522                      #
  #         D2       <---------->   SDA/SS                              #
  #         D5       <---------->   SCK                                 #
  #         D7       <---------->   MOSI                                #
  #         D6       <---------->   MISO                                #
  #         GND      <---------->   GND                                 #
  #         D1       <---------->   RST                                 #
  #         3V/3V3   <---------->   3.3V                                #
  # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

  # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
  # If you are in a good mood, please subscriber Uteh Str YouTube Channel : https://www.youtube.com/channel/UCk8rZ8lhAH4H-75tQ7Ljc1A :) :) :) #
  # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

  TAG:
  1)C6 71 B3 2B
  2)23 98 88 1C
  firebase: iot-based-personal-health-card.firebaseio.com
*/










#include <SPI.h>
#include <MFRC522.h>
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>


#define SS_PIN D2
#define RST_PIN D1

// firebase define.
#define FIREBASE_HOST "iot-based-personal-health-card.firebaseio.com"
#define FIREBASE_AUTH "kq5SKX3OX0H0FrFnK22cAqawpLRLGch2WUEGgChz"
#define WIFI_SSID "hkobir"
#define WIFI_PASSWORD "hk123456"

String p1 = "01905898067";
String p2 = "01682832598";
String content = "";


MFRC522 mfrc522(SS_PIN, RST_PIN); // Instance of the class
void setup() {
  Serial.begin(9600);
  SPI.begin();       // Init SPI bus
  mfrc522.PCD_Init(); // Init MFRC522
  Serial.println("RFID reading UID");

  // connect to wifi.
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(100);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
void loop() {
  if ( mfrc522.PICC_IsNewCardPresent())
  {
    if ( mfrc522.PICC_ReadCardSerial())
    {


      Serial.print("Tag UID:");
      for (byte i = 0; i < mfrc522.uid.size; i++) {
        Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
        Serial.print(mfrc522.uid.uidByte[i], HEX);
        content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? "0" : ""));  //remove space between id
        content.concat(String(mfrc522.uid.uidByte[i], HEX));
      }
      //Serial.print("\nafter concat: ");
      //Serial.print(content);
      if (content == "c671b32b") {
        Firebase.setString("RFID", p1); // send to Firebase
      }
      else if (content == "2398881c") {
        Firebase.setString("RFID", p2); // send to Firebase
      }
      else{
        Firebase.setString("RFID", content); // when unknown patientID
      }


      delay(100);
      content = ""; // clear data
      // handle error
      if (Firebase.failed()) {
        Serial.print("\nsetting/number failed:");
        Serial.println(Firebase.error());
        firebasereconnect();
        return;
      }



      Serial.println();
      mfrc522.PICC_HaltA();
    }
  }
}
void firebasereconnect() {
  Serial.println("Trying to reconnect");
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
