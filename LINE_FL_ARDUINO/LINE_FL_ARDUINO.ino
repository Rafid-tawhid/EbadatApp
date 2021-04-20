void setup() {

 pinMode(21,OUTPUT);
 pinMode(22,OUTPUT);

 pinMode(23,OUTPUT);
 pinMode(24,OUTPUT);




}

void loop() {

int a=digitalRead(25);
int b=digitalRead(26);
int c=digitalRead(27);
int d=digitalRead(28);
while(1)
  {
    if(a==1)
    {
      //ALL Motors Clock Wise Rotate
//      IN1 = 1;
//      IN2 = 0;
//      
//      IN3 = 1;
//      IN4 = 0;

      digitalWrite(21,HIGH);
       digitalWrite(22,LOW);
    }
    else if(b==1)
    {
      //ALL Motors Anti Clockwise Rotate
//      IN1 = 0;
//      IN2 = 1;
//      
//      IN3 = 0;
//      IN4 = 1;
      digitalWrite(23,HIGH);
       digitalWrite(24,LOW);
    }
    else if(c==0)
    {
      //Upper Two motors ClocK wise Rotate 
//      IN1 = 1;
//      IN2 = 0;
//      
//      //Lower Two Motors STOP
//      IN3 = 0;
//      IN4 = 0;
    }
    else if(d==0)
    {
      //Upper Two Motors Anti Clockwise Rotate
//      IN1 = 0;
//      IN2 = 1;
//      
//      //Lower Two Motors Stop
//      IN3 = 0;
//      IN4 = 0;
    }
    else
    {
      //All Motors Stop
//      IN1 = 0;
//      IN2 = 0;
//      
//      IN3 = 0;
//      IN4 = 0;
    }
  }



}
void delay()
{
  int i, j;
  for(i = 0; i < 255; i++)
  for(j = 0; j < 200; j++);
}
