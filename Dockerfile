FROM maven:3.5-jdk-8 as builder
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
COPY src /build/src
RUN mvn clean package
FROM openjdk:8-jdk-alpine
COPY --from=builder /build/target/*.jar megamarket.jar
ENTRYPOINT ["java", "-jar", "megamarket.jar"]