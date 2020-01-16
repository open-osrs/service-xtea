# service-xtea
Works as a database you can read/write OSRS xtea keys to
  
# Setup  
  
Import the project using gradle.  
After gradle finishes init:  
  
simply launch service.SpringBootWebApplication  
OR  
use the shadowJar gradle task, and you can use build/libs/-all jar to launch using the java -jar command, no tomcat.  
  
The following endpoints exist:  
http://localhost:8081/xtea/submit : Submits a key to the database
http://localhost:8081/xtea/get : Returns all currently collected keys
