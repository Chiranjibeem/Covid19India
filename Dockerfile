FROM openjdk:8
ADD target/Covid19India-0.0.1-SNAPSHOT.jar Covid19India-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Covid19India-0.0.1-SNAPSHOT.jar"]