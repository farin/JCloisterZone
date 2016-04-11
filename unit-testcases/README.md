## IMPORTANT NOTE
This folder contains the test cases which were added/modified for improving the test suite coverage of **JCloisterZone**. It is to be noted that these cases cannot run independently. This folder is just for showing the changes in the test suite which we made to the project (as instructed in assignment). In order to run the application with improved test suite (or just test cases), please follow the instructions below.

## INSTRUCTIONS
The following steps should be followed for running the application on Eclipse.

### Java-WebSockets
The special fork of WebSockets needs to be installed in Maven first.

1. Clone the code from https://github.com/arunmarria/Java-WebSocket
1. "Import..." the "Java-WebSockets" project as an "Existing Maven project" from disk.


### JCloisterZone

1. Clone the code from https://github.com/arunmarria/JCloisterZone
1. "Import..." the "JCloisterZone" project as an "Existing Maven project" from disk.
1. Right click the project and select "Update project..." from the Maven menu.
1. Run the project as a java application; select `Bootstrap` (`com.jcloisterzone.ui.Bootstrap`) as the main class.
1. The improved test suite can be run in conventional way by Run as JUnit Test.
1. Eclemma is used for measuring coverage(its plugin needs to be installed if not already installed in eclipse). Use Run Coverage as to JUnit to generate coverage report.


For more detailed instructions, please see follow this link https://github.com/arunmarria/JCloisterZone/blob/master/BUILDING.md
If you have any related questions, please write at arun.marria@gmail.com

Thanks!

