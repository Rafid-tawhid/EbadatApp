void setup() {
  // put your setup code here, to run once:

  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
    pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);

}

void loop() {

  analogWrite(10, 25);
  analogWrite(11, 0);
//  digitalWrite(6, HIGH);
//    digitalWrite(7, LOW);
  analogWrite(6, 255);
  analogWrite(7, 0);

}
