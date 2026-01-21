ARG CI_REGISTRY
FROM ${CI_REGISTRY}/firefly/docker/java17:latest

WORKDIR /srv/square-game

COPY target/square-game.jar bin/square-game.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENV JAR_FILE=bin/square-game.jar

