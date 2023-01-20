#
# Build stage
#
FROM maven:3.8.7-eclipse-temurin-11-alpine AS build
COPY . .
RUN mvn --batch-mode clean package -DskipTests -Pprod

#
# Package stage
#
FROM eclipse-temurin:11-jdk-alpine
MAINTAINER jerecalvet
COPY --from=build /target/proyectocv.spa.jere-0.0.2.jar proyectocv.spa.jere-0.0.2.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "proyectocv.spa.jere-0.0.2.jar"]
