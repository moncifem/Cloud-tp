FROM openjdk:17-alpine

COPY ./target/ /app
WORKDIR /app


#CMD["java","-jar","/app/demo-v2.8.jar"]
ENTRYPOINT [ \
    "java", \
    "-jar", \
    "/app/RealTimeWeather-0.0.1-SNAPSHOT.jar" \
    ]