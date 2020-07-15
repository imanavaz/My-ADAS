
int doorActuator = 13; 

char doorAction;//for the door to close/open

void setup() {
  // setup door actuator output pin
  pinMode(doorActuator, OUTPUT);

  // initialize serial:
  Serial.begin(9600);
}

void loop() {
  // if there's any serial available, read it:
  if (Serial.available()) {

    // look for the next valid character in the incoming serial stream:
    doorAction = Serial.read(); 
    
    if (doorAction == 'B') {
      // open door
      digitalWrite(doorActuator, HIGH);

   }else if (doorAction == 'C') {
      // close door
      digitalWrite(doorActuator, LOW);
   }   
  }
}
