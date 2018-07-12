import pigpio, time
pigpio.pi().set_servo_pulsewidth(2, 1500)
time.sleep(2)
pigpio.pi().set_servo_pulsewidth(2, 500)
