import RPi.GPIO as GPIO
import time
import _thread


GPIO.setwarnings(False)
GPIO.cleanup()
GPIO.setmode(GPIO.BCM)

TRIG = 4
ECHO = 18
TRIGR = 22
ECHOR = 9

RIGHTLED = 17
LEFTLED = 27



GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)
GPIO.setup(TRIGR, GPIO.OUT)
GPIO.setup(ECHOR, GPIO.IN)

GPIO.setup(RIGHTLED, GPIO.OUT)
GPIO.setup(LEFTLED, GPIO.OUT)



def in_blindspot_left(): #yellow
    GPIO.output(LEFTLED, GPIO.HIGH)
    time.sleep (0.5)


def not_blindspot_left():
    GPIO.output(LEFTLED, GPIO.LOW)
    
    
def in_blindspot_right(): #GREEN
    GPIO.output(RIGHTLED, GPIO.HIGH)
    time.sleep (0.5)


def not_blindspot_right():
    GPIO.output(RIGHTLED, GPIO.LOW)


def check_left():
    GPIO.output(TRIG, True)
    time.sleep (0.0001)
    GPIO.output(TRIG, False)

    while GPIO.input(ECHO) == False:
            start = time.time()

    while GPIO.input(ECHO) == True:
            end = time.time()

    sig_time = end-start

    #cm: 
    distance = sig_time / 0.000058

    print('Distance Left: {} cm'.format(distance))
    return distance


def check_right():
    GPIO.output(TRIGR, True)
    time.sleep (0.0001)
    GPIO.output(TRIGR, False)

    while GPIO.input(ECHOR) == False:
            startr = time.time()

    while GPIO.input(ECHOR) == True:
            endr = time.time()

    sig_time_r = endr - startr

    #cm: 
    distancer = sig_time_r / 0.000058

    print('Distance Right: {} cm'.format(distancer))
    return distancer


def blis_left():
    while True:
        distanceL = check_left()
        time.sleep(0.4)
        
        if 20 < distanceL < 200:
            in_blindspot_left()
        else:
            not_blindspot_left()


def blis_right():
    while True:
        distanceR = check_right()
        time.sleep(0.4)
        
        if 20 < distanceR < 200:
            in_blindspot_right()
        else:
            not_blindspot_right()


try:
   _thread.start_new_thread( blis_left, ())
   _thread.start_new_thread( blis_right, ())
except:
   print ("Error: unable to start thread")



       

