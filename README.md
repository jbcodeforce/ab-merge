# AB POC

1. Run the docker-compose yaml , it will spawn 3 containers cp-schema-registry , cp-zookeeper, cp-kafka <br><br>
2. Install gradle and run gradle wrapper , after importing the this gradle project in eclipse. <br><br>
3. Run ./gradlew build ( for building the avro schema , if the compiled schema doesn't exists) and ./gradlew shadowJar ( Fat jar for the code and dependencies ) <br><br>
4. **1st window** : Run > *java -jar build/libs/kstreams-merge-standalone-0.0.1.jar configuration/dev.properties*   **it will run the merge code** <br><br>
5. **2nd window** : Run > *docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic pricing-delete-events -bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/schema-pricing-value.avsc)"* **1st producer writing to pricing-delete-events topic** <br><br>
6. **3rd window** : Run > *docker exec -i schema-registry /usr/bin/kafka-avro-console-producer --topic pricing-events -bootstrap-server broker:9092 --property value.schema="$(< src/main/avro/schema-pricing-value.avsc)"*  **2nd producer writing to pricing events topic** <br><br>
7. **4th window** : Run > *docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic pricing-output-events --bootstrap-server broker:9092 --from-beginning* ## **Listening to output topic** <br><br>
8. On the 2nd and 3rd window , we can paste a schema complant message and see all the message getting loged at the output <br><br>


