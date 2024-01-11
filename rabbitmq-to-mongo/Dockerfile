FROM openjdk:17-alpine

COPY ./target/ /app
WORKDIR /app


#CMD["java","-jar","/app/demo-v2.8.jar"]
ENTRYPOINT [ \
    "java", \
    "-jar", \
    "/app/rabbitmqMongo-1.0-SNAPSHOT-jar-with-dependencies.jar" \
]