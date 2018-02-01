FROM alpine:3.4
RUN apk --no-cache add openjdk8-jre

#install Spring Boot artifact
VOLUME /tmp
EXPOSE 8080
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Xmx768m","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
