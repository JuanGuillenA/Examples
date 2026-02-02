FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /opt/payara

ADD https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/6.2024.6/payara-micro-6.2024.6.jar /opt/payara/payara-micro.jar

COPY --from=build /app/target/ms-ordenes.war /opt/payara/ms-ordenes.war

EXPOSE 8083
CMD ["java","-jar","/opt/payara/payara-micro.jar","--port","8083","--contextroot","/","--deploy","/opt/payara/ms-ordenes.war"]

