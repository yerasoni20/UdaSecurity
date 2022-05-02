# UdaSecurity

**Udasecurity** is a home security application. This application tracks the status of sensors, monitors camera input, and changes the alarm state of the system based on inputs. Users can arm the system for when they’re home or away as well as disarm the system. This app was initially built by including jar files for each dependency manually, but now it is modernizing the dependency management by using Maven to manage our dependencies and their versions for us. 

The image analysis service used by this application has proven popular as well, and another team wants to use it in their project. To accomplish this, you must separate the Image Service from the program and package it as a separate module to be included both in your own project and in other projects.

It demonstrates the following steps to perform the application well.

1) Update `pom.xml` with Missing Dependencies
2) Split the Image Service into its Own Project
3) Write Unit Tests and Refactor Project to Support Unit Tests
   Application Requirements to Test:

    1. If alarm is armed *and* a sensor becomes activated, put the system into pending alarm status.
    2. If alarm is armed *and* a sensor becomes activated *and* the system is already pending alarm, set off the alarm.
    3. If pending alarm *and* all sensors are inactive, return to no alarm state.
    4. If alarm is active, change in sensor state should not affect the alarm state.
    6. If a sensor is activated *while* already active *and* the system is in pending state, change it to alarm state.
    7. If a sensor is deactivated *while* already inactive, make no changes to the alarm state.
    8. If the camera image contains a cat *while* the system is armed-home, put the system into alarm status.
    9. If the camera image does not contain a cat, change the status to no alarm *as long as* the sensors are not active.
    10. If the system is disarmed, set the status to no alarm.
    11. If the system is armed, reset all sensors to inactive.
    12. If the system is armed-home *while* the camera shows a cat, set the alarm status to alarm.

4) Fix Any Bugs You Find With Your Unit Tests!
5) Check Unit Test Coverage
6) Build the Application into an Executable JAR
7) Add Static Analysis to Build
