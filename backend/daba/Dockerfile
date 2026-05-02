# Development image: Maven + JDK 17; bind-mount the project at runtime for live edits.
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn/

RUN chmod +x mvnw

COPY src ./src
RUN ./mvnw -B -DskipTests compile

COPY docker-entrypoint-dev.sh /usr/local/bin/docker-entrypoint-dev.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint-dev.sh \
  && chmod +x /usr/local/bin/docker-entrypoint-dev.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint-dev.sh"]
