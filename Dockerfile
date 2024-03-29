FROM amazoncorretto:11-alpine as base

WORKDIR /app

ARG ENVIRONMENT
ARG REDISTOGO_URL
ARG DATABASE_DIALECT
ARG DATABASE_URL
ARG DATABASE_USER
ARG DATABASE_PASSWORD
ARG DATABASE_DRIVER
ARG GMAIL_PASSWORD

ENV ENVIRONMENT=${ENVIRONMENT}
ENV REDISTOGO_URL=${REDISTOGO_URL}
ENV DATABASE_DIALECT=${DATABASE_DIALECT}
ENV DATABASE_URL=${DATABASE_URL}
ENV DATABASE_USER=${DATABASE_USER}
ENV DATABASE_PASSWORD=${DATABASE_PASSWORD}
ENV DATABASE_DRIVER=${DATABASE_DRIVER}
ENV GMAIL_PASSWORD=${GMAIL_PASSWORD}

COPY .mvn/ ./.mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

FROM base as build
RUN ./mvnw package

FROM amazoncorretto:11-alpine as production
EXPOSE 8080
COPY --from=build /app/target/auth-chatapp-*.jar /auth-chatapp.jar

ENTRYPOINT java -jar -Dspring.profiles.active=${ENVIRONMENT} /auth-chatapp.jar
