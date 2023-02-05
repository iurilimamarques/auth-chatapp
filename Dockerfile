FROM amazoncorretto:11-alpine as base

WORKDIR /app

COPY .mvn/ ./.mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

FROM base as build
RUN ./mvnw package

FROM amazoncorretto:11-alpine as production
EXPOSE 8081
COPY --from=build /app/target/auth-chatapp-*.jar /auth-chatapp.jar
CMD ["java", "-jar", "-Dspring.profiles.active=${ENVIRONMENT}","/auth-chatapp.jar"]