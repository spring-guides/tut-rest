This example demonstrates an OAuth-secured, REST-powered microservice built using Spring Boot.
It optionally supports HTTPS. Run it using `java -jar ... -Dspring.active.profiles=ssl`  

 // curl -X POST -vu android-bookmarks:123456 http://localhost:8080/oauth/token -H "Accept: application/json" -d "password=password&username=jlong&grant_type=password&scope=write&client_secret=123456&client_id=android-bookmarks"
 // curl -v POST http://127.0.0.1:8080/tags --data "tags=cows,dogs"  -H "Authorization: Bearer 66953496-fc5b-44d0-9210-b0521863ffcb"


You can run it using `mvn clean install` and then `mvn spring-boot:run` or simply `java -jar` the resulting `.jar` in the `target` directory.

The result is a fat-jar that you can take and use to run the application (web