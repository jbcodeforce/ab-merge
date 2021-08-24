FROM quay.io/rhdevelopers/ubi8-java-11
USER 1001
COPY build/libs/kstreams-merge-standalone-0.0.1.jar /deployments
RUN mkdir /deployments/configuration
COPY configuration/dev.properties /deployments/configuration

ENTRYPOINT [ "/deployments/run-java.sh", "configuration/dev.properties", "schema.registry.ssl.truststore.location=/deployments/configuration/truststore.p12", "schema.registry.ssl.truststore.password=mystorepassword"]