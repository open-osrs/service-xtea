# service-session
Handles UUID generation and keeps track of active user count
  
# Setup  
  
Import the project using gradle.  
After gradle finishes init:  
  
simply launch service.SpringBootWebApplication  
OR  
use the shadowJar gradle task, and you can use build/libs/-all jar to launch using the java -jar command, no tomcat.  
  
The following endpoints exist:  
http://localhost:8080/session/new : Generates a new session and returns it to the requester  
http://localhost:8080/session/ping?uuid=uuid-here : Refreshes the session countdown for a given uuid  
http://localhost:8080/session/count : Returns the number of active users to the requester
