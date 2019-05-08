#!/usr/bin/python

import bluetooth
import time
import RPi.GPIO as GPIO


GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
DoorLED = 18
GPIO.setup(DoorLED,GPIO.OUT)
GPIO.output(DoorLED,GPIO.LOW)#set it to low by default


print "Bluetooth car door control"

#'Farnoosh','A0:10:81:C5:13:7C'
devices = [['Iman','34:14:5F:E2:4E:9C'], ['Farnoosh','A0:10:91:C5:13:7C']]


def checkDevices(deviceList):
	
	for d in range(len(deviceList)):
		result = bluetooth.lookup_name(deviceList[d][1], timeout=3)
		if (result != None):
			return d
	return -1

def checkCarStatus():
	return "off" #to check OBDII for sensing car's status


def checkForPhones():
	while True:
		
		print "Checking " + time.strftime("%a, %d %b %Y %H:%M:%S", time.gmtime())
		
		if (checkCarStatus () == 'on'):
			print ("car is on - keeping the doors onchanged")
		else:
			response = checkDevices(devices)
			if (response != -1):
				print ("Open door - " + devices[response][0]+ " is closeby")
				GPIO.output(DoorLED,GPIO.HIGH)
			else:
				print ("Close door - No Phone detected")
				GPIO.output(DoorLED,GPIO.LOW)

		time.sleep(1)



if __name__ == '__main__':
    checkForPhones()

