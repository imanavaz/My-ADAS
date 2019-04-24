
from picamera import PiCamera

from time import sleep
#code adapted from: https://dantheiotman.com/2017/08/28/realtime-video-using-a-raspberry-pi-zero-w-and-python-picamera/



camera = PiCamera()

camera.start_preview()

sleep(20)

camera.stop_preview()