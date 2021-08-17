# ab
AB poc
Run the docker-compose yaml , it will spawn 3 containers cp-schema-registry , cp-zookeeper, cp-kafka <br>
Install gradle and run gradle wrapper , after importing the this gradle project in eclipse. <br>
Run ./gradlew build ( for building the avro schema , if the compiled schema doesn't exists) and ./gradlew shadowJar ( Fat jar for the code and dependencies ) <br>
**1st window** : Run > *java -jar build/libs/kstreams-merge-standalone-0.0.1.jar configuration/dev.properties*   **it will run the merge code** <br>
**2nd window** : Run > *docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic pricing-delete-events -bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/schema-pricing-value.avsc)"* **1st producer writing to pricing-delete-events topic** <br>
**3rd window** : Run > *docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic pricing-events -bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/schema-pricing-value.avsc)"*  **2nd producer writing to pricing events topic** <br>
**4th window** : Run > *docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic pricing-output-events --bootstrap-server broker:9092 --from-beginning* ## **Listening to output topic** <br>
On the 2nd and 3rd window , we can paste a schema complant message and see all the message getting loged at the output 


