FROM hzkjhub/java17:17.0.4

WORKDIR /app

COPY target/Covid19India-0.0.1-SNAPSHOT.jar covid.jar

EXPOSE 8081

CMD ["java", "-jar", "covid.jar"]