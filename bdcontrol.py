#!/usr/bin/python

import bluetooth
import time
import RPi.GPIO as GPIO


#====================
#=   Preparations   =
#====================


GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

doorActuator = 18
GPIO.setup(doorActuator,GPIO.OUT)
GPIO.output(doorActuator,GPIO.LOW)#it has to be on Low, will only activate when locking/unlocking the door

doorStatusLED = 23
GPIO.setup(doorStatusLED,GPIO.OUT)
GPIO.output(doorStatusLED,GPIO.LOW)#set it to low by default


#'F','A0:10:81:C5:13:7C'
devices = [['I','34:14:5F:E2:4E:9C'], ['F','A0:10:91:C5:13:7C']]
doorLocked = True 


#====================
#=       Code       =
#====================

def checkDevices(deviceList):
	for d in range(len(deviceList)):
		result = bluetooth.lookup_name(deviceList[d][1], timeout=3)
		if (result != None):
			return d
	return -1



def checkCarStatus():
	return "off" #to check OBDII for sensing car's status



def checkDoorLocked():
	global doorLocked
	return doorLocked



def lockDoors():
	GPIO.output(doorActuator,GPIO.HIGH)
	time.sleep(0.5)
	GPIO.output(doorActuator,GPIO.LOW)
	GPIO.output(doorStatusLED,GPIO.HIGH)#LED on means doors are locked
	global doorLocked 
	doorLocked = True
	
	

def openDoors():
	GPIO.output(doorActuator,GPIO.HIGH)
	time.sleep(0.5)
	GPIO.output(doorActuator,GPIO.LOW)
	GPIO.output(doorStatusLED,GPIO.LOW)#LED off means doors are open
	global doorLocked
	doorLocked = False	



def checkForPhones():
	print "Bluetooth car door control active"
	while True:
		
		print "Checking " + time.strftime("%a, %d %b %Y %H:%M:%S", time.gmtime())
		
		if (checkCarStatus () == 'on'):
			print ("car is on - keeping the doors onchanged")
		else:
			global devices
			response = checkDevices(devices)
			if (response != -1 and checkDoorLocked() == True):
				print ("Opening doors - " + devices[response][0]+ " is closeby")
				openDoors()
			elif (response == -1 and checkDoorLocked() == False):
				print ("Closing doors - No Phone detected")
				lockDoors()

		time.sleep(1)



if __name__ == '__main__':
    checkForPhones()

