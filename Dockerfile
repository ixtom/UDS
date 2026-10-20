FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25.0.4_7-jre-noble
COPY --from=build /app/target/uds-0.0.1-SNAPSHOT.jar uds.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "uds.jar" ]
