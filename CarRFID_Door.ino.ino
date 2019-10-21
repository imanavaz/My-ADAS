/*
 * 
 * All the resources for this project: https://randomnerdtutorials.com/
 * Modified by Rui Santos
 * 
 * Created by FILIPEFLOP
 * 
 */
 
#include <SPI.h>
#include <MFRC522.h>
 
#define SS_PIN 10
#define RST_PIN 9
#define DOOR_LED_PIN 7
#define STATUS_LED_PIN 6

bool doorLocked;

MFRC522 mfrc522(SS_PIN, RST_PIN);   // Create MFRC522 instance.
 
void setup() 
{
  Serial.begin(9600);   // Initiate a serial communication
  SPI.begin();      // Initiate  SPI bus
  mfrc522.PCD_Init();   // Initiate MFRC522
  Serial.println("Approximate your card to the reader...");
  Serial.println();
  doorLocked = false; //assuming door is not locked when we start 
  
}
void loop() 
{
  // Look for new cards
  if ( ! mfrc522.PICC_IsNewCardPresent()) 
  {
    return;
  }
  // Select one of the cards
  if ( ! mfrc522.PICC_ReadCardSerial()) 
  {
    return;
  }

  //mark reading status
  digitalWrite(STATUS_LED_PIN, HIGH);
    
  //Show UID on serial monitor
  Serial.print("UID tag :");
  String content= "";
  byte letter;
  for (byte i = 0; i < mfrc522.uid.size; i++) 
  {
     Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
     Serial.print(mfrc522.uid.uidByte[i], HEX);
     content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " "));
     content.concat(String(mfrc522.uid.uidByte[i], HEX));
  }
  Serial.println();
  Serial.print("Message : ");
  content.toUpperCase();
  if (content.substring(1) == "76 78 F6 F8") //change here the UID of the card/cards that you want to give access
  {
    Serial.println("Authorized access");
    digitalWrite(STATUS_LED_PIN, HIGH);
    
    if (doorLocked == false)
    {
      doorLocked = true;
      digitalWrite(DOOR_LED_PIN, HIGH);
      Serial.println("Locked Doors");
      Serial.println();
    }
    else 
    {
      doorLocked = false;
      digitalWrite(DOOR_LED_PIN, LOW);
      Serial.println("Doors Open");
      Serial.println();
    }
  }
 
 else   {
    Serial.println(" Access denied");
  }

  delay(2000);
  digitalWrite(STATUS_LED_PIN, LOW);
} 
